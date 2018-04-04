package gaydar.util.settings


import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.io.File

class Settings
{

  data class jsonsettings(
        ////
        //// Style Settings
        ////
        val miniMapWindowWidth : Float = 400f,
        val miniMapRadius : Float = 500 * 100f,
        val playerRadius : Float = 4000f,
        val healthBarWidth : Float = 15000f,
        val healthBarHeight : Float = 2000f,
        val directionRadius : Float = 16000f,
        val fov : Float = 90f,
        val aimLineWidth : Float = 1000f,
        val aimLineRange : Float = 50000f,
        val aimCircleRadius : Float = 200f,
        val firingLineLength : Float = 20000f,
        val itemZoomThreshold : Float = 0.06f,
        val airDropTextScale : Float = 1000f,

        //
        // Scales
        //
        val itemScale : Float = 16f,
        val staticItemScale : Float = 200f,
        val mapMarkerScale : Float = 150f,
        val airDropScale : Float = 250f,
        val vehicleScale : Float = 25f,
        val planeScale : Float = 350f,
        val grenadeScale : Float = 15f,
        val corpseScale : Float = 30f,
        val redzoneBombScale : Float = 30f,
        val aimTimeThreshold : Int = 1000,

        //
        // Timers
        //
        val attackLineDuration : Int = 1000,
        val attackMeLineDuration : Int = 10000,
        val firingLineDuration : Int = 500,


        //
        // Armor Etc
        //
        val Level2Armor : Boolean = false,
        val Level2Head : Boolean = false,
        val Level2Head1 : Boolean = false,
        val Level2Back : Boolean = false,
        val Level2Back1 : Boolean = false,
        val Level3Armor : Boolean = false,
        val Level3Head : Boolean = false,
        val Level3Back : Boolean = false,
        val Level3Back1 : Boolean = false,
        //
        // Decent Weapons
        //
        val M16A4 : Boolean = false,
        val AWM : Boolean = false,
        val M24 : Boolean = false,
        val Kar98k : Boolean = false,
        val AUG : Boolean = false,
        val M249 : Boolean = false,
        val MK14 : Boolean = false,
        val Groza : Boolean = false,
        val HK416 : Boolean = false,
        val SCARL : Boolean = false,
        val Mini14 : Boolean = false,
        var SKS : Boolean = false,
        val AK47 : Boolean = false,
        val DP28 : Boolean = false,
        val Saiga12 : Boolean = false,
        val UMP : Boolean = false,
        val Vector : Boolean = false,
        val UZI : Boolean = false,

        //
        // Pistols
        //
        val G18 : Boolean = false,
        val Rhino45 : Boolean = false,
        val M1911 : Boolean = false,
        val R1895 : Boolean = false,
        val M9 : Boolean = false,

        //
        // Meds
        //
        val Bandage : Boolean = false,
        val MedKit : Boolean = false,
        val FirstAid : Boolean = false,
        val PainKiller : Boolean = false,
        val EnergyDrink : Boolean = false,
        val Syringe : Boolean = false,

        //
        // Attachments
        //
        var QDSnipe : Boolean = false,
        var ExSR : Boolean = false,
        var ExSMG : Boolean = false,
        var ExQuickAR : Boolean = false,
        var ExtQuickSMG : Boolean = false,
        var ExAR : Boolean = false,
        var CheekSR : Boolean = false,
        var LoopsSR : Boolean = false,
        var StockAR : Boolean = false,
        var SuppressorSR : Boolean = false,
        var SuppressorAR : Boolean = false,
        var SuppressorSMG : Boolean = false,
        var FlashHiderSMG : Boolean = false,
        var FlashHiderAR : Boolean = false,
        var CompensatorAR : Boolean = false,
        var Foregrip : Boolean = false,
        var AngledForegrip : Boolean = false,

        ///
        /// Information Toggles
        /// Default Item Information Toggles
        // -1 Disabled
        // 1  Enabled
        var filterWeapon : Int = -1,
        var filterAttach : Int = -1,
        var filterScope : Int = -1,
        var filterHeals : Int = -1,
        var filterAmmo : Int = 1,
        var filterThrow : Int = 1,

        // Draw Compass
        var drawcompass : Int = -1,

        // Draw Menu
        var drawmenu : Int = 1,

        // Toggle View Line
        var toggleView : Int = 1,

        // Toggle Mini-Map
        var drawDaMap : Int = 1,
        // private var toggleVehicles = -1
        //  private var toggleVNames = -1

        // Player Info Toggles 1-4
        var nameToggles : Int = 4,

        // Filter Equipment 1-2
        var filterLvl2 : Int = 0,

        // Vehicle Information Toggles 1-2
        var VehicleInfoToggles : Int = 1,

        // Zoom Toggles 1-3
        var ZoomToggles : Int = 1,


        //
        //  Key settings
        //  Scarjit: Im using strings here for user comfort
        //
        val nameToogle_Key : String = Input.Keys.toString(Input.Keys.F1),
        val VehicleInfoToggles_Key : String = Input.Keys.toString(Input.Keys.F5),
        val ZoomToggles_Key : String = Input.Keys.toString(Input.Keys.NUMPAD_8),

        val drawcompass_Key : String = Input.Keys.toString(Input.Keys.F2),
        val toggleView_Key : String = Input.Keys.toString(Input.Keys.F4),
        val drawDaMap_Key : String = Input.Keys.toString(Input.Keys.F3),
        val drawmenu_Key : String = Input.Keys.toString(Input.Keys.F12),

        val filterWeapon_Key : String = Input.Keys.toString(Input.Keys.NUMPAD_1),
        val filterLvl2_Key : String = Input.Keys.toString(Input.Keys.NUMPAD_2),
        val filterHeals_Key : String = Input.Keys.toString(Input.Keys.NUMPAD_3),

        val filterThrow_Key : String = Input.Keys.toString(Input.Keys.NUMPAD_4),
        val filterAttach_Key : String = Input.Keys.toString(Input.Keys.NUMPAD_5),
        val filterScope_Key : String = Input.Keys.toString(Input.Keys.NUMPAD_6),
        val filterAmmo_Key : String = Input.Keys.toString(Input.Keys.NUMPAD_7),

        val camera_zoom_Minus_Key : String = Input.Keys.toString(Input.Keys.MINUS),
        val camera_zoom_Plus_Key : String = Input.Keys.toString(Input.Keys.PLUS),


        //
        //  Font settings
        //

        //AGENCYFB
        val hubFont_size : Int = 30,
        val hubFont_color : Color = Color.WHITE,
        val hubFontShadow_color : Color = Color(1f, 1f, 1f, 0.4f),

        val espFont_size : Int = 16,
        val espFont_color : Color = Color.WHITE,
        val espFontShadow_color : Color = Color(1f, 1f, 1f, 0.2f),

        //NUMBER
        val largeFont_size : Int = 38,
        val largeFont_color : Color = Color.WHITE,

        //GOTHICB
        val largeFont_size2 : Int = 38,
        val largeFont_color2 : Color = Color.WHITE,

        val littleFont_size : Int = 15,
        val littleFont_color : Color = Color.WHITE,

        val nameFont_size : Int = 10,
        val nameFont_color : Color = Color.BLACK,

        val itemFont_size : Int = 6,
        val itemFont_color : Color = Color.WHITE,

        val compaseFont_size : Int = 10,
        val compaseFont_color : Color = Color(0f, 0.95f, 1f, 1f),

        val compaseFontShadow_color : Color = Color(0f, 0f, 0f, 0.5f),

        val littleFont_size2 : Int = 15,
        val littleFont_color2 : Color = Color.WHITE,

        val littleFontShadow_color : Color = Color(0f, 0f, 0f, 0.5f),

        val menuFont_size : Int = 12,
        val menuFont_color : Color = Color.WHITE,

        val menuFontText_size : Int = 12,
        val menuFontText_color : Color = Color.WHITE,

        val menuFontOn_size : Int = 12,
        val menuFontOn_color : Color = Color.GREEN,

        val menuFontOFF_size : Int = 12,
        val menuFontOFF_color : Color = Color.RED,

        val hporange_size : Int = 10,
        val hporange_color : Color = Color.ORANGE,

        val hpgreen_size : Int = 10,
        val hpgreen_color : Color = Color.GREEN,

        val hpred_size : Int = 10,
        val hpred_color : Color = Color.RED


                         )

  val settingsname = "settings.json"

  fun loadsettings() : jsonsettings
  {
    checkifsettingsexists()
    val f = File(settingsname)
    if (!f.canRead())
    {
      throw SecurityException("Can't read settings.json")
    }


    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter(jsonsettings::class.java)
    val set = adapter.fromJson(f.readText())
    if (set != null)
    {
      return set
    }
    else
    {
      throw NullPointerException()
    }
  }

  fun savesettings(settings : jsonsettings)
  {
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val json = moshi.adapter(jsonsettings::class.java).indent("  ").toJson(settings)
    val f = File(settingsname)
    f.writeText(json)
  }

  fun checkifsettingsexists()
  {
    val f = File(settingsname)
    if (!f.exists())
    {
      savesettings(jsonsettings())
    }
  }

}