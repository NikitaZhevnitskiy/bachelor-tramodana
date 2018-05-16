package no.sysco.middleware.tramodana.builder



import java.util.Properties

import org.junit.Assert._
import no.sysco.middleware.tramodana.schema.{JsonSpanProtocol, Span, SpanTree, Topic}
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{Serdes, StringDeserializer, StringSerializer}
import org.apache.kafka.streams.state.{KeyValueStore, Stores}
import org.apache.kafka.streams.test.{ConsumerRecordFactory, OutputVerifier}
import org.apache.kafka.streams.{StreamsBuilder, StreamsConfig, Topology, TopologyTestDriver}
import org.scalatest.{BeforeAndAfter, WordSpec}

import scala.collection.mutable.ListBuffer

class StreamTopologySpec extends WordSpec with BeforeAndAfter with JsonSpanProtocol {

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

//    topology.addStateStore(
//      Stores.keyValueStoreBuilder(
//        Stores.inMemoryKeyValueStore("set-span-trees"),
//        Serdes.String(),
//        Serdes.String()).withLoggingDisabled(), // need to disable logging to allow store pre-populating
//      "reducer")
//    topology.addSink("sinkk", Topic.ROOT_OPERATION_SET_SPAN_TREES, "parent")
  }

  after {
    testDriver.close()
  }

  "Stream topology " should {

    "correct Topic.SPANS" in {
      // Arrange
      val spanListSuccessfullSimultation = Utils.getTraceWith3Spans()
      writeToKafka(Topic.SPANS_JSON_ORIGINAL, spanListSuccessfullSimultation)

      // Act
      val records = getRecords(Topic.SPANS)

      // Assert
      assertEquals(3, records.size)
    }

    "correct Topic.TRACES" in {

      // Arrange
      val spanListSuccessfullSimultation = Utils.getTraceWith3Spans()
      val spanListFailedSimultaion = Utils.getTraceWith1Span()
      writeToKafka(Topic.SPANS_JSON_ORIGINAL, spanListSuccessfullSimultation)
        // needs for accessing result of reduce operation inside this processor
      val tracesStorage: KeyValueStore[String, String] = testDriver.getKeyValueStore(Topic.TRACES_STORAGE)

      // Act
      val traceByKeyJson = s"[${tracesStorage.get(Utils.span1._1)}]"
      val traceSpans = JsonParser(traceByKeyJson).convertTo[Seq[Span]]

      // Assert
        // Trace has 3 spans
      assertEquals(3, traceSpans.size)
        // All spans has same trace id
      assertTrue(traceSpans.forall(span => span.traceId.equalsIgnoreCase(Utils.span1._1)))

    }

    "correct Topic.PROCESSED-TRACES" in {
      // Arrange
      val spanListSuccessfullSimultation = Utils.getTraceWith3Spans()
      val spanListFailedSimultaion = Utils.getTraceWith1Span()
      writeToKafka(Topic.SPANS_JSON_ORIGINAL, spanListSuccessfullSimultation)

      //Act
      val spanTrees = getRecords(Topic.PROCESSED_TRACES)
      val resultTree = JsonParser(spanTrees.reverse.head.value).convertTo[SpanTree]

      // Assert
      assertEquals(1, resultTree.children.size)
      assertEquals(1, resultTree.children.head.children.size)
      assertEquals(0, resultTree.children.head.children.head.children.size)
    }

    "correct Topic.ROOT_OPERATION_SET_SPAN_TREES" in {

      // Arrange
      val spanListSuccessfullSimultation = Utils.getTraceWith3Spans()
      val spanListFailedSimultaion = Utils.getTraceWith1Span()
      writeToKafka(Topic.SPANS_JSON_ORIGINAL, spanListFailedSimultaion)
      writeToKafka(Topic.SPANS_JSON_ORIGINAL, spanListSuccessfullSimultation)

      //Act
//      val value = testDriver.readOutput(Topic.ROOT_OPERATION_SET_SPAN_TREES, new StringDeserializer, new StringDeserializer)

      val lastRec = getLastRecord(Topic.ROOT_OPERATION_SET_SPAN_TREES)
      println(lastRec.value)
//      val spanTrees = getRecords(Topic.ROOT_OPERATION_SET_SPAN_TREES)
//      spanTrees.foreach(tree => println(tree.key+" : "+tree.value + "\n"))

//      val resultSet = JsonParser(spanTrees.reverse.head.value).convertTo[Set[SpanTree]]

//      // Assert
//      println(resultSet.size)
//      resultSet.foreach(spanT => println(s"${spanT.toJson}"))

    }


  }








  def writeToKafka(topic: String, dataTuples: List[(String, String)]) : Unit = {
    dataTuples
      .foreach(keyValue => testDriver.pipeInput(recordFactory.create(topic, keyValue._1, keyValue._2, 9999L)))
  }

  def getRecords(topic: String): List[ProducerRecord[String, String]] = {
    val stringDeserializer = new StringDeserializer
    var record: ProducerRecord[String, String] = null

    var list = new ListBuffer[ProducerRecord[String, String]]()
    do {
      record = testDriver.readOutput(topic, stringDeserializer, stringDeserializer)
      if (record != null) list += record
    } while (record != null)

    list.toList
  }

  def getLastRecord(topic: String): ProducerRecord[String, String] = {
    val stringDeserializer = new StringDeserializer
    var record: ProducerRecord[String, String] = null
    var tmp: ProducerRecord[String, String] = null

    do {
      tmp = testDriver.readOutput(topic, stringDeserializer, stringDeserializer)
      if (tmp != null) record=tmp
    } while (tmp != null)

    record
  }

  def printRecords(list: List[ProducerRecord[String, String]]): Unit = {
    list.foreach(record => println(s"${record.key} : ${record.value} \n"))
  }





}
