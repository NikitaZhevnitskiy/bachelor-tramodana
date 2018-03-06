package no.middleware.tramodana.connector

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.stream.scaladsl.Sink
import com.datastax.driver.core.{Cluster, Row, SimpleStatement}

object ConnectorApp extends App {

  //#init-mat
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  //#init-mat
  implicit val ec = system.dispatcher

  val connectorConfig = ConnectorConfig.buildServerConfig()
  //#init-session
  implicit val session = Cluster.builder
    .addContactPoint(connectorConfig.cassandra.host)
    .withPort(connectorConfig.cassandra.port)
    .build
    .connect()

  //#init-session
//  val keyspaceName = "jaeger_v1_dc1"
  val keyspaceName = connectorConfig.cassandra.keyspace
  val stmt = new SimpleStatement(s"SELECT * FROM $keyspaceName.traces").setFetchSize(20)
  val rows = CassandraSource(stmt).runWith(Sink.seq)
  val row : Option[Row] = Option.empty[Row]

  val runnableGraph = rows.foreach(row => println(row))

}
