package module2

object Module2 extends Greeting with App {
  println(greeting)
}

trait Greeting {
  lazy val greeting: String = "hello from Module 2"
}
