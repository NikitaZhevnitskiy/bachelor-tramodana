package no.sysco.middleware.tramodana.modeler

import java.util.Properties
import java.util.concurrent.CountDownLatch

import no.sysco.middleware.tramodana.modeler.util.AppConfig
import no.sysco.middleware.tramodana.schema.{Topic, TramodanaKafkaAdministrator}
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams._


object ModelerApp extends App {

  run

  def run: Unit = {

    val modelerConfig: AppConfig.ModelerConfig = AppConfig.load()
    val props = getProps(modelerConfig.name, modelerConfig.kafka.bootstrapServers)

    TramodanaKafkaAdministrator.preStart(props)

    val builder = new StreamsBuilder
    val topology = buildTopology(builder)
    val streams = new KafkaStreams(topology, props)

    TramodanaKafkaAdministrator.addShutdownHook(streams, new CountDownLatch(1), modelerConfig.name)
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

}
