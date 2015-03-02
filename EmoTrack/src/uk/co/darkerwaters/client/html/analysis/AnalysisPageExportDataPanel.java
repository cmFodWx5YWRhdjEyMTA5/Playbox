package uk.co.darkerwaters.client.html.analysis;

import java.util.ArrayList;
import java.util.Date;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.EmoTrackMessages;
import uk.co.darkerwaters.client.controls.FlatUI;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.graph.DataGraphsPanel;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;
import uk.co.darkerwaters.shared.TrackPointData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.gwt.user.datepicker.client.DatePicker;

@SuppressWarnings("deprecation")
public class AnalysisPageExportDataPanel {
	
	private final DisclosurePanel mainPanel = new DisclosurePanel(EmoTrackConstants.Instance.exportData());
	
	private DatePicker fromDatePicker;
	
	private DatePicker toDatePicker;

	private Button getDataButton;

	private FlowPanel gridPanel;

	private Button selectDataButton;

	private Label helpLabel;

	private ValueEntryListener listener;

	private final TrackPointServiceAsync trackService;

	public AnalysisPageExportDataPanel(TrackPointServiceAsync trackService, ValueEntryListener listener) {
		this.listener = listener;
		this.trackService = trackService;
		
		FlowPanel dateSelectPanel = new FlowPanel();
		
		mainPanel.addStyleName("sub-page-section");
		
		// set the from controls
		InlineLabel label = FlatUI.createLabel("Export data from", null);
		label.addStyleName("entryValue");
		dateSelectPanel.add(label);
		fromDatePicker = new DatePicker();
		Date from = new Date();
		// this is the start of today though, take a month off
		CalendarUtil.addMonthsToDate(from, -1);
		fromDatePicker.setValue(from);
		fromDatePicker.setCurrentMonth(from);
		fromDatePicker.addStyleName("entryValue analysis-date-picker");
		dateSelectPanel.add(fromDatePicker);
		
		// set the to controls
		label = FlatUI.createLabel("to", null);
		label.addStyleName("entryValue");
		dateSelectPanel.add(label);
		toDatePicker = new DatePicker();
		Date to = new Date();
		// this is the start of today though, add a day
		CalendarUtil.addDaysToDate(to, 1);
		toDatePicker.setValue(to);
		toDatePicker.setCurrentMonth(to);
		toDatePicker.addStyleName("entryValue analysis-date-picker");
		dateSelectPanel.add(toDatePicker);
		
		FlowPanel buttonPanel = new FlowPanel();
		
		this.getDataButton = new Button("Get Data");
		FlatUI.makeButton(getDataButton, "analysisGetDataButton", "Get the data within the selected data range from the server");
		this.getDataButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// get the data
				getData();
			}
		});
		buttonPanel.add(this.getDataButton);
		
		this.selectDataButton = new Button("Select Data");
		FlatUI.makeButton(this.selectDataButton, "analysisExportDataButton", "Export the retrieved data to a file");
		this.selectDataButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// select the data
				selectData();
			}
		});
		buttonPanel.add(this.selectDataButton);
		this.helpLabel = FlatUI.createLabel(EmoTrackConstants.Instance.nowCopyAndPaste(), "anaysisCopyHelpLabel", false);
		this.helpLabel.setVisible(false);
		this.selectDataButton.setEnabled(false);
		buttonPanel.add(this.helpLabel);
		
		dateSelectPanel.add(buttonPanel);
		
		FlowPanel contentPanel = new FlowPanel();
		contentPanel.add(dateSelectPanel);
		this.gridPanel = new FlowPanel();
		this.gridPanel.addStyleName("analysis-export-grid");
		contentPanel.add(this.gridPanel);
		mainPanel.add(contentPanel);
	}

	public DisclosurePanel getContent() {
		return mainPanel;
	}

	protected void selectData() {
		markText(this.gridPanel.getElement());
		this.helpLabel.setVisible(true);
	}

	protected void getData() {
		Date from = this.fromDatePicker.getValue();
		Date to = this.toDatePicker.getValue();
		if (false == listener.checkLoginStatus()) {
			return;
		}
		markText(this.helpLabel.getElement());
		this.helpLabel.setVisible(false);
		this.selectDataButton.setEnabled(false);
		
		if (from == null || to == null || to.before(from) || to.equals(from)) {
			// this is no good
			FlatUI.createErrorMessage("Please enter a valid date range, 'to' has to be before 'from'.", this.getDataButton);
		}
		else {
			String fromDayDate = DataGraphsPanel.dayDate.format(from);
			String toDayDate = DataGraphsPanel.dayDate.format(to);
			this.gridPanel.clear();
			trackService.getTrackPoints(fromDayDate, toDayDate, new AsyncCallback<TrackPointData[]>() {
				@Override
				public void onSuccess(TrackPointData[] result) {
					populateGrid(result);
				}
				@Override
				public void onFailure(Throwable caught) {
					populateGrid(null);
				}
			});
		}
	}

	protected void populateGrid(TrackPointData[] result) {
		if (result == null) {
			FlatUI.createErrorMessage("Unable to get any data for this date range, sorry.", this.getDataButton);
			return;
		}
		// populate the grid of all our data, first get all the headings
		ArrayList<String> dataHeadings = new ArrayList<String>();
		for (TrackPointData data : result) {
			for (String columnTitle : data.getValuesNames()) {
				if (false == dataHeadings.contains(columnTitle)) {
					dataHeadings.add(columnTitle);
				}
			}
		}
		// create the grid
		Grid dataGrid = new Grid(result.length + 1, dataHeadings.size() + 3);
		this.gridPanel.add(dataGrid);
		dataGrid.setWidget(0, 1, createColHeader(EmoTrackConstants.Instance.date()));
		dataGrid.setWidget(0, 2, createColHeader(EmoTrackConstants.Instance.events()));
		for (int col = 0; col < dataHeadings.size(); ++col) {
			dataGrid.setWidget(0, col + 3, createColHeader(dataHeadings.get(col)));
		}
		for (int row = 1; row <= result.length; ++row) {
			TrackPointData data = result[row-1];
			String dateLabel = EmoTrackMessages.Instance.date(data.getTrackDate()) + " " + 
					EmoTrackMessages.Instance.time(data.getTrackDate());
			dataGrid.setWidget(row, 0, createRowDeleteButton(data, dataGrid, row));
			dataGrid.setWidget(row, 1, createCellLabel(dateLabel));
			dataGrid.setWidget(row, 2, createCellLabel(data.getEvent()));
			Integer[] valuesValues = data.getValuesValues();
			String[] valuesNames = data.getValuesNames();
			for (int i = 0; i < dataHeadings.size(); ++i) {
				String heading = dataHeadings.get(i);
				int dataIndex = getHeaderIndex(heading, valuesNames);
				String content = null;
				if (dataIndex != -1) {
					content = valuesValues[dataIndex].toString();
				}
				dataGrid.setWidget(row, i + 3, createCellLabel(content));
			}
		}
		this.selectDataButton.setEnabled(true);
	}

	private Widget createRowDeleteButton(final TrackPointData point, final Grid dataGrid, final int row) {
		final Button deleteButton = new Button("X");
		FlatUI.makeButton(deleteButton, null, EmoTrackConstants.Instance.tipDeleteRowButton());
		deleteButton.addStyleName("analysis-row-delete");
		deleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (deleteButton.getText().equals("X")) {
					// are to delete
					deleteData(deleteButton, point, dataGrid, row);
				}
				else {
					restoreData(deleteButton, point, dataGrid, row);;
				}
			}
		});
		deleteButton.addStyleName("entryValue");
		return deleteButton;
	}

	protected void deleteData(final Button deleteButton, final TrackPointData point, final Grid dataGrid, final int row) {
		this.trackService.removeTrackPoint(point, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				deleteButton.setText(EmoTrackConstants.Instance.restoreRow());
				FlatUI.makeTooltip(deleteButton, EmoTrackConstants.Instance.tipRestoreRowButton());
				for (int col = 1; col < dataGrid.getColumnCount(); ++col) {
					dataGrid.getWidget(row, col).addStyleName("analysis-deleted");
				}
				AnalysisPageExportDataPanel.this.listener.removeTrackEntry(point.getTrackDate());
			}
			@Override
			public void onFailure(Throwable caught) {
				FlatUI.createErrorMessage("Failed to delete the data for this date, sorry.", deleteButton);
			}
		});
	}

	protected void restoreData(final Button deleteButton, final TrackPointData point, final Grid dataGrid, final int row) {
		this.trackService.addTrackPoint(point, new AsyncCallback<TrackPointData>() {
			@Override
			public void onSuccess(TrackPointData result) {
				deleteButton.setText("X");
				FlatUI.makeTooltip(deleteButton, EmoTrackConstants.Instance.tipDeleteRowButton());
				for (int col = 1; col < dataGrid.getColumnCount(); ++col) {
					dataGrid.getWidget(row, col).removeStyleName("analysis-deleted");
				}
				AnalysisPageExportDataPanel.this.listener.updateTrackEntry(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				FlatUI.createErrorMessage("Failed to restore the data for this date, sorry.", deleteButton);
			}
		});
	}

	private int getHeaderIndex(String heading, String[] dataHeadings) {
		int index = -1;
		for (int i = 0; i < dataHeadings.length; ++i) {
			if (heading.equals(dataHeadings[i])) {
				index = i;
				break;
			}
		}
		return index;
	}

	private Widget createColHeader(String content) {
		FlowPanel container = new FlowPanel();
		container.addStyleName("analysis-data-cell");
		InlineLabel label = FlatUI.createLabel(content, null);
		label.addStyleName("analysis-data-col-header");
		container.add(label);
		return container;
	}

	private Widget createCellLabel(String content) {
		FlowPanel container = new FlowPanel();
		container.addStyleName("analysis-data-cell");
		if (content == null || content.isEmpty()) {
			container.getElement().setInnerHTML("none");
			container.addStyleName("invisible-text");
		}
		else {
			container.getElement().setInnerHTML(content);
		}
		return container;
	}
	
	private native void markText(Element elem) /*-{
	    if ($doc.selection && $doc.selection.createRange) {
	        var range = $doc.selection.createRange();
	        range.moveToElementText(elem);
	        range.select();
	    } else if ($doc.createRange && $wnd.getSelection) {
	        var range = $doc.createRange();
	        range.selectNode(elem);
	        var selection = $wnd.getSelection();
	        selection.removeAllRanges();
	        selection.addRange(range);
	    }
	}-*/;
	
}
