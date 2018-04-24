package no.sysco.middleware.tramodana.modeler
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BpmnCreatorSpec extends WordSpec with BeforeAndAfter {



  /*
                  start_1
                    /  \
                   /    \
                  /      \
          service_1       service_2
               /            /   \
              /            /     \
           end_1        end_2     404
   */

  val correctSpanTree: Node =
    Utils.createNode("0","Log in", "start_1",
      List(
        Utils.createNode("start_1","Create Membership", "service_1",
          List(
            Utils.createNode("service_1","Show Main Page", "end_1")
          )
        ),
        Utils.createNode( "start_1", "Find Membership", "service_2",
          List(
            Utils.createNode("service_2","Show main page with suggestions", "end_2"),
            Utils.createNode("service_2","Error 404", "404" )
          )
        )
      )
    )
  val uniqueNodeSpanTree: Node = Utils.createNode("0")
  val spanTreeWithWrongIdFormat: Node = Utils.createNode("0","1234", "#1234")

  var start =       Utils.createNode("0", "Log in", "start_1")

  before {
    var service_1 =   Utils.createNode("start_1", "Service 1", "service_1")
    var service_2 =   Utils.createNode("start_1", "Service 2", "service_2")
    var end_1 =   Utils.createNode("service_1", "End 1", "end_1")
    var end_2 =   Utils.createNode("service_2", "End 2", "end_2")
    var error =   Utils.createNode("service_2", "Error", "1234")

    service_1 = service_1.addChild( end_1 )
    service_2 = service_2.addChildren( end_2 :: error :: Nil)
    start = start.addChildren(service_1 :: service_2 :: Nil)
  }

  "A BpmnParsable element" when{
    "composed of only one node" should{
      "parse to BPMN without error" in{
        assertCompiles("BpmnCreator.parseToBpmn(uniqueNodeSpanTree)")
      }
    }
    "containing an id of the wrong format" should{
      "throw an error: all int id - e.g: \"1234\"" in{
        assertThrows[Exception]{
          BpmnCreator.parseToBpmn(start)
        }
      }
    }
    "its nodes have parent-child links" should{
      "parse to BPMN without error" in{
        assertCompiles("BpmnCreator.parseToBpmn(start)")
      }
    }
  }

}
