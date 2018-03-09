import no.middleware.tramodana.connector.CassandraSpanParser
import no.middleware.tramodana.connector.CassandraSpanParser.Span
import org.scalatest._
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException


class JsonParserTests extends WordSpec with Matchers {

  "CassandraSpanParser" should {


    "Parse strIdraw to parsedId" in {
      val strIdRaw = "0:0:0:0:580e:4402:fc7a:c24a"
      val expectedId = "0x0000000000000000580e4402fc7ac24a"

      val parsedId = CassandraSpanParser.getStringId(strIdRaw)
      parsedId should equal(expectedId)
    }

    "Add quotations to service_name and tags" in {
      val processRaw =
        """
          |{
          |   service_name:'Main Process',
          |   tags:[
          |     {
          |       key:'hostname',
          |       value_type:'string',
          |       value_string:'nikita-lenovo-yoga-710-14ikb',
          |       value_bool:false,
          |       value_long:0,
          |       value_double:0.0,
          |       value_binary:NULL
          |     },
          |     {
          |       key:'jaeger.version',
          |       value_type:'string',
          |       value_string:'Java-0.21.0',
          |       value_bool:false,
          |       value_long:0,
          |       value_double:0.0,
          |       value_binary:NULL
          |     },
          |     {
          |       key:'ip',
          |       value_type:'string',
          |       value_string:'127.0.1.1',
          |       value_bool:false,
          |       value_long:0,
          |       value_double:0.0,
          |       value_binary:NULL
          |     }
          |   ]
          | }""".stripMargin

      val newProcess = CassandraSpanParser.getJsonProcess(processRaw)
      newProcess should include("\"service_name\":")
      newProcess should include("\"tags\":")
    }

    "Add quotation marks to ref_type, trace_id, span_id and turn single quotation into double. Also structure as json" in {

      var refsRaw =
        """
          |[
          | {
          |   ref_type:'follows-from',trace_id:0x0000000000000000580e4402fc7ac24a,span_id:6345083704628134474
          | },
          | {
          |   ref_type:'follows-to',trace_id:0x0000000000000000580e4402fc7ac133,span_id:6345083704628134474
          | }
          |]
        """.stripMargin
      var refsNew = CassandraSpanParser.getJsonRefs(refsRaw.trim)
      refsNew should include("\"trace_id\"")
      refsNew should include("\"ref_type\"")
      refsNew should include("\"span_id\"")

      // testing single quotation has become double
      refsRaw should include("\'follows-from\'")
      refsNew should include("\"follows-from\"")

      // validate parsed is valid json format
      assert(!isJSONValid(refsRaw))
      assert(isJSONValid(refsNew))
    }

    "convert span data to json" in {
      val span = Span(
        "0x0000000000000000580e4402fc7ac24a",
        1515896982295615193L,
        -3155976967717968692L,
        770919L,
        1,
        """
          |[{"ts":1520414091706000,"fields":[{"key":"event","value_type":"string","value_string":"goHomeLog","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}]}]
        """.stripMargin,
        "goHomeSpan",
        6345083704628134474L,
        """
          |{"service_name":"Main Process","tags":[{"key":"hostname","value_type":"string","value_string":"nikita-lenovo-yoga-710-14ikb","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"jaeger.version","value_type":"string","value_string":"Java-0.21.0","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"},{"key":"ip","value_type":"string","value_string":"127.0.1.1","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}]}
        """.stripMargin,
        """
          |[{"ref_type":"follows-from","trace_id":"0x0000000000000000580e4402fc7ac24a","span_id":6345083704628134474}]
        """.stripMargin,
        1520414091728000L,
        """
          |[{"key":"processId","value_type":"string","value_string":"1","value_bool":false,"value_long":0,"value_double":0.0,"value_binary":"NULL"}]
        """.stripMargin
      )

      val spanAsJson = CassandraSpanParser.getJson(span)


      // validating that some values have been mapped to the correct keys
      spanAsJson should include ("\"trace_id\":\"0x0000000000000000580e4402fc7ac24a\"")
      spanAsJson should include ("\"span_id\":1515896982295615193")
      spanAsJson should include ("\"duration\":770919")

      // validate json before and after conversion
      assert(!isJSONValid(span.toString))
      assert(isJSONValid(spanAsJson))

    }
  }


  def isJSONValid(jsonInString: String): Boolean = try {
    val mapper = new ObjectMapper
    mapper.readTree(jsonInString)
    true
  } catch {
    case e: IOException =>
      false
  }

}
