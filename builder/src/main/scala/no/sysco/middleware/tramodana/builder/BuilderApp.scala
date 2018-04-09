package no.sysco.middleware.tramodana.builder

import java.util.{Properties, UUID}
import java.util.concurrent.CountDownLatch
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream.KStream
import spray.json.JsonParser


object BuilderApp extends App with JsonSpanProtocol {
  // topic names
  val SPANS_JSON_ORIGINAL = "spans-json-original"
  val SPANS = "spans"
  val TRACES = "traces"




  val props = getProps()
  val builder = new StreamsBuilder
  val topology = buildTopology(builder)
  val streams = new KafkaStreams(topology, props)

  addShutdownHook(streams, new CountDownLatch(1))



















  def addShutdownHook(streams: KafkaStreams, latch: CountDownLatch): Unit ={
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
  def getProps():Properties = {
    val props = new Properties
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "experiments"+UUID.randomUUID().toString)
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props
  }



  def buildTopology(builder: StreamsBuilder):Topology = {

    //   1 - stream from kafka
    val originalSource: KStream[java.lang.String, String] = builder.stream[String, String](SPANS_JSON_ORIGINAL)

    // to TRACES topic [trace_id, List[Span]]
    originalSource
      .groupByKey()
      // concatenate
      .reduce((value1, value2)=>value1+","+ value2)
      .toStream
      .mapValues(v=>s"[$v]")
      .to(TRACES)


    // to SPANS topic [span_id, Span]
    originalSource
      .map[java.lang.String, String]( (_,value) => {
      val span: Span = JsonParser(value).convertTo[Span]
      val traceId = java.lang.String.valueOf(span.spanId)
      println(span.spanId)
      println(traceId)
      new KeyValue[java.lang.String,String](traceId, value)
    })
      .to(SPANS)


    // TODO: to Processed-traces [trace_id, Tree[Span] ]
    val tracesSource: KStream[String, String] = builder.stream[String,String](TRACES)
    println("HERE: ")
    tracesSource.peek((k, v)=>{
      val list: List[Span] = JsonParser(v).convertTo[List[Span]]
      println(list)
      //    list.foreach(span=>println(span.parentId))
    })


    // build topology
    builder.build

  }
}
