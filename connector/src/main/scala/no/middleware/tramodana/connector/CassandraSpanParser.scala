package no.middleware.tramodana.connector

import com.datastax.driver.core.{Row, TypeCodec}
import no.middleware.tramodana.connector.CassandraSpanParser.Span
import play.api.libs.json.Json

import scala.collection.mutable.ListBuffer

object CassandraSpanParser {

  def parse(row: Row): Span = {
    // id
    //    val byteBuffer = row.get("trace_id", TypeCodec.blob())
    //    val id = TypeCodec
    //      .varint()
    //      .deserialize(byteBuffer, ProtocolVersion.V5)
    //      .longValue()
    val id = getStringId(row.get(0, TypeCodec.inet()).getCanonicalHostName)

    // span_id
    val spanId = row.get("span_id", TypeCodec.bigint())

    //span_hash
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
    val process = getJsonProcess(row.getObject("process").toString)

    // refs
    val refs = getJsonRefs(row.getObject("refs").toString)

    // start_time
    val startTime = row.get("start_time", TypeCodec.bigint())

    // tags
    val tagsJson = getJsonLogs(row.getObject("tags").toString)

    Span(id, spanId, spanHash, duration, flags, logsJson, operationName, parentId, process, refs, startTime, tagsJson)
  }

  def getJson(span: Span):String = {
    var json =
      s"""
        |{
        | "trace_id":"${span.traceId}",
        | "span_id":${span.spanId},
        | "span_hash":${span.spanHash},
        | "duration":${span.duration},
        | "flags":${span.flags},
        | "logs":${span.logsJson},
        | "operation_name":"${span.operationName}",
        | "parent_id":"${span.parentId}",
        | "process":${span.processJson},
        | "refs":${span.refsJson},
        | "start_time":${span.startTime},
        | "tags":${span.tagsJson}
        |}
      """.stripMargin
    json
  }

  // id looks like 0:0:0:0:580e:4402:fc7a:c24a
  def getStringId(canonicalHostName: String): String = {
    val arr = canonicalHostName.split(":")
    val id = "0x" + arr.toStream.map(elem => {
      if ("0".equals(elem)) {
        "0000"
      } else {
        elem
      }
    })
      .mkString
    id
  }

  def getJsonRefs(refs: String): String = {
    var json = refs
      .replace("ref_type", "\"ref_type\"")
      .replace("trace_id", "\"trace_id\"")
      .replace("span_id", "\"span_id\"")
      .replace("\'", "\"")

    // !!!
    val keyStart = "\"trace_id\":"
    val keyEnd = "\"span_id\":"
    var startIndex = json.trim.indexOf(keyStart)
    var endIndex = json.trim.indexOf(keyEnd)
    var listOfReplacement = new ListBuffer[String]()
    while (startIndex != -1 && endIndex != -1) {
      val idReplace = json.substring(startIndex + keyStart.length, endIndex - 1)
      listOfReplacement += idReplace
      startIndex = json.indexOf(keyStart, startIndex + keyStart.length)
      endIndex = json.trim.indexOf(keyEnd, endIndex + keyEnd.length)
    }

    listOfReplacement.foreach(replacement => {
      val to = "\"" + replacement + "\""
      //      println(json)
      json = json.replace(replacement, to)
    })

    json
  }

  def getJsonProcess(process: String): String = {
    getJsonLogs(process)
      .replace("service_name:", "\"service_name\":")
      .replace("tags:", "\"tags\":")
  }

  // Crutch !!!
  def getJsonLogs(logs: String): String = {
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
      .replace("\'", "\"")
  }

  // CRUUUUUUUUTCH !!!!

  case class Span(
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
                   tagsJson: String)
}


// For tests
// TODO: Rob
object ForTestingPurpose extends App {
  val strIdRaw = "0:0:0:0:580e:4402:fc7a:c24a"
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
  var processNew = CassandraSpanParser.getJsonProcess(processRaw)
  Json.parse(processNew)



  // getRefsJson
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
  var refsNew = CassandraSpanParser.getJsonRefs(refsRaw.trim)
  Json.parse(refsNew)

  var tagsRaw = "[{key:'processId',value_type:'string',value_string:'1',value_bool:false,value_long:0,value_double:0.0,value_binary:NULL}, {key:'sampler.type',value_type:'string',value_string:'const',value_bool:false,value_long:0,value_double:0.0,value_binary:NULL}, {key:'sampler.param',value_type:'bool',value_string:'',value_bool:true,value_long:0,value_double:0.0,value_binary:NULL}]"
  var tagsNew = CassandraSpanParser.getJsonLogs(tagsRaw)
  Json.parse(tagsNew)




  // getJson

//  traceId: String,
//  spanId: Long,
//  spanHash: Long,
//  duration: Long,
//  flags: Int,
//  logsJson: String,
//  operationName: String,
//  parentId: Long,
//  processJson: String,
//  refsJson: String,
//  startTime: Long,
//  tagsJson: String
// Span(0x0000000000000000580e4402fc7ac24a,-3934047479617254955,5466372416507478571,125197,1,[],buyingFoodSpan,0,{"service_name":"Sub Process","tags":[{"key":"hostname","value_type":"string","value_string":"nikita-lenovo-yoga-710-14ikb","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"jaeger.version","value_type":"string","value_string":"Java-0.21.0","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"ip","value_type":"string","value_string":"127.0.1.1","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}]},[{"ref_type":"follows-from","trace_id":"0x0000000000000000580e4402fc7ac24a","span_id":6345083704628134474}],1520414091728000,[{"key":"processId","value_type":"string","value_string":"1","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}])

  val span = Span(
    "0x0000000000000000580e4402fc7ac24a",
    1515896982295615193L,
    -3155976967717968692L,
    770919L,
    1,
    """
      |[{"ts":1520414091706000,"fields":[{"key":"event","value_type":"string","value_string":"goHomeLog","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}]}]
    """.stripMargin,
    "goHomeSpan",
    6345083704628134474L,
    """
      |{"service_name":"Main Process","tags":[{"key":"hostname","value_type":"string","value_string":"nikita-lenovo-yoga-710-14ikb","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"jaeger.version","value_type":"string","value_string":"Java-0.21.0","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"ip","value_type":"string","value_string":"127.0.1.1","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}]}
    """.stripMargin,
    """
      |[{"ref_type":"follows-from","trace_id":"0x0000000000000000580e4402fc7ac24a","span_id":6345083704628134474}]
    """.stripMargin,
    1520414091728000L,
    """
      |[{"key":"processId","value_type":"string","value_string":"1","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}]
    """.stripMargin
  )

//  println(CassandraSpanParser.getJson(span))
  Json.parse(CassandraSpanParser.getJson(span))
}