package at.hazm.anthidium

import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption._
import java.nio.{ByteBuffer, ByteOrder}

import at.hazm.anthidium.io.{FormatException, using}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

class DataStore private[DataStore](val path:Path)(implicit _executionContext:ExecutionContext) {

  private[DataStore] val verify:Future[Short] = {
    exclusiveLock { session =>
      val buffer = ByteBuffer.allocate(DataStore.SignatureLength)
      buffer.order(ByteOrder.LITTLE_ENDIAN)
      session.read(buffer, 0).flatMap { length =>
        buffer.flip()
        if(length < buffer.capacity() || buffer.get() != 'V' || buffer.get() != 'S') {
          session.close()
          Future.failed(throw new FormatException(s"$path: is NOT a vector space data-store file"))
        } else {
          val version = buffer.getShort()
          if(version < 0 || version > DataStore.CurrentVersion) {
            session.close()
            Future.failed(throw new FormatException(s"$path: unsupported data-store version: $version"))
          } else {
            Future.successful(version)
          }
        }
      }
    }
  }

  /**
    * 書き込み非同期処理を開始します。このメソッドの呼び出しはすぐに終了し、ファイルのロックが獲得できた時点で処理 `f` が実行されます。
    *
    * @param f 書き込み処理
    * @tparam T 書き込み処理の結果
    * @return 書き込み処理の結果の Future
    */
  def exclusiveLock[T](f:(Session) => Future[T]):Future[T] = exec(sharedLock = false, f)

  /**
    * 読み込み非同期処理を開始します。このメソッドの呼び出しはすぐに終了し、ファイルのロックが獲得できた時点で処理 `f` が実行されます。
    *
    * @param f 読み込み処理
    * @tparam T 読み込み処理の結果
    * @return 読み込み処理の結果の Future
    */
  def sharedLock[T](f:(Session) => Future[T]):Future[T] = exec(sharedLock = true, f)

  private[this] def exec[T](sharedLock:Boolean, f:(Session) => Future[T]):Future[T] = {
    SessionImpl(this, sharedLock).flatMap { session =>
      using(session) { _ =>
        f(session)
      }
    }
  }

}

object DataStore {
  private[DataStore] val logger = LoggerFactory.getLogger(classOf[DataStore[_]])

  /**
    * このライブラリの対応するデータベースファイルフォーマットバージョン。
    */
  private[anthidium] val CurrentVersion:Short = 0

  private[anthidium] val SignatureLength = 4

  /**
    * 指定されたパスに新規のベクトル空間 DB を作成します。すでにファイルが存在する場合、データベースの作成は失敗します。
    *
    * @param path  作成するデータベースのパス
    * @param shape ベクトル空間の形
    * @return 作成したベクトル空間
    */
  def create(path:Path, shape:Shape)(implicit _ec:ExecutionContext):Future[DataStore] = {
    using(FileChannel.open(path, CREATE, READ, WRITE)) { channel =>
      using(channel.lock(0, SignatureLength, false)) { _ =>
        val buffer = ByteBuffer.allocate(SignatureLength + 1)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        if(channel.size() == 0) {
          buffer.put('V'.toByte)
          buffer.put('S'.toByte)
          buffer.putShort(0)
          buffer.put(Block.Type.Terminator.toByte)
          buffer.flip()
          channel.write(buffer)
          assert(channel.position() == SignatureLength + 1)
          logger.info(s"new data-store create and initialized: $path")
          channel.position(0)
        } else {
          throw new FormatException(s"$path: file exists")
        }
      }
      open(path)(_ec)
    }
  }

  /**
    * 指定されたパスのベクトル空間 DB をオープンします。このメソッドはすぐに終了しますがデータベースの準備は非同期で行われます。
    *
    * @param path オープンするデータベースのパス
    * @return オープンしたデータベース
    */
  def open(path:Path)(implicit _ec:ExecutionContext):Future[DataStore] = {
    val ds = new DataStore(path)(_ec)
    ds.verify.map(_ => ds)
  }
}