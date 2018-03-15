package no.sysco.middleware.tramodana.builder

import java.util.Properties
import java.util.concurrent.CountDownLatch

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.{KafkaStreams, KeyValue, StreamsBuilder, StreamsConfig}
import org.apache.kafka.streams.kstream.KStream
import spray.json.JsonParser


object BuilderApp extends App {
//  val configs = Configs.buildServerConfig()
//  val kafkaTraceConsumer = new KafkaTraceConsumer(configs.kafkaConfig.bootstrapServers)
//  kafkaTraceConsumer.run()
val props = new Properties
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "experiments")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
  props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)


  val builder = new StreamsBuilder

  // 1 - stream from kafka
  val source: KStream[java.lang.String, String] = builder.stream[String, String]("spans-json-original")


  // to TRACES topic
  source
    .groupByKey()
    // concatenate
    .reduce((value1, value2)=>value1+","+ value2)
    .toStream
    .mapValues(v=>s"[$v]")
    .to("traces")
  //    .foreach((k,v)=>println(k+s"values: $v"))


  // to SPANS topic
  source
    .map[java.lang.String, String]( (_,value) => {
    val span: Span = JsonParser(value).convertTo[Span]
    val traceId = java.lang.String.valueOf(span.spanId)
    println(span.spanId)
    println(traceId)
    new KeyValue[java.lang.String,String](traceId, value)
  })
    .to("spans")

  val topology = builder.build
  val streams = new KafkaStreams(topology, props)
  val latch = new CountDownLatch(1)

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
