package no.sysco.middleware.tramodana.modeler

import no.sysco.middleware.tramodana.schema.{SpanTree, JsonSpanProtocol}
import spray.json._

class JsonToSpantreeParser(val jsonSrc: String)  extends JsonSpanProtocol{

  private def cleanProcessId(pId: String): String = {
    val pattern = "([^A-Za-z]+)".r
    pId match {
      case x if x.equals("#") => "id" ++ "_proc"
      case pattern(_) => "id_" ++ pId
      case _ => pId
    }
  }

  private def preprocess(parsableData: BpmnParsable): BpmnParsable = {
    val formattedId = cleanProcessId(parsableData.getProcessId)
    val cleanedRoot = parsableData.setProcessId( formattedId)
    cleanedRoot
  }

  def rebuildGenealogy(tree: SpanTree): Option[SpanTree] = None
  def mergeTrees(treeList: List[SpanTree]): Option[SpanTree] = None
  def getSpantreeList: List[SpanTree] = JsonParser(jsonSrc).convertTo[List[SpanTree]]

}

