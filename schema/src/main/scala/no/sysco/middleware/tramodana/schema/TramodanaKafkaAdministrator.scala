package no.sysco.middleware.tramodana.schema

import java.util.Properties
import java.util.concurrent.CountDownLatch

import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.streams.KafkaStreams
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionException

object TramodanaKafkaAdministrator {

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
        new NewTopic(Topic.ROOT_OPERATION_MERGED_SPAN_TREE, 1, 1),
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

  def addShutdownHook(streams: KafkaStreams, latch: CountDownLatch, appName: String): Unit = {
    // attach shutdown handler to catch control-c
    Runtime.getRuntime.addShutdownHook(new Thread(s"$appName-shutdown-hook") {
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
