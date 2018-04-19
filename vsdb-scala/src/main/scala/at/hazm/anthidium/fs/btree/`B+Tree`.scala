package at.hazm.anthidium.fs.btree

import java.util.Comparator

class `B+Tree`[KEY, VALUE](comparator:Comparator[KEY], materializer:Materializer[VALUE]) {

  def get(key:KEY):Option[VALUE] = ???

  def add(key:KEY, value:VALUE):Unit = ???

  def delete(key:KEY):Boolean = ???

}

object `B+Tree` {

  sealed trait Node[KEY]

  class Branch[T](n:Int, comparator:Comparator[T]) extends Node[T] {
    private[this] var i:Int = 0
    private[this] val branch = new Array[Long](n + 1)
    private[this] val boundary = new Array[Long](n)
  }

  class Leaf[KEY, VALUE](n:Int, comparator:Comparator[KEY]) extends Node[KEY](n, comparator) {

  }

}
