package no.sysco.middleware.tramodana.builder

import java.util.concurrent.CountDownLatch
import java.util.{Properties, UUID}

import no.sysco.middleware.tramodana.builder.model.SpanTreeBuilder
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig, NewTopic}
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream.{KStream, Materialized, ValueJoiner}
import org.apache.kafka.streams.state.KeyValueStore

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionException


object BuilderApp extends App with JsonSpanProtocol {

  // topic names
  val SPANS_JSON_ORIGINAL = "spans-json-original"
  val SPANS = "spans"
  val TRACES = "traces"
  val PROCESSED_TRACES = "processed-traces"
  val TRACE_ID_ROOT_OPERATION = "trace-id-root-operation"
  val TRACE_ID_SEQ_SPAN = "trace-id-seq-span"
  val ROOT_SPAN_SEQ_SPAN = "root-span-seq-span"
  val ROOT_OPERATION_LIST_SEQ_SPAN = "root-operation-list-seq-span"

  // utils
  val EMPTY_KEY = ""

  start()


  def start(): Unit = {
    val props = getProps()
    preStart(props)

    val builder = new StreamsBuilder
    val topology = buildTopology(builder)
    val streams = new KafkaStreams(topology, props)

    addShutdownHook(streams, new CountDownLatch(1))
  }

  def addShutdownHook(streams: KafkaStreams, latch: CountDownLatch): Unit = {
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

  def getProps(): Properties = {
    val props = new Properties
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "Builder")
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props
  }


  def buildTopology(builder: StreamsBuilder): Topology = {
    import spray.json._

    /**
      * 1 - Original source
      * input: SPANS_JSON_ORIGINAL [trace_id, Span ]
      * */
    val originalSource: KStream[java.lang.String, String] =
      builder.stream[String, String](SPANS_JSON_ORIGINAL)

    /**
      * 2 - Traces. Group spans by trace_id
      * input: SPANS_JSON_ORIGINAL [trace_id, Span ]
      * output: TRACES [trace_id, List[Span] ]
      * */
    originalSource
      .groupByKey()
      // concatenate
      .reduce((value1, value2) => value1 + "," + value2)
      .toStream
      .mapValues(v => s"[$v]")
      .to(TRACES)



    /**
      * 3 - Spans
      * input: SPANS_JSON_ORIGINAL [trace_id, Span ]
      * output: SPANS [span_id, Span ]
      * */
    originalSource
      .map[String, String]((_, value) => {
      val span: Span = JsonParser(value).convertTo[Span]
      val spanId = span.spanId
      KeyValue.pair(spanId, value)
    })
      .to(SPANS)

    /**
      * 4 - Processed-traces. SpanTree of specific trace
      * input: TRACES [trace_id, List[Span] ]
      * output: PROCESSED_TRACES [trace_id, SpanTree ]
      * */
    val tracesSource: KStream[String, String] =
      builder.stream[String, String](TRACES)

    tracesSource.mapValues(v => {
      val spans = JsonParser(v).convertTo[List[Span]]
      val tree = SpanTreeBuilder.build(spans)
      tree.toJson.toString()
    })
      .to(PROCESSED_TRACES)


    /**
      * 5 - Root operation for specific trace.
      * input: PROCESSED_TRACES [trace_id, SpanTree ]
      * output: TRACE_ID_ROOT_OPERATION [trace_id, root-operation-name ]
      *
      * // NB! Topic should be manually created
      * */
    val processedTracesSource: KStream[String, String] =
      builder.stream[String, String](PROCESSED_TRACES)
    val traceIdRootOperationStream = processedTracesSource
      .mapValues(jsonTree => {
        val tree = JsonParser(jsonTree).convertTo[SpanTree]
        tree.value.operationName
      })
      .to(TRACE_ID_ROOT_OPERATION)

    /**
      * 6 tree to span seq
      * input: PROCESSED_TRACES [trace_id, SpanTree]
      * output: TRACE_ID_SEQ_SPAN [root_span, Seq[Span] ]
      * */
    val traceIdSeqOperation = processedTracesSource
      .mapValues(jsonTree => {
        val tree = JsonParser(jsonTree).convertTo[SpanTree]
        val seq = SpanTreeBuilder.getSequence(tree)
        seq.toJson.toString
      })
      .to(TRACE_ID_SEQ_SPAN)

    /**
      * 7 remap
      * input: TRACE_ID_SEQ_SPAN   [trace_id, Seq[Span] ]
      * output: ROOT_SPAN_SEQ_SPAN [root-Span, Seq[Span] ]
      * */
    val rootSpanSeqOperations = builder.stream[String, String](TRACE_ID_SEQ_SPAN)
        .map[String, String]((traceId, spanSeq)=>{
          val spans = JsonParser(spanSeq).convertTo[List[Span]]
          val  rootSpan : Span = spans.headOption.getOrElse(defaultSpan())
          KeyValue.pair[String, String](rootSpan.toJson.toString, spanSeq)
        })
        // TODO: filter for empty values
//        .filter((span, seq)=> JsonParser(span).convertTo[Span].spanId.trim.length>1)
        .to(ROOT_SPAN_SEQ_SPAN)


    /**
      * 8 all possible sequences
      * input: ROOT_SPAN_SEQ_SPAN [root-Span, Seq[Span] ]
      * output: ROOT_OPERATION_LIST_SEQ_SPAN [root-operation-name, List[ Seq[Span] ] ]
      * */
      builder.stream[String, String](ROOT_SPAN_SEQ_SPAN)
        .map[String, String]((rootSpanJson,spanSeqJson) => {
          KeyValue.pair(JsonParser(rootSpanJson).convertTo[Span].operationName, spanSeqJson)
        })
        .groupByKey()
        .reduce((value1, value2) => s"$value1 , $value2")
        .toStream()
        .mapValues(v => s"[$v]")
        .to(ROOT_OPERATION_LIST_SEQ_SPAN)

    // Todo: make Set instead of list
    // 9

    // TODO: 10 alternatively build [root-operation-name, SpanTree] -> [root-operation-name, Set[SpanTree]]

    builder.build

  }

  def defaultSpan():Span = {
    Span(
      traceId = EMPTY_KEY,
      spanId = EMPTY_KEY,
      spanHash = 0l,
      duration = 0l,
      flags = 0,
      logs = Option.empty,
      operationName = EMPTY_KEY,
      parentId = EMPTY_KEY,
      process = Option.empty,
      refs = Option.empty,
      startTime = 0l,
      tags = Option.empty
    )
  }

  /**
    * CREATE TOPICS in PRE_START stage
    **/

  //  // with existing props

  //
  def preStart(properties: Properties): Unit = {

    val adminClient: AdminClient = AdminClient.create(properties)
    val newTopics =
      Seq(
        new NewTopic(SPANS_JSON_ORIGINAL, 1, 1),
        new NewTopic(SPANS, 1, 1),
        new NewTopic(TRACES, 1, 1),
        new NewTopic(PROCESSED_TRACES, 1, 1),
        new NewTopic(TRACE_ID_ROOT_OPERATION, 1, 1),
        new NewTopic(TRACE_ID_SEQ_SPAN, 1, 1),
        new NewTopic(ROOT_SPAN_SEQ_SPAN, 1, 1),
        new NewTopic(ROOT_OPERATION_LIST_SEQ_SPAN, 1, 1)
      )
    try {
      val result = adminClient.createTopics(newTopics.asJava)
      result.all().get()
    } catch {
      case e: ExecutionException =>
        e.getCause match {
          case _: TopicExistsException =>
            println(s"Topics ${newTopics.map(_.name())} already exist")
        }
      case e: Throwable => throw e
    }
  }
}


