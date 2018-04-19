package no.sysco.middleware.tramodana.builder

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.JsonFormat
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


 case class SpanTree(value: Span, children: List[SpanTree])



trait JsonSpanProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  implicit def spanFormat: RootJsonFormat[Span] =
    jsonFormat(Span, "trace_id", "span_id", "span_hash","duration", "flags", "logs", "operation_name","parent_id", "process", "refs", "start_time", "tags")

  implicit def spanLogFormat: RootJsonFormat[Log] = jsonFormat(Log, "ts", "fields")
  implicit def logFieldFormat: RootJsonFormat[Field] = jsonFormat(Field, "key", "value_type", "value_string","value_bool","value_long","value_double","value_binary")
  implicit def spanProcessFormat: RootJsonFormat[Process] = jsonFormat(Process,"service_name", "tags")
  implicit def spanRefFormat: RootJsonFormat[Ref] = jsonFormat(Ref,"ref_type", "trace_id","span_id")
  // recursive
  // https://github.com/spray/spray-json#jsonformats-for-recursive-types
  implicit def node: JsonFormat[SpanTree] = lazyFormat(jsonFormat2(SpanTree))
}
