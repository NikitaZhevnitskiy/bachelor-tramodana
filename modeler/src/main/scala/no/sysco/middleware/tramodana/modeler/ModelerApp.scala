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
    val parser = new JsonToSpannodeParser(jsonSource)
    val processedTraces: List[SpanNode] =  parser.preprocessedSpanNodeList
    val firstTrace = processedTraces.headOption

    val bpmnCreator = firstTrace match {
      case Some(parsable) => new BpmnCreator(parsable, "00 test")
      case None => throw new Exception("No parsable created")
    }

    val bpmnXmlString: String = bpmnCreator.getBpmnXmlStr.get
    println(bpmnXmlString)
  }

  run

}
