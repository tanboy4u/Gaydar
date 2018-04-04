package gaydar.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.YELLOW
import gaydar.gridWidth
import gaydar.mapWidth
import gaydar.util.fromHsv

const val initialWindowWidth = 1000f
const val windowToMapUnit = mapWidth / initialWindowWidth

const val runSpeed = 6.3 * 100//6.3m/s
const val unit = gridWidth / 8
const val unit2 = unit / 10
const val visionRadius = mapWidth / 8

// 1000ms = 1sec
const val redzongBombShowDuration = 2000
const val airDropShowDuration = 40000

val bgColor = Color(0.417f, 0.417f, 0.417f, 1f)
val selfColor = Color(0x32cd32ff)
val teamColor = arrayOf(
      Color(1f, 0.5f, 0f, 1f),
      Color(1f, 1f, 0f, 1f),
      Color(0f, 0.58f, 1f, 1f),
      Color(0.714f, 1f, 0f, 1f)
                       )
val safeDirectionColor = Color(1f, 1f, 1f, 0.5f)
val visionColor = Color(1f, 1f, 1f, 0.1f)
val parachuteColor = Color(0.94f, 1.0f, 1.0f, 1f)
val playerColor = Color.RED!!

val sightColor = Color(1f, 1f, 1f, 0.5f)

val aimLineColor = Color(0f, 0f, 1f, 1f)
val firingLineColor = Color(1.0f, 1.0f, 1.0f, 0.5f)
val attackLineColor = Color(1.0f, 0f, 0f, 1f)
val redZoneColor = Color(1f, 0f, 0f, 0.2f)
val safeZoneColor = Color(1f, 1f, 1f, 0.5f)
val airDropLineColor = YELLOW

val teamNumberColors = HashMap<Int, String>().apply {
  val num = 100
  val unit = 360f / num
  for (i in 0 until num)
  {
    put(i, fromHsv(i * unit, 1f, 1f).toString())
  }
}