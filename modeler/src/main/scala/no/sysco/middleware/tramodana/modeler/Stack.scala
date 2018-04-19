package no.sysco.middleware.tramodana.modeler


class Stack[T](var list: List[T]) {

  def this()= this(List.empty)
  def this(elem: T) = this(List(elem))


  def peek: Option[T] = list match {
    case head :: _ => Option(head)
    case Nil => None
  }

  def pop: Option[T] = list match {
    case head :: tail =>
      list = tail
      Option(head)
    case Nil => None
  }

  def push(elem: T): Unit = list = elem +: list

  def pushAll(elems: List[T]): Unit = list = elems ::: list

  def empty: Boolean = list.isEmpty

  def nonEmpty: Boolean = list.nonEmpty

}

