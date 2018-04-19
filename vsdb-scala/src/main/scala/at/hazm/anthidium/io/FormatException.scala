package at.hazm.anthidium.io

/**
  * An exception when application retrieve a broken data from database.
  */
class FormatException(message:String, ex:Throwable) extends Exception(message, ex) {
  def this(message:String) = this(message, null)

  def this(ex:Throwable) = this(ex.toString, ex)

  def this() = this(null, null)
}
