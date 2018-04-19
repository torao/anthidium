package at.hazm.anthidium.io

/**
  * 任意のデータ列を別の形式に変換するためのトレイトです。データのエンコードとデコードを行います。
  */
trait Codec[PLAIN, ENCODED] {

  def encode(values:Array[PLAIN]):Array[ENCODED]

  def decode(encoded:Array[ENCODED]):Array[PLAIN]

  def +[T](other:Codec[ENCODED, T]):Codec[PLAIN, T] = new Codec[PLAIN, T] {
    override def encode(values:Array[PLAIN]):Array[T] = other.encode(Codec.this.encode(values))

    override def decode(encoded:Array[T]):Array[PLAIN] = Codec.this.decode(other.decode(encoded))
  }
}
