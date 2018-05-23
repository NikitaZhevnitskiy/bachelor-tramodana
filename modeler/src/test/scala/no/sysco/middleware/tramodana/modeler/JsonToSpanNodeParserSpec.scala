package no.sysco.middleware.tramodana.modeler

import no.sysco.middleware.tramodana.schema.JsonSpanProtocol
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, WordSpec}

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class JsonToSpanNodeParserSpec extends WordSpec with BeforeAndAfter with JsonSpanProtocol {

  val INPUT_FILES_DIRECTORY = "examples/input_for_modeler"

  val jsonSource: String = Source
    .fromFile(s"$INPUT_FILES_DIRECTORY/ROOT_OPERATION_SET_SPAN_TREES.json")
    .getLines
    .mkString
  var spanNodes: List[SpanNode] = List.empty
//  val parser = new JsonToSpanNodeParser

  before {
    spanNodes = JsonToSpanNodeParser.getFormattedSpanNodes(jsonSource)
  }

  "A json string storing Jaeger trace models" when {
    "storing several trace models" should {
      "parse to a list of SpanNodes with a size equal to the number of parsed trace models" in {
        assert(spanNodes.size == 2)
      }
      "merge to a single BPMN tree" in {
        val tree = JsonToSpanNodeParser.parse(jsonSource).get.asInstanceOf[SpanNode]
        tree.printPretty()
        val bpmn = new BpmnCreator(tree, tree.getOperationName)
        val xml = bpmn.getBpmnXmlStr.get
        Utils.writeToExampleDir(xml, "aggregated_workflow")
      }
      "parse each trace to a valid BPMN diagram" in {
        val xmls = spanNodes.map(sn => {
          sn.printPretty()
          JsonToSpanNodeParser.mergeIntoTree(sn :: Nil, "id_0")
        })
          .flatMap(sn => sn.toSet)
          .map(sn => {
            sn.printPretty()
            new BpmnCreator(sn, sn.getOperationName).getBpmnXmlStr.get
          })

        var traceCount = 1
        xmls.foreach(
          x => {
            Utils.writeToExampleDir(x, "separate_trace_model_" + traceCount)
            traceCount += 1
          }
        )
      }
      "convert to a list of Edge objects" in {
        println
        println("Parsing SpanNode tree to list of edges: ")
        val resultSet = spanNodes.map(n => JsonToSpanNodeParser.splitIntoEdges(Some(n), Set.empty))
          .map(es => {
            println("---- Node count: " + es.size ++ "------")
            es.foreach(e => println(e))
            println("------------------------")
            es
          })
          .reduce((acc, set) => acc ++ set)
        println
        println("All duplicates eliminated: ")
        resultSet.foreach(e => println(e))
        assert(resultSet.size == 6)
      }
      "eliminates all duplicate Edges" in {
        println(" flatmap + toSet == reduce ")
        val resultSet: Set[SpanEdge] = spanNodes.flatMap(sn => JsonToSpanNodeParser.splitIntoEdges(Some(sn), Set.empty))
          .toSet
        resultSet.foreach(se => println(se))
        assert(resultSet.size == 6)
      }
    }
  }

  "A span" when {
    "compared to another" should {
      "return true if they share the same operationName and Service" in {
        val span1 = spanNodes.head.value
        val equal_to_span_1 = spanNodes.last.value
        assert(JsonToSpanNodeParser.spanEq(span1, equal_to_span_1))
      }
      "return false if operationName and/or Service are not the same" in {
        val span1 = spanNodes.head.value
        val not_equal_to_span1 = spanNodes.head.children.head.value
        assert(!JsonToSpanNodeParser.spanEq(span1, not_equal_to_span1))
      }
    }
  }
}
