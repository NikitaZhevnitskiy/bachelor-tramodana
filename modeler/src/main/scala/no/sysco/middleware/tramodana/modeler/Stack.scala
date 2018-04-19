package no.sysco.middleware.tramodana.modeler


class Stack[T](var list: List[T]) {
  def pop: Option[T] = list match {
    case head :: tail =>
      list = tail
      Option(head)
    case Nil => None
  }

  def push(elem: T): Unit = list = elem +: list

  def empty: Boolean = list.isEmpty

  def nonEmpty: Boolean = list.nonEmpty

}

