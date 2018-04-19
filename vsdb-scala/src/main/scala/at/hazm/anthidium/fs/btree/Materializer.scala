package at.hazm.anthidium.fs.btree

import java.nio.ByteBuffer

trait Materializer[T] {

  def writeTo(value:T, buffer:ByteBuffer):Int

  def readFrom(buffer:ByteBuffer):T

}
