package at.hazm.anthidium.v0

import java.nio.channels.FileChannel
import java.nio.{ByteBuffer, ByteOrder}

import javax.json.JsonObject

class MetaInfo(channel:FileChannel) {

  def init(vectorSize:Int, attributes:JsonObject):Unit = {
    val attrBinary = javaJsonToBinary(attributes)
    val buffer = ByteBuffer.allocate(2 + 2 + 2 + 4 + attrBinary.length)
    if(attrBinary.length > 0xFFFF) {
      throw new IllegalArgumentException(f"attributes too large: ${attrBinary.length}%,dB > ${0xFFFF}%,dB")
    }
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    buffer.put('V'.toByte)
    buffer.put('S'.toByte)
    buffer.putShort(0x01)
    buffer.putShort((attrBinary.length + 4).toShort)
    buffer.putInt(vectorSize)
    buffer.put(attrBinary)

    buffer.flip()

    exclusiveLock {
      channel.position(0)
      channel.write(buffer)
    }
  }

  def exclusiveLock[T](f: => T):T = lock(shared = false)(f)

  def sharedLock[T](f: => T):T = lock(shared = true)(f)

  private[this] def lock[T](shared:Boolean)(f: => T):T = {
    val lock = channel.lock(0, 2, shared)
    try {
      f
    } finally {
      lock.release()
    }
  }

}
