package gaydar

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import gaydar.ui.initialWindowWidth
import gaydar.ui.unit
import gaydar.ui.unit2
import gaydar.ui.windowToMapUnit
import kotlin.math.pow

fun main(args : Array<String>)
{
  GLMapTest().show()
}

class GLMapTest : InputAdapter(), ApplicationListener
{

  fun show()
  {
    val config = Lwjgl3ApplicationConfiguration()
    config.setTitle("PUBG Radar By wumo")
    config.setWindowIcon(Files.FileType.Internal, "icon.png")
    config.useOpenGL3(true, 3, 3)
    config.setWindowedMode(1000, 1000)
    config.setResizable(true)
    config.setBackBufferConfig(8, 8, 8, 8, 32, 0, 8)
    Lwjgl3Application(this, config)
  }

  lateinit var shapeRenderer : ShapeRenderer
  lateinit var spriteBatch : SpriteBatch
  lateinit var mapErangel : Texture
  lateinit var camera : OrthographicCamera
  lateinit var fbo : FrameBuffer
  lateinit var texture : Texture
  lateinit var fboRegion : TextureRegion

  var windowWidth = initialWindowWidth
  var windowHeight = initialWindowWidth

  override fun scrolled(amount : Int) : Boolean
  {
    camera.zoom *= 1.1f.pow(amount)
    return true
  }

  override fun create()
  {
    shapeRenderer = ShapeRenderer()
    spriteBatch = SpriteBatch()
    Gdx.input.inputProcessor = this
    camera = OrthographicCamera(windowWidth, windowHeight)
    with(camera) {
      setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
      zoom = 1 / 4f
      update()
      position.set(mapWidth / 2, mapWidth / 2, 0f)
      update()
    }

    mapErangel = Texture(Gdx.files.internal("maps/Erangel_Minimap.bmp"))

    fbo = FrameBuffer(RGBA8888, 8192, 8192, false)
    texture = fbo.colorBufferTexture
    fboRegion = TextureRegion(texture)
    fboRegion.flip(false, true) // FBO uses lower left, TextureRegion uses

  }

  override fun render()
  {
    fbo.begin()
    Gdx.gl.glClearColor(0.417f, 0.417f, 0.417f, 0f)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    //move camera
//    camera.position.set(mapWidth / 2, mapWidth / 2, 0f)
//    camera.update()
    Gdx.gl.glEnable(GL20.GL_BLEND)
    //draw map
    paint(camera.combined) {
      draw(
            mapErangel, 0f, 0f, mapWidth, mapWidth,
            0, 0, mapErangel.width, mapErangel.height,
            false, true
          )
//      val region1 = itemIcons["Item_Armor_C_01_Lv3_C"]
//      region1!!
//      var w = region1.regionWidth.toFloat()
//      var h = region1.regionHeight.toFloat()
//      draw(region1, mapWidth / 2 - w / 2,
//           mapWidth / 2 - h / 2,
//           w / 2, h / 2,
//           w, h,
//           20.0f, 20.0f,
//           30f
//      )
//      val w = carePackage.regionWidth.toFloat()
//      val h = carePackage.regionHeight.toFloat()
//      draw(carePackage, mapWidth / 2 - w / 2,
//           mapWidth / 2 - h / 2,
//           w / 2, h / 2,
//           w, h,
//           airDropScale * camera.zoom, airDropScale * camera.zoom,
//           0f
//      )
//      val w = enemyIcon.regionWidth.toFloat()
//      val h = enemyIcon.regionHeight.toFloat()
//      Gdx.gl.glEnable(GL20.GL_BLEND)
//      draw(enemyIcon, mapWidth / 2 - w / 2,
//           mapWidth / 2 - h / 2,
//           w / 2, h / 2,
//           w, h,
//           airDropScale, airDropScale,
//           0f
//      )
//    }
//    with(shapeRenderer) {
//      projectionMatrix = camera.combined
//      color = Color.RED
//      begin(Line)
//      line(0f, mapWidth / 2, mapWidth, mapWidth / 2)
//      line(mapWidth / 2, 0f, mapWidth / 2, mapWidth)
//      end()
    }
//    shapeRenderer.projectionMatrix = camera.combined
    draw(ShapeRenderer.ShapeType.Filled) {
      color = Color.BLACK
      //thin grid
      for (i in 0..7)
        for (j in 0..9)
        {
          rectLine(0f, i * unit + j * unit2, gridWidth, i * unit + j * unit2, 100f)
          rectLine(i * unit + j * unit2, 0f, i * unit + j * unit2, gridWidth, 100f)
        }
      color = Color.GRAY
      //thick grid
      for (i in 0..7)
      {
        rectLine(0f, i * unit, gridWidth, i * unit, 500f)
        rectLine(i * unit, 0f, i * unit, gridWidth, 500f)
      }
    }

    Gdx.gl.glDisable(GL20.GL_BLEND)
    fbo.end()
  }

  inline fun draw(type : ShapeRenderer.ShapeType, draw : ShapeRenderer.() -> Unit)
  {
    shapeRenderer.apply {
      begin(type)
      draw()
      end()
    }
  }

  inline fun paint(matrix : Matrix4, paint : SpriteBatch.() -> Unit)
  {
    spriteBatch.apply {
      projectionMatrix = matrix
      begin()
      paint()
      end()
    }
  }

  override fun resize(width : Int, height : Int)
  {
    windowWidth = width.toFloat()
    windowHeight = height.toFloat()
    camera.setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
  }

  override fun pause()
  {
  }

  override fun resume()
  {
  }

  override fun dispose()
  {
    mapErangel.dispose()
    spriteBatch.dispose()
    shapeRenderer.dispose()
    fbo.dispose()
  }

}