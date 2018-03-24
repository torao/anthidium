package at.hazm.anthidium

import at.hazm.anthidium.{logger => SystemLog}
import javax.json._
import org.msgpack.MessagePack

import scala.collection.JavaConverters._

package object v0 {

  def javaJsonToBinary(obj:JsonObject):Array[Byte] = jsonToBinary(obj) {
    case JsonValue.NULL => null
    case JsonValue.TRUE => true
    case JsonValue.FALSE => false
    case num:JsonNumber =>
      num.numberValue() match {
        case i:java.lang.Byte => i.byteValue()
        case i:java.lang.Short => i.shortValue()
        case i:java.lang.Integer => i.intValue()
        case i:java.lang.Long => i.longValue()
        case f:java.lang.Float => f.floatValue()
        case f:java.lang.Double => f.doubleValue()
        case i:java.math.BigInteger => i
        case i:java.math.BigDecimal =>
          SystemLog.warn(s"converted from BigDecimal to FLOAT64, the precision may be degraded: $i -> ${i.doubleValue()}")
          i.doubleValue()
        case i =>
          throw new IllegalArgumentException(s"unsupported json-number type: $i (${i.getClass.getSimpleName})")
      }
    case str:JsonString => str.getString
    case arr:JsonArray => arr.values().asScala
    case map:JsonObject =>
      map.entrySet().asScala.map(e => e.getKey -> e.getValue).toMap
    case unexpected =>
      throw new IllegalStateException(s"unexpected value: $unexpected (${unexpected.getClass.getSimpleName})")
  }

  private[this] def jsonToBinary[T](json:T)(jsonToScala:(T) => Any):Array[Byte] = {
    val msgpack = new MessagePack()
    val packer = msgpack.createBufferPacker()

    def write(value:T):Unit = jsonToScala(value) match {
      case null => packer.writeNil()
      case b:Boolean => packer.write(b)
      case i:Byte => packer.write(i)
      case i:Short => packer.write(i)
      case i:Int => packer.write(i)
      case i:Long => packer.write(i)
      case f:Float => packer.write(f)
      case f:Double => packer.write(f)
      case i:BigInt => packer.write(i)
      case i:BigDecimal =>
        SystemLog.warn(s"converted from BigDecimal to FLOAT64, the precision may be degraded: $i -> ${i.doubleValue()}")
        packer.write(i.doubleValue())
      case t:String => packer.write(t)
      case arr:Iterable[_] =>
        packer.writeArrayBegin(arr.size)
        arr.foreach { case value:T =>
          write(value)
        }
        packer.writeArrayEnd(true)
      case map:Map[_, _] =>
        packer.writeMapBegin(map.size)
        map.foreach { case (key:String, value:T) =>
          packer.write(key)
          write(value)
        }
        packer.writeMapEnd(true)
      case unexpected =>
        throw new IllegalStateException(s"unexpected value: $unexpected (${unexpected.getClass.getSimpleName})")
    }

    write(json)
    packer.toByteArray
  }

}
