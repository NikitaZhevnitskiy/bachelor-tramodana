package no.sysco.middleware.tramodana.modeler

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import no.sysco.middleware.tramodana.schema.{JsonSpanProtocol, Span}
import spray.json._

import scala.annotation.tailrec

trait JsonSpanNodeProtocol extends JsonSpanProtocol with SprayJsonSupport with DefaultJsonProtocol {
  implicit def spanNodeFormat: JsonFormat[SpanNode] = lazyFormat(jsonFormat2(SpanNode))
}

class JsonToSpantreeParser(val jsonSrc: String) extends JsonSpanNodeProtocol {

  def applyXmlIdFormat(pId: String): String = {
    val pattern = "([^A-Za-z]+)".r
    pId match {
      case x if x.equals("#") => "id" ++ "_proc"
      case pattern(_) => "id_" ++ pId
      case _ => pId
    }
  }

  def preprocessSpan(s: Span): Span =
    s.copy(spanId = applyXmlIdFormat(s.spanId),
      parentId = applyXmlIdFormat(s.parentId))

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

    pp_iter(n.children.headOption)
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

  def rebuildGenealogy(tree: SpanNode): Option[SpanNode] = None

  def mergeTrees(treeList: List[SpanNode]): Option[SpanNode] = treeList.headOption

  def getSpanNodeList: List[SpanNode] = {
    JsonParser(jsonSrc).convertTo[List[SpanNode]]
  }

}

