package no.sysco.middleware.tramodana.builder

import java.util.Properties

import no.sysco.middleware.tramodana.schema.JsonSpanProtocol
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.{StreamsBuilder, StreamsConfig, Topology, TopologyTestDriver}
import org.scalatest.{BeforeAndAfter, WordSpec}

class StreamTopologySpec extends WordSpec with BeforeAndAfter {

  import spray.json._


  var sBuilder: StreamsBuilder = _
  var testDriver: TopologyTestDriver = _
  var topology: Topology = _


  before {
    sBuilder = new StreamsBuilder
    topology = BuilderApp.buildTopology(sBuilder)
    val config = new Properties
    config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "mocked")
    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "mockedTestId")
    config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "mocked")
  }

  after {
    testDriver.close()
  }

  "Testing" should {
    "be easy" in {
      println("Yolo")
    }
  }



//  "Stream topology " should {
//    // Arrange
//      // data
//    val list3Spans = Utils.getTraceWith3Spans()
//    val list1Span = Utils.getTraceWith1Span()
//
//      // test driver
//    val sBuilder = new StreamsBuilder
//    val topology: Topology = BuilderApp.buildTopology(sBuilder)
//    val config = new Properties
//    config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "mocked")
//    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "mockedTestId")
//    config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
//    config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
//    config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "mocked")
//    val testDriver: TopologyTestDriver = new TopologyTestDriver(topology, config)
//
//    "work correct" in {
//      println("yolo")
//
//
//    }
//  }

}
