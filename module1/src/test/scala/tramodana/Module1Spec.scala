package tramodana

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class Module1Spec extends FlatSpec with Matchers {
  "The Module1 object" should "say hello" in {
    Module1.greeting shouldEqual "hello from Module1"
  }
}
