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
    val rootParentId = spanNodes.head.getParentId
    mergeIntoTree(spanNodes, rootParentId)
  }

  def getFormattedSpanNodes(jsonSrc: String): List[SpanNode] =
    parseToSpanNodeList(jsonSrc).map(sn => TreeFormatter.formatIds(sn))

  def parseToSpanNodeList(json: String): List[SpanNode] = try {
    JsonParser(json).convertTo[List[SpanNode]]
  } catch {
    case e: DeserializationException =>
      throw DeserializationException(s"Could not parse Trace models list to List[SpanNode]: ${e.msg}")
  }

  def mergeIntoTree(traceList: List[SpanNode], rootId: String): Option[SpanNode] = {
    val edges: Set[SpanEdge] = traceList.flatMap(sn => splitIntoEdges(Some(sn), Set.empty)).toSet

    try {
      val tree = build(edges, rootId)
      Some(tree)
    }
    catch {
      case e: SerializationException =>
        println(e.getMessage)
        None
    }
  }

  @tailrec
  final def splitIntoEdges(trace: Option[SpanNode], edges: Set[SpanEdge]): Set[SpanEdge] = {
    trace match {
      case Some(SpanNode(s, x :: _)) => splitIntoEdges(Some(x), edges + new SpanEdge(s, Some(x.value)))
      case Some(SpanNode(s, Nil)) => splitIntoEdges(None, edges + new SpanEdge(s, None))
      case None => edges
    }
  }


  private def build(edges: Set[SpanEdge], rootId: String): SpanNode = {
    val rootEdge = edges.find(se => se.from.parentId.equals(rootId))
    rootEdge match {
      case Some(r) =>
        val rootNode = SpanNode(r.from, Nil)
        val children = findChildren(r.from, edges.toList)
        buildIter(rootNode, children, children.size, edges.toList)
      case None => throw new IllegalArgumentException(s"Could not find node with parent '$rootId'")
    }
  }

  @tailrec
  private def buildIter(acc: SpanNode,
                        edgeStack: List[SpanNode],
                        childCount: Int,
                        unprocessed: List[SpanEdge]): SpanNode = {
    (childCount, edgeStack) match {
      case (_, Nil) =>
        acc // end case: nothing left to process
      case (0, head :: tail) =>
        // previous root becomes acc again
        // append current root to children of previous root
        buildIter(head.copy(children = head.children :+ acc), tail, findChildren(head.value, unprocessed).size - head.children.size - 1, unprocessed)

      case (_, head :: tail) =>
        val headChildren = findChildren(head.value, unprocessed)
        headChildren match {
          case Nil =>
            // if has children
            //    sort them
            //    add them on stack
            //    first one becomes accumulator for next recursion
            buildIter(acc.copy(children = acc.children :+ head), tail, childCount - 1, unprocessed)
          case child :: others =>
            // if has no children
            //    pop and add to children of current accumulator
            //    update remaining childCount
            buildIter(head, child :: others ::: acc :: tail, others.size + 1, unprocessed)
        }
    }
  }


  private def findChildren(parent: Span, edges: List[SpanEdge]): List[SpanNode] = {
    edges.filter(e => spanEq(parent, e.from)) // find all children
      .flatMap(e => e.to.toSet) // trim all empty values
      .map(to => SpanNode(to.copy(parentId = parent.spanId), Nil)) // adjust ancestry
  }

  /**
    * Equality check for Spans, based on serviceName and operationName
    *
    * @param lhs
    * @param rhs
    * @return
    */
  def spanEq(lhs: Span, rhs: Span): Boolean = {
    (
      lhs.process.get.serviceName.equals(rhs.process.get.serviceName)
        && lhs.operationName.equals(rhs.operationName)
      )
  }


}

class SpanEdge(val from: Span, val to: Option[Span]) {
  override def equals(obj: scala.Any): Boolean =
    obj match {
      case e: SpanEdge => e.hashCode == this.hashCode
      case _ => false
    }

  override def hashCode(): Int = {
    (from.process.get.serviceName.hashCode * 13
      ^ from.operationName.hashCode * 17
      ^ (to match {
      case Some(span) =>
        span.process.get.serviceName.hashCode * 21 ^
          span.operationName.hashCode * 23
      case None =>
        0
    }))
  }

  override def toString: String = {
    val fromName = from.operationName
    val toName = to match {
      case Some(s) => s.operationName
      case None => ""
    }
    fromName ++ " -> " ++ toName
  }
}
