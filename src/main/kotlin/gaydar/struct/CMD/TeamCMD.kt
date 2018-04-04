package gaydar.struct.CMD

import gaydar.struct.Actor
import gaydar.struct.Bunch
import gaydar.struct.NetGuidCacheObject
import gaydar.struct.Team
import gaydar.util.debugln

object TeamCMD
{

  fun process(actor : Actor, bunch : Bunch, repObj : NetGuidCacheObject?, waitingHandle : Int, data : HashMap<String, Any?>) : Boolean
  {
    try
    {
      actor as Team
      with(bunch) {
        //      println("${actor.netGUID} $waitingHandle")
        when (waitingHandle)
        {
          16   ->
          {
            val playerLocation = propertyVector100()
            val a = playerLocation
          }
          17   ->
          {
            val playerRotation = readRotationShort()
            val a = playerRotation
          }
          18   ->
          {
            val playerName = propertyString()
          }
          19   ->
          {//Health
            val health = readUInt8()
            val a = health
          }
          20   ->
          {//HealthMax
            val HealthMax = readUInt8()
            val a = HealthMax
          }
          21   ->
          {//GroggyHealth
            val GroggyHealth = readUInt8()
            val a = GroggyHealth
          }
          22   ->
          {//GroggyHealthMax
            val GroggyHealthMax = readUInt8()
            val a = GroggyHealthMax
          }
          23   ->
          {//MapMarkerPosition
            val MapMarkerPosition = readVector2D()
            actor.mapMarkerPosition.set(MapMarkerPosition)
          }
          24   ->
          {//bIsDying
            val bIsDying = readBit()
            val a = bIsDying
          }
          25   ->
          {//bIsGroggying
            val bIsGroggying = readBit()
            val a = bIsGroggying
          }
          26   ->
          {//bQuitter
            val bQuitter = readBit()
            val a = bQuitter
          }
          27   ->
          {//bShowMapMarker
            val bShowMapMarker = readBit()
            actor.showMapMarker = bShowMapMarker
          }
          28   ->
          {//TeamVehicleType
            val TeamVehicleType = readInt(3)
            val a = TeamVehicleType
          }
          29   ->
          {//BoostGauge
            val BoostGauge = readFloat()
            val a = BoostGauge
          }
          30   ->
          {//MemberNumber
            val MemberNumber = readInt8()
            actor.memberNumber = MemberNumber
          }
          31   ->
          {//UniqueId
            val UniqueId = readString()
            val a = UniqueId
          }
          else -> return ActorCMD.process(actor, bunch, repObj, waitingHandle, data)
        }
        return true
      }
    }
    catch (e : Exception)
    {
      debugln { ("TeamCMD is throwing somewhere: $e ${e.stackTrace} ${e.message} ${e.cause}") }
    }
    return false
  }
}