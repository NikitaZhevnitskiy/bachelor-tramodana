package no.sysco.middleware.tramodana.modeler

import no.sysco.middleware.tramodana.schema.{JsonSpanProtocol, Span}
import spray.json._

import scala.annotation.tailrec

trait JsonSpanNodeProtocol extends JsonSpanProtocol with DefaultJsonProtocol {
  implicit def spanNodeFormat: JsonFormat[SpanNode] = lazyFormat(jsonFormat2(SpanNode))
}

class JsonToSpanNodeParser extends JsonSpanNodeProtocol {
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
    var nodes: List[SpanNode] = flattenSpanNode(n)
    nodes = nodes.map(node => preprocessNode(node))

    var resNode = nodes.head
    nodes = nodes.tail
    while (nodes.nonEmpty) {
      nodes match {
        case x :: xs =>
          resNode = x.copy(children = resNode :: x.children)
          nodes = xs
      }
    }
    resNode
  }

  private def preprocess(list: List[SpanNode]): List[SpanNode] = {
    list.map(node => preprocessSpanNode(node))
  }

  //TODO: implement trace merging
  private def mergeTracesIntoTree(traceList: List[SpanNode], rootId: String): Option[SpanNode]= {
    var edges: Set[SpanEdge] = traceList.flatMap(sn => splitTraceIntoEdges(Some(sn), Set.empty)).toSet

    try
      {
        val tree = build(edges, rootId)
        Some(tree)
      }
    catch {
      case e: SerializationException =>
        println(e.getMessage)
        None
    }
  }



  private def build(edges: Set[SpanEdge], root: String): SpanNode = {
    var rootEdge = edges.find(se => se.from.parentId.equals(root))
    rootEdge match {
      case Some(r) =>
        val rootNode = SpanNode(r.from, Nil)
        val children = getChildrenSpanNodes(r.from, edges.toList)
        buildIter(rootNode, children, children.size, edges.toList)
      case None => throw new IllegalArgumentException(s"Could not find node with parent '$root'")
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


  private def getChildrenSpanNodes(root: Span, edges: List[SpanEdge]): List[SpanNode]={
    edges.filter(e => spanEq(e.from, root)) // take all edges with 'root' as origin
      .flatMap( e => e.to.toSet) // all None become empty sets and disappear. All Some aggregate into one list
      .map( to => SpanNode(to.copy(parentId = root.spanId),Nil))  // adjust ancestry
  }

  def spanEq(lhs: Span, rhs: Span): Boolean={
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

  private def getSpanNodeList(js:String): List[SpanNode] = try {
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
