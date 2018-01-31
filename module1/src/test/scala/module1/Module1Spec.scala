package module1

import org.scalatest._

class Module1Spec extends FlatSpec with Matchers {
  "The Module1 object" should "say hello" in {
    Module1.greeting shouldEqual "hello from Module1"
  }
}
