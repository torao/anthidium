package at.hazm.anthidium.io

import java.nio.channels.AsynchronousFileChannel
import java.nio.{ByteBuffer, ByteOrder}

import at.hazm.anthidium.{Shape, io}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

class Block(channel:AsynchronousFileChannel, val position:Long, val `type`:Char, val size:UINT32)(implicit _ec:ExecutionContext) {

  def read(buffer:ByteBuffer, offset:Int):Future[Int] = Block.read(channel, buffer, position + offset)

  def next():Future[Block] = Block(channel, position + 1 + 4 + size.value)

}

object Block {

  object Type {
    val MetaInfo = '^'
    val Terminator = '$'
  }

  def apply(channel:AsynchronousFileChannel, position:Long)(implicit _ec:ExecutionContext):Future[Block] = {
    val buffer = ByteBuffer.allocate(1 + 4)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    read(channel, buffer, position).flatMap { size =>
      buffer.flip()
      if(size == 1 && buffer.get() == Type.Terminator) {
        Future.successful(new Block(channel, position, Type.Terminator, new UINT32(0)))
      } else if(size >= 2) {
        val `type` = (buffer.get() & 0xFF).toChar
        val length = buffer.getUINT32
        Future.successful(new Block(channel, position, `type`, length))
      } else {
        Future.failed(new FormatException(s"premature end of file: cannot read block header @$position"))
      }
    }
  }


  def read(channel:AsynchronousFileChannel, buffer:ByteBuffer, position:Long):Future[Int] = {
    val promise = Promise[Int]()
    channel.read(buffer, position, (), io.onComplete[Integer, Unit] {
      case Success(length) => promise.success(length)
      case Failure(ex) => promise.failure(ex)
    })
    promise.future
  }

}
