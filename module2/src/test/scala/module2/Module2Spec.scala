package module2

import org.scalatest._

class Module2Spec extends FlatSpec with Matchers {
  "The Module1 object" should "say hello" in {
    Module2.greeting shouldEqual "hello from Module 2"
  }
}
