package no.sysco.middleware.tramodana.modeler

trait BpmnParsable{
  type T <: BpmnParsable

  def getParentId: String

  def setParentId(id: String): T

  def getProcessId: String

  def setProcessId(id: String): T

  def getOperationName: String

  def getChildren: List[T]

  def addChild(node: T): T
  def addChildren(nodes: List[T]): T
}
