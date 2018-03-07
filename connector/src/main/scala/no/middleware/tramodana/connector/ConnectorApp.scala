package no.middleware.tramodana.connector

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.stream.scaladsl.Sink
import com.datastax.driver.core._
import com.datastax.driver.mapping.{Mapper, MappingManager}
import no.middleware.tramodana.connector.CassandraTraceParser.Trace
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.collection.mutable.ListBuffer

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
//  val stmt = new SimpleStatement(s"SELECT * FROM $keyspaceName.traces").setFetchSize(200)
  val stmt = new SimpleStatement(s"SELECT blobAsInet(trace_id), span_id, span_hash, duration, flags, logs, operation_name, parent_id, process, refs, start_time, tags FROM $keyspaceName.traces").setFetchSize(200)
  val result = CassandraSource(stmt).runWith(Sink.seq)


  //  val logsCodec = TypeCodec.list(TypeCodec.custom(DataType.custom(Log.getClass.getCanonicalName)))
  //  CodecRegistry.DEFAULT_INSTANCE.register(logsCodec)

  val runnableGraph = result.foreach(rows => {
    rows.foreach(
      row => {
        val trace = CassandraTraceParser.parse(row)
        println(trace)
      })
  })

}

object CassandraTraceParser {

  case class Trace(
                    traceId: String,
                    spanId: Long,
                    spanHash: Long,
                    duration: Long,
                    flags: Int,
                    logsJson: String,
                    operationName: String,
                    parentId: Long,
                    processJson: String,
                    refsJson: String,
                    startTime: Long,
                    tagsJson: String
                  )

  // id looks like 0:0:0:0:580e:4402:fc7a:c24a
  def getStringId(canonicalHostName: String):String = {
    val arr = canonicalHostName.split(":")
    val id ="0x"+arr.toStream.map(elem => {
      if ("0".equals(elem)){
        "0000"
      } else {
        elem
      }
    })
      .mkString
    id
  }

  def parse(row: Row): Trace = {
    // id
//    val byteBuffer = row.get("trace_id", TypeCodec.blob())
//    val id = TypeCodec
//      .varint()
//      .deserialize(byteBuffer, ProtocolVersion.V5)
//      .longValue()
    val id = getStringId(row.get(0, TypeCodec.inet()).getCanonicalHostName)

    // span id
    val spanId = row.get("span_id", TypeCodec.bigint())

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
    val logsJson = getJsonLogs(row.getObject("logs").toString)

    // operation_name
    val operationName = row.get("operation_name", TypeCodec.varchar())

    // parent_id
    val parentId = row.get("parent_id", TypeCodec.bigint())

    // process
    val process = getJsonLogs(row.getObject("process").toString)

    // refs
    val refs = getJsonRefs(row.getObject("refs").toString)

    // start_time
    val startTime = row.get("start_time", TypeCodec.bigint())

    // tags
    val tagsJson = getJsonLogs(row.getObject("tags").toString)

    Trace(id, spanId, spanHash, duration, flags, logsJson, operationName, parentId, process, refs, startTime, tagsJson)
  }

  // Crutch !!!
  def getJsonLogs(logs: String):String = {
    logs
      .replace("ts:", "\"ts\":")
      .replace("fields:", "\"fields\":")
      .replace("key:", "\"key\":")
      .replace("value_type:", "\"value_type\":")
      .replace("value_string:", "\"value_string\":")
      .replace("value_bool:", "\"value_bool\":")
      .replace("value_long:", "\"value_long\":")
      .replace("value_double:", "\"value_double\":")
      .replace("value_binary:", "\"value_binary\":")
      .replace("NULL", "\"NULL\"")
      .replace("\'","\"")
  }

  def getJsonProcess(process: String):String ={
    getJsonLogs(process)
      .replace("service_name:", "\"service_name\":")
      .replace("tags:", "\"tags\":")
  }
  // CRUUUUUUUUTCH !!!!

  def getJsonRefs(refs: String):String = {
    var json = refs
      .replace("ref_type", "\"ref_type\"")
      .replace("trace_id", "\"trace_id\"")
      .replace("span_id", "\"span_id\"")
      .replace("\'","\"")

    // !!!
    val keyStart = "\"trace_id\":"
    val keyEnd = "\"span_id\":"
    var startIndex = json.trim.indexOf(keyStart)
    var endIndex = json.trim.indexOf(keyEnd)
    var listOfReplacement = new ListBuffer[String]()
    while(startIndex != -1 && endIndex != -1){
      val idReplace = json.substring(startIndex+keyStart.length, endIndex-1)
      listOfReplacement += idReplace
      startIndex = json.indexOf(keyStart,startIndex+keyStart.length)
      endIndex = json.trim.indexOf(keyEnd,endIndex+keyEnd.length)
    }

    listOfReplacement.foreach(replacement => {
      val to = "\""+replacement+"\""
//      println(json)
      json = json.replace(replacement,to)
    })

    json
  }

}


// For tests
object te extends App {
  var processRaw =
    """
      |{
      |   service_name:'Main Process',
      |   tags:[
      |     {
      |       key:'hostname',
      |       value_type:'string',
      |       value_string:'nikita-lenovo-yoga-710-14ikb',
      |       value_bool:false,
      |       value_long:0,
      |       value_double:0.0,
      |       value_binary:NULL
      |     },
      |     {
      |       key:'jaeger.version',
      |       value_type:'string',
      |       value_string:'Java-0.21.0',
      |       value_bool:false,
      |       value_long:0,
      |       value_double:0.0,
      |       value_binary:NULL
      |     },
      |     {
      |       key:'ip',
      |       value_type:'string',
      |       value_string:'127.0.1.1',
      |       value_bool:false,
      |       value_long:0,
      |       value_double:0.0,
      |       value_binary:NULL
      |     }
      |   ]
      | }""".stripMargin
  var processNew = CassandraTraceParser.getJsonProcess(processRaw)
  Json.parse(processNew)

  var refsRaw =
    """
      |[
      | {
      |   ref_type:'follows-from',trace_id:0x0000000000000000580e4402fc7ac24a,span_id:6345083704628134474
      | },
      | {
      |   ref_type:'follows-to',trace_id:0x0000000000000000580e4402fc7ac133,span_id:6345083704628134474
      | }
      |]
    """.stripMargin
  var refsNew = CassandraTraceParser.getJsonRefs(refsRaw.trim)
  Json.parse(refsNew)


  var tagsRaw ="[{key:'processId',value_type:'string',value_string:'1',value_bool:false,value_long:0,value_double:0.0,value_binary:NULL}, {key:'sampler.type',value_type:'string',value_string:'const',value_bool:false,value_long:0,value_double:0.0,value_binary:NULL}, {key:'sampler.param',value_type:'bool',value_string:'',value_bool:true,value_long:0,value_double:0.0,value_binary:NULL}]"
  var tagsNew = CassandraTraceParser.getJsonLogs(tagsRaw)
  Json.parse(tagsNew)


  val strIdRaw = "0:0:0:0:580e:4402:fc7a:c24a"
  println(CassandraTraceParser.getStringId(strIdRaw))
}