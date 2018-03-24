package at.hazm.anthidium.v0

import java.nio.charset.StandardCharsets
import java.nio.{ByteBuffer, ByteOrder}

import javax.json.JsonObject

object Header {
  private[this] val Signature = "VS".getBytes(StandardCharsets.US_ASCII)
  private[this] val Version:Short = 0x01

  def init(vectorSize:Int, attributes:JsonObject)(writer:(ByteBuffer)=>Unit):Unit = {
    val binary = javaJsonToBinary(attributes)
    val buffer = ByteBuffer.allocate(Signature.length + 2 + 2 + 4 + binary.length)
    if(binary.length > 0xFFFF) {
      throw new IllegalArgumentException(f"attributes too large: ${binary.length}%,dB > ${0xFFFF}%,dB")
    }
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    buffer.put(Signature)
    buffer.putShort(Version)
    buffer.putShort((binary.length + 4).toShort)
    buffer.putInt(vectorSize)
    buffer.put(binary)

    buffer.flip()

    writer(buffer)
  }
}