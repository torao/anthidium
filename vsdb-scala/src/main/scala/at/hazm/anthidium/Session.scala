package at.hazm.anthidium

import java.nio.ByteBuffer

import scala.concurrent.Future

trait Session extends AutoCloseable {

  def read(buffer:ByteBuffer, position:Long):Future[Int]

  /**
    * セッションの読み込み位置をリセットしデータストアの先頭ブロックへ移動します。
    */
  def reset():Unit

  /**
    */
  def next():Boolean
}
