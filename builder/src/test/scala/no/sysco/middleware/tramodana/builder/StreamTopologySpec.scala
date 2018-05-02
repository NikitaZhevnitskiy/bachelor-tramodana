package no.sysco.middleware.tramodana.builder



import java.util.Properties

import org.junit.Assert._
import no.sysco.middleware.tramodana.schema.{JsonSpanProtocol, Topic}
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{Serdes, StringDeserializer, StringSerializer}
import org.apache.kafka.streams.test.{ConsumerRecordFactory, OutputVerifier}
import org.apache.kafka.streams.{StreamsBuilder, StreamsConfig, Topology, TopologyTestDriver}
import org.scalatest.{BeforeAndAfter, WordSpec}

import scala.collection.mutable.ListBuffer

class StreamTopologySpec extends WordSpec with BeforeAndAfter {

  import spray.json._


  var sBuilder: StreamsBuilder = _
  var testDriver: TopologyTestDriver = _
  var topology: Topology = _
  val recordFactory: ConsumerRecordFactory[String, String] = new ConsumerRecordFactory[String, String](new StringSerializer, new StringSerializer)

  before {
//    recordFactory = new ConsumerRecordFactory[String, String](new StringSerializer, new StringSerializer)
    sBuilder = new StreamsBuilder
    topology = BuilderApp.buildTopology(sBuilder)
    val config = new Properties
    config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "mocked")
    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "mockedTestId")
    config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "mocked")
    testDriver = new TopologyTestDriver(topology,config)
  }

  after {
    testDriver.close()
  }

  "Stream topology " should {
    "work correct" in {

      // Arrange
      val spanListSuccessfullSimultation = Utils.getTraceWith3Spans()
      val spanListFailedSimultaion = Utils.getTraceWith1Span()


      testDriver.pipeInput(recordFactory.create(Topic.SPANS_JSON_ORIGINAL, Utils.span1._1, Utils.span1._2))
//      writeToKafka(Topic.SPANS_JSON_ORIGINAL, spanListFailedSimultaion)
//      writeToKafka(Topic.SPANS_JSON_ORIGINAL, spanListSuccessfullSimultation)
      val record : ProducerRecord[String, String] = testDriver.readOutput(Topic.SPANS_JSON_ORIGINAL, new StringDeserializer, new StringDeserializer)
      println(record.toString)

      // Act
      val records = getRecords(Topic.SPANS_JSON_ORIGINAL)



      // hz
      assertEquals(4, records.size)

    }
  }

  def writeToKafka(topic: String, dataTuples: List[(String, String)]) : Unit = {
    dataTuples
      .foreach(keyValue => testDriver.pipeInput(recordFactory.create(topic, keyValue._1, keyValue._2, 9999L)))
  }

  def getRecords(topic: String): List[ProducerRecord[String, String]] = {
    var record: ProducerRecord[String, String] = null

    var list = new ListBuffer[ProducerRecord[String, String]]()
    do {
      record = testDriver.readOutput(topic, new StringDeserializer, new StringDeserializer)
      if (record != null) list += record
    } while (record != null)

    list.toList
  }





}
