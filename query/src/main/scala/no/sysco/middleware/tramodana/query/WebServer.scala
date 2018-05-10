package no.sysco.middleware.tramodana.query

import java.util.Properties
import java.util.concurrent.CountDownLatch

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import no.sysco.middleware.tramodana.schema.Topic
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.{KeyValueIterator, KeyValueStore, QueryableStoreTypes, ReadOnlyKeyValueStore}
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig, Topology}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.collection.JavaConverters._
import scala.collection.mutable


object WebServer extends App {

  // Akka
  implicit val system : ActorSystem= ActorSystem("my-system")
  implicit val materializer : ActorMaterializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Streams
  // prestart
  val STORAGE : String = s"storage"
  val props = getProps("example2", "localhost:9092")
  Topic.preStart(props)
  // run streams
  val builder = new StreamsBuilder
  val topology = buildTopology(builder)
  val streams = new KafkaStreams(topology, props)

  val route =
    path("key") {
      get {
        getValueByKey("hello")
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Get value by key via akka-http</h1>"))
      }
    } ~
    path("all"){
      get {
        getAllValues()
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Get all values via akka-http</h1>"))
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8888)
  // shutdown hook
  Topic.addShutdownHook(streams, new CountDownLatch(1))


  def getValueByKey(key: String): Unit =  {
    val storeType = QueryableStoreTypes.keyValueStore[String, String]()
    val keyValueStore: ReadOnlyKeyValueStore[String, String] = streams.store(STORAGE,storeType)
    println(keyValueStore.get(key))
  }

  def getAllValues():Unit = {
    val storeType = QueryableStoreTypes.keyValueStore[String, String]()
    val keyValueStore: ReadOnlyKeyValueStore[String, String] = streams.store(STORAGE,storeType)

    val it: KeyValueIterator[String, String] = keyValueStore.all()
    var list = new ListBuffer[String]()

    while (it.hasNext) {
      val nextKV = it.next
      println(s"${nextKV.key} : ${nextKV.value}")
      list += nextKV.value
    }

    println(s"FINAL : ${list.toList}")
  }


  def getProps(appName: String, kafkaBootstrapServer: String): Properties = {
    val props = new Properties
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer)
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, appName)
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer)
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, "localhost:8888")
    props
  }

  def buildTopology(builder: StreamsBuilder): Topology = {
    val oNameStorageXml =
      builder.table(
        Topic.ROOT_OPERATION_BPMN_XML,
        Materialized.as[String, String, KeyValueStore[Bytes, Array[Byte]]](STORAGE))
    builder.build()
  }
}
