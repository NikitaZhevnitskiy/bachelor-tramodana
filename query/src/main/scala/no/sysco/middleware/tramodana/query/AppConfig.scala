package no.sysco.middleware.tramodana.query

import com.typesafe.config.ConfigFactory

object AppConfig {

  private val queryConfig = ConfigFactory.load().getConfig("query")


  private val httpConfig = queryConfig.getConfig("http")
  private val appName: String = queryConfig.getString("name")
  private val kafka = KafkaConfig(queryConfig.getString("kafka.bootstrap-servers"), queryConfig.getString("kafka.storage-name"))
  private val http = HttpConfig(httpConfig.getString("host"), httpConfig.getInt("port"))

  def load(): QueryConfig = QueryConfig(appName, kafka, http)

  case class QueryConfig(name: String, kafka: KafkaConfig, http: HttpConfig)

  case class KafkaConfig(bootstrapServers: String, storageName: String)

  case class HttpConfig(host: String, port: Int)

}
