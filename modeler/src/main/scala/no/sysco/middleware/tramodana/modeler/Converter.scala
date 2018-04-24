package no.sysco.middleware.tramodana.modeler

import spray.json._

class Converter() {
  def jsonToParsable(jsonWorkflow: String): Option[BpmnParsable] =Option.empty

}

case class Jsonable(rootNode: Int, traceModels: List[List[Int]])

object JsonableJsonProtocol extends DefaultJsonProtocol
{
  implicit val whatever = jsonFormat2(Jsonable)
}

import JsonableJsonProtocol._

object Converter{

  def testGenerateJsonWithArray() = {
    def trace(to: Int): List[Int] = for (i <- List.range(0, to)) yield i
    val trace1 = trace(5)
    val trace2 = trace(4)

    val traces = List(trace1, trace2)
    val root = Jsonable( 0 ,  traces ).toJson

    println(root.prettyPrint)
  }

}
