package at.hazm.anthidium.resolution

import java.nio.ByteBuffer

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

/**
  * 空間解像度はベクトル空間の各次元を表す数値の精度を表しています。アプリケーションに対してはあるベクトル空間で使用する数値の型を意味
  * します。解像度によってデータの保存形式やメモリ上での表現が異なります。
  */
trait Resolution {

  /**
    * 指定された多次元配列を出力用のバッファに変換します。
    *
    * @param values 変換する多次元配列
    * @return 出力用のバッファ
    */
  def encode(values:INDArray):ByteBuffer

  /**
    * 指定されたバッファの内容を多次元配列に変換します。
    *
    * @param buffer 読み込むバッファ
    * @param length ベクトルの長さ
    * @return 読み込んだ密ベクトル
    */
  def decode(buffer:ByteBuffer, length:Int):INDArray
}

object Resolution {

  /**
    * 指定された解像度コードに対する解像度を参照します。
    *
    * @param resolution 解像度コード
    * @return 解像度
    */
  def apply(resolution:Int):Resolution = resolution match {
    case 0xD => FLOAT64
  }

  /*
  case object BIT extends Resolution[Byte](0x0) {
    override def readDenseArray(buffer:ByteBuffer, length:Int):Array[Byte] = {
      // 0:0, 1:1, 2:1, ..., 7:1, 8:1, 9:2, ...
      val byteSize = if(length == 0) 0 else (length - 1) / 8 + 1
      val array = new Array[Byte](byteSize)
      buffer.get(array)
      array
    }
  }

  case object INT8 extends Resolution[Byte](0x1) {
    override def readDenseArray(buffer:ByteBuffer, length:Int):Array[Byte] = {
      val array = new Array[Byte](length)
      buffer.get(array)
      array
    }
  }

  case object INT16 extends Resolution[Short](0x2) {
    override def readDenseArray(buffer:ByteBuffer, length:Int):Array[Short] = {
      val array = new Array[Short](length)
      buffer.asShortBuffer().get(array)
      array
    }
  }

  case object INT32 extends Resolution[Int](0x3) {
    override def readDenseArray(buffer:ByteBuffer, length:Int):Array[Int] = {
      val array = new Array[Int](length)
      buffer.asIntBuffer().get(array)
      array
    }
  }

  case object INT64 extends Resolution[Long](0x4) {
    override def readDenseArray(buffer:ByteBuffer, length:Int):Array[Long] = {
      val array = new Array[Long](length)
      buffer.asLongBuffer().get(array)
      array
    }
  }

  case object INT128 extends Resolution[BigInt](0x5) {
    override def readDenseArray(buffer:ByteBuffer, length:Int):Array[BigInt] = {
      val array = new Array[BigInt](length)
      val buf = new Array[Byte](16)
      for(i <- 0 until length) {
        buffer.get(buf)
        bigEndian(buf)
        val signum = if((buf(0) & 0x80) != 0) 1 else -1
        buf(0) &= 0x7F
        array(i) = BigInt(signum, buf)
      }
      array
    }
  }

  case object UINT8 extends Resolution[Byte](0x6) {
    override def readDenseArray(buffer:ByteBuffer, length:Int):Array[Byte] = INT8.readDenseArray(buffer, length)
  }

  case object UINT16 extends Resolution[Short](0x7) {
    override def readDenseArray(buffer:ByteBuffer, length:Int):Array[Short] = INT16.readDenseArray(buffer, length)
  }

  case object UINT32 extends Resolution[Int](0x8) {
    override def readDenseArray(buffer:ByteBuffer, length:Int):Array[Int] = INT32.readDenseArray(buffer, length)
  }

  case object UINT64 extends Resolution[Long](0x9) {
    override def readDenseArray(buffer:ByteBuffer, length:Int):Array[Long] = INT64.readDenseArray(buffer, length)
  }

  case object UINT128 extends Resolution[BigInt](0xA) {
    override def readDenseArray(buffer:ByteBuffer, length:Int):Array[BigInt] = {
      val array = new Array[BigInt](length)
      val buf = new Array[Byte](16)
      for(i <- 0 until length) {
        buffer.get(buf)
        bigEndian(buf)
        array(i) = BigInt(1, buf)
      }
      array
    }
  }

  // Not Implemented
  case object FLOAT16 extends Resolution[Float](0xB) {
    override def readDenseArray(buffer:ByteBuffer, length:Int):Array[Float] = ???
  }

  case object FLOAT32 extends Resolution[Float](0xC) {
    override def readDenseArray(buffer:ByteBuffer, length:Int):Array[Float] = {
      val array = new Array[Float](length)
      buffer.asFloatBuffer().get(array)
      array
    }
  }

  */

  case object FLOAT64 extends Resolution {
    override def encode(values:INDArray):ByteBuffer = {
      ByteBuffer.wrap(values.data().asBytes())
    }

    override def decode(buffer:ByteBuffer, length:Int):INDArray = {
      Nd4j.create(buffer.asDoubleBuffer().array())
    }
  }

  /*

  // Not Implemented
  case object FLOAT80 extends Resolution[Byte](0xE) {
    override def readDenseArray(buffer:ByteBuffer, length:Int):Array[Byte] = ???
  }

  // Not Implemented
  case object FLOAT128 extends Resolution[Byte](0xF) {
    override def readDenseArray(buffer:ByteBuffer, length:Int):Array[Byte] = ???
  }

  */

  private[this] def bigEndian(buffer:Array[Byte]):Unit = {
    for(i <- 0 until buffer.length / 2) {
      val tmp = buffer(i)
      buffer(i) = buffer(buffer.length - i - 1)
      buffer(buffer.length - i - 1) = tmp
    }
  }

}