package no.sysco.middleware.tramodana.builder.model

import no.sysco.middleware.tramodana.builder.{JsonSpanProtocol, Span}
import org.scalatest.{Matchers, WordSpec}
import spray.json.{DeserializationException, JsonParser}
import org.junit.Assert._

class SpanTreeSpec extends WordSpec with Matchers with JsonSpanProtocol{
  import spray.json._

  val listJson = "[{\"trace_id\":\"0x00000000000000004195de5c819a4f8b\",\"span_id\":\"-4476677815029340901\",\"span_hash\":-4149230555422868175,\"duration\":27333,\"flags\":1,\"logs\":[{\"ts\":1521716851756000,\"fields\": [{\"key\":\"handler.class_simple_name\",\"value_type\":\"string\",\"value_string\":\"GreetingController\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"preHandle\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler.method_name\",\"value_type\":\"string\",\"value_string\":\"getBookMethod\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},{\"ts\":1521716851773000,\"fields\": [{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"afterCompletion\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]}],\"operation_name\": \"getBookMethod\",\"parent_id\": \"-3836043444698440558\",\"process\": {\"service_name\":\"Shop\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1521716851748000},{\"trace_id\":\"0x00000000000000004195de5c819a4f8b\",\"span_id\":\"-3836043444698440558\",\"span_hash\":4057376722762880176,\"duration\":124634,\"flags\":1,\"logs\":[],\"operation_name\": \"GET\",\"parent_id\": \"4725927872887934859\",\"process\": {\"service_name\":\"Human\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1521716851656000},{\"trace_id\":\"0x00000000000000004195de5c819a4f8b\",\"span_id\":\"4725927872887934859\",\"span_hash\":7916626245469702708,\"duration\":155043,\"flags\":1,\"logs\":[{\"ts\":1521716851630000,\"fields\": [{\"key\":\"handler.class_simple_name\",\"value_type\":\"string\",\"value_string\":\"GreetingController\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"preHandle\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler.method_name\",\"value_type\":\"string\",\"value_string\":\"buyBookMethod\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},{\"ts\":1521716851784000,\"fields\": [{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"afterCompletion\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]}],\"operation_name\": \"buyBookMethod\",\"parent_id\": \"0\",\"process\": {\"service_name\":\"Human\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1521716851629000}]"
  val listJsonINVALID = "[{\"INVALID\":\"0x00000000000000004195de5c819a4f8b\",\"span_id\":\"-4476677815029340901\",\"span_hash\":-4149230555422868175,\"duration\":27333,\"flags\":1,\"logs\":[{\"ts\":1521716851756000,\"fields\": [{\"key\":\"handler.class_simple_name\",\"value_type\":\"string\",\"value_string\":\"GreetingController\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"preHandle\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler.method_name\",\"value_type\":\"string\",\"value_string\":\"getBookMethod\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},{\"ts\":1521716851773000,\"fields\": [{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"afterCompletion\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]}],\"operation_name\": \"getBookMethod\",\"parent_id\": \"-3836043444698440558\",\"process\": {\"service_name\":\"Shop\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1521716851748000},{\"trace_id\":\"0x00000000000000004195de5c819a4f8b\",\"span_id\":\"-3836043444698440558\",\"span_hash\":4057376722762880176,\"duration\":124634,\"flags\":1,\"logs\":[],\"operation_name\": \"GET\",\"parent_id\": \"4725927872887934859\",\"process\": {\"service_name\":\"Human\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1521716851656000},{\"trace_id\":\"0x00000000000000004195de5c819a4f8b\",\"span_id\":\"4725927872887934859\",\"span_hash\":7916626245469702708,\"duration\":155043,\"flags\":1,\"logs\":[{\"ts\":1521716851630000,\"fields\": [{\"key\":\"handler.class_simple_name\",\"value_type\":\"string\",\"value_string\":\"GreetingController\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"preHandle\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler.method_name\",\"value_type\":\"string\",\"value_string\":\"buyBookMethod\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},{\"ts\":1521716851784000,\"fields\": [{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"afterCompletion\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]}],\"operation_name\": \"buyBookMethod\",\"parent_id\": \"0\",\"process\": {\"service_name\":\"Human\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1521716851629000}]"


  "Tree " should {

    "parsed correctly" in {
      //Create an n-ary tree for testing that looks like this:
      //               a
      //              /
      //             b
      //            /
      //           d

      // Arrange
      val list: List[Span] = JsonParser(listJson).convertTo[List[Span]]

      // Act
      val tree = SpanTree.build(list)

      // Assert
      assertEquals("buyBookMethod",tree.operationName)
      assertEquals(1, tree.children.length)
      assertEquals("GET", tree.children.head.operationName)
      assertEquals(1, tree.children.head.children.length)
      assertEquals("getBookMethod", tree.children.head.children.head.operationName)

      SpanTree.printTree(Option(tree))
    }

    "parsed incorrectly" in {

      try {
        val list: List[Span] = JsonParser(listJsonINVALID).convertTo[List[Span]]
        SpanTree.build(list)
        fail()
      }
      catch {
        case deserializationException: DeserializationException => {}
        case _ => fail()
      }
    }


    "convert to json" in {
      // Arrange
      val list: List[Span] = JsonParser(listJson).convertTo[List[Span]]
      val tree = SpanTree.build(list)

      // Act
//      println(tree.toString)
      println(tree.toJson)
    }


  }


}
