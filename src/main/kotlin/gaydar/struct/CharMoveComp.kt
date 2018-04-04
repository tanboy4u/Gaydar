package gaydar.struct

import gaydar.deserializer.shortRotationScale
import gaydar.struct.CMD.selfCoords
import gaydar.struct.CMD.selfDirection

fun vehicleSyncComp(actor : Actor, bunch : Bunch)
{

  val maxIndex = 8
  val repIndex = bunch.readInt(maxIndex)
  val payloadBits = bunch.readIntPacked()
  val rpcPayload = bunch.deepCopy(payloadBits)
  bunch.skipBits(payloadBits)
  when (repIndex)
  {
    5    ->
    {
      if (rpcPayload.readBit())
      {
        val InCorrectionId = rpcPayload.propertyInt()
      }
      if (rpcPayload.readBit())
      {
        val clientLoc = rpcPayload.readVector(100, 30)
        actor.location.set(clientLoc)
      }
      if (rpcPayload.readBit())
      {
        val ClientLinearVelocity = rpcPayload.readVector(100, 30)
        val a = ClientLinearVelocity
      }
      if (rpcPayload.readBit())
      {
        val view = rpcPayload.readUInt32()
        actor.rotation.y = ((view shr 16) * shortRotationScale + 180) % 360
      }
    }
    else ->
    {
    }
  }
}

fun charmovecomp(bunch : Bunch, client : Boolean = false)
{
  val maxIndex = 144
  val repIndex = bunch.readInt(maxIndex)
  val payloadBits = bunch.readIntPacked()
  val rpcPayload = bunch.deepCopy(payloadBits)
  bunch.skipBits(payloadBits)
  when (repIndex)
  {
    29   ->
    {//void ServerMove(float TimeStamp, const struct FVector_NetQuantize10& InAccel, const struct FVector_NetQuantize100& ClientLoc, unsigned char CompressedMoveFlags, unsigned char ClientRoll, uint32_t View, class UPrimitiveComponent* ClientMovementBase, const struct FName& ClientBaseBoneName, unsigned char ClientMovementMode);
      if (rpcPayload.readBit())
      {
        val timeStamp = rpcPayload.readFloat()
      }
      if (rpcPayload.readBit())
      {
        val inAccel = rpcPayload.readVector(10, 24)
      }
      if (rpcPayload.readBit())
      {
        val clientLoc = rpcPayload.readVector(100, 30)
        selfCoords.set(clientLoc.x, clientLoc.y, clientLoc.z)
      }
      if (rpcPayload.readBit())
      {
        val compressedMoveFlags = rpcPayload.readUInt8()
      }
      if (rpcPayload.readBit())
      {
        val clientRoll = rpcPayload.readUInt8()
      }
      if (rpcPayload.readBit())
      {
        val view = rpcPayload.readUInt32()
        selfDirection = (view shr 16) * shortRotationScale

      }
    }
    30   ->
    {//void ServerMoveDual(float TimeStamp0, const struct FVector_NetQuantize10& InAccel0, unsigned char PendingFlags, uint32_t View0, float TimeStamp, const struct FVector_NetQuantize10& InAccel, const struct FVector_NetQuantize100& ClientLoc, unsigned char NewFlags, unsigned char ClientRoll, uint32_t View, class UPrimitiveComponent* ClientMovementBase, const struct FName& ClientBaseBoneName, unsigned char ClientMovementMode);
      if (rpcPayload.readBit())
      {
        val timeStamp = rpcPayload.readFloat()
      }
      if (rpcPayload.readBit())
      {
        val inAccel = rpcPayload.readVector(10, 24)
      }
      if (rpcPayload.readBit())
      {
        val PendingFlags = rpcPayload.readUInt8()
      }
      if (rpcPayload.readBit())
      {
        val view = rpcPayload.readUInt32()
      }

      if (rpcPayload.readBit())
      {
        val timeStamp = rpcPayload.readFloat()
      }
      if (rpcPayload.readBit())
      {
        val inAccel = rpcPayload.readVector(10, 24)
      }
      if (rpcPayload.readBit())
      {
        val clientLoc = rpcPayload.readVector(100, 30)
        selfCoords.set(clientLoc.x, clientLoc.y, clientLoc.z)
      }
      if (rpcPayload.readBit())
      {
        val compressedMoveFlags = rpcPayload.readUInt8()
      }
      if (rpcPayload.readBit())
      {
        val clientRoll = rpcPayload.readUInt8()
      }
      if (rpcPayload.readBit())
      {
        val view = rpcPayload.readUInt32()
        selfDirection = (view shr 16) * shortRotationScale
      }
    }
    else ->
    {
    }
  }
}