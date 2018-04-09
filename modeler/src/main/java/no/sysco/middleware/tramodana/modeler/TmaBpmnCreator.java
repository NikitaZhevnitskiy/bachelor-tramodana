package no.sysco.middleware.tramodana.modeler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.builder.*;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class TmaBpmnCreator {

    private enum JsonRootKeys {
        ROOT_NODE_KEY,
        NODE_LIST, TRACE_MODELS, WORKFLOW_TREE
    }

    private Map<JsonRootKeys, String> jsonKeyMap;

    TmaBpmnCreator(JsonNode j) {
        jsonKeyMap = loadJsonKeys();

        JsonNode json = j;
        JsonNode root = getRoot(json);


        JsonNode tree = getTree(json);
        String bpmnXML = parseToBpmnXML(tree);
    }

    private JsonNode getRoot(JsonNode j) {
        String rootKey = jsonKeyMap.get(JsonRootKeys.ROOT_NODE_KEY);
        return j.path(rootKey);
    }
    private JsonNode getTree(JsonNode j) {
        String treeKey = jsonKeyMap.get(JsonRootKeys.WORKFLOW_TREE);
        return j.path(treeKey);
    }

    private String parseToBpmnXML(JsonNode json) {
        ProcessBuilder fluentProcess;
        fluentProcess = Bpmn.createExecutableProcess("banana");
        StartEventBuilder a;
        a = fluentProcess.startEvent("bananaStart");
        StartEventBuilder b;
        b = a.name("Banana start");
        ParallelGatewayBuilder c;
        c = b.parallelGateway("fork");
        ParallelGatewayBuilder d;
        d = c.name("Got strawbs?");
        ServiceTaskBuilder e;
        e = d.serviceTask();
        ServiceTaskBuilder f;
        f = e.name("Eat banana and strawbs");
        EndEventBuilder g;
        g = f.endEvent();
        EndEventBuilder h;
        h = g.name("no more banana, no more strawbs");
        AbstractFlowNodeBuilder i;
        i = h.moveToNode("fork");



        return null;
    }

    String getBpmnXML(ITmaWorkflow tmaWorkflow) {
        return "implement me - TmaBpmnCreator.getBpmnXML";
    }


    private Map<JsonRootKeys, String> loadJsonKeys() {
        Map<JsonRootKeys,String> map = new HashMap<>();

        map.put(JsonRootKeys.ROOT_NODE_KEY, "rootNode");
        map.put(JsonRootKeys.WORKFLOW_TREE, "workflowTree");
        map.put(JsonRootKeys.NODE_LIST, "nodeList");
        map.put(JsonRootKeys.TRACE_MODELS, "traceModels");

        return map;
    }

    public static void main(String[] args) {
        ObjectMapper m = new ObjectMapper();
        ObjectNode root = m.createObjectNode();
        root.put("rootNode", 0);
        ArrayNode traceModels = m.createArrayNode();
        ArrayNode trace1= m.createArrayNode();
        for(int i = 0; i < 5; i++) trace1.add(i);
        ArrayNode trace2= m.createArrayNode();
        for(int i = 0; i < 4; i++) trace2.add(i);
        traceModels.add(trace1);
        traceModels.add(trace2);
        root.set("traceModels", traceModels);
        try {
            System.out.println( m.writerWithDefaultPrettyPrinter().writeValueAsString(root));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String testworkflow_path = "modeler/src/main/java/no/sysco/middleware/tramodana/modeler/parsed_workflow.json";
        try{

            byte[] testJsonBytes = Files.readAllBytes(Paths.get(testworkflow_path));
            JsonNode n = m.readTree(testJsonBytes);
            System.out.println(m.writerWithDefaultPrettyPrinter().writeValueAsString(n));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
