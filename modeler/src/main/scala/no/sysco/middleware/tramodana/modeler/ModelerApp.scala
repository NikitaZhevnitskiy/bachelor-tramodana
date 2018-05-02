package no.sysco.middleware.tramodana.modeler

import no.sysco.middleware.tramodana.schema.{Span, SpanTree}

import scala.io.Source



object ModelerApp extends App {

  val INPUT_FILES_DIRECTORY = "examples/input_for_modeler"

  def run: Unit = {

    val jsonSource: String = Source
      .fromFile(s"$INPUT_FILES_DIRECTORY/ROOT_OPERATION_SET_SPAN_TREES.json")
      .getLines
      .mkString
    val parser = new JsonToSpantreeParser(jsonSource)
    val dtoList: List[SpanNode] =  parser.getSpanNodeList
    val pre_clean = dtoList.head
    println("Before cleaning IDs: ")
    pre_clean.printPretty()
    val post_clean = parser.preprocessSpanNode(pre_clean)
    println("After cleaning IDs: ")
    post_clean.printPretty()
    val spanTree: Option[SpanNode] = parser.mergeTrees(dtoList)

    val bpmnCreator = spanTree match {
      case Some(parsable) => new BpmnCreator(parsable)
      case None => throw new Exception("No parsable created")
    }

    val bpmnXmlString: String = bpmnCreator.getBpmnXmlStr.get
    println(bpmnXmlString)
  }

  run

}
