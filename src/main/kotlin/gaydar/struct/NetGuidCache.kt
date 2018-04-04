package gaydar.struct

import gaydar.GameListener
import gaydar.register
import gaydar.struct.NetGUIDCache.Companion.guidCache

class NetGuidCacheObject(
      val pathName : String,
      val outerGUID : NetworkGUID,
      val networkChecksum : Int = 0,
      val bNoLoad : Boolean = false,
      val IgnoreWhenMissing : Boolean = false
                        )
{
  var holdObj : Any? = null
  override fun toString() : String
  {
    return "{'$pathName'}"
  }

  fun fullString() : String
  {
    return "{path='$pathName', outer[$outerGUID]=${guidCache.getObjectFromNetGUID(outerGUID)}}"
  }
}

class NetGUIDCache
{
  companion object : GameListener
  {
    init
    {
      register(this)
    }

    val guidCache = NetGUIDCache()

    override fun onGameOver()
    {
      guidCache.isExportingNetGUIDBunch = false
      guidCache.objectLoop.clear()
    }
  }

  val objectLoop = HashMap<NetworkGUID, NetGuidCacheObject>()
  var isExportingNetGUIDBunch = false

  fun get(index : Int) = objectLoop[NetworkGUID(index)]

  fun getObjectFromNetGUID(netGUID : NetworkGUID) : NetGuidCacheObject?
  {
    val cacheObject = objectLoop[netGUID] ?: return null
    if (cacheObject.pathName.isBlank())
    {
//      check(netGUID.isDynamic())
      return null
    }
    return cacheObject
  }

  fun registerNetGUIDFromPath_Client(
        netGUID : NetworkGUID,
        pathName : String,
        outerGUID : NetworkGUID,
        networkChecksum : Int,
        bNoLoad : Boolean,
        bIgnoreWhenMissing : Boolean
                                    )
  {
    val existingCacheObjectPtr = objectLoop[netGUID]

    // If we find this guid, make sure nothing changes
    if (existingCacheObjectPtr != null)
    {
      var bPathnameMismatch = false
      var bOuterMismatch = false
      var bNetGuidMismatch = false
      if (existingCacheObjectPtr.pathName != pathName)
        bPathnameMismatch = true
      if (existingCacheObjectPtr.outerGUID != outerGUID)
        bOuterMismatch = true

      return
    }

    // Register a new guid with this path
    val cacheObject = NetGuidCacheObject(
          pathName, outerGUID, networkChecksum, bNoLoad, bIgnoreWhenMissing
                                        )
    objectLoop[netGUID] = cacheObject
  }

  fun registerNetGUID_Client(netGUID : NetworkGUID, obj : Any)
  {
    val existingCacheObjectPtr = objectLoop[netGUID]

    // If we find this guid, make sure nothing changes
    if (existingCacheObjectPtr != null)
    {
      val oldObj = existingCacheObjectPtr.holdObj
      objectLoop.remove(netGUID)
    }
    val cacheObject = when (obj)
    {
      is NetGuidCacheObject -> NetGuidCacheObject(obj.pathName, netGUID)
      is Actor              -> NetGuidCacheObject(obj.typeName, netGUID)
      else                  -> NetGuidCacheObject("", netGUID)
    }
//    val cacheObject =if(obj is NetGuidCacheObject) NetGuidCacheObject(obj.pathName, netGUID) else if(o)
    objectLoop[netGUID] = cacheObject
  }
}