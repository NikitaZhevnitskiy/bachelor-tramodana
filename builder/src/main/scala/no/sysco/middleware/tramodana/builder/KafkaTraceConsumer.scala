package no.sysco.middleware.tramodana.builder

import java.util.concurrent.Executors
import java.util.{Collections, Properties}

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.serialization.{StringDeserializer}



class KafkaTraceConsumer(kafkaBootstrapServers: String) {

  final val TOPIC_TRACES = "traces"

  val kafkaConsumerConfigs: Properties = {
    val configs = new Properties()
    configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers)
    configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, new StringDeserializer().getClass.getName)
    configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, new StringDeserializer().getClass.getName)
    configs.put(ConsumerConfig.GROUP_ID_CONFIG, "some_id")
    configs
  }

  val consumer = new KafkaConsumer[String, String](kafkaConsumerConfigs)


  def run() = {
    consumer.subscribe(Collections.singletonList(TOPIC_TRACES))
    Executors.newSingleThreadExecutor.execute(() => {
      while (true) {
        val records: ConsumerRecords[String, String] = consumer.poll(1000)
        records.forEach(r => println(r.value))
      }
    })
  }

}


