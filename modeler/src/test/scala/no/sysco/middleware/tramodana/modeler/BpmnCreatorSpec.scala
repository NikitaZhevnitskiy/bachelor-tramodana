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
  val inlineTree: TestNode =
    Utils.createTestNode("0", "Log in", "start_1",
      List(
        Utils.createTestNode("start_1", "Create Membership", "service_1",
          List(
            Utils.createTestNode("service_1", "Show Main Page", "end_1")
          )
        ),
        Utils.createTestNode("start_1", "Find Membership", "service_2",
          List(
            Utils.createTestNode("service_2", "Get user updates", "service_3",
              List(Utils.createTestNode("service_3", "Show main page with updates", "end_3"))
            ),
            Utils.createTestNode("service_2", "Error 404", "end_error")
          )
        )
      )
    )

  var proceduralTree: TestNode = Utils.createTestNode("0", "Log in", "start_1")
  var service_1: TestNode = Utils.createTestNode("start_1", "Service 1", "service_1")
  var service_2: TestNode = Utils.createTestNode("start_1", "Service 2", "service_2")
  var end_1: TestNode = Utils.createTestNode("service_1", "End 1", "end_1")
  var end_2: TestNode = Utils.createTestNode("service_2", "End 2", "end_2")
  var error: TestNode = Utils.createTestNode("service_2", "Error", "end_error")

  service_1 = service_1.addChild(end_1)
  service_2 = service_2.addChild(end_2)
  service_2 = service_2.addChild(error)
  proceduralTree = proceduralTree.addChild(service_1)
  proceduralTree = proceduralTree.addChild(service_2)

  // edge cases
  val uniqueNodeSpanTree: TestNode = Utils.createTestNode()
  val wrongId_allInt: TestNode = Utils.createTestNode(procId = "1234")
  val wrongId_hex: TestNode = Utils.createTestNode(procId = "0x01")
  val wrongId_startsWithHash: TestNode = Utils.createTestNode(procId = "id#one")
  val wrongId_startsWithPeriod: TestNode = Utils.createTestNode(procId = ".one")
  val wrongId_containsSpace: TestNode = Utils.createTestNode(procId = "id one")

  "A BpmnParsable element" when {
    "composed of only one node" should {
      "parse to BPMN without error" in {
        val res = new BpmnCreator(uniqueNodeSpanTree, uniqueNodeSpanTree.operationName).getBpmnTree
        assert(res.nonEmpty)
      }
    }
    "containing a processId of the wrong format" should {
      "parse correctly: all int id - e.g: \"1234\"" in {
        val cleaned: TestNode = Utils.formatParsableForXml(wrongId_allInt).asInstanceOf[TestNode]
        val c = new BpmnCreator(cleaned, cleaned.operationName)
        val tree = c.getBpmnTree
        assert(tree.nonEmpty)
      }
      "parse correctly: hex id - e.g: \"0x01\"" in {
        val cleaned: TestNode = Utils.formatParsableForXml(wrongId_hex).asInstanceOf[TestNode]
        val c = new BpmnCreator(cleaned, cleaned.operationName)
        val tree = c.getBpmnTree
        assert(tree.nonEmpty)
      }
      "parse correctly: contains '#' - e.g: \"id#id\"" in {
        val cleaned: TestNode = Utils.formatParsableForXml(wrongId_startsWithHash).asInstanceOf[TestNode]
        val c = new BpmnCreator(cleaned, cleaned.operationName)
        val tree = c.getBpmnTree
        assert(tree.nonEmpty)
      }
      "parse correctly: starts with '.' - e.g: \".id\"" in {
        val cleaned: TestNode = Utils.formatParsableForXml(wrongId_startsWithPeriod).asInstanceOf[TestNode]
        val c = new BpmnCreator(cleaned, cleaned.operationName)
        val tree = c.getBpmnTree
        assert(tree.nonEmpty)
      }
      "parse correctly: contains space - e.g: \"id one\"" in {
        val cleaned: TestNode = Utils.formatParsableForXml(wrongId_containsSpace).asInstanceOf[TestNode]
        val c = new BpmnCreator(cleaned, cleaned.operationName)
        val tree = c.getBpmnTree
        assert(tree.nonEmpty)
      }

    }
    "its nodes have parent-child links" should {
      "parse to BPMN (built in-line)" in {
        val res = new BpmnCreator(inlineTree, inlineTree.operationName).getBpmnTree
        assert(res.nonEmpty)
      }
      "parse to BPMN (built procedurally)" in {
        val res = new BpmnCreator(proceduralTree, proceduralTree.operationName).getBpmnTree
        assert(res.nonEmpty)
      }
    }

    "containing a broken child-parent link (parentId != the parent's processId)" should {
      "parse to BPMN" in {
        val root = Utils.createTestNode("0", "Log in", "start_1")
        val orphanNode = Utils.createTestNode("wrong_parent_id", "lone child", "end_1")
        val parentNode = root.addChild(orphanNode)
        parentNode.printPretty()
        assert(new BpmnCreator(parentNode, parentNode.operationName).getBpmnTree.nonEmpty)
      }
    }
  }

  "A verified BpmnParsable file" when {
    "parsed" should {
      "produce a visualisable BPMN file (check on bpmn.io)" in {

        val rootNode: TestNode =
          Utils.createTestNode("0", "Log in", "start_1",
            List(
              Utils.createTestNode("start_1", "Eat potatoes", "potatoes_1", Nil),
              Utils.createTestNode("start_1", "Create Membership", "service_1",
                List(
                  Utils.createTestNode("service_1", "Show Main Page", "end_1")
                )
              ),
              Utils.createTestNode("start_1", "Find Membership", "service_2",
                List(
                  Utils.createTestNode("service_2", "Get user updates", "service_3",
                    List(Utils.createTestNode("service_3", "Show main page with updates", "end_2_3"))
                  ),
                  Utils.createTestNode("service_2", "Error 404", "id404"),
                  Utils.createTestNode("service_2", "Error 405|", "id405")
                )
              )
            )
          )

        val creator = new BpmnCreator(rootNode, rootNode.operationName)
        val testBpmn = creator.getBpmnTree
        testBpmn match {
          case None => println("Bpmn workflow building failed")
          case Some(bpmn) =>
            val loopBasedBpmnStr = creator.getBpmnXmlStr.get
            println(loopBasedBpmnStr)
            rootNode.printPretty()
            Utils.writeToExampleDir(loopBasedBpmnStr, "loop-based")
        }
      }
    }
  }


}
