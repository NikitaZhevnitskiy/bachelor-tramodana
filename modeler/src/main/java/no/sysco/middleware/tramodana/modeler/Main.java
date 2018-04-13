package no.sysco.middleware.tramodana.modeler;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class Main {
    private static final String TEST_FILE_DIR_PATH = "examples/output_for_modeler/";

    public static void main(String[] args) {
        testFunc();

        // get json string

        // parse json string to JSON
        JsonNode root = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            if (args.length != 0) {
                String filepath = args[0];
                root = mapper.readTree(new File(filepath));
            } else {
                System.out.println("no args");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // parse JSON to TmaWorkflow
        // TmaIWorkflow tmaWorkflow = TmaJsonParser.parseToWorkflow(root);

        // parse JsonNode to BpmnXML
        Optional<String> bpmnXml = parseJsonNodeToXML(root);


        // send back xml
        String res = bpmnXml.orElse("BpmnXml is null");
        System.out.println(res);

    }

    private static Optional<String> parseJsonNodeToXML(JsonNode root) {

        TmaBpmnCreator creator = new TmaBpmnCreator(root);
        BpmnModelInstance bpmnTree = creator.parseToBpmn(root);
        return creator.BpmnToXML(bpmnTree);
    }

    public static void testFunc() {

        // 1. receive Json
        // 2. parse Json to TmaWorkflowImpl
        BpmnModelCreator creator = new BpmnModelCreator();
        ObjectMapper mapper = new ObjectMapper();

        System.out.println("---- With fluent API ----");
        BpmnModelInstance testmodel = creator.createTestBpmnDiagramWithFluentAPI("banana thrown");
        System.out.println("---- BEFORE EXTENSION ----");
        String parsedWorkflow = Bpmn.convertToString(testmodel);
        System.out.println(parsedWorkflow);
        System.out.println("---- AFTER EXTENSION ----");
        BpmnModelInstance extendedTestModel = creator.testExtendModel(testmodel, "end-without-book");
        String extendedWorkflow = Bpmn.convertToString(extendedTestModel);
        System.out.println(extendedWorkflow);
        System.out.println("---- EXTENSION OF PROCEDURALLY GENERATED BPMN ----");
        BpmnModelInstance procTestModel = creator.createTestBpmnProcessProcedurally();
        BpmnModelInstance procExtModel = creator.testExtendModel(procTestModel,"end");
        String procExtString = Bpmn.convertToString(procExtModel);
        System.out.println("---- DONE ----");


        //String parsedWorkflow = creator.parseToBpmn(tree);
        File beforeBpmnExtension = new File( TEST_FILE_DIR_PATH+ "fluenttest.bpmn");
        File afterBpmnExtension = new File( TEST_FILE_DIR_PATH+ "fluenttest_after.bpmn");
        File procBpmnExtension = new File( TEST_FILE_DIR_PATH+ "proctest_ext.bpmn");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter( beforeBpmnExtension));
            bw.write(parsedWorkflow);
            bw.close();
            BufferedWriter bw2 = new BufferedWriter(new FileWriter( afterBpmnExtension));
            bw2.write(extendedWorkflow);
            bw2.close();
            BufferedWriter bw3 = new BufferedWriter(new FileWriter( procBpmnExtension));
            bw3.write(procExtString);
            bw3.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
