package no.middleware.tramodana.modeler;

public abstract class SpanWithHierarchy {
  String parentId;
  String spanId;
  public SpanWithHierarchy(String parentId, String spanId){
    this.parentId = parentId;
    this.spanId = spanId;
  }

  public String getParentId() {
    return parentId;
  }

  public String getSpanId() {
    return spanId;
  }

  @Override
  public String toString() {
    return "from: " + parentId + ", to: " + spanId;
  }
}
