package gaydar.struct

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import gaydar.struct.Archetype.*
import gaydar.struct.Archetype.Companion.fromArchetype
import gaydar.struct.Archetype.PlayerState
import gaydar.struct.Archetype.Team
import gaydar.struct.Archetype.Weapon
import gaydar.util.DynamicArray
import gaydar.util.tuple2
import java.util.Collections.newSetFromMap
import java.util.concurrent.ConcurrentHashMap

enum class Archetype
{ //order matters, it affects the order of drawing
  Other,
  RedZoneBomb,
  GameState,
  DroopedItemGroup,
  TwoSeatBoat,
  SixSeatBoat,
  Dacia,
  Uaz,
  Pickup,
  Buggy,
  Bike,
  SideCar,
  Bus,
  Plane,
  Player,
  Parachute,
  AirDrop,
  PlayerState,
  Team,
  DeathDropItemPackage,
  DroppedItem,
  WeaponProcessor,
  Weapon,
  SmokeBomb,
  Molotov,
  Grenade,
  FlashBang;

  companion object
  {
    fun fromArchetype(archetype : String) = when
    {
      archetype.contains("RedZoneBomb")                                               -> RedZoneBomb
      archetype.contains("Default__TSLGameState")                                     -> GameState
      archetype.contains("Default__Player")                                           -> Player
      archetype.contains("DroppedItemGroup")                                          -> DroopedItemGroup
      archetype.contains("Aircraft")                                                  -> Plane
      archetype.contains("Parachute")                                                 -> Parachute
      archetype.contains("buggy", true)                                               -> Buggy
      archetype.contains("bike", true)                                                -> Bike
      archetype.contains("SideCar", true)                                             -> SideCar
      archetype.contains("dacia", true)                                               -> Dacia
      archetype.contains("uaz", true)                                                 -> Uaz
      archetype.contains("pickup", true)                                              -> Pickup
      archetype.contains(Regex("(bus|van)", RegexOption.IGNORE_CASE))                 -> Bus
      archetype.contains("AquaRail", true)                                            -> TwoSeatBoat
      archetype.contains("boat", true)                                                -> SixSeatBoat
      archetype.contains(Regex("(CaraPackage|CarePackage)", RegexOption.IGNORE_CASE)) -> AirDrop
      archetype.contains("ProjSmokeBomb")                                             -> SmokeBomb
      archetype.contains("Molotov")                                                   -> Molotov
      archetype.contains("FlashBang")                                                 -> FlashBang
      archetype.contains("Grenade")                                                   -> Grenade
      archetype.contains("Default__TslPlayerState")                                   -> PlayerState
      archetype.contains("Default__Team", true)                                       -> Team
      archetype.contains("DeathDropItemPackage", true)                                -> DeathDropItemPackage
      archetype.contains("DroppedItem")                                               -> DroppedItem
      archetype.contains("Default__WeaponProcessor")                                  -> WeaponProcessor
      archetype.contains("Weap")                                                      -> Weapon
      else                                                                            -> Other
    }
  }
}

fun makeActor(netGUID : NetworkGUID, archetype : NetGuidCacheObject) : Actor
{
  val type = fromArchetype(archetype.pathName)
  return when (type)
  {
    Player                    -> Character(netGUID, type, archetype.pathName)
    PlayerState               -> PlayerState(netGUID, type, archetype.pathName)
    Team                      -> Team(netGUID, type, archetype.pathName)
    Weapon                    -> Weapon(netGUID, type, archetype.pathName)
    TwoSeatBoat, SixSeatBoat, Dacia, Uaz, Pickup, Buggy,
    Bike, SideCar, Bus, Plane -> Vehicle(netGUID, type, archetype.pathName)
    else                      -> Actor(netGUID, type, archetype.pathName)
  }
}

open class Actor(val netGUID : NetworkGUID, val type : Archetype, val typeName : String)
{

  var location = Vector3.Zero
  var rotation = Vector3.Zero
  var velocity = Vector3.Zero

  var owner : NetworkGUID? = null
  var attachParent : NetworkGUID? = null
  var attachChildren = newSetFromMap(ConcurrentHashMap<NetworkGUID, Boolean>())
  var isStatic = false

  override fun toString() = "[${netGUID.value}]($typeName)"

  val isAPawn = when (type)
  {
    TwoSeatBoat,
    SixSeatBoat,
    Dacia,
    Uaz,
    Pickup,
    Buggy,
    Bike,
    SideCar,
    Bus,
    Plane,
    Player,
    Parachute -> true
    else      -> false
  }
  val isACharacter = type == Player
  val isVehicle = type.ordinal >= TwoSeatBoat.ordinal && type.ordinal <= Bus.ordinal
}

class Character(netGUID : NetworkGUID, type : Archetype, typeName : String) : Actor(netGUID, type, typeName)
{
  var health = 100f
  var groggyHealth = 100f
  var boostGauge = 0f
  var isReviving = false
  var isGroggying = false
  var playerStateID = NetworkGUID(0)
  var teamID = NetworkGUID(0)
}

class PlayerState(netGUID : NetworkGUID, type : Archetype, typeName : String) : Actor(netGUID, type, typeName)
{
  var name : String = ""
  var teamNumber = 0
  var numKills = 0
  val equipableItems = DynamicArray<tuple2<String, Float>?>(3, 0)
  val castableItems = DynamicArray<tuple2<String, Int>?>(8, 0)
}

class Team(netGUID : NetworkGUID, type : Archetype, typeName : String) : Actor(netGUID, type, typeName)
{
  var memberNumber = 0
  val mapMarkerPosition = Vector2()
  var showMapMarker = false
}

class Vehicle(netGUID : NetworkGUID, type : Archetype, typeName : String) : Actor(netGUID, type, typeName)
{
  var driverPlayerState = NetworkGUID(0)
}

class Weapon(netGUID : NetworkGUID, type : Archetype, typeName : String) : Actor(netGUID, type, typeName)
{
  var ammoPerClip = 0
  var currentAmmoInClip = 0
  var currentZeroLevel = 0
  var isHipped = false
  var firingModeIndex = 0
  var weaponSpread = 0f
}