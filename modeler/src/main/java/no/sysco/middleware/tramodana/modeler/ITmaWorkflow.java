package no.sysco.middleware.tramodana.modeler;

public interface ITmaWorkflow {
    String getRoot();
    String[] getNodes();
    int[][] getPaths();
}
