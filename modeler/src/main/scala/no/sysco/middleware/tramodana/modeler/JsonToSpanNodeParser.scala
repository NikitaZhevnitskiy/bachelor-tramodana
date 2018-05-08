package no.sysco.middleware.tramodana.modeler

import no.sysco.middleware.tramodana.schema.{JsonSpanProtocol, Span}
import spray.json._

import scala.annotation.tailrec

trait JsonSpanNodeProtocol extends JsonSpanProtocol with DefaultJsonProtocol {
  implicit def spanNodeFormat: JsonFormat[SpanNode] = lazyFormat(jsonFormat2(SpanNode))
}

object JsonToSpanNodeParser extends JsonSpanNodeProtocol {
  def parse(jsonSource: String): Option[BpmnParsable] = {
    val preprocessed = getSpanNodeList(jsonSource).map(sn => preprocessSpanNode(sn))
    val rootParentId = preprocessed.head.getParentId
    mergeTracesIntoTree(preprocessed, rootParentId)
  }

def getTopRootParentId(spanNodeList: List[SpanNode]):String =
    spanNodeList.head.getParentId
  def getPreprocessedSpanNodeList(jsonSrc: String): List[SpanNode] =
    getSpanNodeList(jsonSrc).map(sn => preprocessSpanNode(sn))


  private def preprocessSpanNode(n: SpanNode): SpanNode = {
    var nodes: List[SpanNode] = flattenSpanNode(n).map(node => preprocessNode(node))

    @tailrec
    def rebuildTrace(list: List[SpanNode], acc: SpanNode): SpanNode =
      list match {
        case x :: xs => rebuildTrace(xs, x.copy(children = acc :: x.children))
        case Nil => acc
      }

    rebuildTrace(nodes.tail, nodes.head)
  }

  /**
    * Format a node's parentId and spanId (processId)
    * to be ready for BPMN xml
    *
    * @param n the node to format
    * @return the formatted node
    */
  private def preprocessNode(n: SpanNode): SpanNode = {
    Utils.formatParsableForXml(n).asInstanceOf[SpanNode]
  }

  /**
    * Return a list of all nodes contained in a tree,
    * each without its children
    *
    * @param n : the tree
    * @return the list of nodes contained by the tree
    */
  private def flattenSpanNode(n: SpanNode): List[SpanNode] = {
    @tailrec
    def pp_iter(in: Option[SpanNode], acc: List[SpanNode]): List[SpanNode] = {
      in match {
        case Some(node) =>
          pp_iter(node.children.headOption, node.copy(node.value, Nil) :: acc)
        case None => acc
      }
    }

    pp_iter(Some(n), List.empty)
  }

  def mergeTracesIntoTree(traceList: List[SpanNode], rootId: String): Option[SpanNode] = {
    var edges: Set[SpanEdge] = traceList.flatMap(sn => splitTraceIntoEdges(Some(sn), Set.empty)).toSet

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


  private def build(edges: Set[SpanEdge], rootId: String): SpanNode = {
    var rootEdge = edges.find(se => se.from.parentId.equals(rootId))
    rootEdge match {
      case Some(r) =>
        val rootNode = SpanNode(r.from, Nil)
        val children = getChildrenSpanNodes(r.from, edges.toList)
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
        buildIter(head.copy(children = head.children :+ acc), tail, getChildrenSpanNodes(head.value, unprocessed).size - head.children.size - 1, unprocessed)

      case (_, head :: tail) =>
        val headChildren = getChildrenSpanNodes(head.value, unprocessed)
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
            //    update remaing childCount
            buildIter(head, child :: others ::: acc :: tail, others.size + 1, unprocessed)
        }
    }
  }


  private def getChildrenSpanNodes(root: Span, edges: List[SpanEdge]): List[SpanNode] = {
    edges.filter(e => spanEq(e.from, root)) // take all edges with 'root' as origin
      .flatMap(e => e.to.toSet) // all None become empty sets and disappear. All Some aggregate into one list
      .map(to => SpanNode(to.copy(parentId = root.spanId), Nil)) // adjust ancestry
  }

  def spanEq(lhs: Span, rhs: Span): Boolean = {
    (
      lhs.process.get.serviceName.equals(rhs.process.get.serviceName)
        && lhs.operationName.equals(rhs.operationName)
      )
  }

  @tailrec
  final def splitTraceIntoEdges(trace: Option[SpanNode], edges: Set[SpanEdge]): Set[SpanEdge] = {
    trace match {
      case Some(SpanNode(s, x :: _)) => splitTraceIntoEdges(Some(x), edges + new SpanEdge(s, Some(x.value)))
      case Some(SpanNode(s, Nil)) => splitTraceIntoEdges(None, edges + new SpanEdge(s, None))
      case None => edges
    }
  }

  def getSpanNodeList(js: String): List[SpanNode] = try {
    JsonParser(js).convertTo[List[SpanNode]]
  } catch {
    case e: DeserializationException =>
      throw DeserializationException(s"Could not parse Trace models list to List[SpanNode]: ${e.msg}")
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
