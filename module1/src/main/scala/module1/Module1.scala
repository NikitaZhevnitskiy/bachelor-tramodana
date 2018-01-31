package module1

object Module1 extends Greeting with App {
  println(greeting)
}

trait Greeting {
  lazy val greeting: String = "hello from Module1"
}
