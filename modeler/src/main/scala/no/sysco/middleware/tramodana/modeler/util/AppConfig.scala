package no.sysco.middleware.tramodana.modeler.util

import com.typesafe.config.ConfigFactory

object AppConfig {

  private val modelerConfig = ConfigFactory.load().getConfig("modeler")
  private val appName: String = modelerConfig.getString("name")

  private final val kafka = KafkaConfig(ConfigFactory.load().getString("modeler.kafka.bootstrap-servers"))

  def load(): ModelerConfig = ModelerConfig(appName, kafka)

  case class ModelerConfig(name: String, kafka: KafkaConfig)

  case class KafkaConfig(bootstrapServers: String)

}
