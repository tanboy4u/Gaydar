package gaydar

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO

fun main(args : Array<String>)
{
  GridMap().show()
}

class GridMap : ApplicationListener
{
  override fun pause()
  {
  }

  override fun resume()
  {
  }

  fun show()
  {
    val config = Lwjgl3ApplicationConfiguration()
    config.useOpenGL3(true, 3, 3)
    config.setWindowedMode(1000, 1000)
    config.setResizable(true)
    config.setBackBufferConfig(8, 8, 8, 8, 32, 0, 8)
    Lwjgl3Application(this, config)
  }

  override fun create()
  {
    drawGrid("maps/Erangel_Minimap.bmp")
    drawGrid("maps/Miramar_Minimap.bmp")
    Gdx.app.exit()
  }

  fun drawGrid(file : String)
  {
    val pixmap = Pixmap(Gdx.files.internal(file))
    val gridWidth = 8130f
    val unit = gridWidth / 8f
    val unit2 = unit / 10f
    pixmap.apply {
      setColor(Color.BLACK)
      drawRectangle(0, 0, pixmap.width, pixmap.height)
      //thin grid
      for (i in 0..7)
        for (j in 0..9)
        {
          fillRectangle(0, (i * unit + j * unit2).toInt(), gridWidth.toInt(), 1)
          fillRectangle((i * unit + j * unit2).toInt(), 0, 1, gridWidth.toInt())
        }
      setColor(Color.GRAY)
      //thick grid
      for (i in 0..7)
      {
        fillRectangle(0, (i * unit).toInt(), gridWidth.toInt(), 5)
        fillRectangle((i * unit).toInt(), 0, 5, gridWidth.toInt())
      }
      setColor(0.417f, 0.417f, 0.417f, 1f)
    }
    PixmapIO.writePNG(FileHandle("grid_$file"), pixmap)
  }

  override fun render()
  {
  }

  override fun resize(width : Int, height : Int)
  {
  }

  override fun dispose()
  {
  }

}