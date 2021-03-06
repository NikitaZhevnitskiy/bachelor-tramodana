package no.sysco.middleware.tramodana.schema

import org.junit.Assert._
import org.scalatest.{Matchers, WordSpec}

class SpanTreeSpec extends WordSpec with Matchers with JsonSpanProtocol{
  import spray.json._



  "SpanTreeBuilder " should {

    "build correctly 3 nodes" in {
      //Create an n-ary tree for testing that looks like this:
      //               A
      //              /
      //             B
      //            /
      //           D

      // Arrange
      val list: List[Span] = JsonParser(Utils.listJson).convertTo[List[Span]]

      // Act
      val tree = SpanTreeBuilder.build(list)

      // Assert
      assertEquals("buyBookMethod", tree.value.operationName)
      assertEquals(1, tree.children.length)
      assertEquals("GET", tree.children.head.value.operationName)
      assertEquals(1, tree.children.head.children.length)
      assertEquals("getBookMethod", tree.children.head.children.head.value.operationName)
    }

    "build correctly 4 nodes" in {
      //                        1A
      //                     /   |
      //                   2B    5E
      //                  /
      //                3C

      // Arrange
      val list: List[Span] = Utils.getSpanListWith4Nodes()

      // Act
      val tree = SpanTreeBuilder.build(list)

      // Assert
      assertEquals("A", tree.value.operationName)
      assertEquals(2, tree.children.length)
      assertEquals("B", tree.children.head.value.operationName)
      assertEquals(1, tree.children.head.children.length)
      assertEquals("E", tree.children.tail.head.value.operationName)
      assertEquals("C", tree.children.head.children.head.value.operationName)
      assertEquals(0, tree.children.tail.head.children.length)
    }

    "build correctly 7 nodes" in {
      //                        1A
      //                     /   |   \
      //                   2B    5E   7G
      //                  / \     \
      //                3C   4D    6F

      // Arrange
      val list: List[Span] = Utils.getSpanListWith7Nodes()

      // Act
      val tree = SpanTreeBuilder.build(list)

      // Assert
      assertEquals("A", tree.value.operationName)
      assertEquals(3, tree.children.length)
      assertEquals("B", tree.children.head.value.operationName)
      assertEquals(2, tree.children.head.children.length)
      assertEquals("E", tree.children.tail.head.value.operationName)
      assertEquals("C", tree.children.head.children.head.value.operationName)
      assertEquals(1, tree.children.tail.head.children.length)

    }

    "build correctly 7 nodes + 1 inncorrect" in {
      //        8M           1A
      //                  /   |   \
      //                2B    5E   7G
      //               / \     \
      //             3C   4D    6F

      // Arrange
      val incorrectSpan: Span = Utils.getSpan().copy(traceId = "8", parentId = "15", startTime = 8, operationName = "M")
      val list: List[Span] = Utils.getSpanListWith7Nodes().+:(incorrectSpan)

      // Act
      val tree = SpanTreeBuilder.build(list)

      // Assert
      assertEquals(3, tree.children.size)

    }
    "build correctly 1 nodes + 1 inncorrect" in {
      //        8M           1A
      //

      // Arrange
      val incorrectSpan: Span = Utils.getSpan().copy(traceId = "8", parentId = "15", startTime = 8, operationName = "M")
      val correctSpan: Span = Utils.getSpan().copy(traceId = "1", parentId = "0", startTime = 8, operationName = "A")
      val list: List[Span] = List(incorrectSpan, correctSpan)

      // Act
      val tree = SpanTreeBuilder.build(list)

      // Assert
        // incorrect node will be ignored
      assertEquals(0, tree.children.size)

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
      assertEquals("buyBookMethod", tree.value.operationName)
      assertEquals(0, tree.children.size)
    }

    "convert json -> pojo 1 child" in {
      // Arrange
      val span = JsonParser(Utils.spanJson).convertTo[Span]
      val someId = "dasdasdasdasdasfsdgdsfg"

      val sTreeChild = SpanTree(
        value = span.copy(operationName = "B", parentId = "1", spanId = someId),
        List.empty
      )
      val sTreeParent = SpanTree(
        value = span.copy(operationName = "A", parentId = "0", spanId="1"),
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

    "convert List[Seq[Span]] to Set[Seq[Span]]" in {

      // Arrange
      val spans1: Seq[Span] = SpanTreeBuilder
        .getSequence(SpanTreeBuilder.build(Utils.getSpanListWith4Nodes()))
      val spans2: Seq[Span] = SpanTreeBuilder
        .getSequence(SpanTreeBuilder.build(Utils.getSpanListWith7Nodes()))
      val spansDuplication = spans1

      val listOfSeq: List[Seq[Span]] = List(spans1, spans2, spansDuplication)

      // Act
      val setOfSeq: Set[Seq[Span]] = SpanTreeBuilder.getSetOfSeq(listOfSeq)

      // Assert
      assertEquals(3,listOfSeq.size)
      assertEquals(2,setOfSeq.size)



    }

    "convert Set[Seq[Span]] to Set[SpanTree]" in {
      // Arrange
      val spans1: Seq[Span] = SpanTreeBuilder
        .getSequence(SpanTreeBuilder.build(Utils.getSpanListWith4Nodes()))
      val spans2: Seq[Span] = SpanTreeBuilder
        .getSequence(SpanTreeBuilder.build(Utils.getSpanListWith7Nodes()))
      val listOfSeq: List[Seq[Span]] = List(spans1, spans2)
      val setOfSeq: Set[Seq[Span]] = SpanTreeBuilder.getSetOfSeq(listOfSeq)
//      val jsonSpanSeq = setOfSeq.toJson.toString


      // Act
      val setOfTrees: Set[SpanTree] = setOfSeq.map((seq)=>SpanTreeBuilder.build(seq.toList))
      println(setOfTrees.toJson.toString)

      // Assert
      assertEquals(2, setOfTrees.size)
      assertEquals(2, setOfTrees.head.children.size)
    }
  }

  "SpanTreeBuilder SEQ " should {
    "make SEQ correctly" in {
      // Arrange
      val spans: List[Span] = Utils.getSpanListWith7Nodes()
      val tree = SpanTreeBuilder.build(spans)

      val expectedList = Seq("A","B","C","D","E","F","G")

      // Act
      val actualList = SpanTreeBuilder.getSequence(tree).flatMap(span => span.operationName)

      // Assert
      assertEquals(actualList.toString, expectedList.toString)
    }
  }
}
