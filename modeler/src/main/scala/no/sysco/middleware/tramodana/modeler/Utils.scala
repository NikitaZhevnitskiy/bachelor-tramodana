package no.sysco.middleware.tramodana.modeler

import java.io.{BufferedWriter, File, FileWriter}

import no.sysco.middleware.tramodana.schema.Span

object Utils {
  var counter: Int = 1

  def createTestNode(parentId: String = "0", op: String = "op", procId: String = "proc", children: List[TestNode] = List.empty): TestNode = {
    val id = counter.toString
    counter += 1
    TestNode(op, procId, children, parentId)
  }


  /**
    * Format the parentId and processId of a BpmnParsable node
    * to be ready for use as XML parameter
    *
    * @param n the node
    * @return the node with formatted parent- and processId
    */
  def formatParsableForXml(n: BpmnParsable): BpmnParsable = {
    val validProcessId = applyXmlIdFormat(n.getProcessId)
    val validParentId = applyXmlIdFormat(n.getParentId)
    val nodeWithValidProcId = n.setProcessId(validProcessId)
    val validNode = nodeWithValidProcId.setParentId(validParentId)
    validNode
  }

  def applyXmlIdFormat(inputId: String): String = {
    val goodChars = (('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9')).toSet
    val pId = inputId.map(c => if (goodChars.contains(c)) c else '_')
    val pattern = "([^A-Za-z]+).*".r // match against all that doesn't start with alpha char
    pId match {
      case pattern(_) => "id_" ++ pId
      case _ => pId
    }
  }

  def writeToExampleDir(content: String, fileNameWithoutExt: String): Unit = {
    val file = new File(s"examples/output_for_modeler/$fileNameWithoutExt.bpmn")
    val bufferedWriter = new BufferedWriter(new FileWriter(file))
    bufferedWriter.write(content)
    bufferedWriter.close()
  }
}

case class SpanNode(value: Span, children: List[SpanNode]) extends BpmnParsable {
  override type T = SpanNode

  override def getParentId: String = value.parentId

  override def setParentId(id: String): SpanNode = {
    val span = value.copy(parentId = id)
    SpanNode(span, children)
  }

  override def getProcessId: String = value.spanId

  override def setProcessId(id: String): SpanNode = {
    val span = value.copy(spanId = id)
    SpanNode(span, children)
  }

  override def getOperationName: String = value.operationName

  override def getChildren: List[SpanNode] = children

  /**
    * returns the argument SpanNode with updated children
    * (SpanNode is immutable)
    */
  override def addChild(node: SpanNode): SpanNode =
    SpanNode(value, node :: children)

  /**
    * returns the argument SpanNode with updated children
    * (SpanNode is immutable)
    */
  override def addChildren(nodes: List[SpanNode]): SpanNode =
    SpanNode(value, nodes ::: children)

  def printPretty(): Unit = printPrettyIter("", last = true)

  private def printPrettyIter(ind: String, last: Boolean): Unit = {
    var indent = ind
    print(indent)

    if (last) {
      print("\\-")
      indent ++= "  "
    } else {
      print("|-")
      indent ++= "| "
    }
    println(getOperationName ++ " - (" ++ getProcessId ++ ")")
    for (i <- getChildren.indices) {
      getChildren(i).printPrettyIter(indent, i == (getChildren.size - 1))
    }
  }
}

case class TestNode(operationName: String,
                    processId: String,
                    children: List[TestNode],
                    parentId: String) extends BpmnParsable {

  self: TestNode =>
  type T = TestNode

  override def getChildren: List[T] = children

  override def getParentId: String = parentId

  override def setParentId(id: String): T = copy(parentId = id)

  override def getProcessId: String = processId

  override def setProcessId(id: String): T = copy(processId = id)

  override def getOperationName: String = operationName

  override def addChild(node: T): T = this.copy(children = node :: children)

  override def addChildren(nodes: List[T]): T = this.copy(children = nodes ::: children)

  def printPretty(): Unit = printPrettyIter("", last = true)

  private def printPrettyIter(ind: String, last: Boolean): Unit = {
    var indent = ind
    print(indent)

    if (last) {
      print("\\-")
      indent ++= "  "
    } else {
      print("|-")
      indent ++= "| "
    }
    println(getProcessId)
    for (i <- getChildren.indices) {
      getChildren(i).printPrettyIter(indent, i == (getChildren.size - 1))
    }
  }
}

trait TreeNodeUtils[T] {
  def addChild(node: T): T

  def addChildren(nodes: List[T]): T
}
