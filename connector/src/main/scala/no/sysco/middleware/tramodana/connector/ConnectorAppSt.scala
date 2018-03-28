package no.sysco.middleware.tramodana.connector

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Source}
import com.datastax.driver.core.{Cluster, SimpleStatement}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import spray.json.JsonParser

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ConnectorAppSt extends App with JsonSpanProtocol {
  final val SPANS_ORIGINAL_TOPIC: String = "spans-json-original"

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

  val stmt = new SimpleStatement(s"SELECT json * FROM $keyspaceName.traces")
  val source = CassandraSource(stmt)
  //  val flow = Flow[Row].map(row => row.getString(0))
  val sourceCassandra: Source[String, NotUsed] = CassandraSource(stmt)
    .map(row => {
//      println(row)
      row.getString(0)
    })
  val flowParsing: Flow[String, ProducerRecord[String, String], NotUsed] =
    Flow
      .fromFunction[String, ProducerRecord[String, String]](
      json => {
        val span = JsonParser(json).convertTo[Span]
        val id = span.traceId
        val record = new ProducerRecord[java.lang.String, String](SPANS_ORIGINAL_TOPIC, id.toString, getJsonStringifyIds(span))
        println(record)
        record
      }
    )

  //  val sink = Sink.foreach[String](foo => println(foo))
  val kafkaProducerSettings = ProducerSettings
    .create(system, new StringSerializer(), new StringSerializer())
    .withBootstrapServers(connectorConfig.kafka.bootstrapServers)
  val sinkKafka = Producer.plainSink(kafkaProducerSettings)

  val graph = RunnableGraph.fromGraph(GraphDSL.create(sinkKafka){ implicit b =>
    sink =>
      import GraphDSL.Implicits._
      sourceCassandra.take(1000) ~> flowParsing ~> sink
      ClosedShape
  })

  val future = graph.run()

  future.onComplete{_ =>
    session.close()
    Await.result(system.terminate(), Duration.Inf)
  }
  Await.result(future, Duration.Inf)
  System.exit(0)
}
