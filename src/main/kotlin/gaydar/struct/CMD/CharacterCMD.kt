package gaydar.struct.CMD

import com.badlogic.gdx.math.Vector3
import gaydar.deserializer.byteRotationScale
import gaydar.deserializer.channel.ActorChannel.Companion.playerStateToActor
import gaydar.struct.*
import gaydar.struct.CMD.ActorCMD.actorWithPlayerState
import gaydar.util.debugln
import java.util.concurrent.ConcurrentHashMap

var selfDirection = 0f
val selfCoords = Vector3()

object CharacterCMD
{
  val actorHealth = ConcurrentHashMap<NetworkGUID, Float>()
  fun process(actor : Actor, bunch : Bunch, repObj : NetGuidCacheObject?, waitingHandle : Int, data : HashMap<String, Any?>) : Boolean
  {
    try
    {
      actor as Character
      with(bunch) {
        when (waitingHandle)
        {
          16   ->
          {
            val (playerStateGUID, playerState) = propertyObject()
            if (playerStateGUID.isValid() && !actor.playerStateID.isValid())
            {
              actorWithPlayerState[actor.netGUID] = playerStateGUID
              playerStateToActor[playerStateGUID] = actor.netGUID
              actor.playerStateID = playerStateGUID
            }
          }
        //ACharacter//struct FBasedMovementInfo
          19   ->
          {
            val MovementBase = propertyObject()
            val b = MovementBase
          }
          20   ->
          {
            val BoneName = propertyName()
            val b = BoneName
          }
          21   ->
          {
            val Location = propertyVector100()
            val b = Location
          }
          22   ->
          {
            val Rotation = readRotationShort()
            val b = Rotation
          }//propertyRotator()
          23   ->
          {
            val bServerHasBaseComponent = propertyBool()
            val b = bServerHasBaseComponent
          }
          24   ->
          {
            val bRelativeRotation = propertyBool()
            val b = bRelativeRotation
          }
          25   ->
          {
            val bServerHasVelocity = propertyBool()
            val b = bServerHasVelocity
          }//end struct FBasedMovementInfo
          26   ->
          {
            val AnimRootMotionTranslationScale = propertyFloat()
            val b = AnimRootMotionTranslationScale
          }
          27   ->
          {
            val ReplicatedServerLastTransformUpdateTimeStamp = propertyFloat()
            val b = ReplicatedServerLastTransformUpdateTimeStamp
          }
          28   ->
          {
            val ReplicatedMovementMode = propertyByte()
            val b = ReplicatedMovementMode
          }
          29   ->
          {
            val bIsCrouched = propertyBool()
            val b = bIsCrouched
          }
          30   ->
          {
            val JumpMaxHoldTime = propertyFloat()
            val b = JumpMaxHoldTime
          }
          31   ->
          {
            val JumpMaxCount = propertyInt()
            val b = JumpMaxCount
          }
        //struct FRepRootMotionMontage RepRootMotion;
          32   ->
          {
            val bIsActive = propertyBool()
            val b = bIsActive
          }
          33   ->
          {
            val AnimMontage = propertyObject()
            val b = AnimMontage
          }
          34   ->
          {
            val Position = propertyFloat()
            val b = Position
          }
          35   ->
          {
            val Location = propertyVector100()
            val b = Location
          }
          36   ->
          {
            val Rotation = readRotationShort()
            val b = Rotation
          }//propertyRotator()
          37   ->
          {
            val MovementBase = propertyObject()
            val b = MovementBase
          }
          38   ->
          {
            val MovementBaseBoneName = propertyName()
            val b = MovementBaseBoneName
          }
          39   ->
          {
            val bRelativePosition = propertyBool()
            val b = bRelativePosition
          }
          40   ->
          {
            val bRelativeRotation = propertyBool()
            val b = bRelativeRotation
          }
          41   ->
          {//player
            val bHasAdditiveSources = readBit()
            val bHasOverrideSources = readBit()
            val lastPreAdditiveVelocity = propertyVector10()
            val bIsAdditiveVelocityApplied = readBit()
            val flags = readUInt8()
          }
          42   ->
          {
            val Acceleration = propertyVector10()
            val b = Acceleration
          }
          43   ->
          {
            val LinearVelocity = propertyVector10()
            val b = LinearVelocity
          }
        //AMutableCharacter
          44   ->
          {//InstanceDescriptor
            val arrayNum = readUInt16()
            var index = readIntPacked()
            while (index != 0)
            {
              val value = readUInt8()
              index = readIntPacked()
            }
          }
        //ATslCharacter
          45   ->
          {
            val remote_CastAnim = readInt(8)
            val a = remote_CastAnim
          }
          46   ->
          {
            val CurrentVariableZoomLevel = propertyInt()
            val b = CurrentVariableZoomLevel
          }
          47   ->
          {
            val BuffFinalSpreadFactor = propertyFloat()
            val b = BuffFinalSpreadFactor
          }
          48   ->
          {
            val InventoryFacade = propertyObject()
            val b = InventoryFacade
          }
          49   ->
          {
            val WeaponProcessor = propertyObject()
            val b = WeaponProcessor
          }
          50   ->
          {
            val CharacterState = propertyByte()
            val b = CharacterState
          }
          51   ->
          {
            val bIsScopingRemote = propertyBool()
            val b = bIsScopingRemote
          }
          52   ->
          {
            val bIsAimingRemote = propertyBool()
          }
          53   ->
          {
            val bIsFirstPersonRemote = propertyBool()
            val b = bIsFirstPersonRemote
          }
          54   ->
          {
            val bIsInVehicleRemote = propertyBool()
            val b = bIsInVehicleRemote
          }
          55   ->
          {
            val (teamsId) = propertyObject()
              actor.teamID = teamsId
          }
          56   ->
          {//struct FTakeHitInfo
            val ActualDamage = propertyFloat()
//          println("ActualDamage=$ActualDamage")
          }
          57   ->
          {
            val damageType = propertyObject()
//          println("damageType=$damageType")
          }
          58   ->
          {
            val PlayerInstigator = propertyObject()
//          if (PlayerInstigator.first in actors)
//            println("PlayerInstigator=${actors[PlayerInstigator.first]}")
          }
          59   ->
          {
            val DamageOrigin = propertyVectorQ()
//          println("DamageOrigin=$DamageOrigin")
          }
          60   ->
          {
            val RelHitLocation = propertyVectorQ()
//          println("RelHitLocation=$RelHitLocation")
          }
          61   ->
          {
            val BoneName = propertyName()
//          println("BoneName=$BoneName")
          }
          62   ->
          {
            val DamageMaxRadius = propertyFloat()
//          println("DamageMaxRadius=$DamageMaxRadius")
          }
          63   ->
          {
            var ShotDirPitch = propertyByte()
            val a = ShotDirPitch * byteRotationScale
//          println("ShotDirPitch=$a")
          }
          64   ->
          {
            val ShotDirYaw = propertyByte()
            val a = ShotDirYaw * byteRotationScale
//          println("ShotDirYaw=$a")
          }
          65   ->
          {
            val bPointDamage = propertyBool()
//          println("bPointDamage=$bPointDamage")
          }
          66   ->
          {
            val bRadialDamage = propertyBool()
//          println("bRadialDamage=$bRadialDamage")
          }
          67   ->
          {
            val bKilled = propertyBool()
//          println("bKilled=$bKilled")
          }
          68   ->
          {
            val EnsureReplicationByte = propertyByte()
            val b = EnsureReplicationByte
          }
          69   ->
          {
            val AttackerWeaponName = propertyName()
//          println("AttackerWeaponName=$AttackerWeaponName")
          }
          70   ->
          {
            val AttackerLocation = propertyVector()
          }
          71   ->
          {
            val TargetingType = readInt(4)
            val a = TargetingType
          }
          72   ->
          {
            val reviveCastingTime = propertyFloat()
            val a = reviveCastingTime
          }
          73   ->
          {
            val bWantsToRun = propertyBool()
            val b = bWantsToRun
          }
          74   ->
          {
            val bWantsToSprint = propertyBool()
            val b = bWantsToSprint
          }
          75   ->
          {
            val bWantsToSprintingAuto = propertyBool()
            val b = bWantsToSprintingAuto
          }
          76   ->
          {
            val bWantsToRollingLeft = propertyBool()
            val b = bWantsToRollingLeft
          }
          77   ->
          {
            val bWantsToRollingRight = propertyBool()
            val b = bWantsToRollingRight
          }
          78   ->
          {
            val bIsPeekLeft = propertyBool()
            val b = bIsPeekLeft
          }
          79   ->
          {
            val bIsPeekRight = propertyBool()
            val b = bIsPeekRight
          }
          80   ->
          {
            val IgnoreRotation = propertyBool()
            val b = IgnoreRotation
          }
          81   ->
          {
            val bIsGroggying = propertyBool()
            val b = bIsGroggying
          }
          82   ->
          {
            val bIsGroggying = propertyBool()
            actor.isGroggying = bIsGroggying
          }
          83   ->
          {
            val bIsReviving = propertyBool()
            actor.isReviving = bIsReviving
          }
          84   ->
          {
            val bIsWeaponObstructed = propertyBool()
            val b = bIsWeaponObstructed
          }
          85   ->
          {
            val bIsCoatEquipped = propertyBool()
            val b = bIsCoatEquipped
          }
          86   ->
          {
            val bIsZombie = propertyBool()
            val b = bIsZombie
          }
          87   ->
          {
            val bIsThrowHigh = propertyBool()
            val b = bIsThrowHigh
          }
          88   ->
          {
            val bUseRightShoulderAiming = propertyBool()
            val b = bUseRightShoulderAiming
          }
          89   ->
          {
            val GunDirectionSway = readRotationShort()//propertyRotator()
            val b = GunDirectionSway
          }
          90   ->
          {
            val AimOffsets = propertyVectorNormal()
            val b = AimOffsets
          }
          91   ->
          {
            val NetOwnerController = readObject()
            val b = NetOwnerController
          }
          92   ->
          {
            val bAimStateActive = propertyBool()
            val b = bAimStateActive
          }
          93   ->
          {
            val bIsHoldingBreath = propertyBool()
            val b = bIsHoldingBreath
          }
          94   ->
          {
            val health = propertyFloat()
            actor.health = health
            actorHealth[actor.netGUID] = health

//          println("health=$health")
          }
          95   ->
          {
            val healthMax = propertyFloat()
//          println("health max=$healthMax")
          }
          96   ->
          {
            val GroggyHealth = propertyFloat()
            actor.groggyHealth = GroggyHealth
          }
          97   ->
          {
            val GroggyHealthMax = propertyFloat()
//          println("GroggyHealthMax=$GroggyHealthMax")
          }
          98   ->
          {
            val BoostGauge = propertyFloat()
            actor.boostGauge = BoostGauge
          }
          99   ->
          {
            val BoostGaugeMax = propertyFloat()
//          println("BoostGaugeMax=$BoostGaugeMax")
          }
          100  ->
          {
            val ShoesSoundType = readInt(8)
            val b = ShoesSoundType
          }
          101  ->
          {
            val VehicleRiderComponent = readObject()
            val b = VehicleRiderComponent
          }
          102  ->
          {
            val bIsActiveRagdollActive = propertyBool()
            val b = bIsActiveRagdollActive
          }
          103  ->
          {
            val PreReplicatedStanceMode = readInt(4)
            val b = PreReplicatedStanceMode
          }
          104  ->
          {
            val bServerFinishedVault = propertyBool()
            val b = bServerFinishedVault
          }
          105  ->
          {
            val bWantsToCancelVault = propertyBool()
            val b = bWantsToCancelVault
          }
          106  ->
          {
            val bIsDemoVaulting_CP = propertyBool()
            val b = bIsDemoVaulting_CP
          }
          else -> return APawnCMD.process(actor, bunch, repObj, waitingHandle, data)
        }
        return true
      }
    }
    catch (e : Exception)
    {
      debugln { ("CharacterCMD is throwing somewhere: $e ${e.stackTrace} ${e.message} ${e.cause}") }
    }
    return false
  }
}