package no.sysco.middleware.tramodana.modeler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.*;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.xml.ModelValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class TmaBpmnCreator {

    private final BpmnModelInstance bpmnTree;
    private final String bpmnXML;
    private final List<TmaNode> nodeList;
    private final JsonNode rootNodeIndex;
    private Map<JsonRootKeys, String> jsonKeyMap;

    TmaBpmnCreator(JsonNode json) {
        jsonKeyMap = loadJsonKeys();

        rootNodeIndex = getRoot(json);
        nodeList = getNodeList(json);


        JsonNode workflowTree = getWorkflowTree(json);
        bpmnTree = parseToBpmn(workflowTree);
        bpmnXML = BpmnToXML(bpmnTree).orElse(getErrorBpmnXml());
    }

    public static void main(String[] args) {
        testGenerateJsonWithArray();
        testReadingAndParsingJsonFile();
        AbstractFlowNodeBuilder processbuilder = makeExampleProcess();

    }

    private static void testReadingAndParsingJsonFile() {

        ObjectMapper m = new ObjectMapper();
        //String testworkflow_path = "modeler/src/main/java/no/sysco/middleware/tramodana/modeler/workflow_json_v02.json";
        String testworkflow_path = "examples/input_for_modeler/workflow_v03.json";

        try {

            byte[] testJsonBytes = Files.readAllBytes(Paths.get(testworkflow_path));
            JsonNode testWorkflowJson = m.readTree(testJsonBytes);
            System.out.println(m.writerWithDefaultPrettyPrinter().writeValueAsString(testWorkflowJson));

            String tree = new TmaBpmnCreator(testWorkflowJson).getBpmnXML();
            System.out.println(tree);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void testGenerateJsonWithArray() {
        ObjectMapper m = new ObjectMapper();
        ObjectNode root = m.createObjectNode();
        root.put("rootNode", 0);
        ArrayNode traceModels = m.createArrayNode();
        ArrayNode trace1 = m.createArrayNode();
        for (int i = 0; i < 5; i++) trace1.add(i);
        ArrayNode trace2 = m.createArrayNode();
        for (int i = 0; i < 4; i++) trace2.add(i);
        traceModels.add(trace1);
        traceModels.add(trace2);
        root.set("traceModels", traceModels);
        try {
            System.out.println(m.writerWithDefaultPrettyPrinter().writeValueAsString(root));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private static AbstractFlowNodeBuilder makeExampleProcess() {
        ProcessBuilder fluentProcess;
        fluentProcess = Bpmn.createExecutableProcess("banana");
        AbstractFlowNodeBuilder a;
        a = fluentProcess.startEvent("bananaStart");

        AbstractFlowNodeBuilder b;
        b = ((StartEventBuilder) a).name("Banana start");

        AbstractFlowNodeBuilder c;
        c = b.parallelGateway("fork");

        AbstractFlowNodeBuilder d;
        d = ((ParallelGatewayBuilder) c).name("Got strawbs?");

        AbstractFlowNodeBuilder e;
        e = d.serviceTask();

        AbstractFlowNodeBuilder f;
        f = ((ServiceTaskBuilder) e).name("Eat banana and strawbs");

        AbstractFlowNodeBuilder g;
        g = f.endEvent();

        AbstractFlowNodeBuilder h;
        h = ((EndEventBuilder) g).name("no more banana, no more strawbs");

        AbstractFlowNodeBuilder i;
        i = h.moveToNode("fork");

        AbstractFlowNodeBuilder j;
        j = i.userTask().name("Eat only banana");
        return j;
    }

    public String getBpmnXML() {
        return bpmnXML;
    }

    public BpmnModelInstance getBpmnTree() {
        return bpmnTree;
    }

    private Map<JsonRootKeys, String> loadJsonKeys() {
        Map<JsonRootKeys, String> map = new HashMap<>();

        map.put(JsonRootKeys.ROOT_NODE_KEY, "rootNode");
        map.put(JsonRootKeys.WORKFLOW_TREE, "workflowTree");
        map.put(JsonRootKeys.NODE_LIST, "nodeList");
        map.put(JsonRootKeys.TRACE_MODELS, "traceModels");
        map.put(JsonRootKeys.NODE_CHILDREN, "children");
        map.put(JsonRootKeys.NODE_INDEX, "node");

        return map;
    }

    private List<TmaNode> getNodeList(JsonNode json) {
        JsonNode nodeArray = json.path(jsonKeyMap.get(JsonRootKeys.NODE_LIST));
        TypeReference<List<TmaNode>> nodeListType = new TypeReference<List<TmaNode>>() {
        };
        ObjectReader reader = new ObjectMapper().readerFor(nodeListType);
        List<TmaNode> nodeList = null;
        try {
            nodeList = reader.readValue(nodeArray);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nodeList;
    }

    String getErrorBpmnXml() {

        BpmnModelInstance errorBpmn =
                Bpmn.createExecutableProcess("error")
                        .startEvent().name("error")
                        .endEvent()
                        .done();
        return Bpmn.convertToString(errorBpmn);
    }

    Optional<String> BpmnToXML(BpmnModelInstance bpmnTree) {

        try {
            Bpmn.validateModel(bpmnTree);
        } catch (ModelValidationException e) {
            System.err.println("The BPMN instance is not valid:\n" + e.getMessage());
            return Optional.empty();
        }

        String processXml = Bpmn.convertToString(bpmnTree);
        return Optional.of(processXml);

    }

    private JsonNode getRoot(JsonNode j) {
        String rootKey = jsonKeyMap.get(JsonRootKeys.ROOT_NODE_KEY);
        return j.path(rootKey);
    }

    private JsonNode getWorkflowTree(JsonNode j) {
        String treeKey = jsonKeyMap.get(JsonRootKeys.WORKFLOW_TREE);
        return j.path(treeKey);
    }

    public BpmnModelInstance parseToBpmn(JsonNode json) {
        TmaNode workflowRoot = nodeList.get(rootNodeIndex.intValue());
        String processId = workflowRoot.name + "_" + workflowRoot.id;
        StartEventBuilder startingEventBuilder =
                Bpmn.createExecutableProcess(processId)
                        .startEvent(workflowRoot.id)
                        .name(workflowRoot.name);

        // Iterate through the tree, create one node at a time
        AbstractFlowNodeBuilder completedTree = parseToBpmnIter(startingEventBuilder, json);


        // Finalise the build ( and builds the diagram elements)
        BpmnModelInstance finalisedBuild = completedTree.done();
        return finalisedBuild;
    }

    private <T extends AbstractFlowNodeBuilder> EndEventBuilder parseToBpmnIter(T processBuilder, JsonNode node) {

        TmaNode nodeDetails = getTmaNode(node);
        Iterator<JsonNode> children_it = getChildren(node);
        // nodes without children are leaves ( end events, or replying nodes)
        if (!children_it.hasNext()) {
            return processBuilder.endEvent(nodeDetails.id)
                    .name(nodeDetails.name);
            // processBuilder = addElement(processBuilder, node, EndEvent.class);
            // return processBuilder;
        }

        // processBuilder = addElement(processBuilder, node, ServiceTask.class);
        List<JsonNode> children = new ArrayList<>();
        children_it.forEachRemaining(children::add);
        if(children.size() == 1) {
            ServiceTaskBuilder sbt = processBuilder.serviceTask(nodeDetails.id)
                            .name(nodeDetails.name);
                return parseToBpmnIter(sbt,children.get(0));
        }

        // TODO: add annotation (possible?)

        String currentNodeId = getId(node);
        // if the current node has more than one child,
        // we create a gateway element and use it
        // as a return point until all children are processed
        if (children.size() > 1) {
            currentNodeId = "fork_from_node_" + getId(node);
            ParallelGatewayBuilder pgwb = processBuilder.parallelGateway(currentNodeId);

            // start recursion for each child
            for (JsonNode child : children) {
                 parseToBpmnIter(pgwb, child)
                                .moveToNode(currentNodeId);
            }
        }
        return (EndEventBuilder) processBuilder;
    }

    private Iterator<JsonNode> getChildren(JsonNode node) {
        return node.path(jsonKeyMap.get(JsonRootKeys.NODE_CHILDREN)).elements();
    }

    private boolean hasChildren(JsonNode node) {
        return getChildren(node).hasNext();
    }

    private <T extends BpmnModelElementInstance> AbstractFlowNodeBuilder addElement(
            AbstractFlowNodeBuilder pb,
            JsonNode node,
            Class<T> elementClass) {

        BpmnModelInstance bpmnInst = Bpmn.createEmptyModel();
        T element = bpmnInst.newInstance(elementClass);
        TmaNode tmaNode = getTmaNode(node);
        element.setAttributeValue("id", tmaNode.id, true);
        element.setAttributeValue("name", tmaNode.name);
        pb.addExtensionElement(element);
        return pb;
    }

    private String getId(JsonNode node) {
        return getTmaNode(node).id;
    }

    private String getName(JsonNode node) {
        return getTmaNode(node).name;
    }

    private TmaNode getTmaNode(JsonNode node) {
        JsonNode nodeListIndex = node.path(jsonKeyMap.get(JsonRootKeys.NODE_INDEX));
        int index = nodeListIndex.intValue();
        return nodeList.get(index);
    }

    private enum JsonRootKeys {
        ROOT_NODE_KEY,
        NODE_LIST,
        TRACE_MODELS,
        NODE_CHILDREN,
        NODE_INDEX,
        WORKFLOW_TREE
    }

}
