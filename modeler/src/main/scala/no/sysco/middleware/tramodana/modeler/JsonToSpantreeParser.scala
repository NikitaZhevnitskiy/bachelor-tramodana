package no.sysco.middleware.tramodana.modeler

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import no.sysco.middleware.tramodana.schema.{JsonSpanProtocol, Span}
import spray.json._

import scala.annotation.tailrec

trait JsonSpanNodeProtocol extends JsonSpanProtocol with SprayJsonSupport with DefaultJsonProtocol {
  implicit def spanNodeFormat: JsonFormat[SpanNode] = lazyFormat(jsonFormat2(SpanNode))
}

class JsonToSpantreeParser(val jsonSrc: String) extends JsonSpanNodeProtocol {


  def preprocessSpan(s: Span): Span =
    s.copy(spanId = Utils.applyXmlIdFormat(s.spanId),
      parentId = Utils.applyXmlIdFormat(s.parentId))

  def preprocessNode(n: SpanNode): SpanNode = {
    val cleanedSpan = preprocessSpan(n.value)
    n.copy(value = cleanedSpan)
  }

  def flattenSpanNode(n: SpanNode): List[SpanNode] = {
    var nodes: List[SpanNode] = Nil

    @tailrec
    def pp_iter(in: Option[SpanNode]): Unit = {
      in match {
        case Some(node) => {
          nodes = node.copy(node.value, Nil) :: nodes
          pp_iter(node.children.headOption)
        }
        case None => ()
      }
    }

    pp_iter(Some(n))
    nodes
  }

  def preprocessSpanNode(n: SpanNode): SpanNode = {
    var nodes: List[SpanNode] = flattenSpanNode(n)
    nodes = nodes.map(node => node.copy(value = preprocessSpan(node.value)))

    var resNode = nodes.head
    nodes = nodes.tail
    while (nodes.nonEmpty) {
      nodes match {
        case x :: xs => {
          resNode = x.copy(children = resNode :: x.children)
          nodes = xs
        }
      }
      //val currentNode = nodes.head
      //(resNode, nodes) = (
      //  currentNode.copy( children = resNode :: currentNode.children ),
      //  nodes.tail
      // )
    }
    resNode
  }

  def preprocess(list: List[SpanNode]): List[SpanNode] = {
    list.map(node => preprocessSpanNode(node))
  }

  //TODO: link nodes by logical dependency (parent-child), not by reference (parentId-spanId)
  def rebuildGenealogy(tree: SpanNode): Option[SpanNode] = None

  //TODO: implement trace merging
  def mergeTraces(traceList: List[SpanNode]): Option[SpanNode] = traceList.headOption

  def getSpanNodeList: List[SpanNode] = {
    JsonParser(jsonSrc).convertTo[List[SpanNode]]
  }

}

