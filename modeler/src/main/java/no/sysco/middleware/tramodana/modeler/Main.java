package no.sysco.middleware.tramodana.modeler;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        // 1. receive Json
        // 2. parse Json to TmaWorkflowImpl
        BpmnModelCreator creator = new BpmnModelCreator();

        String workflowJson =
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
