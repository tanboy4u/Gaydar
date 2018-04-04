package gaydar.ui

import com.badlogic.gdx.*
import com.badlogic.gdx.Input.Buttons.*
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
import com.badlogic.gdx.graphics.Texture.TextureFilter.MipMap
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.DEFAULT_CHARS
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line
import com.badlogic.gdx.math.*
import org.lwjgl.opengl.GL11.GL_TEXTURE_BORDER_COLOR
import org.lwjgl.opengl.GL11.glTexParameterfv
import org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER
import gaydar.*
import gaydar.Sniffer.Companion.localAddr
import gaydar.Sniffer.Companion.sniffOption
import gaydar.deserializer.channel.ActorChannel.Companion.actorHasWeapons
import gaydar.deserializer.channel.ActorChannel.Companion.actors
import gaydar.deserializer.channel.ActorChannel.Companion.airDropLocation
import gaydar.deserializer.channel.ActorChannel.Companion.attacks
import gaydar.deserializer.channel.ActorChannel.Companion.corpseLocation
import gaydar.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import gaydar.deserializer.channel.ActorChannel.Companion.firing
import gaydar.deserializer.channel.ActorChannel.Companion.playerStateToActor
import gaydar.deserializer.channel.ActorChannel.Companion.redZoneBombLocation
import gaydar.deserializer.channel.ActorChannel.Companion.selfID
import gaydar.deserializer.channel.ActorChannel.Companion.selfStateID
import gaydar.deserializer.channel.ActorChannel.Companion.teams
import gaydar.deserializer.channel.ActorChannel.Companion.visualActors
import gaydar.deserializer.channel.ActorChannel.Companion.weapons
import gaydar.struct.*
import gaydar.struct.Archetype.*
import gaydar.struct.Archetype.Plane
import gaydar.struct.CMD.ActorCMD.actorWithPlayerState
import gaydar.struct.CMD.CharacterCMD.actorHealth
import gaydar.struct.CMD.GameStateCMD.ElapsedWarningDuration
import gaydar.struct.CMD.GameStateCMD.MatchElapsedMinutes
import gaydar.struct.CMD.GameStateCMD.NumAlivePlayers
import gaydar.struct.CMD.GameStateCMD.NumAliveTeams
import gaydar.struct.CMD.GameStateCMD.PoisonGasWarningPosition
import gaydar.struct.CMD.GameStateCMD.PoisonGasWarningRadius
import gaydar.struct.CMD.GameStateCMD.RedZonePosition
import gaydar.struct.CMD.GameStateCMD.RedZoneRadius
import gaydar.struct.CMD.GameStateCMD.RemainingTime
import gaydar.struct.CMD.GameStateCMD.SafetyZonePosition
import gaydar.struct.CMD.GameStateCMD.SafetyZoneRadius
import gaydar.struct.CMD.GameStateCMD.TotalWarningDuration
import gaydar.struct.CMD.GameStateCMD.isTeamMatch
import gaydar.struct.CMD.playerNumKills
import gaydar.struct.CMD.selfCoords
import gaydar.struct.CMD.selfDirection
import gaydar.struct.Item.Companion.order
import gaydar.struct.PlayerState
import gaydar.struct.Team
import gaydar.struct.Weapon
import gaydar.util.debugln
import gaydar.util.settings.Settings
import gaydar.util.tuple4
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.absoluteValue
import kotlin.math.asin
import kotlin.math.pow

typealias renderInfo = tuple4<Actor, Float, Float, Float>

val itemIcons = HashMap<String, AtlasRegion>()
val crateIcons = HashMap<String, AtlasRegion>()

class GLMap(private val jsettings : Settings.jsonsettings) : InputAdapter(), ApplicationListener, GameListener {
  companion object {
    operator fun Vector3.component1(): Float = x
    operator fun Vector3.component2(): Float = y
    operator fun Vector3.component3(): Float = z
    operator fun Vector2.component1(): Float = x
    operator fun Vector2.component2(): Float = y

  }

  init {
    register(this)
  }

  override fun onGameOver() {
    mapCamera.zoom = 1 / 4f

    aimStartTime.clear()
    attackLineStartTime.clear()
    firingStartTime.clear()
  }

  fun show() {
    val config = Lwjgl3ApplicationConfiguration()
    config.setTitle("[${localAddr.hostAddress} ${sniffOption.name}] - Gaydar v6.9")
    config.setWindowIcon(Files.FileType.Internal, "icon.png")
    config.useOpenGL3(false, 2, 1)
    config.setWindowedMode(initialWindowWidth.toInt(), initialWindowWidth.toInt())
    config.setResizable(true)
    config.setBackBufferConfig(8, 8, 8, 8, 16, 1, 2)
    Lwjgl3Application(this, config)
  }

  lateinit var spriteBatch: SpriteBatch
  lateinit var shapeRenderer: ShapeRenderer
  lateinit var mapErangel: Texture
  lateinit var mapMiramar: Texture
  lateinit var map: Texture
  lateinit var fbo: FrameBuffer
  lateinit var miniMap: TextureRegion
  lateinit var carePackage: TextureRegion
  lateinit var corpseIcon: TextureRegion
  lateinit var vehicleIcons: Map<Archetype, TextureRegion>
  lateinit var grenadeIcons: Map<Archetype, TextureRegion>
  lateinit var redzoneBombIcon: TextureRegion
  lateinit var largeFont: BitmapFont
  lateinit var littleFont: BitmapFont
  lateinit var fontCamera: OrthographicCamera
  lateinit var camera: OrthographicCamera
  lateinit var mapCamera: OrthographicCamera
  lateinit var miniMapCamera: OrthographicCamera
  lateinit var alarmSound: Sound
  lateinit var pawnAtlas: TextureAtlas
  lateinit var itemAtlas: TextureAtlas
  lateinit var crateAtlas: TextureAtlas
  lateinit var markerAtlas: TextureAtlas
  lateinit var markers: Array<TextureRegion>
  private lateinit var parachute: Texture
  private lateinit var teamarrow: Texture
  private lateinit var teamsight: Texture
  private lateinit var arrow: Texture
  private lateinit var arrowsight: Texture
  private lateinit var jetski: Texture
  private lateinit var player: Texture
  private lateinit var playersight: Texture

  private lateinit var hubFont: BitmapFont
  private lateinit var hubFontShadow: BitmapFont
  private lateinit var espFont: BitmapFont
  private lateinit var espFontShadow: BitmapFont
  private lateinit var compaseFont: BitmapFont
  private lateinit var compaseFontShadow: BitmapFont
  private lateinit var littleFontShadow: BitmapFont
  private lateinit var nameFont: BitmapFont
  private lateinit var itemFont: BitmapFont
  private lateinit var hporange: BitmapFont
  private lateinit var hpred: BitmapFont
  private lateinit var hpgreen: BitmapFont
  private lateinit var menuFont: BitmapFont
  private lateinit var menuFontOn: BitmapFont
  private lateinit var menuFontOFF: BitmapFont
  private lateinit var hubpanel: Texture
  private lateinit var hubpanelblank: Texture
  private lateinit var menu: Texture
  private lateinit var bgcompass: Texture
  val firingStartTime = LinkedList<tuple4<Float, Float, Float, Long>>()
  private val layout = GlyphLayout()
  private var windowWidth = initialWindowWidth
  private var windowHeight = initialWindowWidth
  val clipBound = Rectangle()
  private val aimStartTime = HashMap<NetworkGUID, Long>()
  private val attackLineStartTime = LinkedList<Triple<NetworkGUID, NetworkGUID, Long>>()
  private val pinLocation = Vector2()


  // Please change your pre-build settings in Settings.kt
  // You can change them in Settings.json after you run the game once too.
  private var filterWeapon = jsettings.filterWeapon
  private var filterAttach = jsettings.filterAttach
  private var filterLvl2 = jsettings.filterLvl2
  private var filterScope = jsettings.filterScope
  private var filterHeals = jsettings.filterHeals
  private var filterAmmo = jsettings.filterAmmo
  private var filterThrow = jsettings.filterThrow
  private var drawcompass = jsettings.drawcompass
  private var drawmenu = jsettings.drawmenu
  private var toggleView = jsettings.toggleView
  private var drawDaMap = jsettings.drawDaMap

  // Please change your pre-build settings in Settings.kt
  // You can change them in Settings.json after you run the game once too.

  // private var toggleVehicles = -1
  //  private var toggleVNames = -1

  private var nameToggles = jsettings.nameToggles
  private var VehicleInfoToggles = jsettings.VehicleInfoToggles
  private var ZoomToggles = jsettings.ZoomToggles
  private var scopesToFilter = arrayListOf("")
  private var weaponsToFilter = arrayListOf("")
  private var attachToFilter = arrayListOf("")
  private var level2Filter = arrayListOf("")
  private var level3Filter = arrayListOf("")
  private var level23Filter = arrayListOf("")
  private var level1Filter = arrayListOf("")
  private var equipFilter = arrayListOf("")
  private var healsToFilter = arrayListOf("")
  private var ammoToFilter = arrayListOf("")
  private var throwToFilter = arrayListOf("")
  private var dragging = false
  private var prevScreenX = -1f
  private var prevScreenY = -1f
  private var screenOffsetX = 0f
  private var screenOffsetY = 0f


  val miniMapWindowWidth = jsettings.miniMapWindowWidth
  val miniMapRadius = jsettings.miniMapRadius
  val playerRadius = jsettings.playerRadius
  val healthBarWidth = jsettings.healthBarWidth
  val healthBarHeight = jsettings.healthBarHeight
  val directionRadius = jsettings.directionRadius
  val fov = jsettings.fov
  val aimLineWidth = jsettings.aimLineWidth
  val aimLineRange = jsettings.aimLineRange
  val aimCircleRadius = jsettings.aimCircleRadius
  val aimTimeThreshold = jsettings.aimTimeThreshold
  val attackLineDuration = jsettings.attackLineDuration
  val attackMeLineDuration = jsettings.attackMeLineDuration
  val firingLineDuration = jsettings.firingLineDuration
  val firingLineLength = jsettings.firingLineLength
  val itemZoomThreshold = jsettings.itemZoomThreshold
  val airDropTextScale = jsettings.airDropTextScale
  val itemScale = jsettings.itemScale
  val staticItemScale = jsettings.staticItemScale
  val mapMarkerScale = jsettings.mapMarkerScale
  val airDropScale = jsettings.airDropScale
  val vehicleScale = jsettings.vehicleScale
  val planeScale = jsettings.planeScale
  val grenadeScale = jsettings.grenadeScale
  val corpseScale = jsettings.corpseScale
  val redzoneBombScale = jsettings.redzoneBombScale

  // Please change your pre-build settings in Settings.kt
  // You can change them in Settings.json after you run the game once too.


  private fun windowToMap(x: Float, y: Float) =
          Vector2(
                  selfCoords.x + (x - windowWidth / 2.0f) * mapCamera.zoom * windowToMapUnit + screenOffsetX,
                  selfCoords.y + (y - windowHeight / 2.0f) * mapCamera.zoom * windowToMapUnit + screenOffsetY
          )

  private fun mapToWindow(x: Float, y: Float) =
          Vector2(
                  (x - selfCoords.x - screenOffsetX) / (mapCamera.zoom * windowToMapUnit) + windowWidth / 2.0f,
                  (y - selfCoords.y - screenOffsetY) / (mapCamera.zoom * windowToMapUnit) + windowHeight / 2.0f
          )


  fun Vector2.windowToMap() = windowToMap(x, y)
  fun Vector2.mapToWindow() = mapToWindow(x, y)
  fun windowToMap(length: Float) = length * mapCamera.zoom * windowToMapUnit
  fun mapToWindow(length: Float) = length / (mapCamera.zoom * windowToMapUnit)


  override fun scrolled(amount: Int): Boolean {

    if (mapCamera.zoom >= 0.01f && mapCamera.zoom <= 1f) {
      mapCamera.zoom *= 1.05f.pow(amount)
      miniMapCamera.zoom = if (mapCamera.zoom > 1 / 8f) 1 / 2f else 1 / 4f
    } else {
      if (mapCamera.zoom < 0.01f) {
        mapCamera.zoom = 0.01f
        println("Max Zoom")
      }
      if (mapCamera.zoom > 1f) {
        mapCamera.zoom = 1f
        println("Min Zoom")
      }
    }

    return true
  }

  override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
    when (button) {
      RIGHT -> {
        pinLocation.set(pinLocation.set(screenX.toFloat(), screenY.toFloat()).windowToMap())
        camera.update()
        println(pinLocation)
        return true
      }
      LEFT -> {
        dragging = true
        prevScreenX = screenX.toFloat()
        prevScreenY = screenY.toFloat()
        return true
      }
      MIDDLE -> {
        screenOffsetX = 0f
        screenOffsetY = 0f
      }
    }
    return false
  }

  override fun keyDown(keycode: Int): Boolean {

    when (keycode) {


    // Change Player Info
      Input.Keys.valueOf(jsettings.nameToogle_Key) -> {
        if (nameToggles < 5) {
          nameToggles += 1
        }
        if (nameToggles == 5) {
          nameToggles = 0
        }
      }

      Input.Keys.valueOf(jsettings.VehicleInfoToggles_Key) -> {
        if (VehicleInfoToggles <= 4) {
          VehicleInfoToggles += 1
        }
        if (VehicleInfoToggles == 4) {
          VehicleInfoToggles = 1
        }
      }
    // Zoom (Loot, Combat, Scout)
      Input.Keys.valueOf(jsettings.ZoomToggles_Key) -> {
        if (ZoomToggles <= 4) {
          ZoomToggles += 1
        }
        if (ZoomToggles == 4) {
          ZoomToggles = 1
        }
        if (ZoomToggles == 1) {
          mapCamera.zoom = 1 / 8f
          camera.zoom = 1 / 24f

        }
        if (ZoomToggles == 2) {
          mapCamera.zoom = 1 / 12f
          camera.zoom = 1 / 12f
        }
        if (ZoomToggles == 3) {
          mapCamera.zoom = 1 / 24f
          camera.zoom = 1 / 8f
        }
      }

    // Level 1 and 2 item filters
      Input.Keys.valueOf(jsettings.filterLvl2_Key) -> {
        if (filterLvl2 < 5) {
          filterLvl2 += 1
        }
        if (filterLvl2 == 5) {
          filterLvl2 = 0
        }
      }

    // Please Change Your Settings in Util/Settings.kt
    // Other Filter Keybinds
      Input.Keys.valueOf(jsettings.drawcompass_Key) -> drawcompass = drawcompass * -1


    // Toggle View Line
      Input.Keys.valueOf(jsettings.toggleView_Key) -> toggleView = toggleView * -1

    // Toggle Da Minimap
      Input.Keys.valueOf(jsettings.drawDaMap_Key) -> drawDaMap = drawDaMap * -1

    // Toggle Menu
      Input.Keys.valueOf(jsettings.drawmenu_Key) -> drawmenu = drawmenu * -1

    // Icon Filter Keybinds
      Input.Keys.valueOf(jsettings.filterWeapon_Key) -> filterWeapon = filterWeapon * -1
      Input.Keys.valueOf(jsettings.filterHeals_Key) -> filterHeals = filterHeals * -1
      Input.Keys.valueOf(jsettings.filterThrow_Key) -> filterThrow = filterThrow * -1
      Input.Keys.valueOf(jsettings.filterAttach_Key) -> filterAttach = filterAttach * -1
      Input.Keys.valueOf(jsettings.filterScope_Key) -> filterScope = filterScope * -1
      Input.Keys.valueOf(jsettings.filterAmmo_Key) -> filterAmmo = filterAmmo * -1

    // Zoom In/Out || Overrides Max/Min Zoom
      Input.Keys.valueOf(jsettings.camera_zoom_Minus_Key) -> mapCamera.zoom = mapCamera.zoom + 0.00525f
      Input.Keys.valueOf(jsettings.camera_zoom_Plus_Key) -> mapCamera.zoom = mapCamera.zoom - 0.00525f

    // Please Change Your Settings in Util/Settings.kt
    }
    return false
  }

  override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
    if (!dragging) return false
    with(camera) {
      screenOffsetX += (prevScreenX - screenX.toFloat()) * camera.zoom * 500
      screenOffsetY += (prevScreenY - screenY.toFloat()) * camera.zoom * 500
      prevScreenX = screenX.toFloat()
      prevScreenY = screenY.toFloat()
    }
    return true
  }


  override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
    if (button == LEFT) {
      dragging = false
      return true
    }
    return false
  }


  override fun create() {
    spriteBatch = SpriteBatch()
    shapeRenderer = ShapeRenderer()
    Gdx.input.inputProcessor = this
    mapCamera = OrthographicCamera(windowWidth, windowHeight)
    miniMapCamera = OrthographicCamera()
    with(mapCamera) {
      setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
      zoom = 1 / 4f
      update()
      position.set(mapWidth / 2, mapWidth / 2, 0f)
      update()
    }
    with(miniMapCamera) {
      val z = 1 / 4f
      setToOrtho(true, miniMapRadius * 2 / z, miniMapRadius * 2 / z)
      zoom = z
      update()
    }
    camera = mapCamera
    fontCamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
    alarmSound = Gdx.audio.newSound(Gdx.files.internal("Alarm.wav"))
    glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, floatArrayOf(bgColor.r, bgColor.g, bgColor.b, bgColor.a))
    mapErangel = Texture(Gdx.files.internal("maps/Erangel_Minimap.png"), null, true).apply {
      setFilter(MipMap, Linear)
      Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER.toFloat())
      Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER.toFloat())
    }
    mapMiramar = Texture(Gdx.files.internal("maps/Miramar_Minimap.png"), null, true).apply {
      setFilter(MipMap, Linear)
      Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER.toFloat())
      Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER.toFloat())
    }
    map = mapErangel
    fbo = FrameBuffer(RGBA8888, miniMapWindowWidth.toInt(), miniMapWindowWidth.toInt(), false)
    miniMap = TextureRegion(fbo.colorBufferTexture)

    hubpanel = Texture(Gdx.files.internal("images/hub_panel.png"))
    menu = Texture(Gdx.files.internal("images/menu.png"))
    bgcompass = Texture(Gdx.files.internal("images/bg_compass.png"))
    arrow = Texture(Gdx.files.internal("images/arrow.png"))
    player = Texture(Gdx.files.internal("images/player.png"))
    playersight = Texture(Gdx.files.internal("images/green_view_line.png"))
    teamsight = Texture(Gdx.files.internal("images/teamsight.png"))
    arrowsight = Texture(Gdx.files.internal("images/red_view_line.png"))
    teamarrow = Texture(Gdx.files.internal("images/team.png"))
    parachute = Texture(Gdx.files.internal("images/parachute.png"))

    parachute = Texture(Gdx.files.internal("images/parachute.png"))

    itemAtlas = TextureAtlas(Gdx.files.internal("icons/itemIcons.txt"))
    for (region in itemAtlas.regions)
      itemIcons[region.name] = region.apply { flip(false, true) }


    crateAtlas = TextureAtlas(Gdx.files.internal("icons/crateIcons.txt"))
    for (region in crateAtlas.regions)
      crateIcons[region.name] = region.apply { flip(false, true) }

    pawnAtlas = TextureAtlas(Gdx.files.internal("icons/APawnIcons.txt"))
    for (region in pawnAtlas.regions)
      region.flip(false, true)

    carePackage = pawnAtlas.findRegion("CarePackage")
    corpseIcon = pawnAtlas.findRegion("corpse")
    redzoneBombIcon = pawnAtlas.findRegion("redzoneBomb")
    vehicleIcons = mapOf(
            TwoSeatBoat to pawnAtlas.findRegion("AquaRail"),
            SixSeatBoat to pawnAtlas.findRegion("boat"),
            Dacia to pawnAtlas.findRegion("dacia"),
            Uaz to pawnAtlas.findRegion("uaz"),
            Pickup to pawnAtlas.findRegion("pickup"),
            Buggy to pawnAtlas.findRegion("buggy"),
            Bike to pawnAtlas.findRegion("bike"),
            SideCar to pawnAtlas.findRegion("bike"),
            Bus to pawnAtlas.findRegion("bus"),
            Plane to pawnAtlas.findRegion("plane")
    )
    grenadeIcons = mapOf(
            SmokeBomb to pawnAtlas.findRegion("smokebomb"),
            Molotov to pawnAtlas.findRegion("molotov"),
            Grenade to pawnAtlas.findRegion("fragbomb"),
            FlashBang to pawnAtlas.findRegion("flashbang")
    )


    markerAtlas = TextureAtlas(Gdx.files.internal("icons/Markers.txt"))
    for (region in markerAtlas.regions)
      region.flip(false, true)


    markers = arrayOf(
            markerAtlas.findRegion("marker1"), markerAtlas.findRegion("marker2"),
            markerAtlas.findRegion("marker3"), markerAtlas.findRegion("marker4"),
            markerAtlas.findRegion("marker5"), markerAtlas.findRegion("marker6"),
            markerAtlas.findRegion("marker7"), markerAtlas.findRegion("marker8")
    )

    val generatorHub = FreeTypeFontGenerator(Gdx.files.internal("font/AGENCYFB.TTF"))
    val paramHub = FreeTypeFontParameter()
    paramHub.characters = DEFAULT_CHARS
    paramHub.size = jsettings.hubFont_size
    paramHub.color = jsettings.hubFont_color
    hubFont = generatorHub.generateFont(paramHub)
    paramHub.color = jsettings.hubFontShadow_color
    hubFontShadow = generatorHub.generateFont(paramHub)
    paramHub.size = jsettings.espFont_size
    paramHub.color = jsettings.espFont_color
    espFont = generatorHub.generateFont(paramHub)
    paramHub.color = jsettings.espFontShadow_color
    espFontShadow = generatorHub.generateFont(paramHub)
    val generatorNumber = FreeTypeFontGenerator(Gdx.files.internal("font/NUMBER.TTF"))
    val paramNumber = FreeTypeFontParameter()
    paramNumber.characters = DEFAULT_CHARS
    paramNumber.size = jsettings.largeFont_size
    paramNumber.color = jsettings.largeFont_color
    largeFont = generatorNumber.generateFont(paramNumber)
    val generator = FreeTypeFontGenerator(Gdx.files.internal("font/GOTHICB.TTF"))
    val param = FreeTypeFontParameter()
    param.characters = DEFAULT_CHARS
    param.size = jsettings.largeFont_size2
    param.color = jsettings.largeFont_color2
    largeFont = generator.generateFont(param)
    param.size = jsettings.littleFont_size
    param.color = jsettings.littleFont_color
    littleFont = generator.generateFont(param)
    param.color = jsettings.nameFont_color
    param.size = jsettings.nameFont_size
    nameFont = generator.generateFont(param)
    param.color = jsettings.itemFont_color
    param.size = jsettings.itemFont_size
    itemFont = generator.generateFont(param)
    param.color = jsettings.compaseFont_color
    param.size = jsettings.compaseFont_size
    compaseFont = generator.generateFont(param)
    param.color = jsettings.compaseFontShadow_color
    compaseFontShadow = generator.generateFont(param)
    param.characters = DEFAULT_CHARS
    param.size = jsettings.littleFont_size2
    param.color = jsettings.littleFont_color2
    littleFont = generator.generateFont(param)
    param.color = jsettings.littleFontShadow_color
    littleFontShadow = generator.generateFont(param)
    param.color = jsettings.menuFont_color
    param.size = jsettings.menuFont_size
    menuFont = generator.generateFont(param)
    param.color = jsettings.menuFontOn_color
    param.size = jsettings.menuFontOn_size
    menuFontOn = generator.generateFont(param)
    param.color = jsettings.menuFontOFF_color
    param.size = jsettings.menuFontOFF_size
    menuFontOFF = generator.generateFont(param)
    param.color = jsettings.hporange_color
    param.size = jsettings.hporange_size
    hporange = generator.generateFont(param)
    param.color = jsettings.hpgreen_color
    param.size = jsettings.hpgreen_size
    hpgreen = generator.generateFont(param)
    param.color = jsettings.hpred_color
    param.size = jsettings.hpred_size
    hpred = generator.generateFont(param)


    generatorHub.dispose()
    generatorNumber.dispose()
    generator.dispose()
  }

  private val dirUnitVector = Vector2(1f, 0f)

  override fun render() {
    Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    if (gameStarted)
      map = if (isErangel) mapErangel else mapMiramar
    else
      return
    actors[selfID]?.apply {
      actors[attachParent ?: return@apply]?.apply {
        selfCoords.set(location.x, location.y, location.z)
        selfDirection = rotation.y
      }
    }
    val (selfX, selfY) = selfCoords
    //move camera
    mapCamera.position.set(selfCoords.x + screenOffsetX, selfCoords.y + screenOffsetY, 0f)
    mapCamera.update()

    val mapRegion = Rectangle().apply {
      setPosition(windowToMap(0f, 0f))
      width = windowToMap(windowWidth)
      height = windowToMap(windowHeight)
    }
    val miniMapRegion = Rectangle().apply {
      x = selfCoords.x - miniMapRadius
      y = selfCoords.y - miniMapRadius
      width = miniMapRadius * 2
      height = miniMapRadius * 2
    }

    var parachutes: ArrayList<renderInfo>? = null
    var players: ArrayList<renderInfo>? = null
    var vehicles: ArrayList<renderInfo>? = null
    var grenades: ArrayList<renderInfo>? = null

    for ((_, actor) in visualActors) {
      val (x, y) = actor.location
      if (!mapRegion.contains(x, y) && !miniMapRegion.contains(x, y)) continue
      val visualActor = tuple4(actor, x, y, actor.rotation.y)
      val list = when (actor.type) {
        Parachute -> {
          parachutes = parachutes ?: ArrayList()
          parachutes
        }
        Player -> {
          players = players ?: ArrayList()
          players
        }
        TwoSeatBoat, SixSeatBoat, Dacia, Uaz, Pickup, Buggy,
        Bike, SideCar, Bus, Plane -> {
          vehicles = vehicles ?: ArrayList()
          actor as Vehicle
          actor.apply {
            var driver: Actor? = null
            for (child in attachChildren) {
              driver = actors[child] ?: continue
              break
            }
            if (driver == null && driverPlayerState.isValid()) {
              val driverID = playerStateToActor[driverPlayerState]
              driver = if (driverID != null) actors[driverID] else null
            }
            if (driver == null) return@apply
            val _players = players ?: ArrayList()
            _players.add(visualActor.copy(_1 = driver))
            players = _players
          }
          vehicles
        }
        SmokeBomb, Molotov, Grenade, FlashBang -> {
          grenades = grenades ?: ArrayList()
          grenades
        }
        else -> null
      }
      list?.add(visualActor)
    }
    clipBound.set(mapRegion)
    camera = mapCamera

    //draw map
    paint(camera.combined) {
      draw(
              map, 0f, 0f, mapWidth, mapWidth,
              0, 0, map.width, map.height,
              false, true
      )
      drawRedZoneBomb()
      drawMapMarkers()
      drawVehicles(vehicles)
      drawCorpse()
      drawItem()
      drawGrenades(grenades)
      drawAirDrop()
    }


    val numKills = playerNumKills[selfStateID] ?: 0
    val zero = numKills.toString()
    paint(fontCamera.combined) {
      val timeHints = if (RemainingTime > 0) "${RemainingTime}s"
      else "${MatchElapsedMinutes}min"

      // NUMBER PANEL
      val numText = "$NumAlivePlayers"
      layout.setText(hubFont, numText)
      spriteBatch.draw(hubpanel, windowWidth - 130f, windowHeight - 60f)
      hubFontShadow.draw(spriteBatch, "ALIVE", windowWidth - 85f, windowHeight - 29f)
      hubFont.draw(spriteBatch, "$NumAlivePlayers", windowWidth - 110f - layout.width / 2, windowHeight - 29f)
      val teamText = "$NumAliveTeams"


      if (isTeamMatch) {
        layout.setText(hubFont, teamText)
        spriteBatch.draw(hubpanel, windowWidth - 260f, windowHeight - 60f)
        hubFontShadow.draw(spriteBatch, "TEAM", windowWidth - 215f, windowHeight - 29f)
        hubFont.draw(spriteBatch, "$NumAliveTeams", windowWidth - 240f - layout.width / 2, windowHeight - 29f)
      }
      if (isTeamMatch) {

        layout.setText(hubFont, zero)
        spriteBatch.draw(hubpanel, windowWidth - 390f, windowHeight - 60f)
        hubFontShadow.draw(spriteBatch, "KILLS", windowWidth - 345f, windowHeight - 29f)
        hubFont.draw(spriteBatch, "$zero", windowWidth - 370f - layout.width / 2, windowHeight - 29f)
      } else {
        spriteBatch.draw(hubpanel, windowWidth - 390f + 130f, windowHeight - 60f)
        hubFontShadow.draw(spriteBatch, "KILLS", windowWidth - 345f + 128f, windowHeight - 29f)
        hubFont.draw(spriteBatch, "$zero", windowWidth - 370f + 128f - layout.width / 2, windowHeight - 29f)

      }


      // ITEM ESP FILTER PANEL
      //  spriteBatch.draw(hubpanelblank, 30f, windowHeight - 60f)

      // This is what you were trying to do
      if (filterWeapon != 1)
        espFont.draw(spriteBatch, "WEAPON", 40f, windowHeight - 25f)
      else
        espFontShadow.draw(spriteBatch, "WEAPON", 39f, windowHeight - 25f)

      if (filterAttach != 1)
        espFont.draw(spriteBatch, "ATTACH", 40f, windowHeight - 42f)
      else
        espFontShadow.draw(spriteBatch, "ATTACH", 40f, windowHeight - 42f)

      if (filterLvl2 != 0)
        espFont.draw(spriteBatch, "EQUIP", 100f, windowHeight - 25f)
      else
        espFontShadow.draw(spriteBatch, "EQUIP", 100f, windowHeight - 25f)

      if (filterScope != 1)
        espFont.draw(spriteBatch, "SCOPE", 98f, windowHeight - 42f)
      else
        espFontShadow.draw(spriteBatch, "SCOPE", 98f, windowHeight - 42f)

      if (filterHeals != 1)
        espFont.draw(spriteBatch, "MEDS", 150f, windowHeight - 25f)
      else
        espFontShadow.draw(spriteBatch, "MEDS", 150f, windowHeight - 25f)

      if (filterAmmo != 1)
        espFont.draw(spriteBatch, "AMMO", 150f, windowHeight - 42f)
      else
        espFontShadow.draw(spriteBatch, "AMMO", 150f, windowHeight - 42f)
      if (drawcompass == 1)
        espFont.draw(spriteBatch, "COMPASS", 200f, windowHeight - 42f)
      else
        espFontShadow.draw(spriteBatch, "COMPASS", 200f, windowHeight - 42f)
      if (filterThrow != 1)
        espFont.draw(spriteBatch, "THROW", 200f, windowHeight - 25f)
      else
        espFontShadow.draw(spriteBatch, "THROW", 200f, windowHeight - 25f)

      if (drawmenu == 1)
        espFont.draw(spriteBatch, "[" + jsettings.drawmenu_Key + "] Menu ON", 270f, windowHeight - 25f)
      else
        espFontShadow.draw(spriteBatch, "[" + jsettings.drawmenu_Key + "] Menu OFF", 270f, windowHeight - 25f)

      val num = nameToggles
      espFontShadow.draw(spriteBatch, "[" + jsettings.nameToogle_Key + "] Player Info: $num", 270f, windowHeight - 42f)

      val znum = ZoomToggles
      espFontShadow.draw(spriteBatch, "[" + jsettings.ZoomToggles_Key + "] Zoom Toggle: $znum", 40f, windowHeight - 68f)

      val vnum = VehicleInfoToggles
      espFontShadow.draw(spriteBatch, "[" + jsettings.VehicleInfoToggles_Key + "] Vehicle Toggles: $vnum", 40f, windowHeight - 85f)


      val pinDistance = (pinLocation.cpy().sub(selfCoords.x, selfCoords.y).len() / 100).toInt()
      val (x, y) = pinLocation.mapToWindow()


      safeZoneHint()


      val camnum = camera.zoom

      if (drawmenu == 1) {
        spriteBatch.draw(menu, 20f, windowHeight / 2 - 200f)

        //
        menuFont.draw(spriteBatch, jsettings.filterWeapon_Key, 120f, windowHeight / 2 + 103f)
        menuFont.draw(spriteBatch, jsettings.filterLvl2_Key, 120f, windowHeight / 2 + 85f)
        menuFont.draw(spriteBatch, jsettings.filterHeals_Key, 120f, windowHeight / 2 + 67f)
        menuFont.draw(spriteBatch, jsettings.filterThrow_Key, 120f, windowHeight / 2 + 49f)
        menuFont.draw(spriteBatch, jsettings.filterAttach_Key, 120f, windowHeight / 2 + 31f)
        menuFont.draw(spriteBatch, jsettings.filterScope_Key, 120f, windowHeight / 2 + 13f)
        menuFont.draw(spriteBatch, jsettings.filterAmmo_Key, 120f, windowHeight / 2 - 5f)

        // Filters
        if (filterWeapon != 1)
          menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 103f)
        else
          menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 103f)

        if (filterLvl2 == 1)
          menuFontOn.draw(spriteBatch, "Level 1", 187f, windowHeight / 2 + 85f)

        if (filterLvl2 == 2)
          menuFontOn.draw(spriteBatch, "Level 2", 187f, windowHeight / 2 + 85f)

        if (filterLvl2 == 3)
          menuFontOn.draw(spriteBatch, "Level 3", 187f, windowHeight / 2 + 85f)

        if (filterLvl2 == 4)
          menuFontOn.draw(spriteBatch, "Level 2/3", 187f, windowHeight / 2 + 85f)


        if (filterLvl2 == 0)
          menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 85f)

        if (filterHeals != 1)
          menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 67f)
        else
          menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 67f)

        if (filterThrow != 1)
          menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 49f)
        else
          menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 49f)

        if (filterAttach != 1)
          menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 31f)
        else
          menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 31f)

        if (filterScope != 1)
          menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + 13f)
        else
          menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + 13f)

        if (filterAmmo != 1)
          menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -5f)
        else
          menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -5f)



        menuFont.draw(spriteBatch, jsettings.camera_zoom_Minus_Key, 120f, windowHeight / 2 - 45f)
        menuFont.draw(spriteBatch, jsettings.camera_zoom_Plus_Key, 120f, windowHeight / 2 - 63f)

        val camvalue = camera.zoom
        when {
          camvalue <= 0.0100f -> menuFontOFF.draw(spriteBatch, "Max Zoom", 187f, windowHeight / 2 + -27f)
          camvalue >= 1f -> menuFontOFF.draw(spriteBatch, "Min Zoom", 187f, windowHeight / 2 + -27f)
          camvalue == 0.2500f -> menuFont.draw(spriteBatch, "Default", 187f, windowHeight / 2 + -27f)
          camvalue == 0.1250f -> menuFont.draw(spriteBatch, "Scouting", 187f, windowHeight / 2 + -27f)
          camvalue >= 0.0833f -> menuFont.draw(spriteBatch, "Combat", 187f, windowHeight / 2 + -27f)
          camvalue <= 0.0417f -> menuFont.draw(spriteBatch, "Looting", 187f, windowHeight / 2 + -27f)

          else -> menuFont.draw(spriteBatch, ("%.4f").format(camnum), 187f, windowHeight / 2 + -27f)
        }

        menuFont.draw(spriteBatch, jsettings.nameToogle_Key, 120f, windowHeight / 2 - 89f)
        menuFont.draw(spriteBatch, jsettings.drawcompass_Key, 120f, windowHeight / 2 - 107f)
        menuFont.draw(spriteBatch, jsettings.drawDaMap_Key, 120f, windowHeight / 2 - 125f)
        menuFont.draw(spriteBatch, jsettings.toggleView_Key, 120f, windowHeight / 2 - 143f)
        menuFont.draw(spriteBatch, jsettings.VehicleInfoToggles_Key, 120f, windowHeight / 2 - 161f)
        menuFont.draw(spriteBatch, jsettings.drawmenu_Key, 120f, windowHeight / 2 - 179f)

        // Name Toggles
        val togs = nameToggles
        if (nameToggles >= 1)

          menuFontOn.draw(spriteBatch, "Enabled: $togs", 187f, windowHeight / 2 + -89f)
        else
          menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -89f)


        // Compass
        if (drawcompass != 1)

          menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -107f)
        else
          menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -107f)


        if (drawDaMap == 1)

          menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -125f)
        else
          menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -125f)

        if (toggleView == 1)
          menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -143f)
        else
          menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -143f)

        if (VehicleInfoToggles < 3)
          menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -161f)
        if (VehicleInfoToggles == 3)
          menuFontOFF.draw(spriteBatch, "Disabled", 187f, windowHeight / 2 + -161f)

        // DrawMenu == 1 already
        menuFontOn.draw(spriteBatch, "Enabled", 187f, windowHeight / 2 + -179f)
      }
      // DrawMenu == 0 (Disabled)


      if (drawcompass == 1) {

        spriteBatch.draw(bgcompass, windowWidth / 2 - 168f, windowHeight / 2 - 168f)

        layout.setText(compaseFont, "0")
        compaseFont.draw(spriteBatch, "0", windowWidth / 2 - layout.width / 2, windowHeight / 2 + layout.height + 150)                  // N
        layout.setText(compaseFont, "45")
        compaseFont.draw(spriteBatch, "45", windowWidth / 2 - layout.width / 2 + 104, windowHeight / 2 + layout.height / 2 + 104)          // NE
        layout.setText(compaseFont, "90")
        compaseFont.draw(spriteBatch, "90", windowWidth / 2 - layout.width / 2 + 147, windowHeight / 2 + layout.height / 2)                // E
        layout.setText(compaseFont, "135")
        compaseFont.draw(spriteBatch, "135", windowWidth / 2 - layout.width / 2 + 106, windowHeight / 2 + layout.height / 2 - 106)          // SE
        layout.setText(compaseFont, "180")
        compaseFont.draw(spriteBatch, "180", windowWidth / 2 - layout.width / 2, windowHeight / 2 + layout.height / 2 - 151)                // S
        layout.setText(compaseFont, "225")
        compaseFont.draw(spriteBatch, "225", windowWidth / 2 - layout.width / 2 - 109, windowHeight / 2 + layout.height / 2 - 109)          // SW
        layout.setText(compaseFont, "270")
        compaseFont.draw(spriteBatch, "270", windowWidth / 2 - layout.width / 2 - 153, windowHeight / 2 + layout.height / 2)                // W
        layout.setText(compaseFont, "315")
        compaseFont.draw(spriteBatch, "315", windowWidth / 2 - layout.width / 2 - 106, windowHeight / 2 + layout.height / 2 + 106)          // NW
      }
      littleFont.draw(spriteBatch, "$pinDistance", x, windowHeight - y)

      safeZoneHint()
      drawPlayerSprites(parachutes, players)
      drawPlayerInfos(players)
    }


    Gdx.gl.glEnable(GL20.GL_BLEND)
    shapeRenderer.projectionMatrix = camera.combined
    draw(Line) {
      players?.forEach {
        aimAtMe(it)
      }
      drawCircles()
      drawAttackLine()
      drawAirDropLine()
    }

    draw(Filled) {
      color = redZoneColor
      circle(RedZonePosition, RedZoneRadius, 100)

      color = visionColor
      circle(selfX, selfY, visionRadius, 100)

      drawPlayersH(players)

    }
    Gdx.gl.glDisable(GL20.GL_BLEND)
    clipBound.set(miniMapRegion)
    camera = miniMapCamera

    if (drawDaMap == 1) {
      drawMiniMap(parachutes, players, vehicles)
    }
  }

  private fun ShapeRenderer.drawPlayersH(players: ArrayList<renderInfo>?) {
    //draw self
    // drawAllPlayerHealth(selfColor , tuple4(actors[selfID] ?: return , selfCoords.x , selfCoords.y , selfDirection))
    players?.forEach {

      drawAllPlayerHealth(playerColor, it)

    }
    drawAllPlayerHealth(selfColor, tuple4(actors[selfID] ?: return, selfCoords.x, selfCoords.y, selfDirection))
  }


  private fun ShapeRenderer.DrawMyselfH() {

    drawAllPlayerHealth(selfColor, tuple4(actors[selfID] ?: return, selfCoords.x, selfCoords.y, selfDirection))
  }


  private fun ShapeRenderer.drawPlayersMini(parachutes: ArrayList<renderInfo>?, players: ArrayList<renderInfo>?) {
    parachutes?.forEach {
      drawPlayer(parachuteColor, it)
    }
    //draw self
    drawPlayer(selfColor, tuple4(actors[selfID] ?: return, selfCoords.x, selfCoords.y, selfDirection))
    players?.forEach {
      drawPlayer(playerColor, it)
    }
  }


  private fun drawPlayerSprites(parachutes: ArrayList<renderInfo>?, players: ArrayList<renderInfo>?) {
    parachutes?.forEach {
      val (_, x, y, dir) = it
      val (sx, sy) = Vector2(x, y).mapToWindow()
      spriteBatch.draw(
              parachute,
              sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
              4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 8f, 8f,
              dir * -1, 0, 0, 128, 128, true, false
      )
    }
    players?.forEach {

      val (actor, x, y, dir) = it
      val (sx, sy) = Vector2(x, y).mapToWindow()
      val playerStateGUID = actorWithPlayerState[actor.netGUID] ?: return@forEach
      val PlayerState = actors[playerStateGUID] as? PlayerState ?: return@forEach
      val selfStateGUID = actorWithPlayerState[selfID] ?: return@forEach
      val selfState = actors[selfStateGUID] as? PlayerState ?: return@forEach


      // val teamId = isTeamMate(actor)
      //println(teamId)
      // if (teamId > 0) {
      if (PlayerState.teamNumber == selfState.teamNumber) {
        // Can't wait for the "Omg Players don't draw issues
        spriteBatch.draw(
                teamarrow,
                sx, windowHeight - sy - 2, 4.toFloat() / 2,
                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                dir * -1, 0, 0, 64, 64, true, false
        )

        if (toggleView == 1) {
          spriteBatch.draw(
                  teamsight,
                  sx + 1, windowHeight - sy - 2,
                  2.toFloat() / 2,
                  2.toFloat() / 2,
                  12.toFloat(), 2.toFloat(),
                  10f, 10f,
                  dir * -1, 0, 0, 512, 64, true, false
          )
        }

      }

      if (PlayerState.teamNumber != selfState.teamNumber) {

        spriteBatch.draw(
                arrow,
                sx, windowHeight - sy - 2, 4.toFloat() / 2,
                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                dir * -1, 0, 0, 64, 64, true, false
        )

        if (toggleView == 1) {
          spriteBatch.draw(
                  arrowsight,
                  sx + 1, windowHeight - sy - 2,
                  2.toFloat() / 2,
                  2.toFloat() / 2,
                  12.toFloat(), 2.toFloat(),
                  10f, 10f,
                  dir * -1, 0, 0, 512, 64, true, false
          )
        }
      }

    }
    //draw self
    drawMyself(tuple4(actors[selfID] ?: return, selfCoords.x, selfCoords.y, selfDirection))
  }

  private fun drawMyself(actorInfo: renderInfo) {
    val (actor, x, y, dir) = actorInfo
    val (sx, sy) = Vector2(x, y).mapToWindow()
    if (toggleView == 1) {
      // Just draw them both at the same time to avoid player not drawing ¯\_(ツ)_/¯
      spriteBatch.draw(
              player,
              sx, windowHeight - sy - 2, 4.toFloat() / 2,
              4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
              dir * -1, 0, 0, 64, 64, true, false
      )

      spriteBatch.draw(
              playersight,
              sx + 1, windowHeight - sy - 2,
              2.toFloat() / 2,
              2.toFloat() / 2,
              12.toFloat(), 2.toFloat(),
              10f, 10f,
              dir * -1, 0, 0, 512, 64, true, false
      )
    } else {

      spriteBatch.draw(
              player,
              sx, windowHeight - sy - 2, 4.toFloat() / 2,
              4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
              dir * -1, 0, 0, 64, 64, true, false
      )
    }
  }

  private fun SpriteBatch.drawMapMarkers() {
    for (team in teams.values) {
      if (team.showMapMarker) {
        val icon = markers[team.memberNumber]
        val (x, y) = team.mapMarkerPosition
        draw(icon, x, y, 0f, mapMarkerScale, false)
      }
    }
  }


  fun ShapeRenderer.drawPlayer(pColor: Color?, actorInfo: renderInfo) {
    val (actor, x, y, dir) = actorInfo
    if (!clipBound.contains(x, y)) return
    val zoom = camera.zoom
    val backgroundRadius = (playerRadius + 2000f) * zoom
    val playerRadius = playerRadius * zoom
    val directionRadius = directionRadius * zoom

    color = BLACK
    circle(x, y, backgroundRadius, 10)

    val attach = actor.attachChildren.firstOrNull()
    val teamId = isTeamMate(actor)
    color = when {
      teamId >= 0 -> teamColor[teamId]
      attach == null -> pColor
      attach == selfID -> selfColor
      else -> {
        val teamId = isTeamMate(actors[attach])
        if (teamId >= 0)
          teamColor[teamId]
        else
          pColor
      }
    }
    if (actor is Character)
      color = when {
        actor.isGroggying -> {
          GRAY
        }
        actor.isReviving -> {
          WHITE
        }
        else -> color
      }
    circle(x, y, playerRadius, 10)

    color = sightColor
    arc(x, y, directionRadius, dir - fov / 2, fov, 10)

    if (actor is Character) {//draw health
      val health = if (actor.health <= 0f) actor.groggyHealth else actor.health
      val width = healthBarWidth * zoom
      val height = healthBarHeight * zoom
      val y = y + backgroundRadius + height / 2
      val healthWidth = (health / 100.0 * width).toFloat()
      color = when {
        health > 80f -> GREEN
        health > 33f -> ORANGE
        else -> RED
      }
      rectLine(x - width / 2, y, x - width / 2 + healthWidth, y, height)
    }
  }


  private fun drawMiniMap(parachutes: ArrayList<renderInfo>?, players: ArrayList<renderInfo>?, vehicles: ArrayList<renderInfo>?) {
    fbo.begin()
    Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    val (selfX, selfY) = selfCoords
    miniMapCamera.apply {
      position.set(selfX, selfY, 0f)
      update()
    }
    spriteBatch.projectionMatrix = miniMapCamera.combined
    paint {
      draw(
              map, 0f, 0f, mapWidth, mapWidth,
              0, 0, map.width, map.height,
              false, true
      )
      drawVehicles(vehicles)
      drawAirDrop()
      drawMapMarkers()
      drawItem()
    }
    shapeRenderer.projectionMatrix = miniMapCamera.combined
    Gdx.gl.glEnable(GL20.GL_BLEND)
    draw(Filled) {
      drawPlayersH(players)
      drawPlayersMini(parachutes, players)

    }
    draw(Line) {
      players?.forEach {
        aimAtMe(it)
      }
      drawCircles()
      drawAttackLine()
      drawAirDropLine()
    }
    Gdx.gl.glDisable(GL20.GL_BLEND)
    fbo.end()

    val miniMapWidth = windowToMap(miniMapWindowWidth)
    val (rx, ry) = windowToMap(windowWidth, windowHeight).sub(miniMapWidth, miniMapWidth)
    spriteBatch.projectionMatrix = mapCamera.combined
    paint {

      draw(miniMap, rx, ry, miniMapWidth, miniMapWidth)
    }
    shapeRenderer.projectionMatrix = mapCamera.combined
    Gdx.gl.glLineWidth(2f)
    draw(Line) {
      color = BLACK
      rect(rx, ry, miniMapWidth, miniMapWidth)
    }
    Gdx.gl.glLineWidth(1f)
  }

  private fun SpriteBatch.drawVehicles(vehicles: ArrayList<renderInfo>?) {
    vehicles?.forEach { (actor, x, y, dir) ->
      if (!clipBound.contains(x, y)) return@forEach
      val icon = vehicleIcons[actor.type] ?: return
      if (actor.type == Plane)
        draw(icon, x, y, dir, planeScale, false)
      else {
        val zoom = !(actor as Vehicle).driverPlayerState.isValid()
        val scale = vehicleScale
        draw(icon, x, y, dir, scale, zoom)
      }
    }
  }

  private fun SpriteBatch.drawGrenades(grenades: ArrayList<renderInfo>?) {
    grenades?.forEach { (actor, x, y, dir) ->
      if (!clipBound.contains(x, y)) return@forEach
      val icon = grenadeIcons[actor.type] ?: return@forEach
      draw(icon, x, y, dir, grenadeScale, true)
    }
  }


  private fun ShapeRenderer.drawAttackLine() {
    val currentTime = System.currentTimeMillis()
    run {
      while (attacks.isNotEmpty()) {
        val (A, B) = attacks.poll()
        attackLineStartTime.add(Triple(A, B, currentTime))
      }
      if (attackLineStartTime.isEmpty()) return@run
      val iter = attackLineStartTime.iterator()
      while (iter.hasNext()) {
        val (A, B, st) = iter.next()
        if (A == selfStateID || B == selfStateID) {
          if (A != B) {
            val otherGUID = playerStateToActor[if (A == selfStateID) B else A]
            if (otherGUID == null) {
              iter.remove()
              continue
            }
            val other = actors[otherGUID]
            if (other == null || currentTime - st > attackMeLineDuration) {
              iter.remove()
              continue
            }
            color = attackLineColor
            val (xA, yA) = other.location
            val (xB, yB) = selfCoords
            line(xA, yA, xB, yB)
          }
        } else {
          val actorAID = playerStateToActor[A]
          val actorBID = playerStateToActor[B]
          if (actorAID == null || actorBID == null) {
            iter.remove()
            continue
          }
          val actorA = actors[actorAID]
          val actorB = actors[actorBID]
          if (actorA == null || actorB == null || currentTime - st > attackLineDuration) {
            iter.remove()
            continue
          }
          color = attackLineColor
          val (xA, yA) = actorA.location
          val (xB, yB) = actorB.location
          line(xA, yA, xB, yB)
        }
      }
    }
    run {
      while (firing.isNotEmpty()) {
        val (A, st) = firing.poll()
        actors[A]?.apply {
          firingStartTime.add(tuple4(location.x, location.y, rotation.y, st))
        }
      }
      if (firingStartTime.isEmpty()) return@run
      val iter = firingStartTime.iterator()
      while (iter.hasNext()) {
        val (x, y, yaw, st) = iter.next()
        if (currentTime - st > firingLineDuration) {
          iter.remove()
          continue
        }
        color = firingLineColor
        val (xB, yB) = dirUnitVector.cpy().rotate(yaw).scl(firingLineLength).add(x, y)
        line(x, y, xB, yB)
      }
    }
  }

  private fun ShapeRenderer.drawCircles() {
    Gdx.gl.glLineWidth(2f)
    //vision circle
    color = safeZoneColor
    circle(PoisonGasWarningPosition, PoisonGasWarningRadius, 100)

    color = BLUE
    circle(SafetyZonePosition, SafetyZoneRadius, 100)

    if (PoisonGasWarningPosition.len() > 0) {
      color = safeDirectionColor
      line(Vector2(selfCoords.x, selfCoords.y), PoisonGasWarningPosition)
    }
    Gdx.gl.glLineWidth(1f)


  }

  private fun ShapeRenderer.drawAirDropLine() {
    airDropLocation.values.forEach {
      val (x, y) = it
      val airdropcoords = (Vector2(x, y))
      color = YELLOW
      line(Vector2(selfCoords.x, selfCoords.y), airdropcoords)
    }
  }

  private fun SpriteBatch.drawCorpse() {
    corpseLocation.values.forEach {
      if (airDropLocation.values.contains(it)) {
        debugln { ("Ignored corpse locations in airdrop locations") }
      } else {
        val (x, y, z) = it
        if (!clipBound.contains(x, y)) return@forEach
        draw(corpseIcon, x, y, 0f, corpseScale, true)
      }
    }
  }

  private fun SpriteBatch.drawAirDrop() {
    airDropLocation.values.forEach {
      if (corpseLocation.contains(it)) {
        debugln { ("Ignored airdrop locations in corpse locations") }
      } else {
        val (x, y) = it
        if (!clipBound.contains(x, y)) return@forEach

        draw(carePackage, x, y, -90f, airDropScale, false)
      }
    }
  }

  private fun SpriteBatch.drawRedZoneBomb() {
    val currentTime = System.currentTimeMillis()
    val iter = redZoneBombLocation.entries.iterator()
    while (iter.hasNext()) {
      val (loc, time) = iter.next().value
      val (x, y) = loc
      if (currentTime - time > redzongBombShowDuration)
        iter.remove()
      else if (clipBound.contains(x, y))
        draw(redzoneBombIcon, x, y, 0f, redzoneBombScale, true)
    }
  }

  private fun SpriteBatch.drawItem() {
    // This makes the array empty if the filter is off for performance with an inverted function since arrays are expensive
    scopesToFilter = if (filterScope != 1) {
      arrayListOf("")
    } else {
      arrayListOf(
              "Item_Attach_Weapon_Upper_Holosight_C",
              "Item_Attach_Weapon_Upper_DotSight_01_C",
              "Item_Attach_Weapon_Upper_Aimpoint_C",
              "Item_Attach_Weapon_Upper_CQBSS_C",
              "Item_Attach_Weapon_Upper_ACOG_01_C"
      )
    }


    attachToFilter = if (filterAttach != 1) {
      arrayListOf("")
    } else {
      arrayListOf(
              "Item_Attach_Weapon_Magazine_ExtendedQuickDraw_SniperRifle_C",
              "Item_Attach_Weapon_Magazine_Extended_SniperRifle_C",
              "Item_Attach_Weapon_Magazine_ExtendedQuickDraw_Large_C",
              "Item_Attach_Weapon_Magazine_Extended_Large_C",
              "Item_Attach_Weapon_Stock_SniperRifle_CheekPad_C",
              "Item_Attach_Weapon_Stock_SniperRifle_BulletLoops_C",
              "Item_Attach_Weapon_Stock_AR_Composite_C",
              "Item_Attach_Weapon_Muzzle_Suppressor_SniperRifle_C",
              "Item_Attach_Weapon_Muzzle_Suppressor_Large_C",
              "Item_Attach_Weapon_Muzzle_Suppressor_Medium_C",
              "Item_Attach_Weapon_Muzzle_FlashHider_Medium_C",
              "Item_Attach_Weapon_Magazine_ExtendedQuickDraw_Medium_C",
              "Item_Attach_Weapon_Magazine_Extended_Medium_C",
              "Item_Attach_Weapon_Muzzle_FlashHider_Large_C",
              "Item_Attach_Weapon_Muzzle_Compensator_Medium_C",
              "Item_Attach_Weapon_Lower_Foregrip_C",
              "Item_Attach_Weapon_Lower_AngledForeGrip_C"
      )
    }

    weaponsToFilter = if (filterWeapon != 1) {
      arrayListOf("")
    } else {
      arrayListOf(
              "Item_Weapon_AWM_C",
              "Item_Weapon_M24_C",
              "Item_Weapon_Kar98k_C",
              "Item_Weapon_AUG_C",
              "Item_Weapon_M249_C",
              "Item_Weapon_Mk14_C",
              "Item_Weapon_Groza_C",
              "Item_Weapon_HK416_C",
              "Item_Weapon_SCAR-L_C",
              "Item_Weapon_Mini14_C",
              "Item_Weapon_M16A4_C",
              "Item_Weapon_SKS_C",
              "Item_Weapon_AK47_C",
              "Item_Weapon_DP28_C",
              "Item_Weapon_Saiga12_C",
              "Item_Weapon_UMP_C",
              "Item_Weapon_Vector_C",
              "Item_Weapon_UZI_C",
              "Item_Weapon_VSS_C",
              "Item_Weapon_Thompson_C",
              "Item_Weapon_Berreta686_C",
              "Item_Weapon_Winchester_C",
              "Item_Weapon_Win94_C",
              "Item_Weapon_G18_C",
              "Item_Weapon_SawenOff_C",
              "Item_Weapon_Rhino_C",
              "Item_Weapon_FlareGun_C",
              "Item_Weapon_M1911_C",
              "Item_Weapon_NagantM1895_C",
              "Item_Weapon_M9_C"
      )
    }

    healsToFilter = if (filterHeals != 1) {
      arrayListOf("")
    } else {
      arrayListOf(
              "Item_Heal_Bandage_C",
              "Item_Heal_MedKit_C",
              "Item_Heal_FirstAid_C",
              "Item_Boost_PainKiller_C",
              "Item_Boost_EnergyDrink_C",
              "Item_Boost_AdrenalineSyringe_C"
      )
    }

    ammoToFilter = if (filterAmmo != 1) {
      arrayListOf("")
    } else {
      arrayListOf(
              "Item_Ammo_762mm_C",
              "Item_Ammo_556mm_C",
              "Item_Ammo_300Magnum_C",
              "Item_Weapon_Pan_C",
              "Item_Ammo_9mm_C",
              "Item_Ammo_45ACP_C",
              "Item_Ammo_Flare_C",
              "Item_Ammo_12Guage_C"
      )
    }

    throwToFilter = if (filterThrow != 1) {
      arrayListOf("")
    } else {
      arrayListOf(
              "Item_Weapon_Grenade_C",
              "Item_Weapon_FlashBang_C",
              "Item_Weapon_SmokeBomb_C",
              "Item_Weapon_Molotov_C"
      )
    }


    level3Filter = if (filterLvl2 == 3) {
      arrayListOf(
              // 2 1
              "Item_Back_E_01_Lv1_C",
              "Item_Armor_E_01_Lv1_C",
              "Item_Head_E_01_Lv1_C",
              "Item_Back_E_02_Lv1_C",
              "Item_Head_E_02_Lv1_C",
              "Item_Armor_D_01_Lv2_C",
              "Item_Head_F_02_Lv2_C",
              "Item_Head_F_01_Lv2_C",
              "Item_Back_F_01_Lv2_C",
              "Item_Back_F_02_Lv2_C"
      )
    } else {
      arrayListOf("")
    }


    level23Filter = if (filterLvl2 == 4) {
      arrayListOf(
              // 1
              "Item_Back_E_01_Lv1_C",
              "Item_Armor_E_01_Lv1_C",
              "Item_Head_E_01_Lv1_C",
              "Item_Back_E_02_Lv1_C",
              "Item_Head_E_02_Lv1_C"

      )
    } else {
      arrayListOf("")
    }



    level2Filter = if (filterLvl2 == 2) {
      arrayListOf(
              // 1 3
              "Item_Back_E_01_Lv1_C",
              "Item_Armor_E_01_Lv1_C",
              "Item_Head_E_01_Lv1_C",
              "Item_Back_E_02_Lv1_C",
              "Item_Head_E_02_Lv1_C",
              "Item_Armor_C_01_Lv3_C",
              "Item_Head_G_01_Lv3_C",
              "Item_Back_C_02_Lv3_C",
              "Item_Back_C_01_Lv3_C"
      )
    } else {
      arrayListOf("")
    }

    level1Filter = if (filterLvl2 == 1) {
      arrayListOf(
              // 2 3
              "Item_Armor_D_01_Lv2_C",
              "Item_Head_F_02_Lv2_C",
              "Item_Head_F_01_Lv2_C",
              "Item_Back_F_01_Lv2_C",
              "Item_Back_F_02_Lv2_C",
              "Item_Armor_C_01_Lv3_C",
              "Item_Head_G_01_Lv3_C",
              "Item_Back_C_02_Lv3_C",
              "Item_Back_C_01_Lv3_C"
      )
    } else {
      arrayListOf("")
    }


    equipFilter = if (filterLvl2 == 0) {
      arrayListOf(
              // 1 2 3
              "Item_Back_E_01_Lv1_C",
              "Item_Armor_E_01_Lv1_C",
              "Item_Head_E_01_Lv1_C",
              "Item_Back_E_02_Lv1_C",
              "Item_Head_E_02_Lv1_C",
              "Item_Armor_D_01_Lv2_C",
              "Item_Head_F_02_Lv2_C",
              "Item_Head_F_01_Lv2_C",
              "Item_Back_F_01_Lv2_C",
              "Item_Back_F_02_Lv2_C",
              "Item_Armor_C_01_Lv3_C",
              "Item_Head_G_01_Lv3_C",
              "Item_Back_C_02_Lv3_C",
              "Item_Back_C_01_Lv3_C"
      )
    } else {
      arrayListOf("")
    }


    val sorted = ArrayList(droppedItemLocation.values)
    sorted.sortBy {
      order[it._2]
    }
    sorted.forEach {
      if (it._3 && mapCamera.zoom > itemZoomThreshold) return@forEach
      val (x, y, itemHeight) = it._1
      val items = it._2
      val icon = itemIcons[items]!!
      val scale = if (it._3) itemScale else staticItemScale

      if (jsettings.Level2Armor && "Item_Armor_D_01_Lv2_C" in items ||
              jsettings.Level2Head && "Item_Head_F_02_Lv2_C" in items ||
              jsettings.Level2Head1 && "Item_Head_F_01_Lv2_C" in items ||
              jsettings.Level2Back && "Item_Back_F_01_Lv2_C" in items ||
              jsettings.Level2Back1 && "Item_Back_F_02_Lv2_C" in items ||
              jsettings.Level3Armor && "Item_Armor_C_01_Lv3_C" in items ||
              jsettings.Level3Head && "Item_Head_G_01_Lv3_C" in items ||
              jsettings.Level3Back && "Item_Back_C_02_Lv3_C" in items ||
              jsettings.Level3Back1 && "Item_Back_C_01_Lv3_C" in items ||


              jsettings.Bandage && "Item_Heal_Bandage_C" in items ||
              jsettings.MedKit && "Item_Heal_MedKit_C" in items ||
              jsettings.FirstAid && "Item_Heal_FirstAid_C" in items ||
              jsettings.PainKiller && "Item_Boost_PainKiller_C" in items ||
              jsettings.EnergyDrink && "Item_Boost_EnergyDrink_C" in items ||
              jsettings.Syringe && "Item_Boost_AdrenalineSyringe_C" in items ||

              jsettings.AWM && "Item_Weapon_AWM_C" in items ||
              jsettings.M24 && "Item_Weapon_M24_C" in items ||
              jsettings.Kar98k && "Item_Weapon_Kar98k_C" in items ||
              jsettings.AUG && "Item_Weapon_AUG_C" in items ||
              jsettings.M249 && "Item_Weapon_M249_C" in items ||
              jsettings.MK14 && "Item_Weapon_Mk14_C" in items ||
              jsettings.Groza && "Item_Weapon_Groza_C" in items ||
              jsettings.HK416 && "Item_Weapon_HK416_C" in items ||
              jsettings.SCARL && "Item_Weapon_SCAR-L_C" in items ||
              jsettings.SCARL && "Item_Weapon_SCAR-L_C" in items ||
              jsettings.Mini14 && "Item_Weapon_Mini14_C" in items ||
              jsettings.M16A4 && "Item_Weapon_M16A4_C" in items ||
              jsettings.SKS && "Item_Weapon_SKS_C" in items ||
              jsettings.AK47 && "Item_Weapon_AK47_C" in items ||
              jsettings.DP28 && "Item_Weapon_DP28_C" in items ||
              jsettings.Saiga12 && "Item_Weapon_Saiga12_C" in items ||
              jsettings.UMP && "Item_Weapon_UMP_C" in items ||
              jsettings.UZI && "Item_Weapon_UZI_C" in items ||
              jsettings.Vector && "Item_Weapon_Vector_C" in items ||


              jsettings.QDSnipe && "Item_Attach_Weapon_Magazine_ExtendedQuickDraw_SniperRifle_C" in items ||
              jsettings.ExSR && "Item_Attach_Weapon_Magazine_Extended_SniperRifle_C" in items ||
              jsettings.ExSMG && "Item_Attach_Weapon_Magazine_Extended_Medium_C" in items ||
              jsettings.ExQuickAR && "Item_Attach_Weapon_Magazine_ExtendedQuickDraw_Large_C" in items ||
              jsettings.ExtQuickSMG && "Item_Attach_Weapon_Magazine_ExtendedQuickDraw_Medium_C" in items ||
              jsettings.ExAR && "Item_Attach_Weapon_Magazine_Extended_Large_C" in items ||
              jsettings.CheekSR && "Item_Attach_Weapon_Stock_SniperRifle_CheekPad_C" in items ||
              jsettings.LoopsSR && "Item_Attach_Weapon_Stock_SniperRifle_BulletLoops_C" in items ||
              jsettings.StockAR && "Item_Attach_Weapon_Stock_AR_Composite_C" in items ||
              jsettings.SuppressorSR && "Item_Attach_Weapon_Muzzle_Suppressor_SniperRifle_C" in items ||
              jsettings.SuppressorAR && "Item_Attach_Weapon_Muzzle_Suppressor_Large_C" in items ||
              jsettings.SuppressorSMG && "Item_Attach_Weapon_Muzzle_Suppressor_Medium_C" in items ||
              jsettings.FlashHiderSMG && "Item_Attach_Weapon_Muzzle_FlashHider_Medium_C" in items ||
              jsettings.FlashHiderAR && "Item_Attach_Weapon_Muzzle_FlashHider_Large_C" in items ||
              jsettings.CompensatorAR && "Item_Attach_Weapon_Muzzle_Compensator_Medium_C" in items ||
              jsettings.Foregrip && "Item_Attach_Weapon_Lower_Foregrip_C" in items ||
              jsettings.AngledForegrip && "Item_Attach_Weapon_Lower_AngledForeGrip_C" in items ||


              jsettings.G18 && "Item_Weapon_G18_C" in items ||
              jsettings.Rhino45 && "Item_Weapon_Rhino_C" in items ||
              jsettings.M1911 && "Item_Weapon_M1911_C" in items ||
              jsettings.R1895 && "Item_Weapon_NagantM1895_C" in items ||
              jsettings.M9 && "Item_Weapon_M9_C" in items
      ) {
        // If The Setting is true (boolean) and Item_ is in Items, Don't draw disabled item.
      }
      // Else draw depending on filter
      else if (
              items !in weaponsToFilter && items !in scopesToFilter && items !in attachToFilter && items !in level2Filter && items !in level3Filter && items !in level23Filter
              && items !in level1Filter && items !in ammoToFilter && items !in healsToFilter && items !in throwToFilter && items !in equipFilter
      ) {
        if (items in crateIcons) {

          val adt = crateIcons[items]!!
          draw(adt, x + 50, y, 0f, airDropTextScale, it._3)

        } else {
          draw(icon, x, y, 0f, scale, it._3)
        println(itemHeight)
        }
      }
          when {
            itemHeight > (selfCoords.z + 200) -> itemFont.draw(spriteBatch, "^", x - 50, y)
            itemHeight < (selfCoords.z - 100) -> itemFont.draw(spriteBatch, "v", x - 50, y)
            else -> itemFont.draw(spriteBatch, "o", x - 50, y)
          }
        }
      }

  fun drawPlayerInfos(players : MutableList<renderInfo>?)
  {
    players?.forEach {
      val (actor, x, y, _) = it
      if (!clipBound.contains(x, y)) return@forEach
      val dir = Vector2(x - selfCoords.x, y - selfCoords.y)
      val distance = (dir.len() / 100).toInt()
      val angle = ((dir.angle() + 90) % 360).toInt()
      val (sx, sy) = mapToWindow(x, y)
      val playerStateGUID = (actor as? Character)?.playerStateID ?: return@forEach
      val playerState = actors[playerStateGUID] as? PlayerState ?: return@forEach
      val name = playerState.name
      val health = actorHealth[actor.netGUID] ?: 100f
      val teamNumber = playerState.teamNumber
      val numKills = playerState.numKills
      val equippedWeapons = actorHasWeapons[actor.netGUID]
      val df = DecimalFormat("###.#")
      var weapon = ""
      if (equippedWeapons != null)
      {
        for (w in equippedWeapons)
        {
          val weap = weapons[w ?: continue] as? Weapon ?: continue
          val result = weap.typeName.split("_")
          weapon += "${result[2].substring(4)}-->${weap.currentAmmoInClip}\n"
        }
      }
      var items = ""
      for (element in playerState.equipableItems)
      {
        if (element == null || element._1.isBlank()) continue
        items += "${element._1}->${element._2.toInt()}\n"
      }
      for (element in playerState.castableItems)
      {
        if (element == null || element._1.isBlank()) continue
        items += "${element._1}->${element._2}\n"
      }


      when (nameToggles)
      {

        0 ->
        {
        }

        1 ->
        {
          nameFont.draw(
                spriteBatch,
                "$angle°${distance}m\n" +
                "|N: $name\n" +
                "|H: \n" +
                "|K: ($numKills)\nTN.($teamNumber)\n" +
                "|S: \n" +
                "|W: $weapon" +
                "|I: $items"

                , sx + 20, windowHeight - sy + 20
                       )

          val healthText = health
          when
          {
            healthText > 80f -> hpgreen.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 9)
            healthText > 33f -> hporange.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 9)
            else             -> hpred.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 9)
          }

          if (actor is Character)
            when
            {
              actor.isGroggying ->
              {
                hpred.draw(spriteBatch, "DOWNED", sx + 40, windowHeight - sy + -42)
              }
              actor.isReviving  ->
              {
                hporange.draw(spriteBatch, "GETTING REVIVED", sx + 40, windowHeight - sy + -42)
              }
              else              -> hpgreen.draw(spriteBatch, "Alive", sx + 40, windowHeight - sy + -42)
            }

        }
        2 ->
        {
          nameFont.draw(
                spriteBatch, "${distance}m\n" +
                             "|N: $name\n" +
                             "|H: ${df.format(health)}\n" +
                             "|W: $weapon",
                sx + 20, windowHeight - sy + 20
                       )
        }
        3 ->
        {

          nameFont.draw(spriteBatch, "|N: $name\n|D: ${distance}m", sx + 20, windowHeight - sy + 20)
          // rectLine(x - width / 2, hpY, x - width / 2 + healthWidth, hpY, height)
        }
        4 ->
        {

          // Change color of hp
          val healthText = health
          when
          {
            healthText > 80f -> hpgreen.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 8)
            healthText > 33f -> hporange.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 8)
            else             -> hpred.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 8)

          }
          nameFont.draw(
                spriteBatch, "|N: $name\n|D: ${distance}m $angle°\n" +
                             "|H:\n" +
                             "|S:\n" +
                             "|W: $weapon",
                sx + 20, windowHeight - sy + 20
                       )

          if (actor is Character)
            when
            {
              actor.isGroggying ->
              {
                hpred.draw(spriteBatch, "DOWNED", sx + 40, windowHeight - sy + -16)
              }
              actor.isReviving  ->
              {
                hporange.draw(spriteBatch, "GETTING REVIVED", sx + 40, windowHeight - sy + -16)
              }
              else              -> hpgreen.draw(spriteBatch, "Alive", sx + 40, windowHeight - sy + -16)
            }
        }
      }
    }
  }

  var lastPlayTime = System.currentTimeMillis()
  fun safeZoneHint()
  {
    if (PoisonGasWarningPosition.len() > 0)
    {
      val dir = PoisonGasWarningPosition.cpy().sub(Vector2(selfCoords.x, selfCoords.y))
      val road = dir.len() - PoisonGasWarningRadius
      if (road > 0)
      {
        val runningTime = (road / runSpeed).toInt()
        val (x, y) = dir.nor().scl(road).add(Vector2(selfCoords.x, selfCoords.y)).mapToWindow()
        littleFont.draw(spriteBatch, "$runningTime", x, windowHeight - y)
        val remainingTime = (TotalWarningDuration - ElapsedWarningDuration).toInt()
        if (remainingTime == 60 && runningTime > remainingTime)
        {
          val currentTime = System.currentTimeMillis()
          if (currentTime - lastPlayTime > 10000)
          {
            lastPlayTime = currentTime
            alarmSound.play()
          }
        }
      }
    }
  }

  fun SpriteBatch.draw(texture : TextureRegion, x : Float, y : Float, yaw : Float, scale : Float, zoom : Boolean = true)
  {
    val w = texture.regionWidth.toFloat()
    val h = texture.regionHeight.toFloat()
    val scale = if (zoom) scale else scale * camera.zoom
    draw(
          texture, x - w / 2,
          y - h / 2,
          w / 2, h / 2,
          w, h,
          scale, scale,
          yaw
        )
  }

  inline fun draw(type : ShapeType, draw : ShapeRenderer.() -> Unit)
  {
    shapeRenderer.apply {
      begin(type)
      draw()
      end()
    }
  }

  inline fun paint(matrix : Matrix4? = null, paint : SpriteBatch.() -> Unit)
  {
    spriteBatch.apply {
      if (matrix != null) projectionMatrix = matrix
      begin()
      paint()
      end()
    }
  }

  fun ShapeRenderer.circle(loc : Vector2, radius : Float, segments : Int)
  {
    circle(loc.x, loc.y, radius, segments)
  }

  fun ShapeRenderer.aimAtMe(it : renderInfo)
  {
    val currentTime = System.currentTimeMillis()
    val (selfX, selfY) = selfCoords
    val zoom = camera.zoom
    //draw aim line
    val (actor, x, y, dir) = it
    if (isTeamMate(actor) >= 0) return
    val actorID = actor.netGUID
    val dirVec = dirUnitVector.cpy().rotate(dir)
    val focus = Vector2(selfX - x, selfY - y)
    val distance = focus.len()
    var aim = false
    if (distance < aimLineRange && distance > aimCircleRadius)
    {
      val aimAngle = focus.angle(dirVec)
      if (aimAngle.absoluteValue < asin(aimCircleRadius / distance) * MathUtils.radiansToDegrees)
      {//aim
        aim = true
        aimStartTime.compute(actorID) { _, startTime ->
          if (startTime == null) currentTime
          else
          {
            if (currentTime - startTime > aimTimeThreshold)
            {
              color = aimLineColor
              rectLine(x, y, selfX, selfY, aimLineWidth * zoom)
            }
            startTime
          }
        }
      }
    }
    if (!aim)
      aimStartTime.remove(actorID)
  }

  fun ShapeRenderer.drawAllPlayerHealth(pColor : Color?, actorInfo : renderInfo)
  {
    val (actor, x, y, dir) = actorInfo
    if (!clipBound.contains(x, y)) return
    val zoom = camera.zoom
    val backgroundRadius = (playerRadius + 2000f) * zoom

//        val attach = actor.attachChildren.firstOrNull()
//        val teamId = isTeamMate(actor)
//        color = when {
//            teamId >= 0 -> teamColor[teamId]
//            attach == null -> pColor
//            attach == selfID -> selfColor
//            else -> {
//                val teamId = isTeamMate(actors[attach])
//                if (teamId >= 0)
//                    teamColor[teamId]
//                else
//                    pColor
//            }
//        }
//        if (actor is Character)
//            color = when {
//                actor.isGroggying -> {
//                    GRAY
//                }
//                actor.isReviving -> {
//                    WHITE
//                }
//                else -> color
//            }

    if (actor is Character)
    {//draw health
      val health = if (actor.health <= 0f) actor.groggyHealth else actor.health
      val width = healthBarWidth * zoom
      val height = healthBarHeight * zoom
      val y = y + backgroundRadius + height / 2
      val healthWidth = (health / 100.0 * width).toFloat()
      color = when
      {
        health > 80f -> GREEN
        health > 33f -> ORANGE
        else         -> RED
      }
      rectLine(x - width / 2, y, x - width / 2 + healthWidth, y, height)
    }
  }

  private fun isTeamMate(actor : Actor?) : Int
  {
    val teamID = (actor as? Character)?.teamID ?: return -1
    val team = actors[teamID] as? Team ?: return -1
    return team.memberNumber
  }

  override fun resize(width : Int, height : Int)
  {
    windowWidth = width.toFloat()
    windowHeight = height.toFloat()
    mapCamera.setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
    fontCamera.setToOrtho(false, windowWidth, windowHeight)
  }

  override fun pause()
  {
  }

  override fun resume()
  {
  }

  override fun dispose()
  {
    deregister(this)
    alarmSound.dispose()
    largeFont.dispose()
    littleFont.dispose()
    mapErangel.dispose()
    mapMiramar.dispose()
    carePackage.texture.dispose()
    itemAtlas.dispose()
    crateAtlas.dispose()
    pawnAtlas.dispose()
    spriteBatch.dispose()
    shapeRenderer.dispose()
    fbo.dispose()
  }

}