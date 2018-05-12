package no.sysco.middleware.tramodana.query

import java.util.Properties
import java.util.concurrent.CountDownLatch

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import no.sysco.middleware.tramodana.schema.{Topic, TramodanaKafkaAdministrator}
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.{KeyValueIterator, KeyValueStore, QueryableStoreTypes, ReadOnlyKeyValueStore}
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig, Topology}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor


object QueryWebServer extends App {
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
  TramodanaKafkaAdministrator.addShutdownHook(streamsService.streams, new CountDownLatch(1))
}
