package no.middleware.tramodana.connector


import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.NotUsed
import akka.actor.{AbstractLoggingActor, ActorLogging, ActorSystem}
import akka.stream.ActorMaterializer
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Flow, Source}
import akka.kafka.ProducerSettings
import com.datastax.driver.core.{Cluster, SimpleStatement}
import no.middleware.tramodana.connector.CassandraSpanParser.Span
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{LongSerializer, StringSerializer}

object ConnectorApp extends App {

  final val SPANS_ORIGINAL_TOPIC:  String = "spans-original"

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


  // Source
  // 1 fetch cassandra data
  val sourceCassandra: Source[Span, NotUsed] = CassandraSource(stmt)
    .map(row => {
      println(row)
      val span = CassandraSpanParser.parse(row)
      span
    })

  // Flow
  // 2 parse cassandra data
  val flowParsing: Flow[Span, ProducerRecord[String, String], NotUsed] =
    Flow
      .fromFunction[Span, ProducerRecord[String, String]](
        span => new ProducerRecord[String, String](SPANS_ORIGINAL_TOPIC, span.spanId.toString, CassandraSpanParser.getJson(span))
    )

  //Sink
  //3 send to kafka
  val kafkaProducerSettings = ProducerSettings
    .create(system, new StringSerializer(), new StringSerializer())
    .withBootstrapServers(connectorConfig.kafka.bootstrapServers)
  val sinkKafka = Producer.plainSink(kafkaProducerSettings)


  // Run streams
  val runnableGraph = sourceCassandra.via(flowParsing).to(sinkKafka)
  runnableGraph.run()
}



