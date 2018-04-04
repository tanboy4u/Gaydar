package gaydar

import gaydar.ui.GLMap
import gaydar.util.settings.Settings
import java.util.Collections.newSetFromMap
import java.util.concurrent.ConcurrentHashMap

const val gridWidth = 813000f
const val mapWidth = 819200f

var gameStarted = false
var isErangel = true
var haveEncryptionToken = false
var EncryptionToken = ByteArray(24)

interface GameListener
{
  fun onGameOver()
}

private val gameListeners = newSetFromMap(ConcurrentHashMap<GameListener, Boolean>())

fun register(gameListener : GameListener)
{
  gameListeners.add(gameListener)
}

fun deregister(gameListener : GameListener)
{
  gameListeners.remove(gameListener)
}

fun gameStart()
{
  println("New Game is Starting")

  gameStarted = true
}

fun gameOver()
{
  gameStarted = false
  haveEncryptionToken = false
  EncryptionToken.fill(0)
  gameListeners.forEach { it.onGameOver() }
}

lateinit var Args : Array<String>
fun main(args : Array<String>)
{
  Args = args
  when
  {
    args.size < 3 ->
    {
      println("Online usage: <ip> <sniff option> <gaming pc>")

      println("Offline usage: <ip> <sniff option> <gaming pc> <offline.pcap>")
      System.exit(-1)

    }
    args.size > 3 ->
    {

      println("Loading PCAP File.. " + args[3])

      Sniffer.sniffLocationOffline(args[3])
      val jsettings = Settings()
      val ui = GLMap(jsettings.loadsettings())
      ui.show()
    }
    else          ->
    {
      Sniffer.sniffLocationOnline()
      val jsettings = Settings()
      val ui = GLMap(jsettings.loadsettings())
      ui.show()
    }
  }
}