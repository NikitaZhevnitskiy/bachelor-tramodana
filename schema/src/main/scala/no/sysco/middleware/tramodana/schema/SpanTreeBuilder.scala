package no.sysco.middleware.tramodana.schema

import spray.json.SerializationException

import scala.annotation.tailrec


/**
  * This Tree class implements an N-ary tree which is a rooted tree in which each node has no more than n children.
  */
object SpanTreeBuilder extends JsonSpanProtocol {

  def mergeIntoTree(traceList: List[SpanTree], rootId: String): Option[SpanTree] = {
    val edges: Set[SpanEdge] = traceList.flatMap(sn => splitIntoEdges(Some(sn), Set.empty)).toSet

    try {
      val tree = build(edges, rootId)
      Some(tree)
    }
    catch {
      case e: Exception =>
        println(e.getMessage)
        None
    }
  }

  @tailrec
  final def splitIntoEdges(trace: Option[SpanTree], edges: Set[SpanEdge]): Set[SpanEdge] = {
    trace match {
      case Some(SpanTree(s, x :: _)) => splitIntoEdges(Some(x), edges + new SpanEdge(s, Some(x.value)))
      case Some(SpanTree(s, Nil)) => splitIntoEdges(None, edges + new SpanEdge(s, None))
      case None => edges
    }
  }
  private def build(edges: Set[SpanEdge], rootId: String): SpanTree = {
    val rootEdge = edges.find(se => se.from.parentId.equals(rootId))
    rootEdge match {
      case Some(r) =>
        val rootNode = SpanTree(r.from, Nil)
        val children = findChildren(r.from, edges.toList)
        buildIter(rootNode, children, children.size, edges.toList,findChildren)
      case None => throw new IllegalArgumentException(s"Could not find node with parent '$rootId'")
    }
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

  def build(spans: List[Span]): SpanTree = {
    // TODO: remove magic number for rootId
    val rootId = "0"
    // with validation
    if (spans.isEmpty) throw new IllegalArgumentException("List is empty")
    spans.count(_.parentId.equalsIgnoreCase("0")) match {
      case 1 =>
        build(spans, rootId)
      case 0 => throw new IllegalArgumentException("Spans in the list does not have parentId = '0' ")
      case _ => throw new IllegalArgumentException("List contains several spans with parentId ='0' ")
    }
  }


  def build(spans: List[Span], parentId: String): SpanTree = {

    val rootSpan = spans.find(_.parentId.equalsIgnoreCase(parentId))
    rootSpan match {
      case Some(root) =>
        val rootSpanTree = SpanTree(root, List.empty)
        val sortedRootChildren = findSortedChildrenSpanTrees(root, spans)
        buildIter(rootSpanTree, sortedRootChildren, sortedRootChildren.size, spans, findSortedChildrenSpanTrees)
      case None => throw new IllegalArgumentException(s"No Span with parent $parentId in list: ${spans.toString}")
    }
  }


  @tailrec
  private def buildIter[E](acc: SpanTree,
                           stack: List[SpanTree],
                           childCount: Int,
                           unprocessed: List[E],
                           findChildren: (Span, List[E]) => List[SpanTree]): SpanTree = {
    (childCount, stack) match {
      case (_, Nil) =>
        acc // end case: nothing left to process
      case (0, head :: tail) =>
        // previous root becomes acc again
        // append current root to children of previous root
        buildIter(head.copy(children = head.children :+ acc),
          tail,
          findChildren(head.value, unprocessed).size - head.children.size - 1,
          unprocessed,
          findChildren
        )

      case (_, head :: tail) =>
        //val headChildren = findChildren(head.value, unprocessed)
        val headChildren = findChildren(head.value, unprocessed)
        headChildren match {
          case Nil =>
            // if has children
            //    sort them
            //    add them on stack
            //    first one becomes accumulator for next recursion
            buildIter(
              acc.copy(children = acc.children :+ head),
              tail,
              childCount - 1,
              unprocessed,
              findChildren)
          case child :: others =>
            // if has no children
            //    pop and add to children of current accumulator
            //    update remaining childCount
            buildIter(head,
              child :: others ::: acc :: tail,
              others.size + 1,
              unprocessed,
              findChildren
            )
        }
    }
  }

  private def findChildren(parent: Span, edges: List[SpanEdge]): List[SpanTree] = {
    edges.filter(e => spanEq(parent, e.from)) // find all children
      .flatMap(e => e.to.toSet) // trim all empty values
      .map(to => SpanTree(to.copy(parentId = parent.spanId), Nil)) // adjust ancestry
  }

  def splitChildrenFromRest(mother: Span, pool: List[Span]): (List[Span], List[Span]) = pool.partition(isChildOf(_, mother))

  def intoSpanTree(span: Span): SpanTree = SpanTree(span, Nil)

  def intoSpanTrees(spans: List[Span]): List[SpanTree] = spans.map(intoSpanTree)

  def findSortedChildrenSpanTrees(span: Span, spans: List[Span]): List[SpanTree] =
    findChildrenSpans(span, spans).sortBy(_.startTime).map(intoSpanTree)

  def findChildrenSpans(span: Span, spans: List[Span]): List[Span] = {
    spans.filter(child => isChildOf(child, span))
  }

  def isChildOf(child: Span, parent: Span): Boolean = child.parentId.equalsIgnoreCase(parent.spanId)

  // Based on DFS
  def getSequence(tree: SpanTree, list: List[Span] = List.empty[Span]): List[Span] = {
    var newList = list :+ tree.value
//    println(newList + s"was added ${tree.value.operationName}")
    tree
      .children
      .sortWith(_.value.startTime < _.value.startTime)
      .foreach(t => newList = newList ++ getSequence(t, list))
    newList
  }

  def getSetOfSeq(listOfSpanSeq: List[Seq[Span]]): Set[Seq[Span]] = {
    var set: Set[Seq[Span]] = Set.empty
    var setOfOpName: Set[String] = Set.empty

    listOfSpanSeq.foreach(spanSeq => {
      val opNames = spanSeq.flatMap(span => span.operationName).mkString
      if( ! setOfOpName.contains(opNames)){
        setOfOpName+=opNames
        set+=spanSeq
      }
    })

    set
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
