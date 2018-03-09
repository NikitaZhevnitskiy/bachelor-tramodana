package no.sysco.middleware.tramodana.builder

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

object BuilderApp extends App {
  val configs = Configs.buildServerConfig()
  val kafkaTraceConsumer = new KafkaTraceConsumer(configs.kafkaConfig.bootstrapServers)
  kafkaTraceConsumer.run()
}
