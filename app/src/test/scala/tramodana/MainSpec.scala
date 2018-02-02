package tramodana

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class MainSpec extends FlatSpec with Matchers {
  "The Main object" should "say hello" in {
    Main.greeting shouldEqual "hello from appMain"
  }
}


