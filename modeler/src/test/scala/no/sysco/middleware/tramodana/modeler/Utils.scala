package no.sysco.middleware.tramodana.modeler

object Utils {
 var counter : Int = 1
    def createNode(parentId: String, op: String = "op", procId: String = "proc", children: List[Node] = Nil): Node ={
      val id = counter.toString
      counter += 1
        new Node(
          //op match { case "op" => op ++ id case _ => op},
          op,
          //procId match {case "proc" => procId ++ id case _ => procId },
          procId,
          children,
          parentId
        )
    }


}
case class Node(operationName: String,
                  processId: String,
                  children: List[Node],
                  parentId: String) extends BpmnParsable with NodeUtils {
    override def getChildren: List[BpmnParsable] = children
    override def getParentId: String = parentId
    override def setParentId(id: String): BpmnParsable = copy(parentId = id)
    override def getOperationName: String = operationName
    override def getProcessId: String = processId

  override def addChild(node: Node): Node = this.copy( children = node :: children)
  override def addChildren(nodes: List[Node]): Node = this.copy(children = nodes ::: children)
}

trait NodeUtils{
  def addChild(node: Node): Node
  def addChildren(nodes: List[Node]): Node
}