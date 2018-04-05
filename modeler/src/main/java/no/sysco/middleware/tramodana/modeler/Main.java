package no.sysco.middleware.tramodana.modeler;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        //testFunc();

        // get json string

        // parse json string to JSON
        JsonNode root = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            if (args.length != 0){
                String filepath = args[0];
                root = mapper.readTree(new File(filepath));
            }
            else {
                System.out.println("no args");
                root = mapper.readTree(testJsonFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // parse JSON to TmaWorkflow
        ITmaWorkflow tmaWorkflow = TmaJsonParser.parseToWorkflow(root);

        // parse TmaWorkflow to BpmnXML
        TmaBpmnCreator creator = new TmaBpmnCreator();
        String bpmnXml = creator.getBpmnXML(tmaWorkflow);

        // send back xml

    }

    public static String testJsonFile() {
        return
                "{\n" +
                        "  \"rootOperationName\": \"search_book_event\",\n" +
                        "  \"operationSet\": [\n" +
                        "    \"search_book_event\",\n" +
                        "    \"GET\",\n" +
                        "    \"executeQuery(String query)\",\n" +
                        "    \"process_book_list\",\n" +
                        "    \"no_books_found\",\n" +
                        "    \"getBookByName()\",\n" +
                        "    \"book_name_found\",\n" +
                        "    \"book_name_not_found\"\n" +
                        "  ],\n" +
                        "\n" +
                        "  \"traceModels\": [\n" +
                        "    [0,1,2,3],\n" +
                        "    [0,1,2,4],\n" +
                        "    [0,1],\n" +
                        "    [0,1,5,6],\n" +
                        "    [0,1,5,7]\n" +
                        "  ]\n" +
                        "}\n";

    }

    public static void testFunc() {

        // 1. receive Json
        // 2. parse Json to TmaWorkflowImpl
        BpmnModelCreator creator = new BpmnModelCreator();

        String workflowJson = testJsonFile();
        ObjectMapper mapper = new ObjectMapper();
        TmaWorkflowImpl tree = null;
        try {

            tree = mapper.readValue(workflowJson, TmaWorkflowImpl.class);
            System.out.println("parsed workflow: ");
            System.out.println(tree);

        } catch (IOException ex) {

            ex.printStackTrace();

        }

        String parsedWorkflow = creator.parseToBpmn(tree);

    }


}
