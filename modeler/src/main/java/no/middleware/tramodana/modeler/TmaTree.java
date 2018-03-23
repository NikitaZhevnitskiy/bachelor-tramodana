package no.middleware.tramodana.modeler;

import java.util.Comparator;
import java.util.List;

public class TmaTree {

    public TmaTree(List<TmaSpan> spanList) {
        List<TmaSpan> spans = spanList;
        TmaSpan rootSpan = spans.parallelStream()
                .filter(span -> span.parentId.equals("0"))
                .findFirst()
                .get();

        spans.remove(rootSpan);

        spans.sort(Comparator.comparing(a -> a.parentId));
    }
}
