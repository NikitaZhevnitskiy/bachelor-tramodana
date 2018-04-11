package no.sysco.middleware.tramodana.modeler;

import java.util.Arrays;
import java.util.LinkedList;

public class TmaSpanTest extends TmaSpanWithHierarchy {
  String value;

  public TmaSpanTest(String parentId, String spanId, String value) {
    super(parentId, spanId);
    this.value = value;

  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return super.toString() + ", value: " + getValue();
  }

  public static void main(String[] args) {
    TmaSpanTest[] spans = {
      new TmaSpanTest("48", "343", "streetDoesNotExist"),
      new TmaSpanTest("48", "9", "searchNumber"),
      new TmaSpanTest("0", "48", "searchAddress"),
      new TmaSpanTest("9", "182", "numberNotFoundInStreet"),
      new TmaSpanTest("9", "7777777", "getHouseOwnerName")
    };

    System.out.println("List of spans: ");
    Arrays.stream(spans).forEach(span -> System.out.println(" -" + span));

    System.out.println("\nShow spans in tree order: ");
    new TmaTree<>(Arrays.asList(spans), "0").getFlattenedNodeListBreadthFirst().forEach(System.out::println);

    System.out.println("\nShow same hierarchy for tree after reversing initial span list (i.e. order works):");
    LinkedList<TmaSpanTest> rev = new LinkedList<>();
    Arrays.stream(spans).forEach(rev::addFirst);
    new TmaTree<TmaSpanTest>(rev, "0").getFlattenedNodeListBreadthFirst().forEach(System.out::println);

    System.out.println("\nShow depth first list of spans: ");
    new TmaTree<TmaSpanTest>(Arrays.asList(spans), "0").getFlattenedNodeListDepthFirst().forEach(System.out::println);
  }
}
