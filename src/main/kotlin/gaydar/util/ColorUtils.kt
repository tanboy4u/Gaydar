package gaydar.util

import com.badlogic.gdx.graphics.Color

fun fromHsv(h : Float, s : Float, v : Float) : Color
{
  val color = Color().apply {
    val x = (h / 60f + 6) % 6
    val i = x.toInt()
    val f = x - i
    val p = v * (1 - s)
    val q = v * (1 - s * f)
    val t = v * (1 - s * (1 - f))
    when (i)
    {
      0    ->
      {
        r = v
        g = t
        b = p
      }
      1    ->
      {
        r = q
        g = v
        b = p
      }
      2    ->
      {
        r = p
        g = v
        b = t
      }
      3    ->
      {
        r = p
        g = q
        b = v
      }
      4    ->
      {
        r = t
        g = p
        b = v
      }
      else ->
      {
        r = v
        g = p
        b = q
      }
    }
    a = 1f
  }

  return color.clamp()
}