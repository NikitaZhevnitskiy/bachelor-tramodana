package no.middleware.tramodana.connector

import java.nio.ByteBuffer
import java.sql.Blob

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture.EnhancedFuture
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.stream.scaladsl.Sink
import com.datastax.driver.core.DataType.CustomType
import com.datastax.driver.core._
import com.datastax.driver.extras.codecs.MappingCodec
import no.middleware.tramodana.connector.CassandraParser.{Log, Trace}

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
//  val stmt = new SimpleStatement(s"SELECT blobAsText(trace_id), span_id FROM $keyspaceName.traces").setFetchSize(200)
  val stmt = new SimpleStatement(s"SELECT * FROM $keyspaceName.traces").setFetchSize(200)
  val result = CassandraSource(stmt).runWith(Sink.seq)
//  val row : Option[Row] = Option.empty[Row]


  val runnableGraph = result.foreach(rows => {
    rows.foreach(
      row => {

        // JSON format, parse this array as json !!!
        println(row.getObject("logs"))

    })
  })

}

object CassandraParser {
  case class Trace(traceId: Long, spanId: Long, spanHash: Long, duration: Long, flags: Int)
  case class Log(ts: DataType.CollectionType)

  def parse(row: Row): Trace = {
    // id
    val byteBuffer = row.get("trace_id",TypeCodec.blob())
    val id = TypeCodec
      .varint()
      .deserialize(byteBuffer,ProtocolVersion.V5)
      .longValue()

    // span id
    val spanId = row.get("span_id",TypeCodec.bigint())

    //span hash
    val spanHash = row.get("span_hash", TypeCodec.bigint())

    // duration
    val duration = row.get("duration", TypeCodec.bigint())

    // flags
    val flags = row.get("flags", TypeCodec.cint())

    // logs
//    val customType: CustomType = DataType.custom(Log.getClass.getCanonicalName)
//    val logs = row.get("logs", TypeCodec.list(TypeCodec.custom(customType)))
    // PARSE it as JSON array
    val logs = row.getObject("logs")


    Trace(id, spanId, spanHash, duration, flags)
  }
}