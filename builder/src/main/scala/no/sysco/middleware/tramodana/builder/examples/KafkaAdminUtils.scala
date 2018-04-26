package no.sysco.middleware.tramodana.builder.examples

import java.util.Properties

import no.sysco.middleware.tramodana.schema.Topic
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig, NewTopic}
import org.apache.kafka.common.errors.TopicExistsException
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionException

object KafkaAdminUtils {

  val props = new Properties
  props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")


  def preStart(): Unit = {
    val adminClient: AdminClient = AdminClient.create(props)
    val newTopics =
      Seq(
        new NewTopic(Topic.ROOT_OPERATION_SET_SPAN_TREES, 1, 1)
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
