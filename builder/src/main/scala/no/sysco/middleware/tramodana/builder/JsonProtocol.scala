package no.sysco.middleware.tramodana.builder

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsonParser, RootJsonFormat}

final case class Trace(traceId:Option[String], spans: Option[List[Span]])

final case class Span(
                       traceId: String,
                       spanId: String,
                       spanHash: Long,
                       duration: Long,
                       flags: Int,
                       logs: Option[List[Log]],
                       operationName: String,
                       parentId:String,
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
                      spanId: String)

final case class SpanTree(
                           operationName: String,
                           value: Span,
                           parent: Option[String] = None,
                           children: List[SpanTree] = List.empty)




trait JsonSpanProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  implicit def spanFormat: RootJsonFormat[Span] =
    jsonFormat(Span, "trace_id", "span_id", "span_hash","duration", "flags", "logs", "operation_name","parent_id", "process", "refs", "start_time", "tags")

  implicit def spanLogFormat: RootJsonFormat[Log] = jsonFormat(Log, "ts", "fields")
  implicit def logFieldFormat: RootJsonFormat[Field] = jsonFormat(Field, "key", "value_type", "value_string","value_bool","value_long","value_double","value_binary")
  implicit def spanProcessFormat: RootJsonFormat[Process] = jsonFormat(Process,"service_name", "tags")
  implicit def spanRefFormat: RootJsonFormat[Ref] = jsonFormat(Ref,"ref_type", "trace_id","span_id")
//  implicit def spanTree: RootJsonFormat[SpanTree] = jsonFormat4(SpanTree, "operation_name", "value", "parent","children")
  implicit def spanTree: RootJsonFormat[SpanTree] = jsonFormat4(SpanTree)
}

object MakeFun extends App with JsonSpanProtocol {

  val str =
    """
      |{
      |    "trace_id":"0x000000000000000087e760b2d75f518c",
      |    "span_id":"8525301431701052675",
      |    "span_hash":-4287773619472038007,
      |    "duration":8749,
      |    "flags":1,
      |    "logs":[{"ts":1520595273626000,"fields":[{"key":"handler.class_simple_name","value_type":"string","value_string":"GreetingController","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"handler","value_type":"string","value_string":"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"event","value_type":"string","value_string":"preHandle","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"handler.method_name","value_type":"string","value_string":"getBookMethod","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}]}, {"ts":1520595273633000,"fields":[{"key":"event","value_type":"string","value_string":"afterCompletion","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"handler","value_type":"string","value_string":"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}]}],
      |    "operation_name":"getBookMethod",
      |    "parent_id":"791514443164284288",
      |    "process":{"service_name":"Shop","tags":[{"key":"hostname","value_type":"string","value_string":"nikita-lenovo-yoga-710-14ikb","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"jaeger.version","value_type":"string","value_string":"Java-0.20.0","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"ip","value_type":"string","value_string":"127.0.1.1","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}]},
      |    "refs": [{
      |        "ref_type": "follows-from",
      |        "trace_id": "0x00000000000000007501dc93b11c4999",
      |        "span_id": "8431262504304003481"
      |    }],
      |    "start_time":1520595273625000,
      |    "tags":[{"key":"http.status_code","value_type":"int64","value_string":"","value_bool":false,"value_long":200,"value_double":0.0,"value_binary":"NULL"}, {"key":"component","value_type":"string","value_string":"java-web-servlet","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}, {"key":"span.kind","value_type":"string","value_string":"server","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}, {"key":"http.url","value_type":"string","value_string":"http://localhost:10082/getbook","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}, {"key":"http.method","value_type":"string","value_string":"GET","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}]
      |  }
    """.stripMargin

  val more =
    """
      |{"trace_id": "0x0000000000000000409796cd6e3ffe97", "span_id": "-6293894814040201916", "span_hash": 4205255064732529423, "duration": 19810, "flags": 1, "logs": [{"ts": 1520926782553000, "fields": [{"key": "handler.class_simple_name", "value_type": "string", "value_string": "GreetingController", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "handler", "value_type": "string", "value_string": "public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "event", "value_type": "string", "value_string": "preHandle", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "handler.method_name", "value_type": "string", "value_string": "getBookMethod", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}]}, {"ts": 1520926782565000, "fields": [{"key": "event", "value_type": "string", "value_string": "afterCompletion", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "handler", "value_type": "string", "value_string": "public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}]}], "operation_name": "getBookMethod", "parent_id": "7070879976147341838", "process": {"service_name": "Shop", "tags": [{"key": "hostname", "value_type": "string", "value_string": "nikita-lenovo-yoga-710-14ikb", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "jaeger.version", "value_type": "string", "value_string": "Java-0.20.0", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "ip", "value_type": "string", "value_string": "127.0.1.1", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}]}, "refs": null, "start_time": 1520926782546000, "tags": [{"key": "http.status_code", "value_type": "int64", "value_string": "", "value_bool": false, "value_long": 200, "value_double": 0.0, "value_binary": null}, {"key": "component", "value_type": "string", "value_string": "java-web-servlet", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "span.kind", "value_type": "string", "value_string": "server", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "http.url", "value_type": "string", "value_string": "http://localhost:10082/getbook", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "http.method", "value_type": "string", "value_string": "GET", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}]}
    """.stripMargin

  val kafkaSample1 = """{"trace_id":"0x0000000000000000409796cd6e3ffe97","span_id":"7070879976147341838","span_hash":-1581177895720236537,"duration":93913,"flags":1,"logs":[],"operation_name": "GET","parent_id": "4654354548972846743","process": {"service_name":"Human","tags":[{"key":"hostname","value_type":"string","value_string":"nikita-lenovo-yoga-710-14ikb","value_bool": false,"value_long":0,"value_double":0.0,"value_binary":null},{"key":"jaeger.version","value_type":"string","value_string":"Java-0.20.0","value_bool": false,"value_long":0,"value_double":0.0,"value_binary":null},{"key":"ip","value_type":"string","value_string":"127.0.1.1","value_bool": false,"value_long":0,"value_double":0.0,"value_binary":null}]},"refs": [], "start_time":1520926782477000} """
  val kafkaSample2 = """ {"trace_id":"0x0000000000000000409796cd6e3ffe97","span_id":"4654354548972846743","span_hash":4846262450321675764,"duration":131731,"flags":1,"logs":[{"ts":1520926782461000,"fields": [{"key":"handler.class_simple_name","value_type":"string","value_string":"GreetingController","value_bool": false,"value_long":0,"value_double":0.0,"value_binary":null},{"key":"handler","value_type":"string","value_string":"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()","value_bool": false,"value_long":0,"value_double":0.0,"value_binary":null},{"key":"event","value_type":"string","value_string":"preHandle","value_bool": false,"value_long":0,"value_double":0.0,"value_binary":null},{"key":"handler.method_name","value_type":"string","value_string":"buyBookMethod","value_bool": false,"value_long":0,"value_double":0.0,"value_binary":null}]},{"ts":1520926782583000,"fields": [{"key":"event","value_type":"string","value_string":"afterCompletion","value_bool": false,"value_long":0,"value_double":0.0,"value_binary":null},{"key":"handler","value_type":"string","value_string":"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()","value_bool": false,"value_long":0,"value_double":0.0,"value_binary":null}]}],"operation_name": "buyBookMethod","parent_id": "0","process": {"service_name":"Human","tags":[{"key":"hostname","value_type":"string","value_string":"nikita-lenovo-yoga-710-14ikb","value_bool": false,"value_long":0,"value_double":0.0,"value_binary":null},{"key":"jaeger.version","value_type":"string","value_string":"Java-0.20.0","value_bool": false,"value_long":0,"value_double":0.0,"value_binary":null},{"key":"ip","value_type":"string","value_string":"127.0.1.1","value_bool": false,"value_long":0,"value_double":0.0,"value_binary":null}]},"refs": [], "start_time":1520926782453000} """
  println(JsonParser(str).convertTo[Span])
  println(JsonParser(more).convertTo[Span])
    println(JsonParser(kafkaSample1).convertTo[Span])
    println(JsonParser(kafkaSample2).convertTo[Span])
}
