function getStations() {
	$.getJSON(
		"http://localhost:8080/api/stations", 
	    function(data) {
			var stationArray = data.stations.split(',')
			var listBody = document.getElementById("station_list_body");
			for (var i = 0; i < stationArray.length; ++i) {
				// make one of these <li><a href="#fakelink">1</a></li>
				var stationName = stationArray[i];
				if (stationName) {
					var li = document.createElement('li');
					var a = document.createElement('a');
					a.appendChild(document.createTextNode(stationArray[i]));
					a.title = stationArray[i];
					a.href = "#" + stationArray[i];
					// add the hyperlink to the list item
					li.appendChild(a);
					// add the list item to the list
					var lastChild = listBody.lastChild.previousElementSibling;
					listBody.insertBefore(li, lastChild);
				}
			}
		}
	);
}

function showStationData(stationName) {
	//http://localhost:8080/api/trains?station=AIRPORT_STATION
	document.getElementById("train_title").textContent = "Getting train data for " + stationName + "...";
	$.getJSON(
			"http://localhost:8080/api/trains?station=" + stationName, 
		    function(data) {
				document.getElementById("train_title").textContent = stationName + " trains:";
				var listBody = document.getElementById("train_list_body");
				while (listBody.firstChild) {
					listBody.removeChild(listBody.firstChild);
				}
				for (var i in data) {
					var train = data[i];
					var li = document.createElement('li');
					var textContent = 
						"Train " + train.TRAIN_ID +
						" on " + train.LINE +
						" departing " + train.DIRECTION +
						" for " + train.DESTINATION;
					// times is a csv - but can contain a trailing comma - stop this
					var trainTimes = removeTrailingComma(train.EVENT_TIMES).split(",");
					if (null == trainTimes || trainTimes.length < 2) {
						// just do the time at
						textContent += " at " + removeTrailingComma(train.EVENT_TIMES);
					}
					else {
						// do the time from and to
						textContent += " from " + trainTimes[0];
						textContent += " to " + trainTimes[trainTimes.length - 1];
					}
					li.appendChild(document.createTextNode(textContent));
					listBody.appendChild(li);
				}
			}
		);
}

function removeTrailingComma(removalString) {
	if (removalString.charAt(removalString.length - 1) == ',') {
		removalString = removalString.substr(0, removalString.length - 1);
	}
	return removalString;
}