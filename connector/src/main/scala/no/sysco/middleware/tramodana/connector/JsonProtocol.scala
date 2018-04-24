package no.sysco.middleware.tramodana.connector

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

