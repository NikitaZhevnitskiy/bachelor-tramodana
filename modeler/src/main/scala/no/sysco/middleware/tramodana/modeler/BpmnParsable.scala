package no.sysco.middleware.tramodana.modeler

trait BpmnParsable{
  def getParentId: String
  def setParentId(id: String): BpmnParsable
  def getChildren: List[BpmnParsable]
  def getOperationName: String
  def getProcessId: String
}
