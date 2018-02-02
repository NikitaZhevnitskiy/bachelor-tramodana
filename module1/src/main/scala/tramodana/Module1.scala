package tramodana

object Module1 extends Greeting with App {
  lazy val greeting: String = "hello from Module1"
  println(greeting)
}

trait Greeting {
  val greeting: String
}
