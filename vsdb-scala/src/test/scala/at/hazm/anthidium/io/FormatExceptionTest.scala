package at.hazm.anthidium.io

import java.nio.{ByteBuffer, ByteOrder}

import org.scalatest._

class FormatExceptionTest extends FlatSpec with Matchers {

  "FormatException" should "be success all constructor" in {
    val cause = new Throwable()
    new FormatException().getMessage should be(new Exception().getMessage)
    new FormatException().getCause should be(new Exception().getCause)
    new FormatException("A").getMessage should be("A")
    new FormatException("A").getCause should be(new Exception("A").getCause)
    new FormatException("A", cause).getMessage should be("A")
    new FormatException("A", cause).getCause should be(cause)
  }

}