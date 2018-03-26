package no.middleware.tramodana.modeler;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;

import java.io.File;
import java.util.List;

/**
 * This class will serialize Trace or Workflow object to BPMN format.
 */
public class BpmnModelCreator {

    private BpmnModelInstance modelInstance;

    public BpmnModelCreator() {
        //##########
        //########## 1. Imperative method of building a BPMN flowchart
        //##########

        // To create a new BPMN model from scratch you have to create an empty BPMN model instance with the following method:
        modelInstance = Bpmn.createEmptyModel();

        // The next step is to create a BPMN definitions element. Set the target namespace on it and add it to the newly created empty model instance.
        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setTargetNamespace("http://camunda.org/examples");
        modelInstance.setDefinitions(definitions);

        // create elements
        // create the process
        Process process = createElement(definitions, "process-with-one-task", Process.class);
        StartEvent startEvent = createElement(process, "start", StartEvent.class);
        ParallelGateway fork = createElement(process, "fork", ParallelGateway.class);
        ServiceTask task1 = createElement(process, "task1", ServiceTask.class);
        task1.setName("Service Task");
        UserTask task2 = createElement(process, "task2", UserTask.class);
        task2.setName("User Task");
        ParallelGateway join = createElement(process, "join", ParallelGateway.class);
        EndEvent endEvent = createElement(process, "end", EndEvent.class);

        // create flows
        createSequenceFlow(process, startEvent, fork);
        createSequenceFlow(process, fork, task1);
        createSequenceFlow(process, fork, task2);
        createSequenceFlow(process, task1, join);
        createSequenceFlow(process, task2, join);
        createSequenceFlow(process, join, endEvent);

        // validate the model
        Bpmn.validateModel(modelInstance);
        // convert to string
        String xmlString = Bpmn.convertToString(modelInstance);

        // write to output stream
        /*
        Bpmn.writeModelToStream(outputStream, modelInstance);
         */

        // write to file
        File file = new File("backendtest.bpmn");
        Bpmn.writeModelToFile(file, modelInstance);

        //##########
        //########## 2. fluent method of building a BPMN workflow
        //##########
        /*
        In this example we show the creation of a 2-branch workflow for a book selling service,
        demonstrating how to tag each element with an id (for reuse and reference in code) and how
        to name them (what will actually be shown in the visualisation);
         */

        BpmnModelInstance testmodel = Bpmn.createExecutableProcess("buybook")
                .startEvent(" buybookEvent")
                .name("Buy book")
                .parallelGateway("fork")
                .name("Enough money?")
                    .serviceTask("registerBook")
                    .name("Register book order")
                    .endEvent("end-with-book")
                    .name("Book ordered")
                .moveToNode("fork")
                    .userTask("user-cry")
                    .name("*user cries*")
                    .endEvent("end-without-book")
                    .name("No book for you!")
                .done();

        Bpmn.validateModel(testmodel);
        String testmodelXMLstring = Bpmn.convertToString(testmodel);
        File testfile = new File("fluenttest.bpmn");
        Bpmn.writeModelToFile(testfile, testmodel);

    }

    /** To simplify the repeating procedure of creating elements, you can use a helper method like this one.
     *
     * @param parentElement
     * @param id
     * @param elementClass
     * @param <T>
     * @return
     */
    private <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, String id, Class<T> elementClass) {
       /*
        Usually you want to add a process to your model. This follows the same 3 steps as the creation of the BPMN definitions element:
        - Create a new instance of the BPMN element
        - Set attributes and child elements of the element instance
        - Add the newly created element instance to the corresponding parent element
        */
        T element = modelInstance.newInstance(elementClass);
        element.setAttributeValue("id", id, true);
        parentElement.addChildElement(element);
        return element;
    }

    private SequenceFlow createSequenceFlow(Process process, FlowNode from, FlowNode to) {
        String identifier = from.getId() + "-" + to.getId();
        SequenceFlow sequenceFlow = createElement(process, identifier, SequenceFlow.class);
        process.addChildElement(sequenceFlow);
        sequenceFlow.setSource(from);
        from.getOutgoing().add(sequenceFlow);
        sequenceFlow.setTarget(to);
        to.getIncoming().add(sequenceFlow);
        return sequenceFlow;
    }

    public String parseToBpmn(TmaWorkflowInterface s) {

        String roots = s.getRoot();


        BpmnModelInstance bpmnModel = Bpmn.createExecutableProcess("empty")
                .startEvent("nothing")
                .done();
        return Bpmn.convertToString(bpmnModel);

    }
}
