package no.sysco.middleware.tramodana.infrastructure.messaging.kafka
import java.util.concurrent.Executors
import java.util.{Collections, Properties}

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.serialization.StringSerializer



class KafkaTraceConsumer(kafkaBootstrapServers: String) {

  val kafkaConsumerConfigs: Properties = {
    val configs = new Properties()
    configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers)
    configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, new StringSerializer().getClass.getName)
    configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, new StringSerializer().getClass.getName)
    configs.put(ConsumerConfig.GROUP_ID_CONFIG, "some_id")
    configs
  }

  val consumer = new KafkaConsumer[String, String](kafkaConsumerConfigs)




  def run() = {
    consumer.subscribe(Collections.singletonList("topic"))
    Executors.newSingleThreadExecutor.execute(    new Runnable {
      override def run(): Unit = {
        while (true) {
          val records : ConsumerRecords[String, String] = consumer.poll(1000)

          for (record:ConsumerRecord[String,String] <- records) {
            System.out.println("Received message: (" + record.key + ", " + record.value + ") at offset " + record.offset())
          }
        }
      }
    })
  }

}
