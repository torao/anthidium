package at.hazm.anthidium.fs

import java.nio.channels.AsynchronousFileChannel
import java.nio.charset.StandardCharsets

import at.hazm.anthidium.io.FormatException

import scala.concurrent.{ExecutionContext, Future}

abstract class StorageFactory(val signature:String, val version:Short) {

  /** ストレージシグネチャのバイト配列表現 */
  private[this] val sigBytes = signature.getBytes(StandardCharsets.UTF_8)

  /** ヘッダのバイトサイズ */
  val headerSize:Int = sigBytes.length + Size.UINT16

  /**
    * 指定された非同期ファイルチャネルを使用するストレージを構築します。このメソッドの Future が成功した場合、チャネルのクローズは
    * ストレージのクローズによって行う必要があります。
    *
    * @param channel 非同期チャネル
    * @param _ctx    非同期処理のためのコンテキスト
    * @return ストレージ
    */
  def build(channel:AsynchronousFileChannel)(implicit _ctx:ExecutionContext):Future[Storage] = {
    build(new DataFile(channel))
  }

  /**
    * 指定された非同期ファイルを使用するストレージを構築します。このメソッドの Future が成功した場合、チャネルのクローズはストレージの
    * クローズによって行う必要があります。
    *
    * @param file 非同期ファイル
    * @param _ctx 非同期処理のためのコンテキスト
    * @return ストレージ
    */
  private[this] def build(file:DataFile)(implicit _ctx:ExecutionContext):Future[Storage] = {
    file.criticalSection(0, headerSize, shared = false) {
      val fileSize = file.size
      if(fileSize == 0) {
        // 新規データファイルの作成
        val buffer = newBuffer(headerSize)
        buffer.put(sigBytes)
        buffer.putShort(version)
        buffer.flip()
        file.write(buffer, 0).flatMap { size =>
          init(file, size).map { _ =>
            new Storage(this.version & 0xFFFF, file, this)
          }
        }
      } else if(fileSize < headerSize) {
        throw new FormatException(s"premature end of file: cannot read file signature: ${fileSize}B length")
      } else {
        val buffer = newBuffer(headerSize)
        file.read(buffer, 0).map { size =>
          if(size < buffer.capacity()) {
            throw new FormatException(s"premature end of file: cannot read file signature: ${size}B length")
          } else {
            buffer.flip()
            val sig = new Array[Byte](this.sigBytes.length)
            buffer.get(sig)
            val version = buffer.getShort & 0xFFFF
            if(!sig.sameElements(this.sigBytes)) {
              throw new FormatException(s"invalid header signature: ${sig.map(x => f"$x%02X").mkString(" ")}")
            }
            new Storage(version, file, this)
          }
        }
      }
    }.flatten
  }

  /**
    * 指定されたファイルを初期状態のデータファイルにします。サブクラスはこのメソッドをオーバーライドして初期状態のブロックを追加することが
    * できます。
    * デフォルトのメソッドは指定された位置に終端ブロックを書き込みます。
    *
    * @param file     初期化するファイル
    * @param position 書き込む位置
    * @param _ctx     非同期処理のためのコンテキスト
    * @return 処理結果
    */
  protected def init(file:DataFile, position:Long)(implicit _ctx:ExecutionContext):Future[Unit] = {
    val buffer = newBuffer(java.lang.Byte.BYTES)
    buffer.put(Block.Signature.Terminator.toByte)
    buffer.flip()
    file.write(buffer, position).map(_ => ())
  }

}
