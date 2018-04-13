package no.sysco.middleware.tramodana.builder.model


import no.sysco.middleware.tramodana.builder.{JsonSpanProtocol, Span, SpanTree}

import scala.io.Source


object SpanTreeDraft extends JsonSpanProtocol {
  import spray.json._

  def build(spans: List[Span], parentId:String="0"): Option[SpanTree] = {
    val rootSpan = spans.find(_.parentId.equalsIgnoreCase(parentId))

    rootSpan match {
      case Some(root) => {
        // send further reduced spans list
        println("\nHERE: "+spans
          .filterNot(p => p.spanId.equalsIgnoreCase(root.spanId))
          .map(s => s.operationName)
          .toString)

        val subtrees = getSubtrees(
          spans.filterNot(p => p.spanId.equalsIgnoreCase(root.spanId)),
          root.spanId
        )
        Option(
          SpanTree(
            root.operationName,
            root,
            Some(parentId),
            subtrees
          ))
      }
      case None => {
        Option.empty
      }
    }


  }


  def getSubtrees(spans: List[Span], parentId: String):List[SpanTree] = {
    if(spans.nonEmpty){
      val list = spans
        .filter(_.parentId.equalsIgnoreCase(parentId))
        .map(s => {
          println(s"process ${s.operationName} with list: ${spans.flatMap(span=>span.operationName).toString}")
          s
        })
        .flatMap(span => build(spans, parentId))
      list
    }
    else {
      List.empty
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


  def spanFromFile(filename: String): Span = {
    val data = Source.fromFile(filename).getLines().mkString
    JsonParser(data).convertTo[Span]
  }

  //                        1A
  //                     /   |   \
  //                   2B    5E   7G
  //                  / \     \
  //                3C   4D    6F
  def getSpanListWith7Nodes(): List[Span] = {
    val span = SpanTreeDraft.spanFromFile("builder/src/test/resources/spanExample.json")

    val span01 = span.copy(startTime = 1, operationName = "A", spanId = "1", parentId = "0")
    val span11 = span.copy(startTime = 2, operationName = "B", spanId = "2", parentId = "1")
    val span12 = span.copy(startTime = 5, operationName = "E", spanId = "5", parentId = "1")
    val span13 = span.copy(startTime = 7, operationName = "G", spanId = "7", parentId = "1")
    val span21 = span.copy(startTime = 3, operationName = "C", spanId = "3", parentId = "2")
    val span22 = span.copy(startTime = 4, operationName = "D", spanId = "4", parentId = "2")
    val span23 = span.copy(startTime = 6, operationName = "F", spanId = "6", parentId = "5")

    val spanList = List(span23, span22, span21, span13, span12, span11, span01)
    println(s"startTime : operationName : spanId : parentId")
    //    spanList.foreach(s => println(s"${s.startTime}: ${s.operationName} : ${s.spanId} : ${s.parentId}"))
    spanList
  }
}

object M extends App with JsonSpanProtocol{
  import spray.json._

  val spans = SpanTreeDraft.getSpanListWith7Nodes()
//  println(spans.toJson)
  val tree = SpanTreeDraft.build(spans)
  println(tree.toJson)

}
