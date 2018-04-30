package no.sysco.middleware.tramodana.modeler

import no.sysco.middleware.tramodana.schema.{Span, SpanTree}

import scala.io.Source

object ModelerApp extends App {

  implicit class SpanTreeToBpmnParsable(st: SpanTree) extends BpmnParsable {
    type T = SpanTree
    override def getParentId: String = st.value.parentId
    override def setParentId(id: String): T = {
      val span = st.value.copy(parentId = id)
      st.copy( value = span)
    }

    override def getProcessId: String = st.value.spanId

    override def setProcessId(id: String): T = {
      val span: Span = st.value.copy(parentId = id)
      st.copy( value = span)
    }

    override def getOperationName: String = st.value.operationName

    override def getChildren: List[T] = st.children

    override def addChild(node: T): T =  st.copy( children = node :: st.children)

    override def addChildren(nodes: List[T]): T = st.copy( children = nodes ::: st.children)
  }

  val INPUT_FILES_DIRECTORY = "examples/input_for_modeler"

  def main(test: String): Unit = {
    println(test)

    val jsonSource: String = Source
      .fromFile(s"$INPUT_FILES_DIRECTORY/workflow_v03.json")
      .getLines
      .mkString
    val parser = new JsonToSpantreeParser(jsonSource)
    val dtoList: List[SpanTree] =  parser.getSpantreeList
    val spanTree: Option[SpanTree] = parser.mergeTrees(dtoList)

    val bpmnCreator = spanTree match {
      case Some(parsable) => new BpmnCreator(parsable)
      case None => throw new Exception("No parsable created")
    }

    val bpmnXmlString: String = bpmnCreator.getBpmnXmlStr.get
    println(bpmnXmlString)
  }


}
