package no.sysco.middleware.tramodana.modeler;

public interface TmaIWorkflow {
    String getRoot();
    String[] getNodes();
    int[][] getPaths();
}
