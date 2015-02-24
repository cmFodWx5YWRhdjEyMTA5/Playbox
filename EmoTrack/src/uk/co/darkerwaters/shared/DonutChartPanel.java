package uk.co.darkerwaters.shared;

import java.util.logging.Level;

import uk.co.darkerwaters.client.EmoTrack;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.events.SelectHandler;
import com.google.gwt.visualization.client.visualizations.PieChart.Options;
import com.google.gwt.visualization.client.visualizations.PieChart;

public class DonutChartPanel {
	
	private PieChart pie = null;
	private DataTable data = null;
	private Options options = null;
	
	private String[] titles = null;
	private int[] values = null;
	private final Runnable onLoadCallback;
	
	public interface CreationListener {
		public void chartCreated(DonutChartPanel panel, PieChart chart);
	}
	
	public DonutChartPanel(final CreationListener listener) {
		// Create a callback to be called when the visualization API
		// has been loaded.
		this.onLoadCallback = new Runnable() {
			public void run() {
				// Create a pie chart visualization.
				data = createTable();
				options = createOptions();
				pie = new PieChart(data, options);
				pie.addStyleName("values-chart");
				// add the handler and add the completed chart to the specified parent
				pie.addSelectHandler(createSelectHandler(pie));
				// inform the listener
				listener.chartCreated(DonutChartPanel.this, pie);
				// update our data with the latest sent (before we were created)
				updateData(titles, values);
			}
		};
		
	}
	
	public void updateData(String[] variableTitles, int[] variableValues) {
		if (null != pie && null != variableTitles && null != variableValues) {
			data = createTable();
			options = createOptions();
			for (int i = 0; i < variableTitles.length && i < variableValues.length; ++i) {
				// for each value, put the data into the data representation
				int rowIndex = data.addRow();
				data.setValue(rowIndex, 0, variableTitles[i]);
				data.setValue(rowIndex, 1, variableValues[i] == 0 ? 1 : variableValues[i]);
			}
			// update all the data in the pie chart with the new data
			pie.draw(data, options);
		}
		this.titles = variableTitles;
		this.values = variableValues;
	}

	public void createChart() {
		// Load the visualization api, passing the onLoadCallback to be called
		// when loading is done.
		VisualizationUtils.loadVisualizationApi(onLoadCallback, PieChart.PACKAGE);
	}
	
	public void destroyChart() {
		if (null != this.pie) {
			this.pie.removeFromParent();
		}
	}

	private Options createOptions() {
		Options optionsCreated = Options.create();
		optionsCreated.setWidth(400);
		optionsCreated.setHeight(240);
		optionsCreated.setOption("pieHole", 0.4);
		optionsCreated.setColors("red","blue","green", "transparent");
		//optionsCreated.set3D(true);
		//optionsCreated.setTitle("My Current Values");
		return optionsCreated;
	}

	private SelectHandler createSelectHandler(final PieChart chart) {
		return new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				String message = "";

				// May be multiple selections.
				JsArray<Selection> selections = chart.getSelections();

				for (int i = 0; i < selections.length(); i++) {
					// add a new line for each selection
					message += i == 0 ? "" : "\n";

					Selection selection = selections.get(i);

					if (selection.isCell()) {
						// isCell() returns true if a cell has been selected.

						// getRow() returns the row number of the selected cell.
						int row = selection.getRow();
						// getColumn() returns the column number of the selected
						// cell.
						int column = selection.getColumn();
						message += "cell " + row + ":" + column + " selected";
					} else if (selection.isRow()) {
						// isRow() returns true if an entire row has been
						// selected.

						// getRow() returns the row number of the selected row.
						int row = selection.getRow();
						message += "row " + row + " selected";
					} else {
						// unreachable
						message += "Pie chart selections should be either row selections or cell selections.";
						message += "  Other visualizations support column selections as well.";
					}
				}
				// log this interesting stuff
				EmoTrack.LOG.log(Level.INFO, message);
			}
		};
	}

	private DataTable createTable() {
		DataTable dataCreated = DataTable.create();
		dataCreated.addColumn(ColumnType.STRING, "Variable");
		dataCreated.addColumn(ColumnType.NUMBER, "Value");
		return dataCreated;
	}

}
