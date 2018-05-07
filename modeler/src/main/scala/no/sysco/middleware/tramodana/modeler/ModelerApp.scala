package no.sysco.middleware.tramodana.modeler

import spray.json.DeserializationException

import scala.io.Source



object ModelerApp extends App {

  val INPUT_FILES_DIRECTORY = "examples/input_for_modeler"

  def run(): Unit = {

    val jsonSource: String = Source
      .fromFile(s"$INPUT_FILES_DIRECTORY/ROOT_OPERATION_SET_SPAN_TREES.json")
      .getLines
      .mkString

    val parser: JsonToSpanNodeParser = new JsonToSpanNodeParser()
    val tree:  Option[BpmnParsable] = parser.parse(jsonSource)

    val bpmnCreator = tree match {
      case Some(parsable) => new BpmnCreator(parsable, "00 test")
      case None => throw new Exception("SpanTrees could not be merged")
    }

    val bpmnXmlString: String = bpmnCreator.getBpmnXmlStr.get
    println(bpmnXmlString)
  }

  run()

}
