package uk.co.darkerwaters.client.variables;

import java.util.ArrayList;

import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.Gauge;
import com.google.gwt.visualization.client.visualizations.Gauge.Options;

public class GaugeChartPanel {
	
	private Gauge chart = null;
	private DataTable data = null;
	private Options options = null;
	
	private String[] variableTitles;
	private int[] variableValues;
	
	public interface CreationListener {
		public void chartCreated(GaugeChartPanel panel, Gauge chart);
	}
	
	public GaugeChartPanel(final CreationListener listener) {
		// Create a callback to be called when the visualization API
		// has been loaded.
		Runnable onLoadCallback = new Runnable() {
			public void run() {
				// Create a pie chart visualization.
				data = createTable();
				options = createOptions();
				chart = new Gauge(data, options);
				// inform the listener
				listener.chartCreated(GaugeChartPanel.this, chart);
				// update our data with the latest sent (before we were created)
				updateData(variableTitles, variableValues);
			}
		};
		// Load the visualization api, passing the onLoadCallback to be called
		// when loading is done.
		VisualizationUtils.loadVisualizationApi(onLoadCallback, Gauge.PACKAGE);
	}
	
	public void updateData(String[] variableTitles, int[] variableValues) {
		synchronized (this) {
			// remember the data we last received
			this.variableTitles = variableTitles;
			this.variableValues = variableValues;
			// check this panel has been loaded properly
			if (null != this.variableTitles 
					&& null != this.variableValues 
					&& null != chart 
					&& null != data 
					&& null != options) {
				// all the data is present and correct, update this data
				ArrayList<String> rowsChecked = new ArrayList<String>();
				for (int i = 0; i < variableTitles.length && i < variableValues.length; ++i) {
					// for each value, put the data into the data representation
					for (int j = 0; j < data.getNumberOfRows(); ++j) {
						String dataTitle = data.getValueString(j, 0);
						if (dataTitle.equals(variableTitles[i])) {
							// this is the one to change, update the value here
							data.setValue(j, 1, variableValues[i]);
							rowsChecked.add(variableTitles[i]);
							break;
						}
					}
					if (false == rowsChecked.contains(variableTitles[i])) {
						// there is no row for this data, add one
						int rowIndex = data.addRow();
						data.setValue(rowIndex, 0, variableTitles[i]);
						data.setValue(rowIndex, 1, variableValues[i]);
						rowsChecked.add(variableTitles[i]);
					}
				}
				// now all the data is updated, were there any in the rows that should no longer
				// be there
				int noRows = data.getNumberOfRows();
				for (int i = 0; i < noRows; ++i) {
					String rowTitle = data.getValueString(i, 0);
					// is this added?
					if (false == rowsChecked.contains(rowTitle)) {
						// this is not OK as a row, remove this
						data.removeRow(i);
						// and update our counters to check the new one, taking into account this has gone
						--noRows;
						--i;
					}
				}
				// update all the data in the pie chart with the new data
				chart.draw(data, options);
			}
		}
	}

	private Options createOptions() {
		Options optionsCreated = Options.create();
		optionsCreated.setGaugeRange(0, 10);
		optionsCreated.setRedRange(7, 10);
		optionsCreated.setYellowRange(3, 7);
		optionsCreated.setGreenRange(0, 3);
		return optionsCreated;
	}

	private DataTable createTable() {
		DataTable dataCreated = DataTable.create();
		dataCreated.addColumn(ColumnType.STRING, "Variable");
		dataCreated.addColumn(ColumnType.NUMBER, "Value");
		return dataCreated;
	}

}
