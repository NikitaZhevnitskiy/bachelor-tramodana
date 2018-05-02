package no.sysco.middleware.tramodana.modeler

import no.sysco.middleware.tramodana.schema.JsonSpanProtocol
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class JsonToSpantreeParserSpec extends WordSpec with JsonSpanProtocol{


  "A json string storing Spantree data" when {
    "storing several Spantrees" should{
      "parse to a list of Spantrees with the same count of Spantrees" in{

        val INPUT_FILES_DIRECTORY = "examples/input_for_modeler"
        val jsonSource: String = Source
          .fromFile(s"$INPUT_FILES_DIRECTORY/ROOT_OPERATION_SET_SPAN_TREES.json")
          .getLines
          .mkString
        val parser = new JsonToSpantreeParser(jsonSource)
        val parsedSpans = parser.getSpanNodeList
        assert(parsedSpans.size == 2)
        val firstTree = parsedSpans.head
        assert(firstTree.children.size == 1)

      }
    }
  }

}
