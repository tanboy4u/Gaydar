package gaydar.struct.CMD

import gaydar.deserializer.channel.ActorChannel.Companion.droppedItemCompToItem
import gaydar.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import gaydar.deserializer.channel.ActorChannel.Companion.itemBag
import gaydar.struct.Actor
import gaydar.struct.Bunch
import gaydar.struct.NetGuidCacheObject
import gaydar.struct.NetworkGUID
import gaydar.util.DynamicArray
import gaydar.util.debugln

object DroppedItemGroupRootComponentCMD
{
  fun process(actor : Actor, bunch : Bunch, repObj : NetGuidCacheObject?, waitingHandle : Int, data : HashMap<String, Any?>) : Boolean
  {
    try
    {
      with(bunch) {
        when (waitingHandle)
        {
          4    -> updateItemBag(actor)
          else -> return false
        }
      }
      return true
    }
    catch (e : Exception)
    {
      debugln { ("DroppedItemGroupRootComponent is throwing somewhere: $e ${e.stackTrace} ${e.message} ${e.cause}") }
    }
    return false
  }
}


fun Bunch.updateItemBag(actor : Actor)
{
  try
  {
    val arraySize = readUInt16()
    val oldSize : Int
    val items : DynamicArray<NetworkGUID?>
    val oldItems = itemBag[actor.netGUID]
    if (oldItems == null)
    {
      oldSize = 0
      items = DynamicArray(arraySize)
    }
    else
    {
      oldSize = oldItems.size
      items = oldItems.resize(arraySize)
    }
    var index = readIntPacked()
    val toRemove = HashSet<NetworkGUID>()
    val toAdd = HashSet<NetworkGUID>()
    while (index != 0)
    {
      val i = index - 1
      val (netguid, obj) = readObject()
      items[i]?.apply {
        toRemove.add(this)
        toAdd.add(netguid)
      }
      items[i] = netguid
      index = readIntPacked()
    }
    for (i in oldSize - 1 downTo arraySize)
      items.rawGet(i)?.apply { toRemove.add(this) }
    toRemove.removeAll(toAdd)
    itemBag[actor.netGUID] = items
    for (removedComp in toRemove)
      droppedItemLocation.remove(droppedItemCompToItem[removedComp] ?: continue)
  }
  catch (e : Exception)
  {
    debugln { ("ItemBag in DroppedItemGroupRootComp is throwing somewhere: $e ${e.stackTrace} ${e.message} ${e.cause}") }
  }
}