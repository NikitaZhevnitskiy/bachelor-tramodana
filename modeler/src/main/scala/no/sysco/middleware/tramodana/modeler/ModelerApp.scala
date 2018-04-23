package no.sysco.middleware.tramodana.modeler

import no.sysco.middleware.tramodana.modeler.BpmnCreator.BpmnCreator

import scala.io.Source

object ModelerApp extends App {

  main("it works")

  private val INPUT_FILES_DIRECTORY = "examples/input_for_modeler"
  def main(test: String): Unit ={
    println(test)
    val converter = new Converter()

    val jsonWorkflow : String = Source
      .fromFile(s"${INPUT_FILES_DIRECTORY }/workflow_v03.json")
      .getLines
      .mkString
    val dto: Option[Parsable] = converter.jsonToParsable(jsonWorkflow)

    val bpmnCreator = dto match {
      case Some(parsable) => new BpmnCreator(parsable)
      case None => throw new Exception("No parsable created")
    }

    val bpmnXmlString : String = bpmnCreator.getBpmnXML
    println(bpmnXmlString)
  }

}
