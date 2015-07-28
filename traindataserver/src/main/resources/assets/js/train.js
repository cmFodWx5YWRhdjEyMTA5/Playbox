function getStations() {
	$.getJSON(
		"http://localhost:8080/api/stations", 
	    function(data) {
			var stationArray = data.stations.split(',')
			for (var i = 0; i < stationArray.length; ++i) {
				var a = document.createElement('a');
				var linkText = document.createTextNode(stationArray[i]);
				a.appendChild(linkText);
				a.title = stationArray[i];
				a.href = "#" + stationArray[i];
				document.getElementById("stations_div").appendChild(a);
			}
		}
	);
}