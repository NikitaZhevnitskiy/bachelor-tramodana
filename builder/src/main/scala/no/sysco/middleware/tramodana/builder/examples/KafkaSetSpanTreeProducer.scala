package no.sysco.middleware.tramodana.builder.examples

import java.util.Properties

import no.sysco.middleware.tramodana.schema.{ Topic}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import scala.io.Source

object KafkaSetSpanTreeProducer extends App {


  val kafkaProducerConfig: Properties = {

    val configs = new Properties()
    configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    configs.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaSetSpanTreeProducer")
    configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
//    configs.put(ProducerConfig.ACKS_CONFIG, "all")
//    configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true)
    configs
  }


  val producer = new KafkaProducer[String, String](kafkaProducerConfig)

  val key = "buyBookMethod"
  val value = getValuerForRecord("builder/src/main/resources/example/ROOT_OPERATION_SET_SPAN_TREES.json")
  val record = new ProducerRecord[String, String](Topic.ROOT_OPERATION_SET_SPAN_TREES, key, value)




  producer.send(record)



  producer.close()




  def getValuerForRecord(filename: String): String = {
    val v = Source.fromFile(filename).getLines().mkString
    v
  }
}
