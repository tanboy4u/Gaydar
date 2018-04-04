package gaydar.struct.CMD

import gaydar.deserializer.channel.ActorChannel.Companion.firing
import gaydar.struct.Actor
import gaydar.struct.Bunch
import gaydar.struct.NetGuidCacheObject
import gaydar.struct.Weapon
import gaydar.util.debugln
import gaydar.util.tuple2

object WeaponCMD
{
  fun process(actor : Actor, bunch : Bunch, repObj : NetGuidCacheObject?, waitingHandle : Int, data : HashMap<String, Any?>) : Boolean
  {
    try
    {
      actor as Weapon
      with(bunch) {
        when (waitingHandle)
        {
        //AWeapon
          16   ->
          {//MyPawn
            val MyPawn = readObject()
            val a = MyPawn
          }
          17   ->
          {//AttachedItemClasses
            val arraySize = readUInt16()
            val equippedWeapons = IntArray(arraySize)
            var index = readIntPacked()
            while (index != 0)
            {
              val (netguid, item) = readObject()
              equippedWeapons[index - 1] = netguid.value
//              if (netguid.isValid())
//                println("$actor has weapon  [$netguid](${weapons[netguid.value]?.Type})")
              index = readIntPacked()
            }
          }
          18   ->
          {//SkinTargetDatas TArray<struct FSkinTargetData>
            return false
          }
          19   ->
          {
            val AmmoPerClip = propertyInt()
            actor.ammoPerClip
          }
          20   ->
          {
            val CurrentAmmoInClip = propertyInt()
            if (CurrentAmmoInClip < actor.currentAmmoInClip)//firing
              actor.owner?.apply {
                firing.add(tuple2(this, System.currentTimeMillis()))
              }
            actor.currentAmmoInClip = CurrentAmmoInClip
          }
          21   ->
          {
            val CurrentZeroLevel = propertyInt()
            actor.currentZeroLevel = CurrentZeroLevel
          }
          22   ->
          {
//          val tmp = propertyInt()
//          val a = tmp
            val bIsHipped = readBit()
            actor.isHipped = bIsHipped
          }
          23   ->
          {
            val FiringModeIndex = propertyInt()
            actor.firingModeIndex = FiringModeIndex
          }
          24   ->
          {
            val WeaponSpread = propertyFloat()
            actor.weaponSpread = WeaponSpread
          }
          else -> return ActorCMD.process(actor, bunch, repObj, waitingHandle, data)
        }
        return true
      }
    }
    catch (e : Exception)
    {
      debugln { ("WeaponCMD is throwing somewhere: $e ${e.stackTrace} ${e.message} ${e.cause}") }
    }
    return false
  }
}