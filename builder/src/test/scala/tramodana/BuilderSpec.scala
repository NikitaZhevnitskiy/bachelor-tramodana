package tramodana

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BuilderSpec extends FlatSpec with Matchers {
  "The Builder object" should "say hello" in {
    Builder.greeting shouldEqual "hello from Builder"
  }
}
