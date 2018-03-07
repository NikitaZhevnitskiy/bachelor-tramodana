package no.middleware.tramodana.connector

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.stream.scaladsl.Sink
import com.datastax.driver.core._

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
  val keyspaceName = connectorConfig.cassandra.keyspace
  val stmt = new SimpleStatement(s"SELECT blobAsInet(trace_id), span_id, span_hash, duration, flags, logs, operation_name, parent_id, process, refs, start_time, tags FROM $keyspaceName.traces").setFetchSize(200)
  val result = CassandraSource(stmt).runWith(Sink.seq)


  //  val logsCodec = TypeCodec.list(TypeCodec.custom(DataType.custom(Log.getClass.getCanonicalName)))
  //  CodecRegistry.DEFAULT_INSTANCE.register(logsCodec)

  val runnableGraph = result.foreach(rows => {
    rows.foreach(
      row => {
        val span = CassandraSpanParser.parse(row)
        println(span)
      })
  })

}



