package gaydar

import com.badlogic.gdx.math.Vector2
import org.pcap4j.core.BpfProgram.BpfCompileMode.OPTIMIZE
import org.pcap4j.core.NotOpenException
import org.pcap4j.core.PcapNetworkInterface
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode.PROMISCUOUS
import org.pcap4j.core.Pcaps
import org.pcap4j.packet.*
import gaydar.SniffOption.PPTPFilter
import gaydar.SniffOption.PortFilter
import gaydar.deserializer.packets
import gaydar.deserializer.parsePackets
import java.io.File.separator
import java.net.Inet4Address
import java.net.InetAddress
import kotlin.concurrent.thread
import kotlin.experimental.and

const val check1 = 12
const val check2 = 8
const val check3 = 4
const val flag1 : Byte = 0
const val flag2 : Byte = -1
fun Byte.check() = this == flag1 || this == flag2
class DevDesc(val dev : PcapNetworkInterface, val address : Inet4Address)
{
  override fun toString() : String
  {
    return "[${address.hostAddress}] - ${dev.description}"
  }
}

enum class SniffOption
{
  PortFilter,
  PPTPFilter
}

val settingHome = "${System.getProperty("user.home")}$separator.gaydar"

class Sniffer
{

  companion object : GameListener
  {

    override fun onGameOver()
    {
    }

    private val nif : PcapNetworkInterface
    val targetAddr : Inet4Address
    val sniffOption : SniffOption
    var selfCoords = Vector2()

    init
    {
      register(this)

      val nif : PcapNetworkInterface?
      val sniffOption : SniffOption?
      val devs = Pcaps.findAllDevs()

      val choices = ArrayList<DevDesc>()
      for (dev in devs)
        dev.addresses
              .filter { it.address is Inet4Address }
              .mapTo(choices) { DevDesc(dev, it.address as Inet4Address) }

      if (choices.isEmpty())
      {
        System.exit(-1)
      }

      val devDesc = choices.first { it.address.hostAddress == Args[0] }
      nif = devDesc.dev
      val localAddr = if (Args.size == 3) InetAddress.getByName(Args[2]) as Inet4Address else devDesc.address
      sniffOption = SniffOption.valueOf(Args[1])
      this.nif = nif
      this.targetAddr = localAddr
      this.sniffOption = sniffOption

    }

    val localAddr = targetAddr

    const val snapLen = 65536
    val mode = PROMISCUOUS
    const val timeout = 1

    const val PPTPFlag : Byte = 0b0011_0000
    const val ACKFlag : Byte = 0b1000_0000.toByte()

    fun ByteArray.toIntBE(pos : Int, num : Int) : Int
    {
      var value = 0
      for (i in 0 until num)
        value = value or ((this[pos + num - 1 - i].toInt() and 0xff) shl 8 * i)
      return value
    }

    fun parsePPTPGRE(raw : ByteArray) : Packet?
    {
      var i = 0
      if (raw[i] != PPTPFlag) return null//PPTP
      i++
      val hasAck = (raw[i] and ACKFlag) != 0.toByte()
      i++
      val protocolType = raw.toIntBE(i, 2)
      i += 2
      if (protocolType != 0x880b) return null
      val payloadLength = raw.toIntBE(i, 2)
      i += 2
      val callID = raw.toIntBE(i, 2)
      i += 2
      val seq = raw.toIntBE(i, 4)
      i += 4
      if (hasAck)
      {
        val ack = raw.toIntBE(i, 4)
        i += 4
      }
      if (raw[i] != 0x21.toByte()) return null//not ipv4
      i--
      raw[i] = 0
      val pppPkt = PppSelector.newPacket(raw, i, raw.size - i)
      return pppPkt.payload
    }

    fun udp_payload(packet : Packet) : UdpPacket?
    {
      return when (sniffOption)
      {
        PortFilter -> packet
        PPTPFilter -> parsePPTPGRE(packet[IpV4Packet::class.java].payload.rawData)

      }?.get(UdpPacket::class.java)
    }

    fun sniffLocationOnline()
    {
      val handle = nif.openLive(snapLen, mode, timeout)
      val filter = when (sniffOption)
      {
        PortFilter -> "udp portrange 7000-7999"
        PPTPFilter -> "ip[9]=47"
      }
      handle.setFilter(filter, OPTIMIZE)
      thread(isDaemon = true) {
        handle.loop(-1) { packet : Packet? ->
          try
          {
            packet!!
            val ip = packet[IpPacket::class.java]
            val udp = udp_payload(packet) ?: return@loop
            val raw = udp.payload.rawData

            if (udp.header.dstPort.valueAsInt() in 7000..7999)
              packets.add(Pair(raw, false))
            else if (udp.header.srcPort.valueAsInt() in 7000..7999)
              packets.add(Pair(raw, true))
          }
          catch (e : Exception)
          {
          }
        }
      }
      parsePackets()
    }


    fun sniffLocationOffline(filename : String? = "c:\\TestServer01.pcap")
    {
      thread(isDaemon = true) {
        //                val files = arrayOf("d:\\test10.pcap", "d:\\test11.pcap", "d:\\test12.pcap")
//        val files = arrayOf("d:\\pptp02.pcap")
        val files = arrayOf(filename)
        for (file in files)
        {
          val handle = Pcaps.openOffline(file)

          while (true)
          {
            try
            {
              val packet = handle.nextPacket ?: break
              val ip = packet[IpPacket::class.java]
              val udp = Sniffer.udp_payload(packet) ?: continue
              val raw = udp.payload.rawData

              if (udp.header.dstPort.valueAsInt() in 7000..7999)
                packets.add(Pair(raw, false))
              else if (udp.header.srcPort.valueAsInt() in 7000..7999)
                packets.add(Pair(raw, true))

            }
            catch (e : Exception)
            {
            }
            catch (e : IndexOutOfBoundsException)
            {
            }
            catch (e : Exception)
            {
            }
            catch (e : NotOpenException)
            {
              e.printStackTrace()
            }
            Thread.sleep(2)

          }
        }
      }
      parsePackets()
    }
  }
}
