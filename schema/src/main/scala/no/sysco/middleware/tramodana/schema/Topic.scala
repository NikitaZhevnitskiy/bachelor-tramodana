package no.sysco.middleware.tramodana.schema

import java.util.Properties
import java.util.concurrent.CountDownLatch

import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.streams.KafkaStreams

import scala.concurrent.ExecutionException
import scala.collection.JavaConverters._

object Topic {
  // topic names
  final val SPANS_ORIGINAL_TOPIC: String = "spans-json-original"
  final val SPANS_JSON_ORIGINAL = "spans-json-original"
  final val SPANS = "spans"
  final val TRACES = "traces"
  final val PROCESSED_TRACES = "processed-traces"
  final val TRACE_ID_ROOT_OPERATION = "trace-id-root-operation"
  final val TRACE_ID_SEQ_SPAN = "trace-id-seq-span"
  final val ROOT_SPAN_SEQ_SPAN = "root-span-seq-span"
  final val ROOT_OPERATION_LIST_SEQ_SPAN = "root-operation-list-seq-span"
  final val ROOT_OPERATION_SET_SEQ_SPANS = "root-operation-set-seq-spans"
  final val ROOT_OPERATION_SET_SPAN_TREES = "root-operation-set-spantree"
  final val ROOT_OPERATION_BPMN_XML = "root-operation-bpmn-xml"
  // tables
  final val ROOT_OPERATION_SET_SEQ_SPANS_TABLE = "root-operation-set-seq-spans-table"
  final val ROOT_OPERATION_SET_SPAN_TREES_TABLE = "root-operation-set-spantree-table"


  // utils
  final val EMPTY_KEY: String = ""

  def preStart(properties: Properties): Unit = {

    val adminClient: AdminClient = AdminClient.create(properties)
    val newTopics =
      Seq(

        new NewTopic(Topic.SPANS_ORIGINAL_TOPIC, 1, 1),
        new NewTopic(Topic.SPANS_JSON_ORIGINAL, 1, 1),
        new NewTopic(Topic.SPANS, 1, 1),
        new NewTopic(Topic.TRACES, 1, 1),
        new NewTopic(Topic.PROCESSED_TRACES, 1, 1),
        new NewTopic(Topic.TRACE_ID_ROOT_OPERATION, 1, 1),
        new NewTopic(Topic.TRACE_ID_SEQ_SPAN, 1, 1),
        new NewTopic(Topic.ROOT_SPAN_SEQ_SPAN, 1, 1),
        new NewTopic(Topic.ROOT_OPERATION_LIST_SEQ_SPAN, 1, 1),
        new NewTopic(Topic.ROOT_OPERATION_SET_SEQ_SPANS, 1, 1),
        new NewTopic(Topic.ROOT_OPERATION_SET_SPAN_TREES, 1, 1),
        new NewTopic(Topic.ROOT_OPERATION_BPMN_XML, 1, 1)
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
}
