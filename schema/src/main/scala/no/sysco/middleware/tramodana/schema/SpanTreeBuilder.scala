package no.sysco.middleware.tramodana.schema

import scala.annotation.tailrec


/**
  * This Tree class implements an N-ary tree which is a rooted tree in which each node has no more than n children.
  */
object SpanTreeBuilder extends JsonSpanProtocol {

  def build(spans: List[Span]): SpanTree = {
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
        buildIter(rootSpanTree, sortedRootChildren, sortedRootChildren.size, spans)
      case None => throw new IllegalArgumentException(s"Can not create SpanTree with given list: ${spans.toString}")
    }
  }

  @tailrec
  def buildIter(acc: SpanTree, spanStack: List[SpanTree], childCount: Int, unprocessedSpans: List[Span]): SpanTree = {
    (childCount, spanStack) match {
      case (_, Nil) =>
        acc // end case: nothing left to process
      case (0, head :: tail) =>
        // previous root becomes acc again
        // append current root to children of previous root
        buildIter(head.copy(children = head.children :+ acc), tail, findChildrenSpans(head.value, unprocessedSpans).size - head.children.size - 1, unprocessedSpans)

      case (_, head :: tail) =>
        val headChildren = findSortedChildrenSpanTrees(head.value, unprocessedSpans)
        headChildren match {
          case Nil =>
            // if has children
            //    sort them
            //    add them on stack
            //    first one becomes accumulator for next recursion
            buildIter(acc.copy(children = acc.children :+ head), tail, childCount - 1, unprocessedSpans)
          case child :: others =>
            // if has no children
            //    pop and add to children of current accumulator
            //    update remaing childCount
            buildIter(head, child :: others ::: acc :: tail, others.size + 1, unprocessedSpans)
        }
    }
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