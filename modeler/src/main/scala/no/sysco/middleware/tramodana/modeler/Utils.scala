package no.sysco.middleware.tramodana.modeler

import java.io.{BufferedWriter, File, FileWriter}

import no.sysco.middleware.tramodana.schema.Span

object Utils {
  var counter: Int = 1

  /**
    * Helper constructor method with defaults for TestNode
    * @param parentId
    * @param op
    * @param procId
    * @param children
    * @return
    */
  def createTestNode(parentId: String = "0", op: String = "op", procId: String = "proc", children: List[TestNode] = List.empty): TestNode = {
    val id = counter.toString
    counter += 1
    new TestNode(op, procId, children, parentId)
  }

  /**
    * Replace all non-alphanum characters by '_' and
    * enforce beginning of string by "id_" if not starting
    * by letter character
    * @param inputId
    * @return
    */
  def applyXmlIdFormat(inputId: String): String = {
    val goodChars = (('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9')).toSet
    val pId = inputId.map(c => if (goodChars.contains(c)) c else '_')
    val pattern = "([^A-Za-z]+).*".r // match against all that doesn't start with alpha char
    pId match {
      case pattern(_) => "id_" ++ pId
      case _ => pId
    }
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

  /**
    * Helper method to write string content to a file
    * @param content the content of the file
    * @param fileNameWithoutExt the name of the output file without extension
    * @param ext the file extension - defaults to 'bpmn'
    */
  def writeToExampleDir(content: String, fileNameWithoutExt: String, ext: String = "bpmn"): Unit = {
    val file = new File(s"examples/output_for_modeler/$fileNameWithoutExt.$ext")
    val bufferedWriter = new BufferedWriter(new FileWriter(file))
    bufferedWriter.write(content)
    bufferedWriter.close()
  }
}

/**
  * This class is basically the SpanTree model from the Schema module
  * implementing the BpmnParsable trait
  * @param value
  * @param children
  */
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
  override def addChild(node: SpanNode): SpanNode = SpanNode(value, node :: children)

  /**
    * returns the argument SpanNode with updated children
    * (SpanNode is immutable)
    */
  override def addChildren(nodes: List[SpanNode]): SpanNode = SpanNode(value, nodes ::: children)

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

/**
  * Test class for SpanTree, requiring only BpmnParsable-essential
  * arguments (instead of having to deal with generating all other DTOs
  * from Schema module)
  * @param operationName
  * @param processId
  * @param children
  * @param parentId
  */
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
