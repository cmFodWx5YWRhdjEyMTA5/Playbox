package com.alonyx.traindataserver;

import com.alonyx.traindataserver.data.TrainData;
import com.alonyx.traindataserver.resources.GatherResource;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue( true );
        
        // test the gather resource
        TrainData[] trains = new GatherResource().getTrains();
        assertNotNull(trains);
    }
}
