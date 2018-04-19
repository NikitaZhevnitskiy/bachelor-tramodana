package no.sysco.middleware.tramodana.modeler;

import java.util.*;
import java.util.Stack;
import java.util.stream.Collectors;

public class TmaTree<T extends TmaSpanWithHierarchy> {

  private class Node {
    private T value;
    private List<Node> children;

    public Node(T value) {
      this.value = value;
    }
  }

  private Node root;

  public TmaTree(List<T> spanList, T root) {
    T rootSpan = spanList.parallelStream()
      .filter(span -> span.getParentId().equals("0"))
      .findFirst()
      .get();

    setRoot(new Node(rootSpan));
    spanList.remove(rootSpan);

    buildTree(spanList);
  }

  public TmaTree(List<T> spans, String rootVal) {

    Map<Boolean, List<T>> areDirectChildren = spans
      .parallelStream()
      .collect(Collectors.partitioningBy(span -> span.getParentId().equals(rootVal)));

    T rootSpan = areDirectChildren.get(true).get(0);
    setRoot(new Node(rootSpan));

    buildTree(areDirectChildren.get(false));
  }

  public void setRoot(Node root) {
    this.root = root;
  }

  public void buildTree(List<T> list) {
    Node current = root;
    LinkedList<Node> queue = new LinkedList<>();
    List<Node> nodes = list
      .parallelStream()
      .map(Node::new)
      .collect(Collectors.toList());
    do {
      Node finalCurrent = current;
      Map<Boolean, List<Node>> areDirectChildren = nodes
        .parallelStream()
        .collect(
          Collectors.partitioningBy(
            node -> node.value.getParentId().equals(finalCurrent.value.getSpanId())
          )
        );
      current.children = areDirectChildren.get(true);
      if (current.children != null) {
        queue.addAll(current.children);
      }
      nodes = areDirectChildren.get(false);
      if (queue.isEmpty()) {
        break;
      }
      current = queue.removeFirst();
    }
    while (true);
  }

  /**
   * Get the nodes in the tree in Breadth First order
   * @return
   */
  public List<T> getFlattenedNodeListBreadthFirst() {
    LinkedList<Node> queue = new LinkedList<>();
    LinkedList<T> res = new LinkedList<>();
    Node current = root;
    while (true) {
      res.add(current.value);
      if (current.children != null) {
        queue.addAll(current.children);
      }
      if (queue.isEmpty()) {
        break;
      }
      current = queue.poll();
    }
    return res;
  }


  /**
   * Get the nodes in the tree in Depth First order
   * @return
   */
  public List<T> getFlattenedNodeListDepthFirst() {
    Stack<Node> stack = new Stack<>();
    LinkedList<T> res = new LinkedList<>();
    Node current = root;
    while (true) {
      res.add(current.value);
      if (current.children != null) {
        stack.addAll(current.children);
      }
      if (stack.isEmpty()) {
        break;
      }
      current = stack.pop();
    }
    return res;
  }

}
