package at.hazm.anthidium.io.codec

import java.nio.{ByteBuffer, ByteOrder}

import at.hazm.anthidium.io.Codec

object DoubleCodec extends Codec[Double, Byte] {
  override def encode(values:Array[Double]):Array[Byte] = {
    val buffer = ByteBuffer.allocate(values.length * 8)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    for(i <- values.indices) {
      buffer.putDouble(values(i))
    }
    buffer.array()
  }

  override def decode(encoded:Array[Byte]):Array[Double] = {
    ByteBuffer.wrap(encoded).order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer().array()
  }

}
