package no.sysco.middleware.tramodana.modeler

import java.util.Properties
import java.util.concurrent.CountDownLatch


import no.sysco.middleware.tramodana.modeler.util.AppConfig
import no.sysco.middleware.tramodana.schema.Topic
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig, NewTopic}
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.{KafkaStreams, KeyValue, StreamsBuilder, StreamsConfig, Topology, _}
import spray.json.DeserializationException

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionException
import scala.io.Source


object ModelerApp extends App {

  //  def run: Unit = {
  //    val INPUT_FILES_DIRECTORY = "examples/input_for_modeler"
  //
  //    val jsonSource: String = Source
  //      .fromFile(s"$INPUT_FILES_DIRECTORY/ROOT_OPERATION_SET_SPAN_TREES.json")
  //      .getLines
  //      .mkString
  //
  //    val parser: JsonToSpanNodeParser = new JsonToSpanNodeParser()
  //    val tree:  Option[BpmnParsable] = parser.parse(jsonSource)
  //
  //    val bpmnCreator = tree match {
  //      case Some(parsable) => new BpmnCreator(parsable, "00 test")
  //      case None => throw new Exception("SpanTrees could not be merged")
  //    }
  //
  //    val bpmnXmlString: String = bpmnCreator.getBpmnXmlStr.get
  //    println(bpmnXmlString)
  //  }

  run

  def run: Unit = {

    val modelerConfig: AppConfig.ModelerConfig = AppConfig.load()
    val props = getProps(modelerConfig.name, modelerConfig.kafka.bootstrapServers)
    preStart(props)

    val builder = new StreamsBuilder
    val topology = buildTopology(builder)
    val streams = new KafkaStreams(topology, props)

    addShutdownHook(streams, new CountDownLatch(1))
  }

  def getProps(appName: String, kafkaBootstrapServer: String): Properties = {
    val props = new Properties
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer)
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, appName)
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer)
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props
  }

  def buildTopology(builder: StreamsBuilder): Topology = {
    import spray.json._

    val spanTreesSource: KStream[String, String] =
      builder.stream[String, String](Topic.ROOT_OPERATION_SET_SPAN_TREES)

    spanTreesSource.map[String, String]((processName, setTrees) => {
      val tree: Option[BpmnParsable] = JsonToSpanNodeParser.parse(setTrees)
      val xml = tree match {
        case Some(parsable) => {
          new BpmnCreator(parsable, "00 test").getBpmnXmlStr.getOrElse(Topic.EMPTY_KEY)
        }
        case None => {
          println(s"Could not parse value $setTrees")
          Topic.EMPTY_KEY
        }
      }
      KeyValue.pair(processName, xml)
    })
      .filterNot((k, v) => Topic.EMPTY_KEY.equalsIgnoreCase(v))
      .to(Topic.ROOT_OPERATION_BPMN_XML)


    builder.build()
  }

  def preStart(properties: Properties): Unit = {

    val adminClient: AdminClient = AdminClient.create(properties)
    val newTopics =
      Seq(
        new NewTopic(Topic.ROOT_OPERATION_SET_SPAN_TREES, 1, 1),
        new NewTopic(Topic.ROOT_OPERATION_BPMN_XML, 1, 1)
      )
    try {
      val result = adminClient.createTopics(newTopics.asJava)
      result.all().get()
    } catch {
      case e: ExecutionException =>
        e.getCause match {
          case _: TopicExistsException =>
            println(s"Topics ${newTopics.map(_.name())} already exist")
        }
      case e: Throwable => throw e
    }
  }

  def addShutdownHook(streams: KafkaStreams, latch: CountDownLatch): Unit = {
    // attach shutdown handler to catch control-c
    Runtime.getRuntime.addShutdownHook(new Thread("streams-experiments-shutdown-hook") {
      override def run(): Unit = {
        streams.close()
        latch.countDown()
      }
    })

    try {
      streams.start()
      latch.await()
    } catch {
      case e: Throwable =>
        System.exit(1)
    }
    System.exit(0)
  }



}
