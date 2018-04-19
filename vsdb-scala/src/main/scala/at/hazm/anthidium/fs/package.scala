package at.hazm.anthidium

import java.nio.{ByteBuffer, ByteOrder}

package object fs {

  /**
    * 指定されたサイズの ByteBuffer をリトルエンディアンで新規に作成します。
    *
    * @param size 作成するバッファサイズ
    * @return バッファ
    */
  def newBuffer(size:Int):ByteBuffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN)

  object Size {
    val UINT8:Int = java.lang.Byte.BYTES
    val UINT16:Int = java.lang.Short.BYTES
    val UINT32:Int = java.lang.Integer.BYTES
    val UINT64:Int = java.lang.Long.BYTES
    val FILE_POSITION:Int = UINT64
  }

}
