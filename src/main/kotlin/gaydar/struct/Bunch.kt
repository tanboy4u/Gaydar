package gaydar.struct

import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import gaydar.deserializer.Buffer

class Bunch(
      val BunchDataBits : Int,
      buffer : Buffer,
      val PacketID : Int,
      val ChIndex : Int,
      val ChType : Int,
      var ChSequence : Int,
      val bOpen : Boolean,
      var bClose : Boolean,
      var bDormant : Boolean,
      var bIsReplicationPaused : Boolean,
      val bReliable : Boolean,
      val bPartial : Boolean,
      val bPartialInitial : Boolean,
      var bPartialFinal : Boolean,
      val bHasPackageMapExports : Boolean,
      var bHasMustBeMappedGUIDs : Boolean
           ) : Buffer(buffer)
{

  override fun deepCopy(copyBits : Int) : Bunch
  {
    val buf = super.deepCopy(copyBits)
    return Bunch(
          BunchDataBits,
          buf,
          PacketID,
          ChIndex,
          ChType,
          ChSequence,
          bOpen,
          bClose,
          bDormant,
          bIsReplicationPaused,
          bReliable,
          bPartial,
          bPartialInitial,
          bPartialFinal,
          bHasPackageMapExports,
          bHasMustBeMappedGUIDs
                )
  }

  var next : Bunch? = null

  fun propertyBool() = readBit()
  fun propertyFloat() = readFloat()
  fun propertyInt() = readInt32()
  fun propertyByte() = readByte()
  fun propertyName() = readName()
  fun propertyObject() = readObject()
  fun propertyUInt32() = readUInt32()
  fun propertyVector() = Vector3(readFloat(), readFloat(), readFloat())
  fun propertyRotator() = Vector3(readFloat(), readFloat(), readFloat())
  fun propertyPlane() = Quaternion(propertyVector(), readFloat())
  fun propertyVector100() = readVector(100, 30)
  fun propertyNetId() = if (readInt32() > 0) readString() else ""
  fun repMovement(actor : Actor)
  {
    val bSimulatedPhysicSleep = readBit()
    val bRepPhysics = readBit()
    actor.location = if (actor.isAPawn)
      readVector(100, 30)
    else
      readVector(1, 24)

    actor.rotation = if (actor.isACharacter)
      readRotationShort()
    else
      readRotation()

    actor.velocity = readVector(1, 24)
    if (bRepPhysics)
      readVector(1, 24)
  }

  fun propertyVectorNormal() = readFixedVector(1, 16)
  fun propertyVector10() = readVector(10, 24)
  fun propertyVectorQ() = readVector(1, 20)
  fun propertyString() = readString()
  fun propertyUInt64() = readInt64()
}