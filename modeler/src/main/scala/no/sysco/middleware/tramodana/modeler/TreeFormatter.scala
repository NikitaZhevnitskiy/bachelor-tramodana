package no.sysco.middleware.tramodana.modeler

import scala.annotation.tailrec

// TODO: fix so that it works with list of n-ary trees (only works with linked list)
object TreeFormatter {


  /**
    * Format a SpanNode's id attributes for use in XML
    *
    * @param n
    * @return
    */
  def formatIds(n: SpanNode): SpanNode = {
    val nodes: List[SpanNode] = flattenSpanNode(n).map(node => preprocessNode(node))

    @tailrec
    def rebuildTrace(list: List[SpanNode], acc: SpanNode): SpanNode =
      list match {
        case x :: xs => rebuildTrace(xs, x.copy(children = acc :: x.children))
        case Nil => acc
      }

    rebuildTrace(nodes.tail, nodes.head)
  }

  /**
    * Return a list of all nodes contained in a tree,
    * each without its children
    *
    * @param n : the tree
    * @return the list of nodes contained by the tree
    */
  private def flattenSpanNode(n: SpanNode): List[SpanNode] = {
    @tailrec
    def flatten_iter(in: Option[SpanNode], acc: List[SpanNode]): List[SpanNode] = {
      in match {
        case Some(node) =>
          flatten_iter(node.children.headOption, node.copy(children = Nil) :: acc)
        case None => acc
      }
    }

    flatten_iter(Some(n), List.empty)
  }

  /**
    * Format a node's parentId and spanId (processId)
    * in a valid XML attribute format
    *
    * @param n the node to format
    * @return the formatted node
    */
  private def preprocessNode(n: SpanNode): SpanNode = {
    Utils.formatParsableForXml(n).asInstanceOf[SpanNode]
  }
}
