package no.sysco.middleware.tramodana.modeler

import no.sysco.middleware.tramodana.schema.JsonSpanProtocol
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class JsonToSpannodeParserSpec extends WordSpec with JsonSpanProtocol{

  val INPUT_FILES_DIRECTORY = "examples/input_for_modeler"

  val jsonSource: String = Source
    .fromFile(s"$INPUT_FILES_DIRECTORY/ROOT_OPERATION_SET_SPAN_TREES.json")
    .getLines
    .mkString

  "A json string storing Jaeger trace models" when {
    "storing several trace models" should{
      "parse to a list of SpanNodes with a size equal to the number of parsed trace models" in{
        val parser = new JsonToSpannodeParser(jsonSource)
        val spanNodes = parser.preprocessedSpanNodeList
        assert(spanNodes.size == 2)
      }
      "parse each trace to a valid BPMN diagram" in {
        val parser = new JsonToSpannodeParser(jsonSource)
        val spanNodes = parser.preprocessedSpanNodeList
        val xmls = spanNodes.map(sn => {
          sn.printPretty()
          new BpmnCreator(sn, sn.getOperationName).getBpmnXmlStr.get
        })
        var traceCount = 1
        xmls.foreach(
          x => {
            Utils.writeToExampleDir(x,"separate_trace_model_" + traceCount)
            traceCount += 1
          }
        )
      }
    }
  }

}
