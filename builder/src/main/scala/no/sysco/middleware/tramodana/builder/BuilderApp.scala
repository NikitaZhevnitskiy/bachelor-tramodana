package no.sysco.middleware.tramodana.builder

import java.util.concurrent.CountDownLatch
import java.util.{Properties, UUID}

import no.sysco.middleware.tramodana.builder.model.SpanTreeBuilder
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream.KStream



object BuilderApp extends App with JsonSpanProtocol {

  // topic names
  val SPANS_JSON_ORIGINAL = "spans-json-original"
  val SPANS = "spans"
  val TRACES = "traces"
  val PROCESSED_TRACES = "processed-traces"
  val TRACE_ID_ROOT_OPERATION = "trace-id-root-operation"
  val TRACE_ID_SEQ_OPERATIONS = "trace-id-seq-operations"



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
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, this.getClass.getSimpleName+UUID.randomUUID().toString)
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props
  }



  def buildTopology(builder: StreamsBuilder):Topology = {
    import spray.json._

    //   1 - stream from kafka
    val originalSource: KStream[java.lang.String, String] =
      builder.stream[String, String](SPANS_JSON_ORIGINAL)

    // 2 to TRACES topic [trace_id, List[Span]]
    originalSource
      .groupByKey()
      // concatenate
      .reduce((value1, value2)=>value1+","+ value2)
      .toStream
      .mapValues(v=>s"[$v]")
      .to(TRACES)


    // 3 to SPANS topic [span_id, Span]
    originalSource
      .map[java.lang.String, String]( (_,value) => {
      val span: Span = JsonParser(value).convertTo[Span]
      val traceId = java.lang.String.valueOf(span.spanId)
      println(span.spanId)
      println(traceId)
      new KeyValue[java.lang.String,String](traceId, value)
    })
      .to(SPANS)

    // 4 Processed-traces [trace_id, Tree[Span] ]
    val tracesSource: KStream[String, String] =
      builder.stream[String,String](TRACES)


    tracesSource.mapValues(v => {
      val spans = JsonParser(v).convertTo[List[Span]]
      val tree = SpanTreeBuilder.build(spans)
      tree.toJson.toString()
    })
      .to(PROCESSED_TRACES)


    // 5 source
    val processedTracesSource: KStream[String, String] =
      builder.stream[String, String](PROCESSED_TRACES)

    // 6 Trace-id-root-operation [trace_id, String]
    processedTracesSource
      .mapValues(jsonTree => {
        val tree = JsonParser(jsonTree).convertTo[SpanTree]
        tree.value.operationName
      })
        .to(TRACE_ID_ROOT_OPERATION)

    // 7 trace-id-seq-operations [trace_id, Seq[Span]]
    processedTracesSource
        .mapValues(jsonTree => {
          val tree = JsonParser(jsonTree).convertTo[SpanTree]
          val seq = SpanTreeBuilder.getSequence(tree)
          seq.toJson.toString
        })
        .to(TRACE_ID_SEQ_OPERATIONS)


    // build topology
    builder.build

  }



  /**
    * CREATE TOPICS in PRE_START stage
    * */

//  // with existing props
//  def createAdminClient(properties: Properties): AdminClient = {
//    val configs = {
//      properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG))
//      properties
//    }
//    AdminClient.create(configs)
//  }
//
//  def preStart(): Unit = {
//    val adminClient: AdminClient = createAdminClient()
//    val newTopics =
//      Seq(
//        new NewTopic(SPANS_JSON_ORIGINAL, 1, 1),
//        new NewTopic(SPANS, 1, 1),
//        new NewTopic(TRACES, 1, 1),
//        new NewTopic(PROCESSED_TRACES, 1, 1),
//        new NewTopic(TRACE_ID_ROOT_OPERATION, 1, 1),
//        new NewTopic(TRACE_ID_SEQ_OPERATIONS, 1, 1)
//      )
//    try {
//      val result = adminClient.createTopics(newTopics.asJava)
//      result.all().get()
//    } catch {
//      case e: ExecutionException =>
//        e.getCause match {
//          case _: TopicExistsException =>
//            println(s"Topics ${newTopics.map(_.name())} already exist")
//        }
//      case e: Throwable => throw e
//    }
//  }
}


