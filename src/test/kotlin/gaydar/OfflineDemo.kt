package gaydar

import org.pcap4j.core.Pcaps
import org.pcap4j.packet.IpPacket
import gaydar.deserializer.packets
import gaydar.deserializer.parsePackets
import gaydar.ui.GLMap
import gaydar.util.settings.Settings
import kotlin.concurrent.thread

fun sniffLocationOffline()
{
  thread(isDaemon = true) {
    //                val files = arrayOf("d:\\test10.pcap", "d:\\test11.pcap", "d:\\test12.pcap")
//        val files = arrayOf("d:\\pptp02.pcap")
    val files = arrayOf("d:\\testServer10.pcap")
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
          if (ip.header.srcAddr == Sniffer.localAddr)
          {
            if (udp.header.dstPort.valueAsInt() in 7000..7999)
              packets.add(Pair(raw, false))
          }
          else if (udp.header.srcPort.valueAsInt() in 7000..7999)
            packets.add(Pair(raw, true))
        }
        catch (e : Exception)
        {
          e.printStackTrace()
        }
        Thread.sleep(1)
      }
    }
  }
  parsePackets()
}

fun main(args : Array<String>)
{
//  System.setOut(PrintStream("dump"))
  sniffLocationOffline()
  val jsettings = Settings()
  GLMap(jsettings.loadsettings()).show()
}