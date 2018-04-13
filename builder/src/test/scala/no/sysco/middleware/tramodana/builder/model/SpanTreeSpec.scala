package no.sysco.middleware.tramodana.builder.model

import no.sysco.middleware.tramodana.builder.{JsonSpanProtocol, Span, SpanTree}
import org.scalatest.{Matchers, WordSpec}
import org.junit.Assert._

class SpanTreeSpec extends WordSpec with Matchers with JsonSpanProtocol{
  import spray.json._



  "SpanTreeBuilder " should {

    "build correctly" in {
      //Create an n-ary tree for testing that looks like this:
      //               a
      //              /
      //             b
      //            /
      //           d

      // Arrange
      val list: List[Span] = JsonParser(Utils.listJson).convertTo[List[Span]]

      // Act
      val tree = SpanTreeBuilder.build(list)

      // Assert
      assertEquals("buyBookMethod", tree.operationName)
      assertEquals(1, tree.children.length)
      assertEquals("GET", tree.children.head.operationName)
      assertEquals(1, tree.children.head.children.length)
      assertEquals("getBookMethod", tree.children.head.children.head.operationName)

      SpanTreeBuilder.printTree(Option(tree))
    }

    "parsed incorrectly" in {

      try {
        val list: List[Span] = JsonParser(Utils.listJsonINVALID).convertTo[List[Span]]
        SpanTreeBuilder.build(list)
        fail()
      }
      catch {
        case deserializationException: DeserializationException => {}
        case _ => fail()
      }
    }

    "convert json -> pojo 0 child" in {

      // Act
      var tree: SpanTree = JsonParser(Utils.jsonTree0Child).convertTo[SpanTree]

      // Assert
      assertEquals("buyBookMethod", tree.operationName)
      assertEquals(tree.operationName, tree.value.operationName)
      assertEquals(0, tree.children.size)
    }

    "convert json -> pojo 1 child" in {
      // Arrange
      val span = JsonParser(Utils.spanJson).convertTo[Span]
      val someId = "qweqweqweq"
      val sTreeChild = SpanTree(
        operationName = "2",
        value = span,
        parent = Option(someId))
      val sTreeParent = SpanTree(
        operationName = "1",
        value = span,
        parent = None,
        children = List(sTreeChild)
      )

      // Act
      val parsedJson = sTreeParent.toJson.toString

      // Assert
      assertTrue(parsedJson.contains(someId))
    }

    "convert pojo -> json 0 child" in {
      // Arrange
      val list: List[Span] = JsonParser(Utils.listJson).convertTo[List[Span]]
      val tree = SpanTreeBuilder.build(list)

      // Act
      println(tree.toJson)
    }

    "convert from pojo -> json spanTree 1 child" in {
      // Arrange
      val list: List[Span] = JsonParser(Utils.listJson).convertTo[List[Span]]
      val tree = SpanTreeBuilder.build(list)

      // Act
      println(tree.toJson.toString)
    }
  }

  "SpanTreeBuilder SEQ " should {
    "make SEQ correctly" in {

    }
  }
}
