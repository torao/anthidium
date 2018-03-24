package at.hazm.anthidium.io

import java.nio.ByteBuffer

import scala.annotation.tailrec

/**
  * 可変長符号なし整数を操作するためのメソッドを定義します。
  */
object VARUINT {

  /**
    * 指定された可変長符号無し整数を保存するために必要なサイズを参照します。
    *
    * @param value サイズを参照する数値
    * @return 可変長符号無し整数としてエンコードしたときのバイトサイズ
    * @throws IllegalArgumentException 負の値を指定した場合
    */
  def byteSize(value:BigInt):Int = {
    if(value < 0) {
      throw new IllegalArgumentException(s"specified $value is not valid UINT")
    }
    (value.bitLength - 1) / 7 + 1
  }

  /**
    * 指定されたバッファの現在位置から可変長符号なし整数を読み込みます。
    *
    * @param buffer   整数を読み込むバッファ
    * @param bitWidth 読み込む想定の整数の最大ビット幅 (8, 16, 32, 64, ...)
    * @return 読み込んだ整数
    * @throws FormatException 可変長整数のフォーマットが不正な場合
    */
  def read(buffer:ByteBuffer, bitWidth:Int = Int.MaxValue):BigInt = {
    val pos = buffer.position()

    @tailrec
    def _getVarInt(value:BigInt, filledBits:Int):BigInt = {
      val b = buffer.get()
      val newValue = (value << 7) | (b & 0x7F)
      if((b & 0x80) == 0) {
        // verify overflow to the type
        val padding = newValue >> bitWidth
        if(padding != 0) {
          throw new FormatException(
            f"VARUINT$bitWidth(${hex(buffer, pos)}) @$pos%,d exceeds its bit-width for type:" +
              f" this contains non-zero 0x$padding%02X in padding bits")
        }
        newValue
      } else {
        val nextFilledBits = filledBits + 7
        if(filledBits + 7 >= bitWidth) {
          throw new FormatException(
            f"VARUINT$bitWidth(${hex(buffer, pos)}) @$pos%,d exceeds its bit-width for type:" +
              f" too many byte sequence")
        }
        _getVarInt(newValue, nextFilledBits)
      }
    }

    _getVarInt(0L, 0)
  }

  /**
    * 指定されたバッファに値を書き込みます。
    *
    * @param buffer バッファ
    * @param value  書き込む値
    */
  def write(buffer:ByteBuffer, value:BigInt):ByteBuffer = {
    if(value < 0) {
      throw new IllegalArgumentException(s"specified $value is not valid UINT")
    }

    @tailrec
    def _write(bitOffset:Int):Unit = {
      val b = ((value >> bitOffset) & 0x7F).toByte
      if(bitOffset == 0) {
        buffer.put(b)
      } else {
        buffer.put((b | 0x80).toByte)
        _write(bitOffset - 7)
      }
    }

    val bitLength = value.bitLength
    val initOffset = if(bitLength == 0) 0 else (bitLength - 1) / 7 * 7
    _write(initOffset)
    buffer
  }

  /**
    * 指定されたバッファの開始位置から現在の位置の直前までの 16 進数ダンプ文字列を参照します。これは例外発生時の問題分析を目的として
    * います。
    *
    * @param buffer バッファ
    * @param begin  ダンプの開始位置
    * @return 16進数文字列
    */
  private[this] def hex(buffer:ByteBuffer, begin:Int):String = {
    val end = buffer.position()
    (begin until end).map(i => f"${buffer.get(i) & 0xFF}%02X").mkString(" ")
  }

}
