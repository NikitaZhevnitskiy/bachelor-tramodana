package no.sysco.middleware.tramodana.builder

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import spray.json.{DefaultJsonProtocol, JsonParser, RootJsonFormat}


final case class Span(
                       traceId: String,
                       spanId: Long,
                       spanHash: Long,
                       duration: Long,
                       flags: Int,
                       logs: Option[List[Log]],
                       operationName: String,
                       parentId:String)


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
                        valueBinary: String)



trait JsonSpanProtocol extends SprayJsonSupport with DefaultJsonProtocol {


  implicit def spanFormat: RootJsonFormat[Span] = jsonFormat(Span, "trace_id", "span_id", "span_hash","duration", "flags", "logs", "operation_name","parent_id")
  implicit def spanLogFormat: RootJsonFormat[Log] = jsonFormat(Log, "ts", "fields")
  implicit def spanFieldFormat: RootJsonFormat[Field] = jsonFormat(Field, "key", "value_type", "value_string","value_bool","value_long","value_double","value_binary")

}

object MakeFun extends App with JsonSpanProtocol {

  val str =
    """
      |{
      |    "trace_id":"0x000000000000000087e760b2d75f518c",
      |    "span_id":8525301431701052675,
      |    "span_hash":-4287773619472038007,
      |    "duration":8749,
      |    "flags":1,
      |    "logs":[{"ts":1520595273626000,"fields":[{"key":"handler.class_simple_name","value_type":"string","value_string":"GreetingController","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"handler","value_type":"string","value_string":"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"event","value_type":"string","value_string":"preHandle","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"handler.method_name","value_type":"string","value_string":"getBookMethod","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}]}, {"ts":1520595273633000,"fields":[{"key":"event","value_type":"string","value_string":"afterCompletion","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"handler","value_type":"string","value_string":"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}]}],
      |    "operation_name":"getBookMethod",
      |    "parent_id":"791514443164284288",
      |    "process":{"service_name":"Shop","tags":[{"key":"hostname","value_type":"string","value_string":"nikita-lenovo-yoga-710-14ikb","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"jaeger.version","value_type":"string","value_string":"Java-0.20.0","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"ip","value_type":"string","value_string":"127.0.1.1","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}]},
      |    "refs":[],
      |    "start_time":1520595273625000,
      |    "tags":[{"key":"http.status_code","value_type":"int64","value_string":"","value_bool":false,"value_long":200,"value_double":0.0,"value_binary":"NULL"}, {"key":"component","value_type":"string","value_string":"java-web-servlet","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}, {"key":"span.kind","value_type":"string","value_string":"server","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}, {"key":"http.url","value_type":"string","value_string":"http://localhost:10082/getbook","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}, {"key":"http.method","value_type":"string","value_string":"GET","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}]
      |  }
    """.stripMargin

//  val str =
//    """
//      |{
//      |    "trace_id":"0x000000000000000087e760b2d75f518c",
//      |    "span_id":8525301431701052675,
//      |    "span_hash":-4287773619472038007,
//      |    "duration":8749,
//      |    "flags":1,
//      |    "logs":[
//      |       {
//      |         "ts":1520595273626000,
//      |         "fields":[
//      |             {
//      |               "key":"hostname",
//      |               "value_type":"string",
//      |               "value_string":"nikita-lenovo-yoga-710-14ikb",
//      |               "value_bool":false,
//      |               "value_long":0,
//      |               "value_double":0.0,
//      |               "value_binary":"NULL"
//      |             }
//      |         ]
//      |       }
//      |    ],
//      |    "operation_name":"getBookMethod"
//      |}
//    """.stripMargin

  println(str)
  println(JsonParser(str).convertTo[Span])
}
