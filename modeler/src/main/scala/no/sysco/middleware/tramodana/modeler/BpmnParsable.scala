package no.sysco.middleware.tramodana.modeler

//TODO: turn generic to allow self-recursion
//c.f.:
// http://www.alessandrolacava.com/blog/scala-self-recursive-types/
// example:
/*    trait Doubler[T <: Doubler[T]] {
          def double: T
      }
*/
trait BpmnParsable{
  type T <: BpmnParsable
  def getParentId: String
  def setParentId(id: String): T
  def getProcessId: String
  def setProcessId(id:String): T
  def getOperationName: String
  def getChildren: List[T]
  def addChild(node: T): T
  def addChildren(nodes: List[T]): T
  def printPretty(): Unit = printPrettyIter("",last = true)
  private def printPrettyIter(ind: String, last: Boolean): Unit = {
    var indent = ind
    print(indent)

    if(last) {
      print("\\-")
      indent ++= "  "
    } else {
      print("|-")
      indent ++= "| "
    }
    println(getProcessId)
    for( i <- getChildren.indices)
    {
      getChildren(i).printPrettyIter(indent, i == (getChildren.size -1) )
    }
  }
}
