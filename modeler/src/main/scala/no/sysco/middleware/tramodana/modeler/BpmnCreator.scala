package no.sysco.middleware.tramodana.modeler


import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import no.sysco.middleware.tramodana.builder.SpanTree
import org.camunda.bpm.model.bpmn.{Bpmn, BpmnModelInstance}
import org.camunda.bpm.model.bpmn.builder._
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance
import org.camunda.bpm.model.xml.ModelValidationException

import scala.io.Source


object JsonRootKeys extends Enumeration {
  type JsonRootKeys = Value
  val ROOT_NODE_KEY, NODE_LIST, TRACE_MODELS, NODE_CHILDREN, NODE_INDEX, WORKFLOW_TREE = Value
}



object BpmnCreator {

  def main(args: Array[String]): Unit = {
    testGenerateJsonWithArray()
    val processbuilder = makeExampleProcess
  }


  private def testGenerateJsonWithArray() = {
    val m = new ObjectMapper
    val root = m.createObjectNode
    root.put("rootNode", 0)
    val traceModels = m.createArrayNode
    val trace1 = m.createArrayNode
    val trace2 = m.createArrayNode
    for (i <- 0 to 5) {
      trace1.add(i)
      trace2.add(i)
    }

    traceModels.add(trace1)
    traceModels.add(trace2)
    root.set("traceModels", traceModels)
    try System.out.println(m.writerWithDefaultPrettyPrinter.writeValueAsString(root))
    catch {
      case e: JsonProcessingException =>
        e.printStackTrace()
    }
  }

  private def makeExampleProcess = {
    Bpmn.createExecutableProcess("banana")
      .startEvent("bananaStart")
      .name("Banana start")
      .parallelGateway("fork")
      .name("Got strawbs?")
      .serviceTask
      .name("Eat banana and strawbs")
      .endEvent
      .name("no more banana, no more strawbs")
      .moveToNode("fork")
      .userTask
      .name("Eat only banana")
  }
}

class BpmnCreator (val json: ParsableToBpmn) {

  def getBpmnXML: String = bpmnXML

  def getBpmnTree: BpmnModelInstance = bpmnTree

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
     val xml : String = Bpmn.convertToString(bpmnTree)
    Option(xml)
  }


  def parseToBpmn(workflowData: ParsableToBpmn): BpmnModelInstance = {
    val workflowRoot = workflowData.getRoot.get
    val processId = workflowRoot.operationName + "_" + workflowRoot.spanId
    val startingEventBuilder =
      Bpmn.createExecutableProcess(processId)
        .startEvent(workflowRoot.spanId)
        .name(workflowRoot.operationName)

    // Iterate through the tree, create one node at a time
    val completedTree : EndEventBuilder = parseToBpmnIter(startingEventBuilder, workflowData.getTree.get)

    // Finalise the build ( and builds the diagram elements)
    val finalisedBuild = completedTree.done
    finalisedBuild
  }

  private def parseToBpmnIter[T](processBuilder: T, node: SpanTree) = {
    val nodeDetails = getTmaNode(node)
    val children_it = getChildren(node)
    // nodes without children are leaves ( end events, or replying nodes)
    if (!children_it.hasNext) return processBuilder.endEvent(nodeDetails.id).name(nodeDetails.name)
    val children = new util.ArrayList[JsonNode]
    children_it.forEachRemaining(children.add)
    if (children.size == 1) {
      val sbt = processBuilder.serviceTask(nodeDetails.id).name(nodeDetails.name)
      return parseToBpmnIter(sbt, children.get(0))
    }
    // TODO: add annotation (possible?)
    var currentNodeId = getId(node)
    // if the current node has more than one child,
    // we create a gateway element and use it
    // as a return point until all children are processed
    if (children.size > 1) {
      currentNodeId = "fork_from_node_" + getId(node)
      val pgwb = processBuilder.parallelGateway(currentNodeId)
      // start recursion for each child
      for (child <- children) {
        parseToBpmnIter(pgwb, child).moveToNode(currentNodeId)
      }
    }
    processBuilder.asInstanceOf[EndEventBuilder]
  }

  private def getChildren(node: JsonNode) = node.path(jsonKeyMap.get(TmaBpmnCreator.JsonRootKeys.NODE_CHILDREN)).elements

  private def hasChildren(node: JsonNode) = getChildren(node).hasNext

  private def addElement[T <: BpmnModelElementInstance](pb: AbstractFlowNodeBuilder[_, _ <: FlowNode], node: JsonNode, elementClass: Class[T]) = {
    val bpmnInst = Bpmn.createEmptyModel
    val element = bpmnInst.newInstance(elementClass)
    val tmaNode = getTmaNode(node)
    element.setAttributeValue("id", tmaNode.id, true)
    element.setAttributeValue("name", tmaNode.name)
    pb.addExtensionElement(element)
    pb
  }

  private def getId(node: JsonNode) = getTmaNode(node).id

  private def getName(node: JsonNode) = getTmaNode(node).name

  private def getTmaNode(node: JsonNode) = {
    val nodeListIndex = node.path(jsonKeyMap.get(TmaBpmnCreator.JsonRootKeys.NODE_INDEX))
    val index = nodeListIndex.intValue
    nodeList.get(index)
  }
}
