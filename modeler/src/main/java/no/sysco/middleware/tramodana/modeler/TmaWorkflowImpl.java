package no.sysco.middleware.tramodana.modeler;


import java.util.Arrays;
import java.util.stream.Collectors;

public class TmaWorkflowImpl implements ITmaWorkflow {
    public String rootOperationName;
    public String[] operationSet;
    public int[][] traceModels;

    public String getRoot() {
        return rootOperationName;
    }

    public String[] getNodes() {
        return operationSet;
    }

    public int[][] getPaths() {
        return traceModels;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("rootOperationName: " + rootOperationName + "\n");

        s = new StringBuilder(Arrays.stream(operationSet)
                .reduce(s + "operationSet: [\n",
                        (a, b) -> a + " - " + b + "\n")
                .concat("]\n"));
        s.append("traceModels: [\n");
        for (int[] i : traceModels) {
            s.append(" - [ ")
                    .append(Arrays.stream(i)
                            .mapToObj(Integer::toString)
                            .collect(Collectors.joining(", ")))
                    .append(" ]\n");
        }
        return s.toString();
    }
}
