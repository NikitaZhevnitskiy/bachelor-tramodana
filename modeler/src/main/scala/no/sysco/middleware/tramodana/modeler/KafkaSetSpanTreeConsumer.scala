package no.sysco.middleware.tramodana.modeler

import java.util.concurrent.Executors
import java.util.{Collections, Properties, UUID}

import no.sysco.middleware.tramodana.schema.{JsonSpanProtocol, SpanTree, Topic}
import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.serialization.StringDeserializer


// TODO: remove it
object KafkaSetSpanTreeConsumer extends App with JsonSpanProtocol {

  import spray.json._

  // create topics
  KafkaAdminUtils.preStart()

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
    consumer.subscribe(Collections.singletonList(Topic.ROOT_OPERATION_SET_SPAN_TREES))
    Executors.newSingleThreadExecutor.execute(() => {
      while (true) {
        // Your logic here
        val records: ConsumerRecords[String, String] = consumer.poll(1000)

//          records.forEach(r => println(s"KEY: ${r.key()} \nVALUE: ${r.value()}\n"))
          val itter = records.iterator()
          while(itter.hasNext){
            try {

              val record = itter.next()
              val setSeqSpanTrees: Set[SpanTree] = JsonParser(record.value()).convertTo[Set[SpanTree]]


              // ALL ur logic
              println(setSeqSpanTrees.toJson.toString)

            } catch {
              case e: Exception => { println("lol")}
            }
          }
      }
    })
  }


  // run consumer
  run()

}


