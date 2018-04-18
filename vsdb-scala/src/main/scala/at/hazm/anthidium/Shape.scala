package at.hazm.anthidium

case class Shape(dim:Int*){
  def rank:Int = dim.length
}
