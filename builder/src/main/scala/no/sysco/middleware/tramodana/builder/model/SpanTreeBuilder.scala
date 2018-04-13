package no.sysco.middleware.tramodana.builder.model

import no.sysco.middleware.tramodana.builder._

import scala.collection.mutable.ListBuffer


/**
  * This Tree class implements an N-ary tree which is a rooted tree in which each node has no more than n children.
  */
object SpanTreeBuilder {

  def build(spanList: List[Span]): SpanTree = {
    val rootSpan = spanList.filter(element => "0".equalsIgnoreCase(element.parentId)).head
    val restList = spanList.filterNot(element => "0".equalsIgnoreCase(element.parentId))
    SpanTree(
      rootSpan.operationName,
      rootSpan,
      None,
      getTrees(restList, rootSpan.spanId)
    )
  }

  def getTrees(spans: List[Span], parentId: String): List[SpanTree] = {
    val listBuffer = new ListBuffer[SpanTree]()
    if (spans.nonEmpty) {
      val listWithParentId = spans.filter(elem => parentId.equalsIgnoreCase(elem.parentId))
      val reduceList = spans.filterNot(elem => parentId.equalsIgnoreCase(elem.parentId))
      listWithParentId.size match {
        case 0 =>
        case 1 => {
          listBuffer +=
            SpanTree(
              listWithParentId.head.operationName,
              listWithParentId.head,
              Option(parentId),
              getTrees(reduceList, listWithParentId.head.spanId))
        }
        case _ => {
          val newListParents = listWithParentId.tail
          listBuffer +=
            SpanTree(
              listWithParentId.head.operationName,
              listWithParentId.head,
              Option(parentId),
              getTrees(reduceList, listWithParentId.head.spanId))
          getTrees(newListParents, parentId)
        }
      }
    } else {
      {}
    }
    listBuffer.toList
  }

  def printTree(tree: Option[SpanTree], acc: Int = 0): Unit = {
    tree match {
      case Some(v) => {
        println(v.operationName + s" |parent: ${v.parent}|childrens: ${v.children.size}|acc: $acc|")
        v.children.size match {
          case 0 => println(s"${v.operationName} has children: ${v.children.size}")
          case _ => {
            for (subtree <- v.children) {
              printTree(Option(subtree), 1 + acc)
            }
          }
        }
      }
      case None => println("Err")
    }
  }

  // Based on DFS () todo: make immutable
  def getSequence(tree: SpanTree, list: List[Span] = List.empty[Span]): List[Span] = {
    var newList = list :+ tree.value
    println(newList + s"was added ${tree.operationName}")
    tree
      .children
      .sortWith(_.value.startTime < _.value.startTime)
      .foreach(t => newList = newList ++ getSequence(t, list))
    newList
  }


}