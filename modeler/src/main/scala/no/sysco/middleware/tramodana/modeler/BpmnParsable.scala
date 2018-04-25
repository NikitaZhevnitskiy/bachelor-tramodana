package no.sysco.middleware.tramodana.modeler

trait BpmnParsable{
  def getParentId: String
  def setParentId(id: String): BpmnParsable
  def getChildren: List[BpmnParsable]
  def getOperationName: String
  def getProcessId: String
  def printPretty: Unit = printPrettyIter("",true)
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
