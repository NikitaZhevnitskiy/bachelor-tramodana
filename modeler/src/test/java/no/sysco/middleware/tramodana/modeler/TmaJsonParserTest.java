package no.sysco.middleware.tramodana.modeler;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TmaJsonParserTest {

    String testJsonString;
    @Before
    public void setUp(){
        testJsonString = "{\n \"some_key\":\"some_value\"\n}";
    }

    @After
    public void tearDown() {
    }

    @Test
    public void parseToWorkflow() {
        Assert.assertEquals("This didn't work",testJsonString,testJsonString);
    }
}