package no.sysco.middleware.tramodana.modeler

import no.sysco.middleware.tramodana.schema.{SpanTree, JsonSpanProtocol}
import spray.json._

class JsonToSpantreeParser(val jsonSrc: String)  extends JsonSpanProtocol{

  def rebuildGenealogy(tree: SpanTree): Option[SpanTree] = None
  def mergeTrees(treeList: List[SpanTree]): Option[SpanTree] = None

  def getSpantreeList: List[SpanTree] = JsonParser(jsonSrc).convertTo[List[SpanTree]]

}

case class Jsonable(rootNode: Int, traceModels: List[List[Int]])

object JsonableJsonProtocol extends DefaultJsonProtocol
{
  implicit val whatever: RootJsonFormat[Jsonable] = jsonFormat2(Jsonable)
}

import JsonableJsonProtocol._

object JsonToSpantreeParser{

  def testGenerateJsonWithArray(): Unit = {
    def trace(to: Int): List[Int] = for (i <- List.range(0, to)) yield i
    val trace1 = trace(5)
    val trace2 = trace(4)

    val traces = List(trace1, trace2)
    val root = Jsonable( 0 ,  traces ).toJson

    println(root.prettyPrint)
  }

}
