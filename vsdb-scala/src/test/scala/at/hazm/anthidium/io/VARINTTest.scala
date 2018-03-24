package at.hazm.anthidium.io

import java.nio.{ByteBuffer, ByteOrder}

import org.scalatest._

class VARINTTest extends FlatSpec with Matchers {

  private[this] def bigIntToVarUIntWithNormValue(value:BigInt):(Array[Byte], BigInt) = (bigIntToVarUInt(value), value)

  private[this] val VARUINT_SAMPLES = Seq(
    Array(0x00) -> 0L,
    Array(0x7F) -> 127L,
    Array(0x81, 0x00) -> 128L,
    Array(0xFF, 0x7F) -> 16383L,
    Array(0x81, 0x80, 0x00) -> 16384L,
    Array(0xFF, 0xFF, 0x7F) -> 2097151L,
    Array(0x81, 0x80, 0x80, 0x00) -> 2097152L,
    Array(0xFF, 0xFF, 0xFF, 0x7F) -> 268435455L
  ).map(x => (x._1.map(_.toByte), BigInt(x._2))) ++ Seq(
    bigIntToVarUIntWithNormValue(0x7FL),
    bigIntToVarUIntWithNormValue(0x80L),
    bigIntToVarUIntWithNormValue(0xFFL),
    bigIntToVarUIntWithNormValue(0x100L),
    bigIntToVarUIntWithNormValue(0x7FFFL),
    bigIntToVarUIntWithNormValue(0x8000L),
    bigIntToVarUIntWithNormValue(0xFFFFL),
    bigIntToVarUIntWithNormValue(0x10000L),
    bigIntToVarUIntWithNormValue(0x7FFFFFFFL),
    bigIntToVarUIntWithNormValue(0x80000000L),
    bigIntToVarUIntWithNormValue(0xFFFFFFFFL),
    bigIntToVarUIntWithNormValue(0x100000000L),
    bigIntToVarUIntWithNormValue(0x7FFFFFFFFFFFFFFFL),
    bigIntToVarUIntWithNormValue(BigInt("8000000000000000", 16)),
    bigIntToVarUIntWithNormValue(BigInt("FFFFFFFFFFFFFFFF", 16)),
  )

  "ByteBuffer.getVARUINT" should "read variant integer" in {
    // temporary test for bigIntToVarUInt()
    VARUINT_SAMPLES.foreach { case (binary, num) =>
      bin(bigIntToVarUIntWithNormValue(num)._1) should be(bin(binary))
      bigIntToVarUIntWithNormValue(num)._2 should be(num)
    }
    (VARUINT_SAMPLES ++ (0 to 0xFFFF).map(i => bigIntToVarUIntWithNormValue(i)))
      .filter(_._2 <= BigInt(0xFFFF))
      .foreach { case (binary, num) =>
        // System.out.println(f"$num -> ${bin(binary)}")
        val buffer = ByteBuffer.wrap(binary)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        BigInt(buffer.getVARUINT16.value) should be(num)
      }
    VARUINT_SAMPLES
      .filter(_._2 <= 0xFFFFFFFFL)
      .foreach { case (binary, num) =>
        val buffer = ByteBuffer.wrap(binary)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        BigInt(buffer.getVARUINT32.value) should be(num)
      }
    VARUINT_SAMPLES
      .filter(_._2 <= BigInt("FFFFFFFFFFFFFFFF", 16))
      .foreach { case (binary, num) =>
        val buffer = ByteBuffer.wrap(binary)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.getVARUINT64.value should be(num)
      }
  }

  it should "throw FormatException when value exceeds bit-width for type" in {
    Seq(
      bigIntToVarUInt(0x10000) -> "in padding bits",
      bigIntToVarUInt(0x7FFFFFFF) -> "too many byte sequence"
    ).foreach { case (binary, msg) =>
      val buffer = ByteBuffer.wrap(binary)
      buffer.order(ByteOrder.LITTLE_ENDIAN)
      val ex = the[FormatException] thrownBy buffer.getVARUINT16
      ex.getMessage should include(msg)
    }
    Seq(
      bigIntToVarUInt(0x100000000L) -> "in padding bits",
      bigIntToVarUInt(0x7FFFFFFFFFFFL) -> "too many byte sequence"
    ).foreach { case (binary, msg) =>
      val buffer = ByteBuffer.wrap(binary)
      buffer.order(ByteOrder.LITTLE_ENDIAN)
      val ex = the[FormatException] thrownBy buffer.getVARUINT32
      ex.getMessage should include(msg)
    }
    Seq(
      bigIntToVarUInt(BigInt("10000000000000000", 16)) -> "in padding bits",
      bigIntToVarUInt(BigInt("7FFFFFFFFFFFFFFFFFFFFFFF", 16)) -> "too many byte sequence"
    ).foreach { case (binary, msg) =>
      val buffer = ByteBuffer.wrap(binary)
      buffer.order(ByteOrder.LITTLE_ENDIAN)
      val ex = the[FormatException] thrownBy buffer.getVARUINT64
      ex.getMessage should include(msg)
    }
  }

  "ByteBuffer.putVARUINT" should "write variant integer" in {
    VARUINT_SAMPLES.filter(_._2 <= 0xFFFF).foreach { case (binary, num) =>
      val buffer = ByteBuffer.allocate(binary.length)
      buffer.order(ByteOrder.LITTLE_ENDIAN)
      buffer.putVARUINT16(new UINT16(num.toInt)).array() should be(binary)
    }
    VARUINT_SAMPLES.filter(_._2 <= 0xFFFFFFFFL).foreach { case (binary, num) =>
      val buffer = ByteBuffer.allocate(binary.length)
      buffer.order(ByteOrder.LITTLE_ENDIAN)
      buffer.putVARUINT32(new UINT32(num.toLong)).array() should be(binary)
    }
    VARUINT_SAMPLES.filter(_._2 <= MaxUINT64).foreach { case (binary, num) =>
      val buffer = ByteBuffer.allocate(binary.length)
      buffer.order(ByteOrder.LITTLE_ENDIAN)
      buffer.putVARUINT64(new UINT64(num)).array() should be(binary)
    }
  }

  val VARINT2VARUINT:Seq[(BigInt, BigInt)] = {
    def bound(b0:BigInt, b1:BigInt):Seq[(BigInt, BigInt)] = Seq(
      (b0 - 1L, ((b0 - 1).abs * 2L) - 1),
      (b0.toLong, (b0.abs * 2L) - 1),
      (b0 + 1L, ((b0 + 1).abs * 2L) - 1),
      (b1 - 1L, (b1 - 1) * 2L),
      (b1.toLong, b1 * 2L),
      (b1 + 1L, (b1 + 1) * 2L)
    )

    Seq(
      (0, 0), (-1, 1), (1, 2), (-2, 3), (2, 4)
    ).map(x => (BigInt(x._1), BigInt(x._2))) ++ Seq(
      (2147483647L, 4294967294L), (-2147483648L, 4294967295L)
    ).map(x => (BigInt(x._1), BigInt(x._2))) ++
      bound(BigInt(Byte.MinValue), BigInt(Byte.MaxValue)) ++
      bound(BigInt(Short.MinValue), BigInt(Short.MaxValue)) ++
      bound(Int.MinValue, Int.MaxValue) ++
      bound(Long.MinValue, Long.MaxValue)
  }

  "ByteBuffer.putVARINT" should "write ZigZag encoding of Protocol Buffers as variant signed integer" in {
    VARINT2VARUINT.filter { t =>
      t._1 >= BigInt(Short.MinValue) && t._1 <= BigInt(Short.MaxValue)
    }.foreach { case (varint, varuint) =>
      val expected = bigIntToVarUInt(varuint)
      val actual = ByteBuffer.allocate(expected.length).putVARINT16(varint.toShort).array()
      bin(actual) should be(bin(expected))
    }
    VARINT2VARUINT.filter { t =>
      t._1 >= Int.MinValue && t._1 <= Int.MaxValue
    }.foreach { case (varint, varuint) =>
      val expected = bigIntToVarUInt(varuint)
      val actual = ByteBuffer.allocate(expected.length).putVARINT32(varint.toInt).array()
      bin(actual) should be(bin(expected))
    }
    VARINT2VARUINT.filter { t =>
      t._1 >= Long.MinValue && t._1 <= Long.MaxValue
    }.foreach { case (varint, varuint) =>
      val expected = bigIntToVarUInt(varuint)
      val actual = ByteBuffer.allocate(expected.length).putVARINT64(varint.toLong).array()
      bin(actual) should be(bin(expected))
    }
  }

  "ByteBuffer.getVARINT" should "read ZigZag encoding of Protocol Buffers as variant signed integer" in {
    val VARINT_SAMPLE = VARINT2VARUINT.map(_._1)
    VARINT_SAMPLE.map(_ < 0) should contain(true)
    VARINT_SAMPLE.filter { t =>
      t >= BigInt(Short.MinValue) && t <= BigInt(Short.MaxValue)
    }.foreach { varint =>
      val buffer = ByteBuffer.allocate(64 + 64 / 7 + 1)
      buffer.putVARINT16(varint.toShort)
      buffer.flip()
      BigInt(buffer.getVARINT16) should be(varint)
    }
    VARINT_SAMPLE.filter { t =>
      t >= BigInt(Int.MinValue) && t <= BigInt(Int.MaxValue)
    }.foreach { varint =>
      val buffer = ByteBuffer.allocate(64 + 64 / 7 + 1)
      buffer.putVARINT32(varint.toInt)
      buffer.flip()
      BigInt(buffer.getVARINT32) should be(varint)
    }
    VARINT_SAMPLE.filter { t =>
      t >= BigInt(Long.MinValue) && t <= BigInt(Long.MaxValue)
    }.foreach { varint =>
      val buffer = ByteBuffer.allocate(64 + 64 / 7 + 1)
      buffer.putVARINT64(varint.toLong)
      buffer.flip()
      BigInt(buffer.getVARINT64) should be(varint)
    }
  }

  "VARINT/VARUINT" should "be success" in {
    val empty = ByteBuffer.allocate(0)
    an[IllegalArgumentException] should be thrownBy VARUINT.byteSize(-1)
    an[IllegalArgumentException] should be thrownBy VARUINT.write(empty, -1)
    VARUINT_SAMPLES.foreach { case (binary, expected) =>
      VARUINT.byteSize(expected) should be(binary.length)

      val buffer = ByteBuffer.allocate(VARUINT.byteSize(expected))
      VARUINT.write(buffer, expected)
      buffer.flip()
      VARUINT.byteSize(expected) should be(buffer.limit())
      VARUINT.read(buffer) should be(expected)
    }
    VARINT2VARUINT.foreach { case (expected, _) =>
      val buffer = ByteBuffer.allocate(VARINT.byteSize(expected))
      VARINT.write(buffer, expected)
      buffer.flip()
      VARINT.byteSize(expected) should be(buffer.limit())
      VARINT.read(buffer) should be(expected)
    }
  }

  private[this] def bigIntToVarUInt(value:BigInt):Array[Byte] = {
    val bytes = value.toByteArray.flatMap { b =>
      (0 until 8).map(i => (b >>> (7 - i)) & 1)
    }.dropWhile(_ == 0).reverse.grouped(7).toSeq.reverse.map { rbits =>
      (0 to 7).map { i =>
        if(i < rbits.length) {
          rbits(i) << i
        } else 0
      }.reduceLeft(_ | _).toByte
    }
    val bytes2 = bytes.dropRight(1).map(b => (b | 0x80).toByte) ++ bytes.takeRight(1)
    if(bytes2.isEmpty) Array[Byte](0x00) else bytes2.toArray
  }

  private[this] def bin(b:Iterable[Byte]):String = b.map { num =>
    val s = (num & 0xFF).toByte.toBinaryString.takeRight(8)
    "0" * (8 - s.length) + s
  }.mkString(" ")
}