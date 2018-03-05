package no.middleware.tramodana.infrastructure.messaging

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.stream.scaladsl.Sink
import com.datastax.driver.core.{Cluster, SimpleStatement}

object Boot extends App {
  //#init-mat
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  //#init-mat

  implicit val ec = system.dispatcher

  //#init-session
  implicit val session = Cluster.builder
    .addContactPoint("127.0.0.1")
    .withPort(9042)
    .build
    .connect()
  //#init-session


  val keyspaceName = "jaeger_v1_dc1"
    val stmt = new SimpleStatement(s"SELECT * FROM $keyspaceName.traces").setFetchSize(20)
//  val stmt = new SimpleStatement(s"SELECT * FROM system_schema.columns").setFetchSize(20)
  val rows = CassandraSource(stmt).runWith(Sink.seq)

  val runnableGraph = rows.foreach(row => println(row))

}
