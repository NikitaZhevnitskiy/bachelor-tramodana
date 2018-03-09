package no.sysco.middleware.tramodana.builder

import java.util.concurrent.Executors
import java.util.{Collections, Properties, UUID}

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.serialization.StringDeserializer



class KafkaTraceConsumer(kafkaBootstrapServers: String) {

//  final val TOPIC_TRACES = "traces"
  final val SPANS_ORIGINAL_TOPIC:  String = "spans-original"
  final val LOGS_BROKER:  String = "logs_broker"

  val kafkaConsumerConfigs: Properties = {
    val configs = new Properties()
    configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, new StringDeserializer().getClass.getName)
    configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, new StringDeserializer().getClass.getName)
    configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    configs.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString)
    configs
  }

  val consumer = new KafkaConsumer[String, String](kafkaConsumerConfigs)

  def run() = {
    consumer.subscribe(Collections.singletonList(SPANS_ORIGINAL_TOPIC))
    Executors.newSingleThreadExecutor.execute(() => {
      while (true) {
        val records: ConsumerRecords[String, String] = consumer.poll(1000)
        records.forEach(r => println(r.value))
      }
    })
  }

}


