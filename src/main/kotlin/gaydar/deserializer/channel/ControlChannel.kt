package gaydar.deserializer.channel

import gaydar.deserializer.CHTYPE_CONTROL
import gaydar.deserializer.NMT_Hello
import gaydar.deserializer.NMT_Welcome
import gaydar.haveEncryptionToken
import gaydar.EncryptionToken
import gaydar.gameOver
import gaydar.gameStart
import gaydar.isErangel
import gaydar.struct.Bunch

class ControlChannel(ChIndex : Int, client : Boolean = true) : Channel(ChIndex, CHTYPE_CONTROL, client)
{
  override fun ReceivedBunch(bunch : Bunch)
  {
    val messageType = bunch.readUInt8()
    when (messageType)
    {
      NMT_Hello ->
      {//server tells client the encryption key
        if (haveEncryptionToken) return
        var IsLittleEndian = bunch.readUInt8()
        var RemoteNetworkVersion = bunch.readUInt32()
        val EncryptionTokenString = bunch.readString()
        EncryptionToken = EncryptionTokenString.toByteArray(Charsets.UTF_8)
        haveEncryptionToken = true
        println("Got EncryptionToken $EncryptionTokenString")
      }
      NMT_Welcome ->
      {// server tells client they're ok'ed to load the server's level
        val map = bunch.readString()
        val gameMode = bunch.readString()
        val unknown = bunch.readString()
        isErangel = map.contains("erangel", true)
        gameStart()
        println("Welcome To ${if (isErangel) "Erangel" else "Miramar"}")
      }
      else        ->
      {

      }
    }
  }

  override fun close()
  {
    println("Game over")
    gameOver()
  }
}