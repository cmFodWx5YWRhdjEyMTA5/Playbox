package uk.co.darkerwaters.client.entry;

import java.util.ArrayList;
import java.util.Date;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.EmoTrackListener;
import uk.co.darkerwaters.client.controls.ListItemWidget;
import uk.co.darkerwaters.client.controls.UnorderedListWidget;
import uk.co.darkerwaters.client.tracks.TrackPointService;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;
import uk.co.darkerwaters.shared.TrackPointData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class ValueEntryPanel extends FlowPanel {
	
	private final TrackPointServiceAsync trackPointService = GWT.create(TrackPointService.class);
	
	public interface ValueEntryListener extends EmoTrackListener {
		void updateVariableValues(String[] titles, int[] values);
		void updateTrackEntry(TrackPointData newPoint);
		void removeTrackEntry(Date trackDate);
		boolean checkLoginStatus();
	}
	
	private final ValueEntryListener listener;

	private DateSelectTab dateSelectPanel;
	
	private final ArrayList<ValueEntryTab> availableTabs = new ArrayList<ValueEntryTab>();
	
	private ValueEntryTab currentTab = null;
	
	public ValueEntryPanel(ValueEntryListener listener) {
		this.listener = listener;
		this.getElement().setId(EmoTrackConstants.K_CSS_ID_VALUEENTRY);
	    // create the date select panel we will always show 
		this.dateSelectPanel = new DateSelectTab(this.listener, this.trackPointService);
	    RootPanel.get("timeValueEntry").add(this.dateSelectPanel.getContent());
	    this.dateSelectPanel.setActiveItem(true);
	    
	    // now create the list for the tabs for entry
	    UnorderedListWidget tabList = new UnorderedListWidget();
	    tabList.add(createNavigationHeading(-1));
	    tabList.add(createTabHeading(EmoTrackConstants.Instance.emotions(), new EmotionsTab(listener, trackPointService, dateSelectPanel)));
	    tabList.add(createTabHeading(EmoTrackConstants.Instance.activity(), new ActivityTab(listener, trackPointService, dateSelectPanel)));
	    tabList.add(createTabHeading(EmoTrackConstants.Instance.sleep(), new SleepTab(listener, trackPointService, dateSelectPanel)));
	    tabList.add(createTabHeading(EmoTrackConstants.Instance.events(), new EventsTab(listener, trackPointService, dateSelectPanel)));
	    tabList.add(createNavigationHeading(1));
	    
	    RootPanel.get("tabList").add(tabList);
	    showTab(this.availableTabs.get(0));
	}

	public void updateTime() {
		if (null != dateSelectPanel) {
			dateSelectPanel.updateTime();
		}
	}
	
	private Widget createNavigationHeading(final int direction) {
		// create the nav header
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant("<a href=\"#navChange\" class=\"fui-arrow-" + (direction < 0 ? "left" : "right") + "\"/>");
		final ListItemWidget item = new ListItemWidget(builder.toSafeHtml());
		if (direction < 0) {
			item.addStyleName("previous");
		}
		else {
			item.addStyleName("next");
		}
	    item.sinkEvents(Event.ONCLICK);
	    item.addHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// stop the processing of the java script event to stop the next / prev buttons being active
				event.stopPropagation();
				// show the next / prev tab
				ValueEntryPanel.this.showTab(getNextTab(direction));
			}
		}, ClickEvent.getType());
	    return item;
	}

	protected ValueEntryTab getNextTab(int direction) {
		int currentIndex = 0;
		for (int i = 0; i < this.availableTabs.size(); ++i) {
			if (this.availableTabs.get(i) == this.currentTab) {
				currentIndex = i;
				break;
			}
		}
		// set the new index
		currentIndex += direction;
		// wrap the counter
		if (currentIndex < 0) {
			currentIndex = this.availableTabs.size() - 1;
		}
		else if (currentIndex >= this.availableTabs.size()) {
			currentIndex = 0;
		}
		return this.availableTabs.get(currentIndex);
	}

	private ListItemWidget createTabHeading(String tabTitle, final ValueEntryTab tab) {
		// remember this tab
		this.availableTabs.add(tab);
		// and create the header for the tab
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
	    builder.appendHtmlConstant("<a href=\"#trackValue\">" + tabTitle + "</a>");
	    
	    ListItemWidget item = new ListItemWidget(builder.toSafeHtml());
	    item.sinkEvents(Event.ONCLICK);
	    item.addHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ValueEntryPanel.this.showTab(tab);
			}
		}, ClickEvent.getType());
	    tab.setHeader(item);
	    return item;
	}

	protected void showTab(ValueEntryTab tab) {
		if (null != this.currentTab) {
			this.currentTab.setActiveItem(false);
			RootPanel.get(EmoTrackConstants.K_CSS_ID_APPPLACEHOLDERVALUEENTRY).remove(this.currentTab.getContent());
		}
		this.currentTab = tab;
		RootPanel.get(EmoTrackConstants.K_CSS_ID_APPPLACEHOLDERVALUEENTRY).add(tab.getContent());
		this.currentTab.setActiveItem(true);
	}
}
