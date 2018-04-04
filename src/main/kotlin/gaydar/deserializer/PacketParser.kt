@file:Suppress("NOTHING_TO_INLINE")

package gaydar.deserializer

import gaydar.haveEncryptionToken
import gaydar.EncryptionToken
import gaydar.deserializer.channel.ActorChannel
import gaydar.deserializer.channel.Channel.Companion.closedInChannels
import gaydar.deserializer.channel.Channel.Companion.closedOutChannels
import gaydar.deserializer.channel.Channel.Companion.inChannels
import gaydar.deserializer.channel.Channel.Companion.outChannels
import gaydar.deserializer.channel.ControlChannel
import gaydar.struct.Bunch
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread
import kotlin.experimental.and
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.GCMParameterSpec

fun Int.d(w : Int) : String
{
  return String.format("%${w}d", this)
}

val packets = ConcurrentLinkedQueue<Pair<ByteArray, Boolean>>()

fun parsePackets()
{
  thread(isDaemon = true) {
    while (true)
    {
      val work = packets.poll()
      if (work == null)
      {
        Thread.yield()
        continue
      }
      val (raw, client) = work
      proc_raw_packet(raw, client)
    }
  }
}

/*
fun proc_raw_packet(raw : ByteArray, client : Boolean = true)
{
  if (raw.isEmpty()) return
  var lastByte = raw.last().toInt() and 0xFF
  if (lastByte != 0)
  {
    var bitsize = (raw.size * 8) - 2
    while ((lastByte and 0x80) == 0)
    {
      lastByte *= 2
      bitsize--
    }
    val reader = Buffer(raw, 0, bitsize)
    reader.proc_raw_packet(client)
  }
}
*/

private fun readRawBit(raw : ByteArray, posBits : Int = 0) : Boolean
{
  val GShift = ByteArray(8) { (1 shl it).toByte() }
  val zeroByte : Byte = 0
  val b = raw[posBits ushr 3] and GShift[posBits and 0b0111]//x & 0b0111 == x % 8
  return b != zeroByte
}

fun proc_raw_packet(raw : ByteArray, client : Boolean = true)
{
  if (raw.isEmpty()) return
  if (readRawBit(raw)) return //IsHandshake
  var IsEncrypted = readRawBit(raw, 1)
  if (!IsEncrypted)
  {
    var lastByte = raw.last().toInt() and 0xFF
    if (lastByte != 0)
    {
      var bitsize = (raw.size * 8) - 2
      while ((lastByte and 0x80) == 0)
      {
        lastByte *= 2
        bitsize--
      }
      val reader = Buffer(raw, 0, bitsize)
      reader.proc_raw_packet(client)
    }
  }
  else if (haveEncryptionToken)
  {
    val reader = Buffer(raw, 0, raw.size * 8)
    val IsHandshake = reader.readBit()
    IsEncrypted = reader.readBit()
    val nonce = reader.readBits(96)
    val tag = reader.readBits(128)
    var bitsLeft = reader.bitsLeft()
    while (bitsLeft > 0)
    {
      try
      {
        val reader2 = Buffer(raw, 1 + 1 + 96 + 128, bitsLeft)
        val ciphertext = reader2.readBits(bitsLeft)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(EncryptionToken, "AES")
        val paramSpec = GCMParameterSpec(128, nonce)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec);
        cipher.update(ciphertext)
        val plaintext = cipher.doFinal(tag)
        if (plaintext.isEmpty()) return
        var lastByte = plaintext.last().toInt() and 0xFF
        if (lastByte != 0)
        {
          var bitsize = (plaintext.size * 8) - 2
          while ((lastByte and 0x80) == 0)
          {
            lastByte *= 2
            bitsize--
          }
          val reader3 = Buffer(plaintext, 0, bitsize)
          reader3.proc_raw_packet(client, true)
        }
      }
      catch (e: javax.crypto.AEADBadTagException)
      {
        bitsLeft--
        continue
      }
      break
    }
  }
}

fun Buffer.proc_raw_packet(client : Boolean, wasEncrypted : Boolean = false)
{
  if (!wasEncrypted)
  {
    val IsHandshake = readBit()
    if (IsHandshake) return
    val IsEncrypted = readBit()
    if (IsEncrypted) return
  }
  val packetId = readInt(MAX_PACKETID)
  while (notEnd())
  {
    val IsAck = readBit()
    if (IsAck)
    {
      val ackPacketId = readInt(MAX_PACKETID)
      val bHasServerFrameTime = readBit()
      val remoteInKBytesPerSecond = readIntPacked()
      continue
    }
    val bControl = readBit()
    var bOpen = false
    var bClose = false
    var bDormant = false
    if (bControl)
    {
      bOpen = readBit()
      bClose = readBit()
      if (bClose) bDormant = readBit()
    }
    val bIsReplicationPaused = readBit()
    val bReliable = readBit()
    val chIndex = readInt(MAX_CHANNELS)
    val a = readBit()
    val bHasPackageMapExports = readBit()
    val bHasMustBeMappedGUIDs = readBit()
    val bPartial = readBit()
    val chSequence = when
    {
      bReliable -> readInt(MAX_CHSEQUENCE)
      bPartial  -> packetId
      else      -> 0
    }
    var bPartialInitial = false
    var bPartialFinal = false
    if (bPartial)
    {
      bPartialInitial = readBit()
      bPartialFinal = readBit()
    }

    val chType = if (bReliable || bOpen) readInt(CHTYPE_MAX) else CHTYPE_NONE
    if (chType > 4)
      return
    val bunchDataBits = readInt(MAX_PACKET_SIZE * 8)
    val pre = bitsLeft()
    if (bunchDataBits > pre)
      return

    val channels = if (client) inChannels else outChannels
    val closedChannels = if (client) closedInChannels else closedOutChannels
    if (chIndex !in channels && (chIndex != 0 || chType != CHTYPE_CONTROL))
    // Can't handle other channels until control channel exists.
      if (client && channels[0] == null)
      {
        return
      }

    // ignore control channel close if it hasn't been opened yet
    if (chIndex == 0 && channels[0] == null && bClose && chType == CHTYPE_CONTROL)
    {
      return
    }

    if (chIndex !in channels && !bReliable)
    {
      //Unreliable bunches that open channels should be bOpen && (bClose || bPartial)
      val validUnreliableOpen = bOpen && (bClose || bPartial)
      if (!validUnreliableOpen)
      {
        skipBits(bunchDataBits)//此bunch不处理
        continue
      }
    }

    if (chIndex !in channels)
    {
      when (chType)
      {
        CHTYPE_CONTROL            ->
        {
          channels[chIndex] = ControlChannel(chIndex, client)
        }
        CHTYPE_VOICE, CHTYPE_FILE ->
        {

        }
        else                      ->
        {
          if (chType == CHTYPE_NONE)
            println("$chSequence lost the first actor creation bunch. just create as we need it.")
          inChannels[chIndex] = ActorChannel(chIndex, true)
          outChannels[chIndex] = ActorChannel(chIndex, false)
        }
      }
    }
    val chan = channels[chIndex]

    if (chan != null)
    {
      check(chType == CHTYPE_NONE || chType == chan.chType)
      try
      {
        val bunch = Bunch(
              bunchDataBits,
              deepCopy(bunchDataBits),
              packetId,
              chIndex,
              chType,
              chSequence,
              bOpen,
              bClose,
              bDormant,
              bIsReplicationPaused,
              bReliable,
              bPartial,
              bPartialInitial,
              bPartialFinal,
              bHasPackageMapExports, bHasMustBeMappedGUIDs
                         )
        chan.ReceivedRawBunch(bunch)
      }
      catch (e : Exception)
      {
      }
    }
    skipBits(bunchDataBits)
  }
  return
}
