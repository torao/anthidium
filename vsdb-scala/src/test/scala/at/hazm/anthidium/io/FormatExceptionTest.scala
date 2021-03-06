package at.hazm.anthidium.io

import org.scalatest._

class FormatExceptionTest extends FlatSpec with Matchers {

  "FormatException" should "be success all constructor" in {
    val cause = new Throwable()
    new FormatException().getMessage should be(new Exception().getMessage)
    new FormatException().getCause should be(new Exception().getCause)
    new FormatException("A").getMessage should be("A")
    new FormatException("A").getCause should be(new Exception("A").getCause)
    new FormatException(cause).getMessage should be(new Exception(cause).getMessage)
    new FormatException(cause).getCause should be(new Exception(cause).getCause)
    new FormatException("A", cause).getMessage should be("A")
    new FormatException("A", cause).getCause should be(cause)
  }

}