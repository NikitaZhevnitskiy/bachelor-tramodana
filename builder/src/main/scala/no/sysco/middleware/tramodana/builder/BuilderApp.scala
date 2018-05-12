package no.sysco.middleware.tramodana.builder

import java.util.Properties
import java.util.concurrent.CountDownLatch

import no.sysco.middleware.tramodana.schema.Topic._
import no.sysco.middleware.tramodana.schema._
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig, NewTopic}
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream.{KStream, Predicate}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionException


object BuilderApp extends App with JsonSpanProtocol {


  start()


  def start(): Unit = {
    val builderConfig: AppConfig.BuilderServerConfig = AppConfig.buildServerConfig()
    val props = getProps(builderConfig.name, builderConfig.kafkaConfig.bootstrapServers)

    TramodanaKafkaAdministrator.preStart(props)

    val builder = new StreamsBuilder
    val topology = buildTopology(builder)
    val streams = new KafkaStreams(topology, props)

    TramodanaKafkaAdministrator.addShutdownHook(streams, new CountDownLatch(1), builderConfig.name)
  }

//  def addShutdownHook(streams: KafkaStreams, latch: CountDownLatch): Unit = {
//    // attach shutdown handler to catch control-c
//    Runtime.getRuntime.addShutdownHook(new Thread("streams-experiments-shutdown-hook") {
//      override def run(): Unit = {
//        streams.close()
//        latch.countDown()
//      }
//    })
//
//    try {
//      streams.start()
//      latch.await()
//    } catch {
//      case e: Throwable =>
//        System.exit(1)
//    }
//    System.exit(0)
//  }

  def getProps(appName: String, kafkaBootstrapServer: String): Properties = {
    val props = new Properties
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer)
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, appName)
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer)
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    props
  }


  def buildTopology(builder: StreamsBuilder): Topology = {
    import spray.json._

    /**
      * 1 - Original source
      * input: SPANS_JSON_ORIGINAL [trace_id, Span ]
      **/
    val originalSource: KStream[java.lang.String, String] =
      builder.stream[String, String](Topic.SPANS_JSON_ORIGINAL)

    /**
      * 2 - Traces. Group spans by trace_id
      * input: SPANS_JSON_ORIGINAL [trace_id, Span ]
      * output: TRACES [trace_id, List[Span] ]
      **/
    originalSource
      .groupByKey()
      // concatenate
      .reduce((value1, value2) => value1 + "," + value2)
      .toStream
      .mapValues(v => s"[$v]")
      .to(Topic.TRACES)


    /**
      * 3 - Spans
      * input: SPANS_JSON_ORIGINAL [trace_id, Span ]
      * output: SPANS [span_id, Span ]
      **/
    originalSource
      .map[String, String]((_, value) => {
      val span: Span = JsonParser(value).convertTo[Span]
      val spanId = span.spanId
      KeyValue.pair(spanId, value)
    })
      .to(Topic.SPANS)

    /**
      * 4 - Processed-traces. SpanTree of specific trace
      * input: TRACES [trace_id, List[Span] ]
      * output: PROCESSED_TRACES [trace_id, SpanTree ]
      **/
    val tracesSource: KStream[String, String] =
      builder.stream[String, String](Topic.TRACES)

    tracesSource.mapValues(v => {
      val spans = JsonParser(v).convertTo[List[Span]]
      val tree = SpanTreeBuilder.build(spans)
      tree.toJson.toString()
    })
//      .filterNot((k,v) => v.toString.equalsIgnoreCase(EMPTY_KEY))
//      .peek((k,v)=>println(k))
      .to(Topic.PROCESSED_TRACES)


    /**
      * 5 - Root operation for specific trace.
      * input: PROCESSED_TRACES [trace_id, SpanTree ]
      * output: TRACE_ID_ROOT_OPERATION [trace_id, root-operation-name ]
      *
      * // NB! Topic should be manually created
      **/
    val processedTracesSource: KStream[String, String] =
      builder.stream[String, String](Topic.PROCESSED_TRACES)
    val traceIdRootOperationStream = processedTracesSource
      .mapValues(jsonTree => {
        val tree = JsonParser(jsonTree).convertTo[SpanTree]
        tree.value.operationName
      })
      .to(Topic.TRACE_ID_ROOT_OPERATION)

    /**
      * 6 tree to span seq
      * input: PROCESSED_TRACES [trace_id, SpanTree]
      * output: TRACE_ID_SEQ_SPAN [root_span, Seq[Span] ]
      **/
    val traceIdSeqOperation = processedTracesSource
      .mapValues(jsonTree => {
        val tree = JsonParser(jsonTree).convertTo[SpanTree]
        val seq = SpanTreeBuilder.getSequence(tree)
        seq.toJson.toString
      })
      .to(Topic.TRACE_ID_SEQ_SPAN)

    /**
      * 7 remap
      * input: TRACE_ID_SEQ_SPAN   [trace_id, Seq[Span] ]
      * output: ROOT_SPAN_SEQ_SPAN [root-Span, Seq[Span] ]
      **/
    val rootSpanSeqOperations = builder.stream[String, String](Topic.TRACE_ID_SEQ_SPAN)
      .map[String, String]((traceId, spanSeq) => {
      val spans = JsonParser(spanSeq).convertTo[List[Span]]
      val rootSpan: Span = spans.headOption.getOrElse(defaultSpan())
      KeyValue.pair[String, String](rootSpan.toJson.toString, spanSeq)
    })
      // TODO: filter for empty values
      //        .filter((span, seq)=> JsonParser(span).convertTo[Span].spanId.trim.length>1)
      .to(Topic.ROOT_SPAN_SEQ_SPAN)


    /**
      * 8 all possible sequences
      * input: ROOT_SPAN_SEQ_SPAN [root-Span, Seq[Span] ]
      * output: ROOT_OPERATION_LIST_SEQ_SPAN [root-operation-name, List[ Seq[Span] ] ]
      **/
    builder.stream[String, String](Topic.ROOT_SPAN_SEQ_SPAN)
      .map[String, String]((rootSpanJson, spanSeqJson) => {
      KeyValue.pair(JsonParser(rootSpanJson).convertTo[Span].operationName, spanSeqJson)
    })
      .groupByKey()
      .reduce((value1, value2) => s"$value1 , $value2")
      .toStream()
      .mapValues(v => s"[$v]")
      .to(Topic.ROOT_OPERATION_LIST_SEQ_SPAN)

    // Todo: make Set instead of list
    /**
      * 9 all possible sequences SET
      * input: ROOT_OPERATION_LIST_SEQ_SPAN [root-Span, Seq[Span] ]
      * output: ROOT_OPERATION_LIST_SEQ_SPAN [root-operation-name, List[ Seq[Span] ] ]
      **/
    builder.stream[String, String](Topic.ROOT_OPERATION_LIST_SEQ_SPAN)
        .map[String, String]((operationName, listOfSpanSeq)=>{
      val listOfSeq = JsonParser(listOfSpanSeq).convertTo[ List[ Seq[Span] ] ]
      val setOfSeq = SpanTreeBuilder.getSetOfSeq(listOfSeq)
      KeyValue.pair[String, String](operationName,setOfSeq.toJson.toString)
    })
      .to(Topic.ROOT_OPERATION_SET_SEQ_SPANS)


    /**
      * 10 build [root-operation-name, SpanTree] -> [root-operation-name, Set[SpanTree] ]
      * input: ROOT_OPERATION_SET_SEQ_SPANS [root-operation-name, List[ Seq[Span] ]
      * output: ROOT_OPERATION_SET_SPAN_TREES [root-operation-name, Set[ Seq[Span] ] ]
      * */
    builder.stream[String, String](Topic.ROOT_OPERATION_SET_SEQ_SPANS)
      .map[String, String]((k,v)=> {
        val setSeqSpans = JsonParser(v).convertTo[Set[Seq[Span]]]
        val setOfTrees: Set[SpanTree] = setSeqSpans.map((seq)=>SpanTreeBuilder.build(seq.toList))
        KeyValue.pair[String, String](k,setOfTrees.toJson.toString)
      })
      .to(Topic.ROOT_OPERATION_SET_SPAN_TREES)


    builder.build

  }

  def defaultSpan(): Span = {
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
}


