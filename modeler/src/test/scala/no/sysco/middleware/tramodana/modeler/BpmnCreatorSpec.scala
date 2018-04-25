package no.sysco.middleware.tramodana.modeler

import java.io.{BufferedWriter, File, FileWriter}

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BpmnCreatorSpec extends WordSpec with BeforeAndAfter {


  /* Example tree:
  To represent a moderate level of complexity:
  - should contain subtrees
  - should contain a subtree with one child
  - should contain a subtree with several children
  - should be unbalanced

                  start_1
                    /  \
                   /    \
                  /      \
            service_1    service_2
               /           /   \
              /           /     \
           end_1       end_2     404
   */

  val inlineTree: Node =
    Utils.createNode("0", "Log in", "start_1",
      List(
        Utils.createNode("start_1", "Create Membership", "service_1",
          List(
            Utils.createNode("service_1", "Show Main Page", "end_1")
          )
        ),
        Utils.createNode("start_1", "Find Membership", "service_2",
          List(
            Utils.createNode("service_2", "Get user updates", "service_3",
              List( Utils.createNode("service_3", "Show main page with updates", "end_2"))
            ),
            Utils.createNode("service_2", "Error 404", "end_error")
          )
        )
      )
    )

  var proceduralTree: Node = Utils.createNode("0", "Log in", "start_1")
  var service_1: Node = Utils.createNode("start_1", "Service 1", "service_1")
  var service_2: Node = Utils.createNode("start_1", "Service 2", "service_2")
  var end_1: Node = Utils.createNode("service_1", "End 1", "end_1")
  var end_2: Node = Utils.createNode("service_2", "End 2", "end_2")
  var error: Node = Utils.createNode("service_2", "Error", "end_error")

  service_1 = service_1.addChild(end_1)
  service_2 = service_2.addChild(end_2)
  service_2 = service_2.addChild(error)
  proceduralTree = proceduralTree.addChild(service_1)
  proceduralTree = proceduralTree.addChild(service_2)

  // edge cases
  val uniqueNodeSpanTree: Node = Utils.createNode()
  val wrongId_allInt: Node = Utils.createNode(procId =  "1234")
  val wrongId_startsWithHash: Node = Utils.createNode(procId = "#one")
  val wrongId_startsWithPeriod: Node = Utils.createNode(procId = ".one")
  val wrongId_containsSpace: Node = Utils.createNode(procId = "id one")

  "A BpmnParsable element" when {
    "composed of only one node" should {
      "parse to BPMN without error" in {
        val res = BpmnCreator.parseToBpmn(uniqueNodeSpanTree)
        assert(res.nonEmpty)
      }
    }
    "containing a processId of the wrong format" should{
      "parse to None: all int id - e.g: \"1234\""       in{assert(BpmnCreator.parseToBpmn(wrongId_allInt).isEmpty) }
      "parse to None: starts with '#' - e.g: \"#id\""   in{assert(BpmnCreator.parseToBpmn(wrongId_startsWithHash).isEmpty)  }
      "parse to None: starts with '.' - e.g: \".id\""   in{assert(BpmnCreator.parseToBpmn(wrongId_startsWithPeriod).isEmpty)  }
      "parse to None: contains space - e.g: \"id one\"" in{assert(BpmnCreator.parseToBpmn(wrongId_containsSpace).isEmpty)  }

    }
    "its nodes have parent-child links" should {
      "parse to BPMN (built in-line)" in {
        val res = BpmnCreator.parseToBpmn(inlineTree)
        assert(res.nonEmpty)
      }
      "parse to BPMN (built procedurally)" in {
        val res = BpmnCreator.parseToBpmn(proceduralTree)
        assert(res.nonEmpty)
      }
    }
  }


}
