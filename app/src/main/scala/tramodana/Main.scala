package tramodana

object Main extends Greeting with App {
  lazy val greeting: String = "hello from appMain"
  println(greeting)
  println(Module1.greeting)
}

