package gaydar.deserializer.channel

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import gaydar.GameListener
import gaydar.deserializer.CHTYPE_ACTOR
import gaydar.register
import gaydar.struct.*
import gaydar.struct.Archetype.*
import gaydar.struct.CMD.receiveProperties
import gaydar.struct.NetGUIDCache.Companion.guidCache
import gaydar.struct.Team
import gaydar.ui.itemIcons
import gaydar.util.DynamicArray
import gaydar.util.tuple2
import gaydar.util.tuple3
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class ActorChannel(ChIndex : Int, client : Boolean = true) : Channel(ChIndex, CHTYPE_ACTOR, client)
{
  companion object : GameListener
  {
    init
    {
      register(this)
    }

    val actors = ConcurrentHashMap<NetworkGUID, Actor>()
    val visualActors = ConcurrentHashMap<NetworkGUID, Actor>()
    val teams = ConcurrentHashMap<NetworkGUID, Team>()
    val airDropLocation = ConcurrentHashMap<NetworkGUID, Vector3>()
    val redZoneBombLocation = ConcurrentHashMap<NetworkGUID, tuple2<Vector3, Long>>()
    val droppedItemToItem = ConcurrentHashMap<NetworkGUID, NetworkGUID>()
    val itemBag = ConcurrentHashMap<NetworkGUID, DynamicArray<NetworkGUID?>>()
    val droppedItemCompToItem = ConcurrentHashMap<NetworkGUID, NetworkGUID>()
    val droppedItemLocation = ConcurrentHashMap<NetworkGUID, tuple3<Vector3, String, Boolean>>()
    val corpseLocation = ConcurrentHashMap<NetworkGUID, Vector3>()
    val actorHasWeapons = ConcurrentHashMap<NetworkGUID, DynamicArray<NetworkGUID?>>()
    val weapons = ConcurrentHashMap<NetworkGUID, Actor>()

    val playerStateToActor = ConcurrentHashMap<NetworkGUID, NetworkGUID>()
    val firing = ConcurrentLinkedQueue<tuple2<NetworkGUID, Long>>()
    val attacks = ConcurrentLinkedQueue<tuple2<NetworkGUID, NetworkGUID>>()//A -> B

    var selfDirection = 0f
    val selfCoords = Vector2()
    var selfID = NetworkGUID(0)
    var selfStateID = NetworkGUID(0)
    val uniqueIds = ConcurrentHashMap<String, NetworkGUID>()

    override fun onGameOver()
    {
      selfID = NetworkGUID(0)
      selfStateID = NetworkGUID(0)
      selfCoords.setZero()
      selfDirection = 0f

      actors.clear()
      teams.clear()
      visualActors.clear()
      airDropLocation.clear()
      redZoneBombLocation.clear()
      droppedItemToItem.clear()
      itemBag.clear()
      droppedItemCompToItem.clear()
      droppedItemLocation.clear()
      corpseLocation.clear()
      weapons.clear()
      actorHasWeapons.clear()
      playerStateToActor.clear()
      firing.clear()
      attacks.clear()
      uniqueIds.clear()
    }
  }

  var actor : Actor? = null

  override fun ReceivedBunch(bunch : Bunch)
  {
    if (client && bunch.bHasMustBeMappedGUIDs)
    {
      val NumMustBeMappedGUIDs = bunch.readUInt16()
      for (i in 0 until NumMustBeMappedGUIDs)
      {
        val guid = bunch.readNetworkGUID()
      }
    }
    ProcessBunch(bunch)
  }

  fun ProcessBunch(bunch : Bunch)
  {
    if (client && actor == null)
    {
      if (!bunch.bOpen)
      {
        return
      }
      SerializeActor(bunch)
      if (actor == null)
        return
    }
    if (!client && actor == null)
    {
      val clientChannel = inChannels[chIndex] ?: return
      actor = (clientChannel as ActorChannel).actor
      if (actor == null) return
    }
    val actor = actor!!
    if (actor.type == DroppedItem && bunch.bitsLeft() == 0)
      droppedItemLocation.remove(droppedItemToItem[actor.netGUID] ?: return)
    while (bunch.notEnd())
    {
      //header
      val bHasRepLayout = bunch.readBit()
      val bIsActor = bunch.readBit()
      var repObj : NetGuidCacheObject?
      if (bIsActor)
      {
        repObj = NetGuidCacheObject(actor.type.name, actor.netGUID)
      }
      else
      {
        val (netguid, _subobj) = bunch.readObject()//SubObject, SubObjectNetGUID
        if (!client)
        {
          if (_subobj == null)// The server should never need to create sub objects
            continue
          repObj = _subobj
        }
        else
        {
          val bStablyNamed = bunch.readBit()
          if (bStablyNamed)
          {// If this is a stably named sub-object, we shouldn't need to create it
            if (_subobj == null)
              continue
            repObj = _subobj
          }
          else
          {
            val (classGUID, classObj) = bunch.readObject()//SubOjbectClass,SubObjectClassNetGUID
            if (!classGUID.isValid() || classObj == null)
              continue
            when (actor.type)
            {
              DroopedItemGroup, DroppedItem, AirDrop, DeathDropItemPackage ->
              {
                if (classObj.pathName in itemIcons)
                  droppedItemLocation[netguid] = tuple3(Vector3(actor.location.x, actor.location.y, 0f), classObj.pathName, actor.type != AirDrop)
              }
              else                                                         ->
              {
              }
            }
            val subobj = NetGuidCacheObject(classObj.pathName, classGUID)
            guidCache.registerNetGUID_Client(netguid, subobj)
            repObj = guidCache.getObjectFromNetGUID(netguid)
          }
        }
      }
      val NumPayloadBits = bunch.readIntPacked()
      if (NumPayloadBits < 0 || NumPayloadBits > bunch.bitsLeft())
      {
        return
      }
      if (NumPayloadBits == 0)
        continue
      try
      {
        val outPayload = bunch.deepCopy(NumPayloadBits)
        var parseComplete = !bHasRepLayout
        if (bHasRepLayout)
        {
          if (!client)// Server shouldn't receive properties.
            return
          if (actor.type == DroopedItemGroup && repObj?.pathName == "RootComponent")
            repObj = NetGuidCacheObject("DroppedItemGroupRootComponent", repObj.outerGUID)
          parseComplete = receiveProperties(outPayload, repObj, actor)
        }
        if (!client)
        {
          when
          {
            actor.isVehicle                ->
              vehicleSyncComp(actor, outPayload)
            actor.isACharacter && bIsActor ->
            {
              selfID = actor.netGUID
              charmovecomp(outPayload)
            }
          }
        }

      }
      catch (e : Exception)
      {
      }
      bunch.skipBits(NumPayloadBits)
    }
  }

  fun SerializeActor(bunch : Bunch)
  {
    val (netGUID, newActor) = bunch.readObject()//NetGUID
    if (netGUID.isDynamic())
    {
      val (archetypeNetGUID, archetype) = bunch.readObject()
      if (archetypeNetGUID.isValid() && archetype == null)
      {
        val existingCacheObjectPtr = guidCache.objectLoop[archetypeNetGUID]
      }
      val bSerializeLocation = bunch.readBit()

      val Location = if (bSerializeLocation)
        bunch.readVector()
      else
        Vector3.Zero
      val bSerializeRotation = bunch.readBit()
      val Rotation = if (bSerializeRotation) bunch.readRotationShort() else Vector3.Zero

      val bSerializeScale = bunch.readBit()
      val Scale = if (bSerializeScale) bunch.readVector() else Vector3.Zero

      val bSerializeVelocity = bunch.readBit()
      val Velocity = if (bSerializeVelocity) bunch.readVector() else Vector3.Zero

      if (actor == null && archetype != null)
      {
        val _actor = makeActor(netGUID, archetype)
        with(_actor) {
          location = Location
          rotation = Rotation
          velocity = Velocity
          guidCache.registerNetGUID_Client(netGUID, this)
          actor = this
          if (client)
          {
            actors[netGUID] = this
            when (type)
            {
              Archetype.Weapon     -> weapons[netGUID] = this
              AirDrop              -> airDropLocation[netGUID] = location
              RedZoneBomb          -> redZoneBombLocation[netGUID] = tuple2(location, System.currentTimeMillis())
              Archetype.Team       -> teams[netGUID] = _actor as Team
              DeathDropItemPackage -> corpseLocation[netGUID] = location
              else                 ->
              {
              }
            }
          }
        }
      }
    }
    else
    {
      if (newActor == null) return
      actor = makeActor(netGUID, newActor)
      actor!!.isStatic = true
    }

  }

  override fun close()
  {
    if (actor != null)
    {
      if (client)
      {
        actors.remove(actor!!.netGUID)
        visualActors.remove(actor!!.netGUID)
      }
      actor = null
    }
  }

}

