package at.hazm.anthidium.fs.btree

import java.util.Comparator

class `B+Tree`[KEY, VALUE](comparator:Comparator[KEY], materializer:Materializer[VALUE]) {

  def get(key:KEY):Option[VALUE] = ???

  def add(key:KEY, value:VALUE):Unit = ???

  def delete(key:KEY):Boolean = ???

}
