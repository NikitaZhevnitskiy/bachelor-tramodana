package no.sysco.middleware.tramodana.builder.model

import no.sysco.middleware.tramodana.builder._
import spray.json.JsonParser

import scala.collection.mutable.ListBuffer




// Source : https://github.com/mbren/scala-tree/blob/master/src/test/scala/TreeSpec.scala
/**
This Tree class implements an N-ary tree which is a rooted tree in which each node has no more than n children.
  */

class SpanTree(
                val operationName: String,
                val value: Span,
                val parent: Option[String] = None,
                val childrens: List[SpanTree] = List.empty) {

  override def toString: String = {
    s""
  }

}

object SpanTree {

  def build(spanList: List[Span]):SpanTree = {

    val rootSpan = spanList.filter(element=>"0".equalsIgnoreCase(element.parentId)).head
    val restList = spanList.filterNot(element=>"0".equalsIgnoreCase(element.parentId))
    new SpanTree(
      rootSpan.operationName,
      rootSpan,
      None,
      getTrees(restList, rootSpan.spanId)
    )
  }

  def getTrees(spans: List[Span], parentId: String): List[SpanTree] = {
    val listBuffer = new ListBuffer[SpanTree]()
    spans.nonEmpty match {
      case true => {
        val listWithParentId = spans.filter(elem => parentId.equalsIgnoreCase(elem.parentId))
        val reduceList = spans.filterNot(elem => parentId.equalsIgnoreCase(elem.parentId))
        listWithParentId.size match {
          case 0 =>
          case 1 => {
            listBuffer +=
              new SpanTree(
                listWithParentId.head.operationName,
                listWithParentId.head,
                Option(parentId),
                getTrees(reduceList, listWithParentId.head.spanId))
          }
          case _ => {
            val newListParents = listWithParentId.tail
            listBuffer +=
              new SpanTree(
                listWithParentId.head.operationName,
                listWithParentId.head,
                Option(parentId),
                getTrees(reduceList, listWithParentId.head.spanId))
            getTrees(newListParents, parentId)
          }
        }
      }
      case false => {}
    }
    listBuffer.toList
  }




    def printTree(tree: Option[SpanTree], acc: Int = 0):Unit = {
      tree match {
        case Some(v) => {
          println(v.operationName + s" |parent: ${v.parent}|childrens: ${v.childrens.size}|acc: $acc|")
          v.childrens.size match {
            case 0 => println(s"${v.operationName} has children: ${v.childrens.size}")
            case _ => {
              for(subtree <- v.childrens) {
                printTree(Option(subtree),1+acc)
              }
            }
          }
        }
        case None => println("Err")
      }
    }



  }

  object A extends App with JsonSpanProtocol{
    val listJson = "[{\"trace_id\":\"0x00000000000000004195de5c819a4f8b\",\"span_id\":\"-4476677815029340901\",\"span_hash\":-4149230555422868175,\"duration\":27333,\"flags\":1,\"logs\":[{\"ts\":1521716851756000,\"fields\": [{\"key\":\"handler.class_simple_name\",\"value_type\":\"string\",\"value_string\":\"GreetingController\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"preHandle\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler.method_name\",\"value_type\":\"string\",\"value_string\":\"getBookMethod\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},{\"ts\":1521716851773000,\"fields\": [{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"afterCompletion\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]}],\"operation_name\": \"getBookMethod\",\"parent_id\": \"-3836043444698440558\",\"process\": {\"service_name\":\"Shop\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1521716851748000},{\"trace_id\":\"0x00000000000000004195de5c819a4f8b\",\"span_id\":\"-3836043444698440558\",\"span_hash\":4057376722762880176,\"duration\":124634,\"flags\":1,\"logs\":[],\"operation_name\": \"GET\",\"parent_id\": \"4725927872887934859\",\"process\": {\"service_name\":\"Human\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1521716851656000},{\"trace_id\":\"0x00000000000000004195de5c819a4f8b\",\"span_id\":\"4725927872887934859\",\"span_hash\":7916626245469702708,\"duration\":155043,\"flags\":1,\"logs\":[{\"ts\":1521716851630000,\"fields\": [{\"key\":\"handler.class_simple_name\",\"value_type\":\"string\",\"value_string\":\"GreetingController\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"preHandle\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler.method_name\",\"value_type\":\"string\",\"value_string\":\"buyBookMethod\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},{\"ts\":1521716851784000,\"fields\": [{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"afterCompletion\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]}],\"operation_name\": \"buyBookMethod\",\"parent_id\": \"0\",\"process\": {\"service_name\":\"Human\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1521716851629000}]"
    val list: List[Span] = JsonParser(listJson).convertTo[List[Span]]


    val tree = SpanTree.build(list)
    SpanTree.printTree(Option(tree))
    //  SpanTree.printTree(tree)

    //  var listTmp = new ListBuffer[String]()
    //  listTmp += "lol"
    //  listTmp += "lol2"
    //  println(listTmp.toList)
  }
