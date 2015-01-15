package uk.co.darkerwaters.client.tracks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.EmoTrackMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.events.SelectHandler;
import com.google.gwt.visualization.client.visualizations.LineChart;
import com.google.gwt.visualization.client.visualizations.LineChart.Options;

public class DataChartPanel extends VerticalPanel {
	
	private LineChart chart = null;
	private DataTable data = null;
	private Options options = null;
	
	private final ArrayList<TrackPointData> dataRows = new ArrayList<TrackPointData>();
	private final HashMap<String, Integer> columnsAdded = new HashMap<String, Integer>();
	
	private final TrackPointServiceAsync trackService = GWT.create(TrackPointService.class);
	
	private Date currentSelectedDate = null;
	
	public DataChartPanel(final String chartId) {
		// create all our controls in this panel
		this.getElement().setId(EmoTrackConstants.K_CSS_ID_DATACHARTPANEL);
		// Create a callback to be called when the visualization API
		// has been loaded.
		Runnable onLoadCallback = new Runnable() {
			public void run() {
				// Create a pie chart visualization.
				data = createTable();
				options = createOptions();
				chart = new LineChart(data, options);
				chart.getElement().setId(chartId);
				// add the completed chart to the specified parent
				DataChartPanel.this.add(chart);
				// create the editing controls
				createDataControls();
				// load any track data into the chart
				loadTrackData();
			}
		};
		// Load the visualization api, passing the onLoadCallback to be called
		// when loading is done.
		VisualizationUtils.loadVisualizationApi(onLoadCallback, LineChart.PACKAGE);
	}

	private void loadTrackData() {
		trackService.getTrackPoints(new AsyncCallback<TrackPointData[]>() {
			@Override
			public void onFailure(Throwable error) {
				handleError(error);
			}
			@Override
			public void onSuccess(TrackPointData[] result) {
				// show this data on this chart
				showTrackData(result);
			}
		});
	}

	protected void deleteSelectedData(final TrackPointData point) {
		trackService.removeTrackPoint(point, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable error) {
				handleError(error);
			}

			@Override
			public void onSuccess(Void result) {
				unshowTrackData(point.getTrackDate());
			}
		});
	}
	
	protected void showTrackData(TrackPointData[] trackData) {
		// show this data on this chart
		while (this.data.getNumberOfColumns() > 0) {
			this.data.removeColumn(0);
		}
		// create the standard initial column (date)
		this.data.addColumn(ColumnType.DATE, "Date");
		// now add all the data
		for (int i = 0; i < trackData.length; ++i) {
			addTrackData(trackData[i]);
		}
		// now we have constructed the ordered list of data
		// re-construct the chart's data to show it
		reconstructChartData();
	}
	
	protected void unshowTrackData(Date removalDate) {
		// remove all data for the specified date
		for (int i = this.dataRows.size() - 1; i >= 0; --i) {
        	// check our date against those in the list
        	Date compareDate = this.dataRows.get(i).getTrackDate();
        	if (null != compareDate) {
        		if (removalDate.equals(compareDate)) {
        			// remove this
        			this.dataRows.remove(i);
        		}
        	}
        }
		// now we have constructed the ordered list of data
		// re-construct the chart's data to show it
		reconstructChartData();
	}
	
	private void reconstructChartData() {
		// show all the data from the ordered list, first remove all the current rows
		this.data.removeRows(0, this.data.getNumberOfRows());
		// now re-populate all the rows of data
		for (TrackPointData trackData : this.dataRows) {
			String[] columns = trackData.getValuesNames();
			Integer[] values = trackData.getValuesValues();
			// add a new row for this data
			int rowIndex = this.data.addRow();
			this.data.setValue(rowIndex, 0, trackData.getTrackDate());
			// now add the relevant column data
			for (int i = 0; i < columns.length && i < values.length; ++i) {
				// for each column, ensure we have the column added
				Integer columnIndex = columnsAdded.get(columns[i]);
				if (null == columnIndex) {
					// we have not come across this data name yet, add a column for it
					columnIndex = this.data.addColumn(ColumnType.NUMBER, columns[i]);
					// and remember it's index in our member map
					columnsAdded.put(columns[i], columnIndex);
				}
				// set the column data in this new row
				this.data.setValue(rowIndex, columnIndex, values[i]);
			}
		}
		// now draw this new chart data
		this.chart.draw(this.data, this.options);
	}

	public void showTrackData(TrackPointData trackData) {
		// add the track data into our ordered list
		addTrackData(trackData);
		// now we have constructed the ordered list of data
		// re-construct the chart's data to show it
		reconstructChartData();
	}

	private void addTrackData(TrackPointData trackData) {
		// insert the item in the correct position in the array
		this.dataRows.add(trackData);
		Date insertDate = trackData.getTrackDate();
		if (null != insertDate) {
	        for (int i = this.dataRows.size()-1; i > 0; i--) {
	        	// check our date against those in the list
	        	Date compareDate = this.dataRows.get(i-1).getTrackDate();
	        	if (null != compareDate) {
	        		if (insertDate.compareTo(compareDate) > 0) {
	        			// this is before us, in the correct place, stop now
	        			break;
	        		}
	        		// else move our inserted one down to the new position
	        		Collections.swap(this.dataRows, i, i-1);
	        	}
	        }
		}
	}

	private Options createOptions() {
		Options optionsCreated = Options.create();
		optionsCreated.setTitle(EmoTrackConstants.Instance.dataChartTitle());
		//optionsCreated.setLineSize(3);
		optionsCreated.setMin(0);
		optionsCreated.setMax(10);
		optionsCreated.setEnableTooltip(true);
		optionsCreated.setSmoothLine(true);
		optionsCreated.setBackgroundColor("none");
		return optionsCreated;
	}

	private DataTable createTable() {
		// create an empty table of data, with the initial date column
		DataTable dataCreated = DataTable.create();
		dataCreated.addColumn(ColumnType.DATE, "Date");
		return dataCreated;
	}

	protected void createDataControls() {
		HorizontalPanel dataPanel = new HorizontalPanel();
		dataPanel.getElement().setId(EmoTrackConstants.K_CSS_ID_DATACHARTDATAPANEL);
		final TextArea selectedTextBox = new TextArea();
		dataPanel.add(selectedTextBox);
		final Button deleteButton = new Button(EmoTrackConstants.Instance.deleteSelectionButton());
		deleteButton.getElement().setId(EmoTrackConstants.K_CSS_ID_DELETEDATABUTTON);
		deleteButton.setEnabled(false);
		dataPanel.add(deleteButton);
		dataPanel.setCellVerticalAlignment(deleteButton, HasVerticalAlignment.ALIGN_MIDDLE);
		deleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// delete the current selected date data
				deleteSelectedData(new TrackPointData(currentSelectedDate));
				currentSelectedDate = null;
				deleteButton.setEnabled(false);
				selectedTextBox.setText("");
			}
		});
		// create the selection handler
		chart.addSelectHandler(createChartSelectionHandler(selectedTextBox, deleteButton));
		
		this.add(dataPanel);
		
	}

	protected SelectHandler createChartSelectionHandler(final TextArea selectedTextBox, final Button deleteButton) {
		return new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				// default to no selection
				currentSelectedDate = null;
				selectedTextBox.setText("");
				deleteButton.setEnabled(false);
				// May be multiple selections, just handle selecting a cell...
				JsArray<Selection> selections = chart.getSelections();
				if (selections.length() == 1) {
					// just handle single selection in the delete / edit code...
					Selection selection = selections.get(0);

					if (selection.isCell()) {
						// user has selected a cell, put together a string of the data selected
						int row = selection.getRow();
						//int col = selection.getColumn();
						currentSelectedDate = data.getValueDate(row, 0);
						if (null != currentSelectedDate) {
							selectedTextBox.setText(EmoTrackMessages.Instance.selectedDate(currentSelectedDate));
							deleteButton.setEnabled(true);
						}
					}
				}
			}
		};
	}

	private void handleError(Throwable error) {
		Window.alert(error.getMessage());
	}

}
