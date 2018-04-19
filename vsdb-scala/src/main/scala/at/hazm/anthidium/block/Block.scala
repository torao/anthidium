package at.hazm.anthidium.block

import java.nio.channels.AsynchronousFileChannel
import java.nio.{ByteBuffer, ByteOrder}

import at.hazm.anthidium.io
import at.hazm.anthidium.io.{FormatException, UINT32, _ByteBuffer}

import scala.concurrent.{ExecutionContext, Future, Promise}

class Block private[Block](channel:AsynchronousFileChannel, position:Long, val `type`:Block.Type, val length:Long)(implicit _ec:ExecutionContext) {
  if(length < 0 || length > io.MaxUINT32) {
    throw new IllegalArgumentException(s"unexpected block size: $length")
  }

  def read(buffer:ByteBuffer, offset:Int):Future[Int] = Block.read(channel, buffer, position + offset)
}


object Block {

  sealed abstract class Type(val id:Char)

  object Type {

    case object MetaInfo extends Type('^')

    case object Terminator extends Type('$')

    case class Unknown(override val id:Char) extends Type(id)

    private[this] val values = Seq(
      MetaInfo, Terminator
    )

    def apply(signature:Char):Type = values.find(_.id == signature).getOrElse(Unknown(signature))
  }

  /**
    * 指定された非同期チャネルの位置からブロックを読み込みます。
    *
    * @param channel  ブロックを読み込むチャネル
    * @param position ブロックの開始位置
    * @param _ec      非同期実行スレッド
    * @return 読み込んだブロック
    */
  def read(channel:AsynchronousFileChannel, position:Long)(implicit _ec:ExecutionContext):Future[Block] = {
    val buffer = ByteBuffer.allocate(1 + 4)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    read(channel, buffer, position).flatMap { size =>
      buffer.flip()
      if(size == 1 && buffer.get() == Type.Terminator.id) {
        Future.successful(new Block(channel, position, Type.Terminator, new UINT32(0)))
      } else if(size >= 2) {
        val `type` = Type((buffer.get() & 0xFF).toChar)
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
      case t => promise.complete(t.map(_.toInt))
    })
    promise.future
  }

}
