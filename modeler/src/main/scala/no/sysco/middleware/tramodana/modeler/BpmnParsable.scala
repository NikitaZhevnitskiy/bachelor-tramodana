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

trait BpmnParsable{
  def getParentId: String
  def setParentId(id: String): BpmnParsable
  def getChildren: List[BpmnParsable]
  def getOperationName: String
  def getProcessId: String
}
