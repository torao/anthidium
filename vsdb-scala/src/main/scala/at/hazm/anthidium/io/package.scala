package at.hazm.anthidium

import java.nio.ByteBuffer

package object io {

  class UINT16(val value:Int) extends AnyVal

  class UINT32(val value:Long) extends AnyVal

  class UINT64(val value:BigInt) extends AnyVal

  val MaxUINT64 = BigInt("FFFFFFFFFFFFFFFF", 16)

  /**
    */
  implicit class _ByteBuffer(buffer:ByteBuffer) {

    def getVARINT16:Short = VARINT.read(buffer, 16).toShort

    def getVARINT32:Int = VARINT.read(buffer, 32).toInt

    def getVARINT64:Long = VARINT.read(buffer, 64).toLong

    def putVARINT16(value:Short):ByteBuffer = VARINT.write(buffer, BigInt(value))

    def putVARINT32(value:Int):ByteBuffer = VARINT.write(buffer, value)

    def putVARINT64(value:Long):ByteBuffer = VARINT.write(buffer, value)

    /** read variant-length unsigned int16 and return. */
    def getVARUINT16:UINT16 = new UINT16(VARUINT.read(buffer, 16).toInt & 0xFFFF)

    /** read variant-length unsigned int32 and return. */
    def getVARUINT32:UINT32 = new UINT32(VARUINT.read(buffer, 32).toLong & 0xFFFFFFFFL)

    /** read variant-length unsigned int64 and return. */
    def getVARUINT64:UINT64 = new UINT64(VARUINT.read(buffer, 64))

    /** write variant-length unsigned int16. */
    def putVARUINT16(value:UINT16):ByteBuffer = {
      if(value.value < 0 || value.value > 0xFFFF) {
        throw new IllegalArgumentException(s"specified $value is not valid UINT16")
      }
      VARUINT.write(buffer, value.value)
    }

    /** write variant-length unsigned int16. */
    def putVARUINT32(value:UINT32):ByteBuffer = {
      if(value.value < 0 || value.value > 0xFFFFFFFFL) {
        throw new IllegalArgumentException(s"specified $value is not valid UINT32")
      }
      VARUINT.write(buffer, value.value)
    }

    /** write variant-length unsigned int64. */
    def putVARUINT64(value:UINT64):ByteBuffer = {
      if(value.value < 0 || value.value > MaxUINT64) {
        throw new IllegalArgumentException(s"specified $value is not valid UINT64")
      }
      VARUINT.write(buffer, value.value)
    }
  }

}
