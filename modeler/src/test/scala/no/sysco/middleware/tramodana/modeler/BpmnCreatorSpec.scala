package no.sysco.middleware.tramodana.modeler

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
           end_1    service_3   404
                        /
                       /
                    end_3
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
              List(Utils.createNode("service_3", "Show main page with updates", "end_3"))
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
  val wrongId_allInt: Node = Utils.createNode(procId = "1234")
  val wrongId_startsWithHash: Node = Utils.createNode(procId = "#one")
  val wrongId_startsWithPeriod: Node = Utils.createNode(procId = ".one")
  val wrongId_containsSpace: Node = Utils.createNode(procId = "id one")

  "A BpmnParsable element" when {
    "composed of only one node" should {
      "parse to BPMN without error" in {
        val res = new BpmnCreator(uniqueNodeSpanTree).getBpmnTree
        assert(res.nonEmpty)
      }
    }
    "containing a processId of the wrong format" should {
      "parse to None: all int id - e.g: \"1234\"" in {
        assert(new BpmnCreator(wrongId_allInt).getBpmnTree.nonEmpty)
      }
      "parse to None: starts with '#' - e.g: \"#id\"" in {
        assert(new BpmnCreator(wrongId_startsWithHash).getBpmnTree.nonEmpty)
      }
      "parse to None: starts with '.' - e.g: \".id\"" in {
        assert(new BpmnCreator(wrongId_startsWithPeriod).getBpmnTree.nonEmpty)
      }
      "parse to None: contains space - e.g: \"id one\"" in {
        assert(new BpmnCreator(wrongId_containsSpace).getBpmnTree.nonEmpty)
      }

    }
    "its nodes have parent-child links" should {
      "parse to BPMN (built in-line)" in {
        val res = new BpmnCreator(inlineTree).getBpmnTree
        assert(res.nonEmpty)
      }
      "parse to BPMN (built procedurally)" in {
        val res = new BpmnCreator(proceduralTree).getBpmnTree
        assert(res.nonEmpty)
      }
    }

    "containing a broken child-parent link (parentId != the parent's processId)" should {
      "parse to BPMN" in {
        val root = Utils.createNode("0", "Log in", "start_1")
        val orphanNode = Utils.createNode("wrong_parent_id", "lone child", "end_1")
        val parentNode = root.addChild(orphanNode)
        parentNode.printPretty
        assert(new BpmnCreator(parentNode).getBpmnTree.nonEmpty)
      }
    }
  }


}
