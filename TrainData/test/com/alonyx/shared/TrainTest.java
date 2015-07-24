package com.alonyx.shared;

import static org.junit.Assert.*;

import org.junit.Test;

import com.alonyx.server.TrainData;

public class TrainTest {
	
	public TrainData[] createTestData() {
		// create train data
		return new TrainData[] {
			new TrainData("Indian Creek","E","5/5/2015 4:48:36 AM","BLUE","05:06:07 AM","AVONDALE STATION",106026,1010,"16 min"),
			new TrainData("Hamilton E Holmes","W","5/5/2015 4:51:36 AM","BLUE","04:54:30 AM","AVONDALE STATION",101206,151,"2 min"),
			new TrainData("Indian Creek","E","5/5/2015 4:51:44 AM","BLUE","05:05:53 AM","AVONDALE STATION",106026,834,"13 min"),
			new TrainData("Indian Creek","E","5/5/2015 4:54:55 AM","BLUE","05:05:34 AM","AVONDALE STATION",106026,628,"9 min"),
			new TrainData("Indian Creek","E","5/6/2015 4:43:06 AM","BLUE","05:06:13 AM","AVONDALE STATION",106026,1358,"22 min"),
			new TrainData("Indian Creek","E","5/6/2015 4:45:38 AM","BLUE","05:05:53 AM","AVONDALE STATION",106026,1185,"19 min"),
			new TrainData("Indian Creek","E","5/6/2015 4:48:08 AM","BLUE","05:05:39 AM","AVONDALE STATION",106026,1012,"16 min"),
			new TrainData("Hamilton E Holmes","W","5/6/2015 4:50:52 AM","BLUE","04:54:47 AM","AVONDALE STATION",101206,205,"2 min"),
			new TrainData("Indian Creek","E","5/6/2015 4:50:42 AM","BLUE","05:05:40 AM","AVONDALE STATION",106026,858,"13 min"),
			new TrainData("Hamilton E Holmes","W","5/7/2015 4:28:38 AM","BLUE","04:31:08 AM","AVONDALE STATION",101026,52,"Arriving"),
			new TrainData("Indian Creek","E","5/8/2015 4:44:44 AM","BLUE","05:08:02 AM","AVONDALE STATION",106026,1371,"22 min"),
			new TrainData("Indian Creek","E","5/8/2015 4:47:31 AM","BLUE","05:08:00 AM","AVONDALE STATION",106026,1217,"19 min"),
			new TrainData("Hamilton E Holmes","W","5/8/2015 4:50:31 AM","BLUE","04:54:41 AM","AVONDALE STATION",101206,235,"3 min"),
			new TrainData("Indian Creek","E","5/8/2015 4:50:33 AM","BLUE","05:08:05 AM","AVONDALE STATION",106026,1039,"16 min"),
			new TrainData("Hamilton E Holmes","W","5/9/2015 6:01:35 AM","BLUE","06:05:38 AM","AVONDALE STATION",104206,233,"3 min"),
			new TrainData("Hamilton E Holmes","W","5/9/2015 6:04:16 AM","BLUE","06:05:22 AM","AVONDALE STATION",104206,58,"Arriving"),
			new TrainData("Hamilton E Holmes","W","5/10/2015 6:01:45 AM","BLUE","06:05:40 AM","AVONDALE STATION",104206,184,"2 min"),
			new TrainData("Indian Creek","E","5/10/2015 6:02:11 AM","BLUE","06:25:59 AM","AVONDALE STATION",102026,1403,"22 min"),
			new TrainData("Hamilton E Holmes","W","5/10/2015 6:05:01 AM","BLUE","06:05:13 AM","AVONDALE STATION",104206,6,"Arriving"),
			new TrainData("Indian Creek","E","5/10/2015 6:04:58 AM","BLUE","06:26:04 AM","AVONDALE STATION",102026,1257,"20 min"),
		};
	}
	
	@Test
	public void testGetEventTimeDate() {
		for (TrainData train : createTestData()) {
			assertNotNull(train.getEventTimeDate());
		}
	}
	
	@Test
	public void testGetArrivalTimeDate() {
		for (TrainData train : createTestData()) {
			assertNotNull(train.getNextArrivalDate());
		}
	}

}
