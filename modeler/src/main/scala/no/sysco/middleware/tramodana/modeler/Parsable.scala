package no.sysco.middleware.tramodana.modeler

import no.sysco.middleware.tramodana.builder._
import spray.json

object JsonKey extends Enumeration{
  type JsonKey = Value
  val ROOT_NODE_KEY,
  NODE_LIST,
  TRACE_MODELS,
  NODE_CHILDREN,
  NODE_INDEX,
  WORKFLOW_TREE = Value
}

trait Parsable{
  def getBaseSpanTree: Option[SpanTree ]
  def getNodeSet: Set[Span]
  def getRoot: Option[SpanTree]
  def getChildren(node: SpanTree) : List[SpanTree]

}
