package no.sysco.middleware.tramodana.modeler;

public abstract class TmaSpanWithHierarchy {
  String parentId;
  String spanId;
  public TmaSpanWithHierarchy(String parentId, String spanId){
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
