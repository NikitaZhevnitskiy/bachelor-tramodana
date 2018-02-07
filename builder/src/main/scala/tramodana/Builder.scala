package tramodana

object Builder extends Greeting with App {
  lazy val greeting: String = "hello from Builder"
  println(greeting)
}

trait Greeting {
  val greeting: String
}
