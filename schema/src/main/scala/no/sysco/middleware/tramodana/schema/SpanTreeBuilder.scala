package no.sysco.middleware.tramodana.schema


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
      case Some(value) =>
        val remainingNodes = spans.filterNot(_.spanId.equalsIgnoreCase(value.spanId))
        buildIter(value, remainingNodes)
      case None => throw new IllegalArgumentException(s"Can not create SpanTree with given list: ${spans.toString}")
    }
  }

  def buildIter(current: Span, spans: List[Span]): SpanTree = {
    spans.size match {
      case 0 => SpanTree(current, Nil)
      case _ =>
        val childrenSpans = getChildrenSpans(current, spans).sortBy(_.startTime)
        val remaining = spans.diff(childrenSpans)
        SpanTree(current, childrenSpans.map(s => buildIter(s, remaining)))
    }
  }

  def getChildrenSpans(span: Span, spans: List[Span]): List[Span] = {
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