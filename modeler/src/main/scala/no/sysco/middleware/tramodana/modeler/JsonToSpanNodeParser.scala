package no.sysco.middleware.tramodana.modeler

import no.sysco.middleware.tramodana.schema.{JsonSpanProtocol, Span}
import spray.json._

import scala.annotation.tailrec

/**
  * Tag trait for the Json parser to recognize SpanNode as a DTO
  */
trait JsonSpanNodeProtocol extends JsonSpanProtocol with DefaultJsonProtocol {
  implicit def spanNodeFormat: JsonFormat[SpanNode] = lazyFormat(jsonFormat2(SpanNode))
}

/**
  * Parse JSON string to List of SpanNode trees, format them for XML
  * and rebuild genealogy when merging all trees
  */
object JsonToSpanNodeParser extends JsonSpanNodeProtocol {


  def parse(jsonSource: String): Option[BpmnParsable] = {
    val spanNodes = getFormattedSpanNodes(jsonSource)
    Option(spanNodes.head)
  }

  def getFormattedSpanNodes(jsonSrc: String): List[SpanNode] =
    parseToSpanNodeList(jsonSrc).map(sn => TreeFormatter.formatIds(sn))

  def parseToSpanNodeList(json: String): List[SpanNode] = try {
    JsonParser(json).convertTo[List[SpanNode]]
  } catch {
    case e: DeserializationException =>
      throw DeserializationException(s"Could not parse Trace models list to List[SpanNode]: ${e.msg}")
  }

}

