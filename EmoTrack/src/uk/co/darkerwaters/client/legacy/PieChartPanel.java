package uk.co.darkerwaters.client.legacy;

import java.util.ArrayList;
import java.util.logging.Level;

import uk.co.darkerwaters.client.EmoTrack;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.events.SelectHandler;
import com.google.gwt.visualization.client.visualizations.PieChart;
import com.google.gwt.visualization.client.visualizations.PieChart.Options;

public class PieChartPanel {
	
	private PieChart pie = null;
	private DataTable data = null;
	private Options options = null;
	
	public PieChartPanel(final RootPanel parentPanel) {
		// Create a callback to be called when the visualization API
		// has been loaded.
		Runnable onLoadCallback = new Runnable() {
			public void run() {
				// Create a pie chart visualization.
				data = createTable();
				options = createOptions();
				pie = new PieChart(data, options);
				pie.addStyleName("values-chart");
				// add the handler and add the completed chart to the specified parent
				pie.addSelectHandler(createSelectHandler(pie));
				parentPanel.add(pie);
			}
		};
		// Load the visualization api, passing the onLoadCallback to be called
		// when loading is done.
		VisualizationUtils.loadVisualizationApi(onLoadCallback, PieChart.PACKAGE);
	}
	
	public void updateData(String[] variableTitles, int[] variableValues) {
		if (null != pie && null != data && null != options) {
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
			pie.draw(data, options);
		}
	}

	private Options createOptions() {
		Options optionsCreated = Options.create();
		optionsCreated.setWidth(400);
		optionsCreated.setHeight(240);
		optionsCreated.set3D(true);
		optionsCreated.setTitle("My Current Values");
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
		dataCreated.addRows(2);
		dataCreated.setValue(0, 0, "Work");
		dataCreated.setValue(0, 1, 14);
		dataCreated.setValue(1, 0, "Sleep");
		dataCreated.setValue(1, 1, 10);
		return dataCreated;
	}

}
