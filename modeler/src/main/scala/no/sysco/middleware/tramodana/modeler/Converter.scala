package no.sysco.middleware.tramodana.modeler

import spray.json._
class Converter() {
  def jsonToParsable(jsonWorkflow: String): Option[ParsableToBpmn] =Option.empty

}

object Converter{

  def testGenerateJsonWithArray() = {
    def trace(to: Int): List[Int] = for (i <- List.range(0, to)) yield i
    val trace1 = trace(5)
    val trace2 = trace(4)

    val traces = List(trace1, trace2)
    val root = new { val rootNode = 0; val traceModels: List[List[Int]] = traces; }

    println(root.toJson.prettyPrint)
  }

}
