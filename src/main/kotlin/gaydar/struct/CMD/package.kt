package gaydar.struct.CMD

import gaydar.struct.Actor
import gaydar.struct.Archetype
import gaydar.struct.Archetype.*
import gaydar.struct.Bunch
import gaydar.struct.CMD.Processor.processors
import gaydar.struct.NetGuidCacheObject

typealias processor = (Actor, Bunch, NetGuidCacheObject?, Int, HashMap<String, Any?>) -> Boolean

fun receiveProperties(bunch : Bunch, repObj : NetGuidCacheObject?, actor : Actor) : Boolean
{
  val processor = processors[repObj?.pathName ?: return false] ?: return false
  val data = HashMap<String, Any?>()
  bunch.readBit()
  var waitingHandle = 0
  do
  {
    waitingHandle = bunch.readIntPacked()
  }
  while (waitingHandle > 0 && processor(actor, bunch, repObj, waitingHandle, data) && bunch.notEnd())
  return waitingHandle == 0
}

//enum class REPCMD {
//  Return,  //1 Return from array, or end of stream
//  Property,  //2 Generic property
//
//  PropertyBool,//3
//  PropertyFloat,//4
//  PropertyInt,//5
//  PropertyByte,//6
//  PropertyName,//7
//  PropertyObject,//8
//  PropertyUInt32,//9
//  PropertyVector,//10
//  PropertyRotator,//11
//  PropertyPlane,//12
//  PropertyVector100,//13
//  PropertyNetId,//14
//  RepMovement,//15
//  PropertyVectorNormal,//16
//  PropertyVector10,//17
//  PropertyVectorQ,//18
//  PropertyString,//19
//  PropertyUInt64,//20
//}

object Processor
{
  val processors = mapOf<String, processor>(
        GameState.name to GameStateCMD::process,
        Archetype.PlayerState.name to PlayerStateCMD::process,
        Archetype.Team.name to TeamCMD::process,

        AirDrop.name to AirDropCMD::process,
        DroppedItem.name to DroppedItemCMD::process,
        DroopedItemGroup.name to APawnCMD::process,
        DeathDropItemPackage.name to DeathDropItemPackageCMD::process,
        "DroppedItemInteractionComponent" to DroppedItemInteractionComponentCMD::process,
        "DroppedItemGroupRootComponent" to DroppedItemGroupRootComponentCMD::process,

        Other.name to APawnCMD::process,

        Grenade.name to APawnCMD::process,
        SmokeBomb.name to APawnCMD::process,
        Molotov.name to APawnCMD::process,
        FlashBang.name to APawnCMD::process,

        TwoSeatBoat.name to VehicleCMD::process,
        SixSeatBoat.name to VehicleCMD::process,
        Dacia.name to VehicleCMD::process,
        Uaz.name to VehicleCMD::process,
        Pickup.name to VehicleCMD::process,
        Buggy.name to VehicleCMD::process,
        Bike.name to VehicleCMD::process,
        SideCar.name to VehicleCMD::process,
        Bus.name to VehicleCMD::process,
        Plane.name to VehicleCMD::process,

        Parachute.name to APawnCMD::process,

        Player.name to CharacterCMD::process,

        WeaponProcessor.name to WeaponProcessorCMD::process,
        Archetype.Weapon.name to WeaponCMD::process
                                           )
}

