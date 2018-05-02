package no.sysco.middleware.tramodana.builder

import no.sysco.middleware.tramodana.schema.JsonSpanProtocol

object Utils extends JsonSpanProtocol {
  import spray.json._

//  val defaultFileSpan = "schema/src/test/resources/spanExample.json"

  //                      1A
  //                     /
  //                   2B
  //                  /
  //                3C

  val span1: (String, String)  = Tuple2(
    "0x0000000000000000ecc79a5158de1254",
    " {\"trace_id\":\"0x0000000000000000ecc79a5158de1254\",\"span_id\":\"-1384968686219161004\",\"span_hash\":-1873032072010914471,\"duration\":231735,\"flags\":1,\"logs\":[{\"ts\":1525264833362000,\"fields\": [{\"key\":\"handler.class_simple_name\",\"value_type\":\"string\",\"value_string\":\"GreetingController\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"preHandle\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler.method_name\",\"value_type\":\"string\",\"value_string\":\"buyBookMethod\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},{\"ts\":1525264833592000,\"fields\": [{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"afterCompletion\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]}],\"operation_name\": \"buyBookMethod\",\"parent_id\": \"0\",\"process\": {\"service_name\":\"Human\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1525264833361000}")

  val span2: (String, String) = Tuple2(
    "0x0000000000000000ecc79a5158de1254",
    " {\"trace_id\":\"0x0000000000000000ecc79a5158de1254\",\"span_id\":\"-174740197246472194\",\"span_hash\":-8309517090057103367,\"duration\":130184,\"flags\":1,\"logs\":[],\"operation_name\": \"GET\",\"parent_id\": \"-1384968686219161004\",\"process\": {\"service_name\":\"Human\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1525264833458000}"
  )

  val span3: (String, String) = Tuple2(
    "0x0000000000000000ecc79a5158de1254",
    "{\"trace_id\":\"0x0000000000000000ecc79a5158de1254\",\"span_id\":\"3519263401412236834\",\"span_hash\":9151989517133798671,\"duration\":31309,\"flags\":1,\"logs\":[{\"ts\":1525264833568000,\"fields\": [{\"key\":\"handler.class_simple_name\",\"value_type\":\"string\",\"value_string\":\"GreetingController\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"preHandle\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler.method_name\",\"value_type\":\"string\",\"value_string\":\"getBookMethod\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},{\"ts\":1525264833583000,\"fields\": [{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"afterCompletion\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]}],\"operation_name\": \"getBookMethod\",\"parent_id\": \"-174740197246472194\",\"process\": {\"service_name\":\"Shop\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1525264833553000}"
  )

  val standaloneSpan: (String, String) = Tuple2(
    "0x000000000000000059f49538abc3a330",
    "{\"trace_id\":\"0x000000000000000059f49538abc3a330\",\"span_id\":\"6481969834325418800\",\"span_hash\":-7666663738831669191,\"duration\":5788,\"flags\":1,\"logs\":[{\"ts\":1525264828789000,\"fields\": [{\"key\":\"handler.class_simple_name\",\"value_type\":\"string\",\"value_string\":\"GreetingController\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"preHandle\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler.method_name\",\"value_type\":\"string\",\"value_string\":\"buyBookMethod\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},{\"ts\":1525264828794000,\"fields\": [{\"key\":\"event\",\"value_type\":\"string\",\"value_string\":\"afterCompletion\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"handler\",\"value_type\":\"string\",\"value_string\":\"public org.springframework.http.ResponseEntity ru.zhenik.spring.rest.hello.one.controller.GreetingController.buyBookMethod()\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]}],\"operation_name\": \"buyBookMethod\",\"parent_id\": \"0\",\"process\": {\"service_name\":\"Human\",\"tags\":[{\"key\":\"hostname\",\"value_type\":\"string\",\"value_string\":\"nikita-lenovo-yoga-710-14ikb\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"jaeger.version\",\"value_type\":\"string\",\"value_string\":\"Java-0.20.0\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null},{\"key\":\"ip\",\"value_type\":\"string\",\"value_string\":\"127.0.1.1\",\"value_bool\": false,\"value_long\":0,\"value_double\":0.0,\"value_binary\":null}]},\"refs\": [], \"start_time\":1525264828788000}"
  )

  def getTraceWith3Spans(): List[(String, String)] = List(span1,span2,span3)
  def getTraceWith1Span(): List[(String, String)] = List(standaloneSpan)






}
