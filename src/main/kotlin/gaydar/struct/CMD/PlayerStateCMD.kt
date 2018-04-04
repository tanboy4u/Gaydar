package gaydar.struct.CMD

import gaydar.GameListener
import gaydar.deserializer.channel.ActorChannel.Companion.attacks
import gaydar.deserializer.channel.ActorChannel.Companion.selfID
import gaydar.deserializer.channel.ActorChannel.Companion.selfStateID
import gaydar.deserializer.channel.ActorChannel.Companion.uniqueIds
import gaydar.register
import gaydar.struct.*
import gaydar.struct.Item.Companion.simplify
import gaydar.util.debugln
import gaydar.util.tuple2
import java.util.concurrent.ConcurrentHashMap

val playerNames = ConcurrentHashMap<NetworkGUID, String>()
val playerNumKills = ConcurrentHashMap<NetworkGUID, Int>()

object PlayerStateCMD : GameListener
{
  init
  {
    register(this)
  }

  override fun onGameOver()
  {
    uniqueIds.clear()
    attacks.clear()
    selfID = NetworkGUID(0)
    selfStateID = NetworkGUID(0)
    playerNumKills.clear()
  }

  fun process(actor : Actor, bunch : Bunch, repObj : NetGuidCacheObject?, waitingHandle : Int, data : HashMap<String, Any?>) : Boolean
  {


    try
    {
      actor as PlayerState
      with(bunch) {
        // println("WAITING HANDLE; $waitingHandle")
        when (waitingHandle)
        {
          16   ->
          {
            val score = propertyFloat()
//          println("score=$score")
          }
          17   ->
          {
            val ping = propertyByte()
          }
          18   ->
          {
            val name = propertyString()
            actor.name = name
            //println("ACTOR NAME: ${actor.netGUID} playerID=$name")
          }
          19   ->
          {
            val playerID = propertyInt()
//          println("${actor.netGUID} playerID=$playerID")
          }
          20   ->
          {
            val bFromPreviousLevel = propertyBool()
//          println("${actor.netGUID} bFromPreviousLevel=$bFromPreviousLevel")
          }
          21   ->
          {
            val isABot = propertyBool()
//          println("${actor.netGUID} isABot=$isABot")
          }
          22   ->
          {
            val bIsInactive = propertyBool()
//          println("${actor.netGUID} bIsInactive=$bIsInactive")
          }
          23   ->
          {
            val bIsSpectator = propertyBool()
//          println("${actor.netGUID} bIsSpectator=$bIsSpectator")
          }
          24   ->
          {
            val bOnlySpectator = propertyBool()
//          println("${actor.netGUID} bOnlySpectator=$bOnlySpectator")
          }
          25   ->
          {
            val StartTime = propertyInt()
//          println("${actor.netGUID} StartTime=$StartTime")
          }
          26   ->
          {
            val uniqueId = propertyNetId()
            uniqueIds[uniqueId] = actor.netGUID
//          println("${playerNames[actor.netGUID]}${actor.netGUID} uniqueId=$uniqueId")
          }
          27   ->
          {//indicate player's death
            val Ranking = propertyInt()
//          println("${playerNames[actor.netGUID]}${actor.netGUID} Ranking=$Ranking")
          }
          28   ->
          {
            val AccountId = propertyString()
//          println("${actor.netGUID} AccountId=$AccountId")
          }
          29   ->
          {
            val ReportToken = propertyString()
          }
          30   ->
          {//ReplicatedCastableItems
            val arraySize = readUInt16()
            actor.castableItems.resize(arraySize)
            var index = readIntPacked()
            while (index != 0)
            {
              val idx = index - 1
              val arrayIdx = idx / 3
              val structIdx = idx % 3
              val element = actor.castableItems[arrayIdx] ?: tuple2("", 0)
              when (structIdx)
              {
                0 ->
                {
                  val (guid, castableItemClass) = readObject()
                  if (castableItemClass != null)
                    element._1 = simplify(castableItemClass.pathName)
                }
                1 ->
                {
                  val ItemType = readInt(8)
                  val a = ItemType
                }
                2 ->
                {
                  val itemCount = readInt32()
                  element._2 = itemCount
                }
              }
              actor.castableItems[arrayIdx] = element
              index = readIntPacked()
            }
            return true
          }
          31   ->
          {
            val ObserverAuthorityType = readInt(4)
          }
          32   ->
          {
            val teamNumber = readInt(100)
            actor.teamNumber = teamNumber
          }
          33   ->
          {
            val bIsZombie = propertyBool()
          }
          34   ->
          {
            val scoreByDamage = propertyFloat()
            // println("SCORE BY DAMAGE: $scoreByDamage")
          }
          35   ->
          {
            val ScoreByKill = propertyFloat()
            // println("SCORE BY KILL: $ScoreByKill")
          }
          36   ->
          {
            val ScoreByRanking = propertyFloat()
            //  println("SCORE BY RANKING: $ScoreByRanking")
          }
          37   ->
          {

            val ScoreFactor = propertyFloat()
            //   println("SCORE FACTOR: $ScoreFactor")
          }
          38   ->
          {
            val NumKills = propertyInt()
            //  println("NUM KILLS: $NumKills")
            // actor.numKills = NumKills
            playerNumKills[actor.netGUID] = NumKills

          }
          39   ->
          {
            val TotalMovedDistanceMeter = propertyFloat()
            //println(TotalMovedDistanceMeter)
            selfStateID = actor.netGUID//only self will get this update
          }
          40   ->
          {
            val TotalGivenDamages = propertyFloat()
            //  println("TOTAL GIVEN DAMAGE: $TotalGivenDamages")
          }
          41   ->
          {
            val LongestDistanceKill = propertyFloat()
            //    println("LONGEST KILL:  $LongestDistanceKill")
          }
          42   ->
          {
            val HeadShots = propertyInt()
            //  println("HEADSHOTS: $HeadShots")
          }
          43   ->
          {//ReplicatedEquipableItems
            try
            {
              val arraySize = readUInt16()
              actor.equipableItems.resize(arraySize)
              var index = readIntPacked()
              while (index != 0)
              {
                val idx = index - 1
                val arrayIdx = idx / 2
                val structIdx = idx % 2
                val element = actor.equipableItems[arrayIdx] ?: tuple2("", 0f)
                when (structIdx)
                {
                  0 ->
                  {
                    val (guid, equipableItemClass) = readObject()
                    if (equipableItemClass != null)
                      element._1 = simplify(equipableItemClass.pathName)
                    val a = guid
                  }
                  1 ->
                  {
                    val durability = readFloat()
                    element._2 = durability
                    val a = durability
                  }
                }
                actor.equipableItems[arrayIdx] = element
                index = readIntPacked()
              }
              return true
            }
            catch (e : Exception)
            {
              println("PlayerState is throwing on 43: $e ${e.stackTrace} ${e.message}")
            }

          }
          44   ->
          {
            val bIsInAircraft = propertyBool()
          }
          45   ->
          {//LastHitTime
            val lastHitTime = propertyFloat()
          }
          46   ->
          {
            val currentAttackerPlayerNetId = propertyString()
            attacks.add(tuple2(uniqueIds[currentAttackerPlayerNetId]!!, actor.netGUID))
          }
          else -> return ActorCMD.process(actor, bunch, repObj, waitingHandle, data)
        }
      }
      return true
    }
    catch (e : Exception)
    {
      debugln { ("PlayerStateCMD is throwing somewhere: $e ${e.stackTrace} ${e.message} ${e.cause}") }
    }
    return false
  }
}