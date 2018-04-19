package no.sysco.middleware.tramodana.modeler

import org.scalatest._

class StackTest extends WordSpec with BeforeAndAfter {

  var fullStack : Stack[Int] = new Stack
  var emptyStack : Stack[Int] = new Stack
  before {
   fullStack.pushAll(List(1,2,3,4))
  }

  after{
    fullStack.clear()
    emptyStack.clear()
  }

  "A full stack" when{
    "initialised with List(1,2,3,4)" should{
     "accept a list to be pushed on top" in {
       fullStack.pushAll(List(5,6))
       assert(fullStack.peek.get == 5)
       assert(fullStack.size == 6)
     }
      "return 1 when popping and have 2 as top" in{
        val popped = fullStack.pop.get
        assert(popped == 1)
        assert(fullStack.peek.get == 2)
      }

      "return 1 when peeked twice" in {
       val p1 = fullStack.peek.get
        assert(p1 == 1)
        assert(fullStack.peek.get == 1)
      }
    }
  }
}
