package no.middleware.tramodana.connector

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, DeserializationException, JsArray, JsNumber, JsValue, JsonParser, RootJsonFormat}


final case class Span(
                       traceId: String,
                       spanId: Long,
                       spanHash: Long,
                       duration: Long,
                       flags: Int,
                       logs: Option[List[Log]],
                       operationName: String,
                       parentId: Option[Long],
                       process:Option[Process],
                       refs: Option[List[Ref]],
                       startTime: Long,
                       tags: Option[List[Field]]
                     )


final case class Log(
                      ts: Long,
                      fields: Option[List[Field]])

final case class Field(
                        key: String,
                        valueType: String,
                        valueString: String,
                        valueBool: Boolean,
                        valueLong: Long,
                        valueDouble: Double,
                        valueBinary: Option[String])

final case class Process(
                          serviceName: String,
                          tags: Option[List[Field]])

final case class Ref(
                      refType: String,
                      traceId: String,
                      spanId: Long)




trait JsonSpanProtocol extends SprayJsonSupport with DefaultJsonProtocol {


  implicit def spanFormat: RootJsonFormat[Span] =
    jsonFormat(Span, "trace_id", "span_id", "span_hash","duration", "flags", "logs", "operation_name","parent_id", "process", "refs", "start_time", "tags")

  implicit def spanLogFormat: RootJsonFormat[Log] = jsonFormat(Log, "ts", "fields")
  implicit def logFieldFormat: RootJsonFormat[Field] = jsonFormat(Field, "key", "value_type", "value_string","value_bool","value_long","value_double","value_binary")
  implicit def spanProcessFormat: RootJsonFormat[Process] = jsonFormat(Process,"service_name", "tags")
  implicit def spanRefFormat: RootJsonFormat[Ref] = jsonFormat(Ref,"ref_type", "trace_id","span_id")

  def getJsonStringifyIds(span: Span): String = {
    s"""{"trace_id":"${span.traceId}","span_id":"${span.spanId}","span_hash":${span.spanHash},"duration":${span.duration},"flags":${span.flags},"logs":[${getJsonLogs(span.logs)}],"operation_name": "${span.operationName}","parent_id": "${span.parentId.getOrElse("0")}","process": ${getJsonProcess(span.process)},"refs": ${getJsonRefs(span.refs)}, "start_time":${span.startTime}}""".stripMargin
  }
  def getJsonLogs(logs: Option[List[Log]]):String ={
    logs match {
      case Some(value) => {
        val logsJson = value.map(logs => {
          s"""{"ts":${logs.ts},"fields": [${getJsonField(logs.fields)}]},""".stripMargin
        }).mkString
        logsJson.substring(0, logsJson.length-1)
      }
      case None => ""
    }
  }

  def getJsonField(field: Option[List[Field]]):String = {
    field match {
      case Some(values) => {
        val fieldsJson = values.map(
          field => {
            s"""{"key":"${field.key}","value_type":"${field.valueType}","value_string":"${field.valueString}","value_bool": ${field.valueBool},"value_long":${field.valueLong},"value_double":${field.valueDouble},"value_binary":null},""".stripMargin
          }
        ).mkString
        fieldsJson.substring(0, fieldsJson.length-1)
      }
      case None => ""
    }
  }

  def getJsonProcess(process: Option[Process]): String = {
    process match {
      case Some(value) =>
            s"""{"service_name":"${value.serviceName}","tags":[${getJsonField(value.tags)}]}""".stripMargin
      case None => "{}"
    }
  }

  def getJsonRefs(refs: Option[List[Ref]]): String = {
    refs match {
      case Some(values) => {
        val refJson = values.map(ref => {
          s"""{"ref_type":"${ref.refType}","trace_id": "${ref.traceId}","span_id": "${ref.spanId}"},""".stripMargin
        }).mkString
      "["+refJson.substring(0,refJson.length -1)+"]"
      }
      case None => "[]"
    }
  }

}

object MakeFun extends App with JsonSpanProtocol {



  val more =
    """
      |{
      |   "trace_id": "0x0000000000000000409796cd6e3ffe97",
      |   "span_id": -6293894814040201916,
      |   "span_hash": 4205255064732529423,
      |   "duration": 19810,
      |   "flags": 1,
      |   "logs": [{"ts": 1520926782553000,
      |     "fields": [
      |       {"key": "handler.class_simple_name", "value_type": "string", "value_string": "GreetingController", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "handler", "value_type": "string", "value_string": "public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "event", "value_type": "string", "value_string": "preHandle", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "handler.method_name", "value_type": "string", "value_string": "getBookMethod", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}]}, {"ts": 1520926782565000, "fields": [{"key": "event", "value_type": "string", "value_string": "afterCompletion", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "handler", "value_type": "string", "value_string": "public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}]}
      |     ],
      |   "operation_name": "getBookMethod",
      |   "parent_id": 7070879976147341838,
      |   "process": {"service_name": "Shop", "tags": [{"key": "hostname", "value_type": "string", "value_string": "nikita-lenovo-yoga-710-14ikb", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "jaeger.version", "value_type": "string", "value_string": "Java-0.20.0", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "ip", "value_type": "string", "value_string": "127.0.1.1", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}]},
      |   "refs": [{
      |        "ref_type": "follows-from",
      |        "trace_id": "0x00000000000000007501dc93b11c4999",
      |        "span_id": 8431262504304003481
      |    }],
      |   "start_time": 1520926782546000,
      |   "tags": [{"key": "http.status_code", "value_type": "int64", "value_string": "", "value_bool": false, "value_long": 200, "value_double": 0.0, "value_binary": null}, {"key": "component", "value_type": "string", "value_string": "java-web-servlet", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "span.kind", "value_type": "string", "value_string": "server", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "http.url", "value_type": "string", "value_string": "http://localhost:10082/getbook", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "http.method", "value_type": "string", "value_string": "GET", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}]
      |}
      |""".stripMargin

//    val fieldsList: Option[List[Field]] = Option.apply[List[Field]](
//      List(
//        Field("event","string","afterCompletion",false,0,0.0,None),
//        Field("event","string","afterCompletion",false,0,0.0,None)))
//
//    println(getJsonField(fieldsList))

//   val logs = Some(List(
//     Log(1520926782551234L,Some(List(
//       Field("handler.class_simple_name","string","GreetingController",false,0,0.0,None),
//       Field("event","string","preHandle",false,0,0.0,None)))),
//     Log(1520926782555678L,Some(List(
//       Field("event","string","afterCompletion",false,0,0.0,None))))))
//
//  println(getJsonLogs(logs))



  val span = JsonParser(more).convertTo[Span]
  println(getJsonStringifyIds(span))




}
