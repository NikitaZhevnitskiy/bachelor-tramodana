package no.sysco.middleware.tramodana.modeler

import org.camunda.bpm.model.bpmn.instance._
import org.camunda.bpm.model.bpmn.{Bpmn, BpmnModelInstance}
import org.camunda.bpm.model.xml.ModelValidationException
import org.camunda.bpm.model.xml.instance.ModelElementInstance


object BpmnCreator {

  def main(args: Array[String]): Unit = { }
}

class BpmnCreator(val parsableData: BpmnParsable, val rootProcessName: String) {

  var branchCount = 1
  val validRootProcessName = Utils.applyXmlIdFormat(rootProcessName)

  var bpmnTree: Option[BpmnModelInstance] = parseToBpmn(parsableData, validRootProcessName)
  def getBpmnXmlStr: Option[String] = bpmnToString(bpmnTree.get)

  def getBpmnTree: Option[BpmnModelInstance] = bpmnTree

  private def getErrorBpmnXml = {
    val errorBpmn = Bpmn.createExecutableProcess("error")
      .startEvent
      .name("error")
      .endEvent
      .done
    Bpmn.convertToString(errorBpmn)
  }

  private def BpmnToXML(bpmnTree: BpmnModelInstance): Option[String] = {
    try Bpmn.validateModel(bpmnTree)
    catch {
      case e: ModelValidationException =>
        println("The BPMN instance is not valid:\n" + e.getMessage)
        Option.empty
    }
    val xml: String = Bpmn.convertToString(bpmnTree)
    Option(xml)
  }

  private def bpmnToString(modelInstance: BpmnModelInstance): Option[String] = {
    var bpmnString: String = ""
    try {
      bpmnString = Bpmn.convertToString(modelInstance)
    } catch {
      case e: ModelValidationException => return None
    }

    Some(bpmnString)

  }

  private def getForkedChildren(mi: BpmnModelInstance, n: BpmnParsable): List[BpmnParsable] = {
    val branchId = n.getProcessId + "_fork_" + branchCount
    branchCount += 1
    appendGateway(mi, n.getProcessId, branchId)
    val forkedChildren: List[BpmnParsable] = n.getChildren.map(child => child.setParentId(branchId))
    forkedChildren
  }

  private def parseToBpmn(rootNode: BpmnParsable, pId: String): Option[BpmnModelInstance] = {

    try {
      val model = parse(rootNode, pId)
      Bpmn.validateModel(model)
      Some(model)
    } catch {
      case e: NullPointerException => None
      case e: ModelValidationException => None
    }

  }

  private def parse(rootNode: BpmnParsable, processId: String): BpmnModelInstance = {

    val modelInstance: BpmnModelInstance = Bpmn.createExecutableProcess(processId)
      .startEvent(rootNode.getProcessId)
      .name(rootNode.getOperationName)
      .done()


    val nodeStack: Stack[BpmnParsable] = new Stack()
    var rootChildren = rootNode.getChildren match {
      case Nil => return modelInstance
      case head :: Nil => head :: Nil
      case _ :: _ => getForkedChildren(modelInstance, rootNode)
    }

    nodeStack.pushAll(rootChildren)

    while (nodeStack.nonEmpty) {
      val currentNode = nodeStack.pop.get
      val children = currentNode.getChildren

      children match {
        // no children -> node is a leaf, i.e. an end event
        case Nil =>
          appendEndEvent(modelInstance, currentNode)
        //appendElement(modelInstance, currentNode)
        case x =>
          appendServiceTask(modelInstance, currentNode)
          x match {
            case head :: Nil =>
              // one child -> node is a task leading to next node
              nodeStack.push(head)
            case _ :: _ =>
              // anything else (multiple children) -> the node is a task leading to a fork containing all children
              val forkedChildren = getForkedChildren(modelInstance, currentNode)
              nodeStack.pushAll(forkedChildren)
            // Handled on the upper level, adding this empty case for the warnings to stop...
            case Nil =>()
          }
      }
    }
    modelInstance
  }

  private def appendServiceTask[T <: FlowNode](mi: BpmnModelInstance,
                                               node: BpmnParsable): BpmnModelInstance = {
    val parentelem: T = mi.getModelElementById(node.getParentId )
    parentelem.builder
      .serviceTask(node.getProcessId)
      .name(node.getOperationName).done()
  }

  private def appendEndEvent[T <: FlowNode](mi: BpmnModelInstance,
                                            node: BpmnParsable): BpmnModelInstance = {
    val parentelem: T = mi.getModelElementById(node.getParentId)
    parentelem.builder
      .endEvent(node.getProcessId)
      .name(node.getOperationName).done()
  }

  private def appendGateway[T <: FlowNode](mi: BpmnModelInstance,
                                   parent_id: String,
                                   nodeId: String): BpmnModelInstance = {
    val parentelem: T = mi.getModelElementById(parent_id)
    parentelem.builder.parallelGateway().id(nodeId).done()
  }


  private def appendElement[T <: BpmnModelElementInstance](mi: BpmnModelInstance,
                                                   parent_id: String,
                                                   nodeId: String,
                                                   nodeName: String = "",
                                                  ): Unit = {
    val elementType = classOf[EndEvent]
    val parentelem: ModelElementInstance = mi.getModelElementById(parent_id)
    val newElem: ModelElementInstance = parentelem.getModelInstance.newInstance(elementType)

    newElem.setAttributeValue("id", nodeId)
    if (!"".equals(nodeName))
      newElem.setAttributeValue("name", nodeName)

    parentelem.addChildElement(newElem)

  }


}