package at.hazm.anthidium.fs

import java.nio.ByteBuffer
import java.nio.channels.FileLock

import at.hazm.anthidium.fs.Block._

import scala.concurrent.{ExecutionContext, Future}

class Block(lock:FileLock, storage:Storage, file:DataFile, position:Long, val `type`:Char, val length:Int) extends AutoCloseable {

  protected implicit val _ctx:ExecutionContext = storage._ctx

  /** 次のブロックの開始位置 */
  def nextPosition:Long = position + TypeLengthSize + length

  /**
    * ペイロードの指定された位置から指定されたバッファへデータを読み込みます。
    *
    * @param buffer 読み込みに使用するバッファ
    * @param offset 読み込む位置
    * @return 読み込んだサイズ
    */
  def read(buffer:ByteBuffer, offset:Int = 0):Future[Int] = {
    if(buffer.remaining() > length - offset){
      throw new IllegalArgumentException(s"buffer overrun: $offset @$position[$`type`]$length")
    }
    file.read(buffer, position + TypeLengthSize + (offset & 0xFFFFFFFFFL))
  }

  /**
    * このブロックのロックを解放します。
    */
  override def close():Unit = lock.release()
}

object Block {

  private[Block] val TypeLengthSize = Size.UINT8 + Size.UINT32

  object Signature {

    /** 終端ブロックを示す識別子 */
    val Terminator = '$'

    /** ガーベッジブロックを示す識別子 */
    val Garbage = '='
  }

}