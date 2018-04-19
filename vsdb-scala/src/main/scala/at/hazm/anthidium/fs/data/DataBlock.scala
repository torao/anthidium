package at.hazm.anthidium.fs.data

import at.hazm.anthidium.fs.{Block, Size, newBuffer}
import at.hazm.anthidium.io.FormatException

import scala.concurrent.Future

class DataBlock(block:Block, prev:Long, next:Long, items:Int, maxItems:Int) {

}

object DataBlock {
  def apply(block:Block):Future[DataBlock] = {
    val buffer = newBuffer(Size.FILE_POSITION + Size.FILE_POSITION + Size.UINT16 + Size.UINT16)
    block.read(buffer).map { size =>
      if(size != buffer.capacity()) {
        throw new FormatException()
      }
      buffer.flip()
      val prev = buffer.getLong
      val next = buffer.getLong
      val items = buffer.getShort & 0xFFFF
      val maxItems = buffer.getShort & 0xFFFF
      new DataBlock(block, prev, next, items, maxItems)
    }
  }
}