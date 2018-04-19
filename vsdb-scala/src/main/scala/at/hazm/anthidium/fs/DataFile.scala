package at.hazm.anthidium.fs

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousFileChannel, CompletionHandler, FileLock}

import at.hazm.anthidium.io.using

import scala.concurrent.{ExecutionContext, Future, Promise}

/**
  * Java 標準の非同期ファイルチャネル AsynchronousFileChannel の Scala アダプタです。
  *
  * @param channel 非同期ファイルチャネル
  * @param _ctx    非同期処理のコンテキスト
  */
class DataFile(channel:AsynchronousFileChannel)(implicit _ctx:ExecutionContext) {

  /**
    * このファイルのサイズを参照します。
    *
    * @return ファイルサイズ
    */
  def size:Long = channel.size()

  /**
    * 非同期チャンネルの指定された領域に対してロックを確保します。
    *
    * @param position ロックする領域の開始位置
    * @param size     ロックする領域サイズ
    * @param shared   共有ロックをかける場合 true
    * @tparam T 処理結果の型
    * @return 処理結果
    */
  def lock[T](position:Long, size:Long, shared:Boolean):Future[FileLock] = {
    async[FileLock](handler => channel.lock(position, size, shared, (), handler))
  }

  /**
    * 非同期チャンネルの指定された領域に対してロックを確保します。
    *
    * @param position ロックする領域の開始位置
    * @param size     ロックする領域サイズ
    * @param shared   共有ロックをかける場合 true
    * @param f        ロックをかけた状態で行う処理
    * @tparam T 処理結果の型
    * @return 処理結果
    */
  def criticalSection[T](position:Long, size:Long, shared:Boolean)(f: => T):Future[T] = {
    lock(position, size, shared).map { lock =>
      using(lock)(_ => f)
    }
  }

  /**
    * 非同期チャネルの全領域に対して排他ロックを確保します。
    *
    * @param f ロックをかけた状態で行う処理
    * @tparam T 処理結果の型
    * @return 処理結果
    */
  def criticalSection[T](f: => T):Future[T] = criticalSection(0L, Long.MaxValue, shared = false)(f)

  /**
    * 指定されたバッファにデータを読み込みます。
    *
    * @param buffer   読み込むバッファ
    * @param position 読み込む位置
    * @return 読み込んだバイトサイズ
    */
  def read(buffer:ByteBuffer, position:Long):Future[Int] = {
    async[java.lang.Integer](handler => channel.read(buffer, position, (), handler)).map(_.toInt)
  }

  /**
    * 指定されたバッファのデータを書き込みます。
    *
    * @param buffer   書き込むバッファ
    * @param position 書き込む位置
    * @return 書き込んだバイトサイズ
    */
  def write(buffer:ByteBuffer, position:Long):Future[Int] = {
    async[java.lang.Integer](handler => channel.write(buffer, position, (), handler)).map(_.toInt)
  }

  /**
    * このファイルをクローズします。
    */
  def close():Unit = channel.close()

  private[this] def async[T](f:(CompletionHandler[T, Unit]) => Unit):Future[T] = {
    val promise = Promise[T]()
    f(new CompletionHandler[T, Unit] {
      override def completed(result:T, attachment:Unit):Unit = promise.success(result)

      override def failed(ex:Throwable, attachment:Unit):Unit = promise.failure(ex)

    })
    promise.future
  }
}
