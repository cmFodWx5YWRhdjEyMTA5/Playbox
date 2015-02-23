package uk.co.darkerwaters.client.entry;

import java.util.Date;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.EmoTrackMessages;
import uk.co.darkerwaters.client.FlatUI;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;
import uk.co.darkerwaters.client.variables.LogDates;
import uk.co.darkerwaters.shared.MirrorLabel;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DatePicker;

public class DateSelectTab extends ValueEntryTab {
	
	private VerticalPanel mainPanel = new VerticalPanel();
	private ListBox logDateList;
	private DatePicker logDatePicker;
	private Label dateSelectLabel;
	private Label timeSelectLabel;

	public DateSelectTab(ValueEntryListener listener, TrackPointServiceAsync service) {
		super(listener, service);
		this.dateSelectLabel = new Label(EmoTrackMessages.Instance.date(LogDates.now.getDate()));
	    dateSelectLabel.getElement().setId(EmoTrackConstants.K_CSS_ID_DATESELECTED);
	    this.timeSelectLabel = new Label(EmoTrackMessages.Instance.time(LogDates.now.getDate()));
	    timeSelectLabel.getElement().setId(EmoTrackConstants.K_CSS_ID_TIMESELECTED);
		
		// add the entry controls
		mainPanel.getElement().setId("entryLeftPanel");
	    Image timeSelectImage = new Image(EmoTrackConstants.K_IMG_TIMESELECT);
	    timeSelectImage.getElement().setId(EmoTrackConstants.K_CSS_ID_TIMESELECTIMAGE);
	    this.logDatePicker = createLogDatePicker(dateSelectLabel, timeSelectLabel);
	    logDatePicker.setWidth("200px");
	    
	    this.logDateList = createLogDateList(logDatePicker, dateSelectLabel, timeSelectLabel);
	    FlowPanel timePanel = new FlowPanel();
	    timePanel.add(timeSelectImage);
	    FlowPanel timeSelectPanel = new FlowPanel();
	    timeSelectPanel.getElement().setId("timeDateSelectPanel");
	    HeadingElement headingElement = Document.get().createHElement(4);
	    headingElement.setInnerText(EmoTrackConstants.Instance.trackValues());
	    timeSelectPanel.getElement().appendChild(headingElement);
	    timeSelectPanel.add(logDateList);
	    timeSelectPanel.add(logDatePicker);
	    FlowPanel labelPanel = new FlowPanel();
	    labelPanel.add(dateSelectLabel);
	    labelPanel.add(timeSelectLabel);
	    timeSelectPanel.add(labelPanel);
	    timePanel.add(timeSelectPanel);
	    mainPanel.add(timePanel);
	}
	
	public Panel getContent() {
		return this.mainPanel;
	}

	public Date getSelectedDate() {
		int selectedIndex = logDateList.getSelectedIndex();
		Date selectedDate = new Date();
		if (selectedIndex == LogDates.other.ordinal()) {
			// user has selected "other" so get the date from the date picker
			selectedDate = logDatePicker.getValue();
		}
		else if (selectedIndex != -1) {
			// get the date this data should be logged for
			selectedDate = LogDates.values()[selectedIndex].getDate();
		}
		return selectedDate;
	}

	private ListBox createLogDateList(final DatePicker logDatePicker, final Label dateSelectLabel, final Label timeSelectLabel) {
		// add all the options to the drop-down
		final ListBox logDateList = new ListBox();
		FlatUI.makeCombo(logDateList, null);
		for (LogDates date : LogDates.values()) {
			logDateList.addItem(date.title);
		}
		logDateList.setSelectedIndex(0);
		logDateList.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				// added to only show the date picker when "other" is selected
				LogDates selected = LogDates.values()[logDateList.getSelectedIndex()];
				if (selected == LogDates.other) {
					// user has selected "other"
					logDatePicker.setVisible(true);
					dateSelectLabel.setVisible(false);
					timeSelectLabel.setVisible(false);
				}
				else {
					// user has selected another specific time
					logDatePicker.setVisible(false);
					timeSelectLabel.setVisible(true);
					dateSelectLabel.setVisible(true);
					setDateLabels(dateSelectLabel, timeSelectLabel, selected.getDate());
				}
			}
		});
		return logDateList;
	}
	
	private void setDateLabels(final Label dateSelectLabel, final Label timeSelectLabel, Date selected) {
		dateSelectLabel.setText(EmoTrackMessages.Instance.date(selected));
		timeSelectLabel.setText(EmoTrackMessages.Instance.time(selected));
		MirrorLabel.update();
	}
	
	private DatePicker createLogDatePicker(final Label dateSelectLabel, final Label timeSelectLabel) {
		// Set the default value on the date picker
	    DatePicker logDatePicker = new DatePicker();
	    logDatePicker.setValue(new Date(), true);
	    logDatePicker.setVisible(false);
	    logDatePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				setDateLabels(dateSelectLabel, timeSelectLabel, event.getValue());
			}
		});
		return logDatePicker;
	}

	public Label getTimeSelectLabel() {
		return this.timeSelectLabel;
	}

	public Label getDateSelectLabel() {
		return this.dateSelectLabel;
	}
}
