package no.sysco.middleware.tramodana.connector

import org.scalatest.{Matchers, WordSpec}
import org.junit.Assert._

class SpanConnectorSpec extends WordSpec with Matchers with JsonSpanProtocol {
  import spray.json._


  "SpanConnectorSpec jsonProtocol " should {

    "build correctly" in {
      val more =
        """
          |{
          |   "trace_id": "0x0000000000000000409796cd6e3ffe97",
          |   "span_id": -6293894814040201916,
          |   "span_hash": 4205255064732529423,
          |   "duration": 19810,
          |   "flags": 1,
          |   "logs": [{"ts": 1520926782553000,
          |     "fields": [
          |       {"key": "handler.class_simple_name", "value_type": "string", "value_string": "GreetingController", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "handler", "value_type": "string", "value_string": "public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "event", "value_type": "string", "value_string": "preHandle", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "handler.method_name", "value_type": "string", "value_string": "getBookMethod", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}]}, {"ts": 1520926782565000, "fields": [{"key": "event", "value_type": "string", "value_string": "afterCompletion", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "handler", "value_type": "string", "value_string": "public java.lang.String ru.zhenik.spring.rest.hello.two.controller.GreetingController.getBookMethod()", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}]}
          |     ],
          |   "operation_name": "getBookMethod",
          |   "parent_id": 7070879976147341838,
          |   "process": {"service_name": "Shop", "tags": [{"key": "hostname", "value_type": "string", "value_string": "nikita-lenovo-yoga-710-14ikb", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "jaeger.version", "value_type": "string", "value_string": "Java-0.20.0", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "ip", "value_type": "string", "value_string": "127.0.1.1", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}]},
          |   "refs": [{
          |        "ref_type": "follows-from",
          |        "trace_id": "0x00000000000000007501dc93b11c4999",
          |        "span_id": 8431262504304003481
          |    }],
          |   "start_time": 1520926782546000,
          |   "tags": [{"key": "http.status_code", "value_type": "int64", "value_string": "", "value_bool": false, "value_long": 200, "value_double": 0.0, "value_binary": null}, {"key": "component", "value_type": "string", "value_string": "java-web-servlet", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "span.kind", "value_type": "string", "value_string": "server", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "http.url", "value_type": "string", "value_string": "http://localhost:10082/getbook", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}, {"key": "http.method", "value_type": "string", "value_string": "GET", "value_bool": false, "value_long": 0, "value_double": 0.0, "value_binary": null}]
          |}
          |""".stripMargin

      // Act
      val span = JsonParser(more).convertTo[Span]
      println(getJsonStringifyIds(span))

      // Assert
      assertEquals("getBookMethod",span.operationName)
    }
  }

}
