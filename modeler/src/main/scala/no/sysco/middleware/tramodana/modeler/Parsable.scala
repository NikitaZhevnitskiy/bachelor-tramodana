package no.sysco.middleware.tramodana.modeler

import no.sysco.middleware.tramodana.schema._
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

trait Parsable[T,S]{
  def getBaseSpanTree: Option[T ]
  def getNodeList: List[S]
  def getRoot: Option[T]
  def getChildren(node: T) : List[T]
}
