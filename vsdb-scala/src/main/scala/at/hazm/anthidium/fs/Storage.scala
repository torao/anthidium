package at.hazm.anthidium.fs

import at.hazm.anthidium.fs
import at.hazm.anthidium.io.FormatException

import scala.concurrent.{ExecutionContext, Future}

class Storage private[fs](val version:Int, file:DataFile, factory:StorageFactory)(implicit val _ctx:ExecutionContext) {

  /** このストレージのヘッダシグネチャー。 */
  val signature:String = factory.signature

  /**
    * このストレージを非同期でクローズします。読み込みまたは書き込みロックを行っている処理が存在する場合はそれらが終了するまで待機します。
    *
    * @return クローズの完了を通知する Future
    */
  def close():Future[Unit] = exclusiveLock(file.close())

  /**
    * このストレージ全体に対して排他ロックを獲得します。
    *
    * @param f ロックを獲得した状態で行う処理
    * @tparam T 処理結果の型
    * @return 処理結果
    */
  def exclusiveLock[T](f: => T):Future[T] = lock(0, factory.headerSize, shared = false)(f)

  /**
    * このストレージ全体に対して共有ロックを獲得します。
    *
    * @param f ロックを獲得した状態で行う処理
    * @tparam T 処理結果の型
    * @return 処理結果
    */
  def sharedLock[T](f: => T):Future[T] = lock(0, factory.headerSize, shared = true)(f)

  /**
    * このストレージの指定された領域のロックを獲得します。
    *
    * @param position ロックの開始位置
    * @param size     ロックする領域のサイズ
    * @param shared   共有ロックの場合 true
    * @param f        ロックを獲得した状態で行う処理
    * @tparam T 処理結果の型
    * @return 処理結果
    */
  def lock[T](position:Long, size:Long, shared:Boolean)(f: => T):Future[T] = file.criticalSection(position, size, shared)(f)

  /**
    * このストレージ内の指定された位置のブロックをロック付きで参照します。
    *
    * @param position 参照するブロックの位置
    * @param shared   共有ロックの場合 true
    * @return 指定された位置のブロック
    */
  def get(position:Long, shared:Boolean = true):Future[Block] = file.lock(position, 1, shared).flatMap { lock =>
    val buffer = newBuffer(java.lang.Byte.BYTES + Integer.BYTES)
    file.read(buffer, position).map { length =>
      if(length >= 1) {
        buffer.flip()
        val `type` = buffer.get().toChar
        if(`type` == Block.Signature.Terminator) {
          new fs.Block(lock, this, file, position, Block.Signature.Terminator, 0)
        } else if(length == buffer.capacity()) {
          val size = buffer.getInt()
          new fs.Block(lock, this, file, position, `type`, size)
        } else {
          throw new FormatException()
        }
      } else {
        throw new FormatException()
      }
    }
  }

}