package no.middleware.tramodana.modeler;

import java.util.List;

public class TmaSpan {
    public String traceId;
    public String spanId;
    public long spanHash;
    public long duration;
    public int flags;
    public List<TmaLog> logs;
    public String operationName;
    public String parentId;
    public TmaProcess process;
    public List<TmaRef> refs;
    public long startTime;
    public List<TmaField> tags;

}


