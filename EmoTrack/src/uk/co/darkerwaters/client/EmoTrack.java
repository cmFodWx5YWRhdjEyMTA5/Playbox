package uk.co.darkerwaters.client;

import java.util.logging.Logger;

import uk.co.darkerwaters.client.entry.ValueEntryPanel;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.login.LoginInfo;
import uk.co.darkerwaters.client.login.LoginService;
import uk.co.darkerwaters.client.login.LoginServiceAsync;
import uk.co.darkerwaters.client.login.NotLoggedInException;
import uk.co.darkerwaters.client.tracks.DataChartPanel;
import uk.co.darkerwaters.client.tracks.TrackPointData;
import uk.co.darkerwaters.client.variables.GaugeChartPanel;
import uk.co.darkerwaters.client.variables.VariablesDataEntryPanel;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.visualization.client.visualizations.Gauge;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class EmoTrack implements EntryPoint {
	
	private LoginInfo loginInfo = null;
	private VerticalPanel loginPanel = new VerticalPanel();
	private Label loginLabel = new Label();
	private Anchor loginLink = new Anchor();

	private Timer loginCheckTimer;
	
	public static final Logger LOG = Logger.getLogger(EmoTrack.class.getName());
	
	private GaugeChartPanel variablesChartPanel;
	
	private DataChartPanel dataChartPanel;
	
	private final EmoTrackConstants constants = EmoTrackConstants.Instance;
	private ValueEntryPanel entryPanel;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		// add the main application components to show the user, set the titles
		Window.setTitle(constants.emotionTracker());
		RootPanel titlePanel = RootPanel.get(EmoTrackConstants.K_CSS_ID_APPTITLE);
		if (titlePanel != null) {
			// add the title
			titlePanel.add(new Label(constants.emotionTracker()));
		}
		
		this.entryPanel = new ValueEntryPanel(createValueListener());
		
		final RootPanel thing = RootPanel.get("moreTextButton");
		RootPanel.get("moreText").setVisible(false);
		thing.sinkEvents(Event.ONCLICK);
		thing.addHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				thing.setVisible(false);
				RootPanel.get("moreText").setVisible(true);
				RootPanel.get("moreText").removeStyleName("hidden-text");
			}
		}, ClickEvent.getType());
		
		/*
		// add the track data entry panel to the app placeholder
	    RootPanel.get(EmoTrackConstants.K_CSS_ID_APPPLACEHOLDERENTRY).add(createTrackDateEntryPanel());
		// create the chart of data to add as values
		this.variablesChartPanel = new GaugeChartPanel(
				new GaugeChartPanel.CreationListener() {
					@Override
					public void chartCreated(GaugeChartPanel panel, Gauge chart) {
						// add this chart to the parent panel
						RootPanel.get(EmoTrackConstants.K_CSS_ID_APPPLACEHOLDERENTRY).add(chart);
						chart.getElement().setId(EmoTrackConstants.K_CSS_ID_VARIABLESCHART);
					}
				});
		*/
		this.dataChartPanel = new DataChartPanel(EmoTrackConstants.K_CSS_ID_DATACHART);
		RootPanel.get(EmoTrackConstants.K_CSS_ID_APPPLACEHOLDERDISPLAY).add(this.dataChartPanel);
		
		// setup the login panel
		loginPanel.addStyleName("login-panel");
		loginPanel.add(new Label(EmoTrackConstants.Instance.loginDescription()));
		loginPanel.add(loginLabel);
		loginPanel.add(loginLink);
		RootPanel.get("header").add(loginPanel);
		
		// Check login status using login service.
		final LoginServiceAsync loginService = GWT.create(LoginService.class);
		this.loginCheckTimer = new Timer() {
			@Override
			public void run() {
				// check our login info
				loginService.login(GWT.getHostPageBaseURL(),
					new AsyncCallback<LoginInfo>() {
						public void onFailure(Throwable error) {
							// we are not logged in, set the info to null
							loginInfo = null;
							// update our details
							updateLoginDetails();
							// and handle the error
							handleLoginError(error);
						}
						public void onSuccess(LoginInfo result) {
							if (false == result.equals(loginInfo)) {
								// a change in login details
								loginInfo = result;
								updateLoginDetails();
							}
							// schedule another check on these details
							loginCheckTimer.schedule(10000);
						}
					});
			}
		};
		// perform the initial login check now
		this.loginCheckTimer.schedule(500);
	}

	private ValueEntryListener createValueListener() {
		return new ValueEntryPanel.ValueEntryListener() {
			@Override
			public void updateVariableValues(String[] titles, int[] values) {
				// update our chart with this data
				updateChartData(titles, values);
			}
			
			@Override
			public void updateTrackEntry(TrackPointData newPoint) {
				// add this data to the chart
				updateChartData(newPoint);
			}
		};
	}

	private VariablesDataEntryPanel createTrackDateEntryPanel() {
		VariablesDataEntryPanel panel = new VariablesDataEntryPanel(new VariablesDataEntryPanel.VariablesDataEntryListener() {
			@Override
			public void updateVariableValues(String[] titles, int[] values) {
				// update our chart with this data
				updateChartData(titles, values);
			}
			
			@Override
			public void updateTrackEntry(TrackPointData newPoint) {
				// add this data to the chart
				updateChartData(newPoint);
			}
		});
		
		return panel;
	}

	protected void updateChartData(TrackPointData newPoint) {
		if (null != this.dataChartPanel) {
			this.dataChartPanel.showTrackData(newPoint);
		}
	}

	protected void updateChartData(String[] titles, int[] values) {
		if (null != this.variablesChartPanel) {
			this.variablesChartPanel.updateData(titles, values);
		}
	}

	private void updateLoginDetails() {
		// Assemble login panel.
		if (null != this.loginInfo && this.loginInfo.isLoggedIn()) {
			// set the name to show
			loginLabel.setText(this.loginInfo.getNickname());
			// add the option to log-out
			loginLink.setHref(loginInfo.getLogoutUrl());
			loginLink.setText(EmoTrackConstants.Instance.loginSignOut());
		}
		else {
			// set the name to show
			loginLabel.setText(EmoTrackConstants.Instance.notLoggedIn());
			// add the option to log-in
			loginLink.setHref(loginInfo.getLoginUrl());
			loginLink.setText(EmoTrackConstants.Instance.loginSignIn());
		}
	}

	public void handleLoginError(Throwable error) {
		//Window.alert(error.getMessage());
		if (error instanceof NotLoggedInException) {
			Window.Location.replace(loginInfo.getLogoutUrl());
		}
	}
}
