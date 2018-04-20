package no.sysco.middleware.tramodana.modeler

import java.io.{BufferedWriter, File, FileWriter}

import no.sysco.middleware.tramodana.builder.SpanTree
import org.camunda.bpm.model.bpmn.builder._
import org.camunda.bpm.model.bpmn.instance._
import org.camunda.bpm.model.bpmn.{Bpmn, BpmnModelInstance}
import org.camunda.bpm.model.xml.ModelValidationException


object BpmnCreator {

  def main(args: Array[String]): Unit = {
    Converter.testGenerateJsonWithArray()
    val exampleProcess: String = makeExampleProcess

    //writeToExampleDir(exampleProcess, "example_process")

    val loopBasedTreeBuilding = example
    println(s"while-loop depth-first (stack based) tree traversal:\n$loopBasedTreeBuilding")
    writeToExampleDir(loopBasedTreeBuilding, "loop-based" )
  }

  def writeToExampleDir(content: String, fileNameWithoutExt: String):Unit ={
    val file = new File(s"examples/output_for_modeler/$fileNameWithoutExt.bpmn")
    val bufferedWriter = new BufferedWriter(new FileWriter(file))
    bufferedWriter.write(content)
    bufferedWriter.close()
  }


  case class Node(operationName: String, processId: String, children: List[Node], parentId: String)

  def example: String = {

    val rootNode = new Node("Log in", "start_1",
      List(
        new Node("Create Membership", "service_1",
          List(new Node("Show main page", "end_1", Nil, "service_1")),
          "start_1"
        ),
        new Node("Find Membership", "service_2",
          List(new Node("Show main page with suggestions", "end_2", Nil, "service_2")),
          "start_1"
        )
      ),
      "0"
    )

    val modelInstance: BpmnModelInstance = Bpmn.createExecutableProcess("example")
      .startEvent(rootNode.processId)
      .name(rootNode.operationName)
      .done()

    val nodeStack: Stack[Node] = new Stack(rootNode.children)
    val branchStack: Stack[String] = new Stack(rootNode.processId)
    var parentId: String = rootNode.processId
    var branchCount = 1


    while (nodeStack.nonEmpty) {
      val currentNode = nodeStack.pop.get
      val children = currentNode.children

      children match {
        // no children -> node is a leaf, i.e. an end event
        case Nil => appendEndEvent(modelInstance, currentNode.parentId, currentNode.processId, currentNode.operationName)
          parentId = branchStack.peek match {
            case Some(branchId) => branchId
            case None => parentId
          }
        // one child -> node is a task leading to next node
        case x :: Nil => appendServiceTask(modelInstance, currentNode.parentId, currentNode.processId, currentNode.operationName)
          nodeStack.push(x)
          parentId = currentNode.processId

        // anything else (multiple children) -> the node is a task leading to a fork containing all children
        case _ :: _ =>
          val branchId = currentNode.processId + "_fork_" + branchCount
          branchCount += 1
          appendGateway(modelInstance, currentNode.parentId, branchId)
          nodeStack.pushAll(children.mapConserve(child => child.copy(parentId = branchId)))
      }
    }

    return Bpmn.convertToString(modelInstance)
  }


  def makeExampleProcess: String = {

    val rootNode = new {
      val id = "log_in"
      val operationName = "Log in"
      val processId = "start_1"
    }

    val modelInstance: BpmnModelInstance = Bpmn.createExecutableProcess(rootNode.id)
      .startEvent(rootNode.processId)
      .name(rootNode.operationName)
      .done()

    val start: StartEvent = modelInstance.getModelElementById(rootNode.processId)
    start.builder
      .parallelGateway("fork_1")
      .name("Has membership?")
      .done()

    appendServiceTask(modelInstance, "fork_1", "service_1", "Create membership")
    appendServiceTask(modelInstance, "fork_1", "service_2", "Search for membership")

    val after_second_ext: String = Bpmn.convertToString(modelInstance)


    println(s"second extended instance\n$after_second_ext")

    after_second_ext
  }

  def appendElement[T <: FlowNode, F <: FlowNode](mi: BpmnModelInstance,
                                                  parent_id: String,
                                                  nodeId: String,
                                                  nodeName: String,
                                                  elemType: Class[F]) = {
    val parentelem: T = mi.getModelElementById(parent_id)
    val newElem: F = mi.newInstance(elemType)
    newElem.setId(nodeId)
    newElem.setName(nodeName)
    parentelem.builder.addExtensionElement(newElem)
  }

  def appendServiceTask[T <: FlowNode](mi: BpmnModelInstance,
                                       parent_id: String,
                                       nodeId: String,
                                       nodeName: String) = {
    val parentelem: T = mi.getModelElementById(parent_id)
    parentelem.builder
      .serviceTask(nodeId)
      .name(nodeName).done()
  }

  def appendEndEvent[T <: FlowNode](mi: BpmnModelInstance,
                                    parent_id: String,
                                    nodeId: String,
                                    nodeName: String) = {
    val parentelem: T = mi.getModelElementById(parent_id)
    parentelem.builder
      .endEvent(nodeId)
      .name(nodeName).done()
  }

  def appendGateway[T <: FlowNode](mi: BpmnModelInstance,
                                   parent_id: String,
                                   nodeId: String) = {
    val parentelem: T = mi.getModelElementById(parent_id)
    parentelem.builder.parallelGateway(nodeId).done()
  }


  class BpmnCreator(val parsableData: Parsable) {

    var bpmnXML: String = ""
    var bpmnTree: BpmnModelInstance = Bpmn.createEmptyModel()

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
      val xml: String = Bpmn.convertToString(bpmnTree)
      Option(xml)
    }

    def parseToBpmn(parsableTree: Parsable): Option[BpmnModelInstance] = {
      val root = parsableTree.getRoot match {
        case Some(r) => r
        case None =>
          println("The tree is empty")
          return Option.empty
      }

      val processId = root.value.operationName + "_process"
      var builder: StartEventBuilder =
        Bpmn.createExecutableProcess(processId)
          .startEvent(root.value.process.get.serviceName)
          .name(root.value.operationName)
      val children: List[SpanTree] = parsableTree.getChildren(root)
      val nodeStack: Stack[SpanTree] = new Stack(children)


      while (nodeStack.nonEmpty) {
        val currentNode = nodeStack.pop.get
        currentNode.children match {
          //case Nil => builder = builder.endEvent(currentNode.) // make end event
          case _ =>
        }


      }

      // Iterate through the tree, create one node at a time
      //val completedTree : EndEventBuilder = parseToBpmnIter(startingEventBuilder, parsableTree.getBaseSpanTree.get)

      // Finalise the build ( and builds the diagram elements)
      //val finalisedBuild = completedTree.done
      //Option(finalisedBuild)
      Option(builder.done())
    }


    /*  private def parseToBpmnIter[T : AbstractFlowElementBuilder](processBuilder: T,
                                                                node: SpanTree): EndEventBuilder = {
        val nodeDetails = node.value
        val children_it = node.children

        // nodes without children are leaves ( end events, or replying nodes)
        children_it match {
          case Nil => return processBuilder.endEvent(nodeDetails.process.get)
            .name(nodeDetails.operationName)
        }
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
      }*/

    //  private def addElement[T <: BpmnModelElementInstance](
    //                                                         pb: AbstractFlowNodeBuilder[_, _ <: FlowNode],
    //                                                         node: JsonNode, elementClass: Class[T]
    //                                                       ) = {
    //    val bpmnInst = Bpmn.createEmptyModel
    //    val element = bpmnInst.newInstance(elementClass)
    //    val tmaNode = getTmaNode(node)
    //    element.setAttributeValue("id", tmaNode.id, true)
    //    element.setAttributeValue("name", tmaNode.name)
    //    pb.addExtensionElement(element)
    //    pb
    // }
  }

}
