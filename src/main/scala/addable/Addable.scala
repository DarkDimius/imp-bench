package addable

object Addable {

  implicit object IntAddable extends Addable[Int] {
    def add(x: Int, y: Int) = x + y
  }

}

trait Addable[A] {
  def add(x: A, y: A): A
}
