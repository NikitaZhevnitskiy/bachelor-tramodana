package no.sysco.middleware.tramodana.builder

import java.util.{Properties, UUID}
import java.util.concurrent.CountDownLatch

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.{KafkaStreams, KeyValue, StreamsBuilder, StreamsConfig}
import org.apache.kafka.streams.kstream.KStream
import spray.json.JsonParser


object BuilderApp extends App with JsonSpanProtocol {
//  val configs = Configs.buildServerConfig()
//  val kafkaTraceConsumer = new KafkaTraceConsumer(configs.kafkaConfig.bootstrapServers)
//  kafkaTraceConsumer.run()
  val props = new Properties
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "experiments"+UUID.randomUUID().toString)
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
  props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)


  val builder = new StreamsBuilder

//   1 - stream from kafka
  val originalSource: KStream[java.lang.String, String] = builder.stream[String, String]("spans-json-original")


  // to TRACES topic [trace_id, List[Span]]
  originalSource
    .groupByKey()
    // concatenate
    .reduce((value1, value2)=>value1+","+ value2)
    .toStream
    .mapValues(v=>s"[$v]")
    .to("traces")


  // to SPANS topic [span_id, Span]
  originalSource
    .map[java.lang.String, String]( (_,value) => {
    val span: Span = JsonParser(value).convertTo[Span]
    val traceId = java.lang.String.valueOf(span.spanId)
    println(span.spanId)
    println(traceId)
    new KeyValue[java.lang.String,String](traceId, value)
  })
    .to("spans")


  // to Processed-traces [trace_id, Tree[Span] ]
  val tracesSource: KStream[String, String] = builder.stream[String,String]("traces")
  println("HERE: ")
  tracesSource.peek((k, v)=>{
    val list: List[Span] = JsonParser(v).convertTo[List[Span]]
    println(list)
//    list.foreach(span=>println(span.parentId))
  })

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
