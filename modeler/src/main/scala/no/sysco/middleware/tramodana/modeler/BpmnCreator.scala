package no.sysco.middleware.tramodana.modeler


import com.fasterxml.jackson.databind.JsonNode
import no.sysco.middleware.tramodana.builder.SpanTree
import org.camunda.bpm.model.bpmn.{Bpmn, BpmnModelInstance}
import org.camunda.bpm.model.bpmn.builder._
import org.camunda.bpm.model.bpmn.instance.{BpmnModelElementInstance, FlowNode}
import org.camunda.bpm.model.xml.ModelValidationException


object BpmnCreator {

  def main(args: Array[String]): Unit = {
    Converter.testGenerateJsonWithArray()
    val processbuilder = makeExampleProcess
  }

  private def makeExampleProcess = {

    val rootNode = new {
      val id = "banana",
      val operationName = "Banana start",
      val process = "bananaStart"
    }

    val modelInstance = Bpmn.createExecutableProcess(rootNode.id)
      .startEvent(rootNode.process)
      .name(rootNode.operationName)
      .done()

    val before_ext: String = Bpmn.convertToString(modelInstance)

    modelInstance.getModelElementById(rootNode.id)
      .builder
      .parallelGateway("fork")
      .name("Got strawbs?")
      .done()
    val after_ext: String = Bpmn.convertToString(modelInstance)

    val service = parallel.serviceTask.name("Eat banana and strawbs")
    val endone = service.endEvent.name("no more banana, no more strawbs")
      .moveToNode("fork")
    val user = endone.userTask.name("Eat only banana").moveToLastGateway()
    val endtwo = user.endEvent.name("endtwo")
    val finished = endtwo.done()

  }
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

    val processId = root.operationName + "_process"
    var builder: StartEventBuilder =
      Bpmn.createExecutableProcess(processId)
        .startEvent(root.value.process.get.serviceName)
        .name(root.operationName)
    val children: List[SpanTree] = parsableTree.getChildren(root)
    val nodeStack: Stack[SpanTree] = new Stack(children)


    while (nodeStack.nonEmpty) {
      val currentNode = nodeStack.pop.get
      currentNode.children match {
        case Nil => builder = builder.endEvent(currentNode.) // make end event
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
  //  }
  //
}
