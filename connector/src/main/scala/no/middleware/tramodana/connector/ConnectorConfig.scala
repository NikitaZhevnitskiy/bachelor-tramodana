package no.middleware.tramodana.connector

import com.typesafe.config.ConfigFactory

object ConnectorConfig {

  private final val kafka = KafkaConfig(ConfigFactory.load().getString("connector.kafka.bootstrap-servers"))
  private final val cassandra = CassandraConfig(
    ConfigFactory.load().getString("connector.cassandra.host"),
    ConfigFactory.load().getInt("connector.cassandra.port"),
    ConfigFactory.load().getString("connector.cassandra.keyspace")
  )

  def buildServerConfig(): ConnectorConfig = ConnectorConfig(kafka, cassandra)

  case class ConnectorConfig(kafka: KafkaConfig, cassandra: CassandraConfig)

  case class KafkaConfig(bootstrapServers: String)

  case class CassandraConfig(host: String, port: Int, keyspace: String)
}