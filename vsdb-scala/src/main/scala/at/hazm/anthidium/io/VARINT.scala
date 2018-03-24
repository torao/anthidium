package at.hazm.anthidium.io

import java.nio.ByteBuffer

/**
  * 可変長符号付き整数を操作するためのメソッドを定義します。
  */
object VARINT {

  /**
    * 指定された可変長符号付き整数を保存するために必要なサイズを参照します。
    *
    * @param value サイズを参照する数値
    * @return 可変長符号付き整数としてエンコードしたときのバイトサイズ
    */
  def byteSize(value:BigInt):Int = VARUINT.byteSize(varIntToVarUInt(value))

  /**
    * 指定されたバッファの現在位置から可変長符号付き整数を読み込みます。
    *
    * @param buffer   整数を読み込むバッファ
    * @param bitWidth 読み込む想定の整数の最大ビット幅 (8, 16, 32, 64, ...)
    * @return 読み込んだ整数
    * @throws FormatException 可変長整数のフォーマットが不正な場合
    */
  def read(buffer:ByteBuffer, bitWidth:Int = Int.MaxValue):BigInt = {
    val value = VARUINT.read(buffer, bitWidth)
    if((value & 1) == 0) value >> 1 else -((value + 1) >> 1)
  }

  /**
    * 指定された可変長符号付き整数をバッファに書き込みます。
    *
    * @param buffer 可変長整数を書き込むバッファ
    * @param value  書き込む値
    * @return バッファ
    */
  def write(buffer:ByteBuffer, value:BigInt):ByteBuffer = {
    VARUINT.write(buffer, varIntToVarUInt(value))
  }

  /**
    * 指定された符号付き整数を Zig Zag エンコーディングで符号無し整数に変換します。
    *
    * @param value VARUINT 型に変換する値
    * @return Zig Zag エンコードした VARUINT 値
    */
  private[this] def varIntToVarUInt(value:BigInt):BigInt = if(value < 0) (value.abs << 1) - 1 else value << 1

}
