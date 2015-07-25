package com.alonyx.traindataserver;

import junit.framework.TestCase;

import com.alonyx.traindataserver.data.TrainData;
import com.alonyx.traindataserver.resources.GatherResource;

public class GetDataTest extends TestCase {
	
	public void testGatherResource() {
        // test the gather resource
        TrainData[] trains = new GatherResource().getTrains();
        assertNotNull(trains);
        assertTrue(trains.length > 0);
	}

}
