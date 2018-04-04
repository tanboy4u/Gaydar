package gaydar.struct.CMD

import com.badlogic.gdx.math.Vector2
import gaydar.deserializer.channel.ActorChannel.Companion.droppedItemCompToItem
import gaydar.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import gaydar.struct.Actor
import gaydar.struct.Bunch
import gaydar.struct.NetGuidCacheObject
import gaydar.util.debugln

object DroppedItemInteractionComponentCMD
{
  fun process(actor : Actor, bunch : Bunch, repObj : NetGuidCacheObject?, waitingHandle : Int, data : HashMap<String, Any?>) : Boolean
  {
    try
    {
      with(bunch) {
        when (waitingHandle)
        {
        //UActorComponent
          1    ->
          {
            val bReplicates = readBit()
//          println("item $bReplicates")
          }
          2    ->
          {
            val isAlive = readBit()
//          println("item $isAlive")
          }
        //USceneComponent
          3    ->
          {
            val attachParent = readObject()
            val a = attachParent
//          println("attachParent:$attachParent")
          }
          4    ->
          {
            val arraySize = readUInt16()
            var index = readIntPacked()
            while (index != 0)
            {
              val (netguid, obj) = readObject()
//            println("$netguid,$obj")
              index = readIntPacked()
            }
          }
          5    ->
          {
            val attachSocketName = readName()
            val a = attachSocketName
          }
          6    ->
          {
            val bReplicatesAttachmentReference = readBit()
            val a = bReplicatesAttachmentReference
          }
          7    ->
          {
            val bReplicatesAttachment = readBit()
            val a = bReplicatesAttachment
          }
          8    ->
          {
            val bAbsoluteLocation = readBit()
            val a = bAbsoluteLocation
          }
          9    ->
          {
            val bAbsoluteRotation = readBit()
            val a = bAbsoluteRotation
          }
          10   ->
          {
            val bAbsoluteScale = readBit()
            val a = bAbsoluteScale
          }
          11   ->
          {
            val bVisible = readBit()
            val a = bVisible
          }
          12   ->
          {
            val relativeLocation = propertyVector()
            data["relativeLocation"] = Vector2(relativeLocation.x, relativeLocation.y)
//          println("relativeLocation:$relativeLocation")
          }
          13   ->
          {
            val relativeRotation = readRotationShort()
            data["relativeRotation"] = relativeRotation.y
//          println("relativeRotation:$relativeRotation")
          }
          14   ->
          {
            val relativeScale3D = propertyVector()
            val a = relativeScale3D
          }
        //DroppedItemInteractionComponent
          15   ->
          {
            val (itemGUID, _) = readObject()
            val (loc, _) = droppedItemLocation[itemGUID] ?: return true
            droppedItemCompToItem[repObj!!.outerGUID] = itemGUID
            val relativeLocation = data["relativeLocation"] as Vector2
            val relativeRotation = data["relativeRotation"] as Float
            loc.add(relativeLocation.x, relativeLocation.y, relativeRotation)
          }
          else -> return false
        }
      }
      return true
    }
    catch (e : Exception)
    {
      debugln { ("DroppedItemInteractionComp is throwing somewhere: $e ${e.stackTrace} ${e.message} ${e.cause}") }
    }
    return false
  }
}