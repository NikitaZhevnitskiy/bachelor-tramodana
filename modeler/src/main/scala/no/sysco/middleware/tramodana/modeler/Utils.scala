package no.sysco.middleware.tramodana.modeler

import java.io.{BufferedWriter, File, FileWriter}

object Utils {
  var counter: Int = 1

  def createNode(parentId: String = "0", op: String = "op", procId: String = "proc", children: List[Node] = List.empty): Node = {
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

  def writeToExampleDir(content: String, fileNameWithoutExt: String): Unit = {
    val file = new File(s"examples/output_for_modeler/$fileNameWithoutExt.bpmn")
    val bufferedWriter = new BufferedWriter(new FileWriter(file))
    bufferedWriter.write(content)
    bufferedWriter.close()
  }
}

case class Node(operationName: String,
                processId: String,
                children: List[Node],
                parentId: String) extends BpmnParsable {

  type T = Node
  override def getChildren: List[T] = children

  override def getParentId: String = parentId

  override def setParentId(id: String): T = copy(parentId = id)

  override def getProcessId: String = processId

  override def setProcessId(id: String): T = copy(processId = id)

  override def getOperationName: String = operationName

  override def addChild(node: T ): T = this.copy(children = node :: children)

  override def addChildren(nodes: List[T]): T = this.copy(children = nodes ::: children)

}

trait TreeNodeUtils[T] {
  def addChild(node: T): T
  def addChildren(nodes: List[T]): T
}
