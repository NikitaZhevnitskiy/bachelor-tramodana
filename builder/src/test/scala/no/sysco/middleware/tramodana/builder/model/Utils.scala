package no.sysco.middleware.tramodana.builder.model

import no.sysco.middleware.tramodana.builder.{JsonSpanProtocol, Span, SpanTree}

import scala.io.Source

object Utils extends JsonSpanProtocol{
  import spray.json._

  val listJson = "[{\"trace_id\":\"0x00000000000000004195de5c819a4f8b\",\"span_id\":\"-4476677815029340901\",\"span_hash\":-4149230555422868175,\"duration\":27333,\"flags\":1,\"logs\":[{\"ts\":1521716851756000,\"fields\": [{\"key\":\"handler.class_simple_name\",\"value_type\":\"string\",\"value_string\":\"GreetingController\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"preHandle\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler.method_name\",\"value_type\":\"string\",\"value_string\":\"getBookMethod\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},{\"ts\":1521716851773000,\"fields\": [{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"afterCompletion\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]}],\"operation_name\": \"getBookMethod\",\"parent_id\": \"-3836043444698440558\",\"process\": {\"service_name\":\"Shop\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1521716851748000},{\"trace_id\":\"0x00000000000000004195de5c819a4f8b\",\"span_id\":\"-3836043444698440558\",\"span_hash\":4057376722762880176,\"duration\":124634,\"flags\":1,\"logs\":[],\"operation_name\": \"GET\",\"parent_id\": \"4725927872887934859\",\"process\": {\"service_name\":\"Human\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1521716851656000},{\"trace_id\":\"0x00000000000000004195de5c819a4f8b\",\"span_id\":\"4725927872887934859\",\"span_hash\":7916626245469702708,\"duration\":155043,\"flags\":1,\"logs\":[{\"ts\":1521716851630000,\"fields\": [{\"key\":\"handler.class_simple_name\",\"value_type\":\"string\",\"value_string\":\"GreetingController\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"preHandle\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler.method_name\",\"value_type\":\"string\",\"value_string\":\"buyBookMethod\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},{\"ts\":1521716851784000,\"fields\": [{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"afterCompletion\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]}],\"operation_name\": \"buyBookMethod\",\"parent_id\": \"0\",\"process\": {\"service_name\":\"Human\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1521716851629000}]"
  val listJsonINVALID = "[{\"INVALID\":\"0x00000000000000004195de5c819a4f8b\",\"span_id\":\"-4476677815029340901\",\"span_hash\":-4149230555422868175,\"duration\":27333,\"flags\":1,\"logs\":[{\"ts\":1521716851756000,\"fields\": [{\"key\":\"handler.class_simple_name\",\"value_type\":\"string\",\"value_string\":\"GreetingController\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"preHandle\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler.method_name\",\"value_type\":\"string\",\"value_string\":\"getBookMethod\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},{\"ts\":1521716851773000,\"fields\": [{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"afterCompletion\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]}],\"operation_name\": \"getBookMethod\",\"parent_id\": \"-3836043444698440558\",\"process\": {\"service_name\":\"Shop\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1521716851748000},{\"trace_id\":\"0x00000000000000004195de5c819a4f8b\",\"span_id\":\"-3836043444698440558\",\"span_hash\":4057376722762880176,\"duration\":124634,\"flags\":1,\"logs\":[],\"operation_name\": \"GET\",\"parent_id\": \"4725927872887934859\",\"process\": {\"service_name\":\"Human\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1521716851656000},{\"trace_id\":\"0x00000000000000004195de5c819a4f8b\",\"span_id\":\"4725927872887934859\",\"span_hash\":7916626245469702708,\"duration\":155043,\"flags\":1,\"logs\":[{\"ts\":1521716851630000,\"fields\": [{\"key\":\"handler.class_simple_name\",\"value_type\":\"string\",\"value_string\":\"GreetingController\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"preHandle\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler.method_name\",\"value_type\":\"string\",\"value_string\":\"buyBookMethod\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},{\"ts\":1521716851784000,\"fields\": [{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"afterCompletion\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]}],\"operation_name\": \"buyBookMethod\",\"parent_id\": \"0\",\"process\": {\"service_name\":\"Human\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1521716851629000}]"
  val spanJson : String =
    """
      |{
      |      "trace_id": "0x00000000000000008cdfd0b8468dc387",
      |      "span_id": "-8295682498716909000",
      |      "span_hash": 7083069031972214000,
      |      "duration": 164851,
      |      "flags": 1,
      |      "logs": [
      |        {
      |          "ts": 1520845538311000,
      |          "fields": [
      |            {
      |              "key": "handler.class_simple_name",
      |              "value_type": "string",
      |              "value_string": "GreetingController",
      |              "value_bool": false,
      |              "value_long": 0,
      |              "value_double": 0,
      |              "value_binary": "NULL"
      |            },
      |            {
      |              "key": "handler",
      |              "value_type": "string",
      |              "value_string": "public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()",
      |              "value_bool": false,
      |              "value_long": 0,
      |              "value_double": 0,
      |              "value_binary": "NULL"
      |            },
      |            {
      |              "key": "event",
      |              "value_type": "string",
      |              "value_string": "preHandle",
      |              "value_bool": false,
      |              "value_long": 0,
      |              "value_double": 0,
      |              "value_binary": "NULL"
      |            },
      |            {
      |              "key": "handler.method_name",
      |              "value_type": "string",
      |              "value_string": "buyBookMethod",
      |              "value_bool": false,
      |              "value_long": 0,
      |              "value_double": 0,
      |              "value_binary": "NULL"
      |            }
      |          ]
      |        },
      |        {
      |          "ts": 1520845538475000,
      |          "fields": [
      |            {
      |              "key": "event",
      |              "value_type": "string",
      |              "value_string": "afterCompletion",
      |              "value_bool": false,
      |              "value_long": 0,
      |              "value_double": 0,
      |              "value_binary": "NULL"
      |            },
      |            {
      |              "key": "handler",
      |              "value_type": "string",
      |              "value_string": "public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()",
      |              "value_bool": false,
      |              "value_long": 0,
      |              "value_double": 0,
      |              "value_binary": "NULL"
      |            }
      |          ]
      |        }
      |      ],
      |      "operation_name": "buyBookMethod",
      |      "parent_id": "0",
      |      "process": {
      |        "service_name": "Human",
      |        "tags": [
      |          {
      |            "key": "hostname",
      |            "value_type": "string",
      |            "value_string": "nikita-lenovo-yoga-710-14ikb",
      |            "value_bool": false,
      |            "value_long": 0,
      |            "value_double": 0,
      |            "value_binary": "NULL"
      |          },
      |          {
      |            "key": "jaeger.version",
      |            "value_type": "string",
      |            "value_string": "Java-0.20.0",
      |            "value_bool": false,
      |            "value_long": 0,
      |            "value_double": 0,
      |            "value_binary": "NULL"
      |          },
      |          {
      |            "key": "ip",
      |            "value_type": "string",
      |            "value_string": "127.0.1.1",
      |            "value_bool": false,
      |            "value_long": 0,
      |            "value_double": 0,
      |            "value_binary": "NULL"
      |          }
      |        ]
      |      },
      |      "refs": [],
      |      "start_time": 1520845538310000,
      |      "tags": [
      |        {
      |          "key": "http.status_code",
      |          "value_type": "int64",
      |          "value_string": "",
      |          "value_bool": false,
      |          "value_long": 200,
      |          "value_double": 0,
      |          "value_binary": "NULL"
      |        },
      |        {
      |          "key": "component",
      |          "value_type": "string",
      |          "value_string": "java-web-servlet",
      |          "value_bool": false,
      |          "value_long": 0,
      |          "value_double": 0,
      |          "value_binary": "NULL"
      |        },
      |        {
      |          "key": "span.kind",
      |          "value_type": "string",
      |          "value_string": "server",
      |          "value_bool": false,
      |          "value_long": 0,
      |          "value_double": 0,
      |          "value_binary": "NULL"
      |        },
      |        {
      |          "key": "sampler.type",
      |          "value_type": "string",
      |          "value_string": "probabilistic",
      |          "value_bool": false,
      |          "value_long": 0,
      |          "value_double": 0,
      |          "value_binary": "NULL"
      |        },
      |        {
      |          "key": "sampler.param",
      |          "value_type": "float64",
      |          "value_string": "",
      |          "value_bool": false,
      |          "value_long": 0,
      |          "value_double": 1,
      |          "value_binary": "NULL"
      |        },
      |        {
      |          "key": "http.url",
      |          "value_type": "string",
      |          "value_string": "http://localhost:10081/buybook",
      |          "value_bool": false,
      |          "value_long": 0,
      |          "value_double": 0,
      |          "value_binary": "NULL"
      |        },
      |        {
      |          "key": "http.method",
      |          "value_type": "string",
      |          "value_string": "GET",
      |          "value_bool": false,
      |          "value_long": 0,
      |          "value_double": 0,
      |          "value_binary": "NULL"
      |        }
      |      ]
      |  }
    """.stripMargin
  val jsonTree0Child : String =
    s"""
       |{
       |  "operation_name":"buyBookMethod",
       |  "value": $spanJson,
       |  "parent": "",
       |  "children":[
       |  ]
       |}
        """.stripMargin
  val jsonTree1Child : String =
    s"""
       |{
       |  "operation_name":"buyBookMethod_PARENT",
       |  "value": $spanJson,
       |  "parent": "",
       |  "children":[
       |   {
       |     $jsonTree0Child
       |   }
       |  ]
       |}
    """.stripMargin





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
    val span = Utils.spanFromFile("builder/src/test/resources/spanExample.json")

    val span01 = span.copy(startTime = 1, operationName = "A", spanId = "1", parentId = "0")
    val span11 = span.copy(startTime = 2, operationName = "B", spanId = "2", parentId = "1")
    val span12 = span.copy(startTime = 5, operationName = "E", spanId = "5", parentId = "1")
    val span13 = span.copy(startTime = 7, operationName = "G", spanId = "7", parentId = "1")
    val span21 = span.copy(startTime = 3, operationName = "C", spanId = "3", parentId = "2")
    val span22 = span.copy(startTime = 4, operationName = "D", spanId = "4", parentId = "2")
    val span23 = span.copy(startTime = 6, operationName = "F", spanId = "6", parentId = "5")

    val spanList = List(span23, span22, span21, span13, span12, span11, span01)
    println(s"startTime : operationName : spanId : parentId")
    spanList.foreach(s => println(s"${s.startTime}: ${s.operationName} : ${s.spanId} : ${s.parentId}"))
    spanList
  }


}

object App extends App with JsonSpanProtocol {
  import spray.json._

  val listOfSpans = Utils.getSpanListWith7Nodes()
  val tree = SpanTreeBuilder.build(listOfSpans)
  println(listOfSpans.toJson)
  println(tree.toJson)

}
