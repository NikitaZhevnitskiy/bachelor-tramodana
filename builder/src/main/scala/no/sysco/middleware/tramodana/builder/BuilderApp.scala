package no.sysco.middleware.tramodana.builder

object BuilderApp extends App {
  val configs = Configs.buildServerConfig()
  val kafkaTraceConsumer = new KafkaTraceConsumer(configs.kafkaConfig.bootstrapServers)
  kafkaTraceConsumer.run()
}
