package no.sysco.middleware.tramodana.query

import java.util.concurrent.CountDownLatch

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import no.sysco.middleware.tramodana.schema.TramodanaKafkaAdministrator

import scala.concurrent.ExecutionContextExecutor


object QueryApp extends App {
  // config
  val config = AppConfig.load()
  // Akka
  implicit val system: ActorSystem = ActorSystem("tramodana-query-actor-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val streamsService = new StreamsService(config)
  val routes = new Routes(streamsService)

  // prestart
  TramodanaKafkaAdministrator.preStart(streamsService.props)
  // run http server
  val bindingFuture = Http().bindAndHandle(routes.route, config.http.host, config.http.port)
  // run streams
  TramodanaKafkaAdministrator.addShutdownHook(streamsService.streams, new CountDownLatch(1), config.name)
}
