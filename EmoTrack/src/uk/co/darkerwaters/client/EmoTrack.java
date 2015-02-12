package uk.co.darkerwaters.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import uk.co.darkerwaters.client.entry.EmoTrackListener;
import uk.co.darkerwaters.client.entry.ValueEntryPanel;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.login.LoginInfo;
import uk.co.darkerwaters.client.login.LoginService;
import uk.co.darkerwaters.client.login.LoginServiceAsync;
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

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class EmoTrack implements EntryPoint {
	
	private LoginInfo loginInfo = null;
	private VerticalPanel loginPanel = new VerticalPanel();
	private Label loginLabel = new Label();
	private Label loginDescrption = new Label(EmoTrackConstants.Instance.loginDescription());
	private Anchor loginLink = new Anchor();

	private Timer loginCheckTimer;
	
	public static final Logger LOG = Logger.getLogger(EmoTrack.class.getName());
	
	private GaugeChartPanel variablesChartPanel;
	
	private DataChartPanel dataChartPanel;
	
	private final EmoTrackConstants constants = EmoTrackConstants.Instance;
	
	private ValueEntryPanel entryPanel;
	private int panelsLoaded = 0;
	
	private static RootPanel errorBox = null;
	private static Label errorBoxLabel = null;
	
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
		
		errorBox = RootPanel.get("errorDisplay");
		errorBox.setVisible(false);
		
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
		this.dataChartPanel = new DataChartPanel(EmoTrackConstants.K_CSS_ID_DATACHART, createChartListener());
		RootPanel.get(EmoTrackConstants.K_CSS_ID_APPPLACEHOLDERDISPLAY).add(this.dataChartPanel);
		
		// setup the login panel
		loginPanel.addStyleName("login-panel");
		loginPanel.add(loginDescrption);
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
							handleError(error);
						}
						public void onSuccess(LoginInfo result) {
							boolean isChanged = false;
							if (loginInfo == null && result != null) {
								// a change
								isChanged = true;
							}
							else if (loginInfo != null && result == null) {
								// a change
								isChanged = true;
							}
							else if (null != loginInfo && null != result) {
								// both are not null, is the user different?
								if (false == result.equals(loginInfo)) {
									// a change in login details
									isChanged = true;
								}
								else if (result.isLoggedIn() != loginInfo.isLoggedIn()) {
									isChanged = true;
								}
							}
							if (isChanged) {
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
			@Override
			public void handleError(Throwable error) {
				EmoTrack.this.handleError(error);
			}
			@Override
			public void loadingComplete() {
				loadingCompleted();
			}
			@Override
			public boolean checkLoginStatus() {
				if (null != loginInfo && loginInfo.isLoggedIn()) {
					// are logged in
					return true;
				}
				else if (null != loginInfo && null != loginInfo.getLoginUrl()) {
					Window.open(loginInfo.getLoginUrl(), "EmoTrack Login", "");
					return false;
				}
				else {
					EmoTrack.alertWidget(EmoTrackConstants.Instance.alertTitle(), EmoTrackMessages.Instance.notLoggedIn());
					return false;
				}
			}
		};
	}

	private EmoTrackListener createChartListener() {
		return new EmoTrackListener() {
			@Override
			public void handleError(Throwable error) {
				EmoTrack.this.handleError(error);
			}
			@Override
			public void loadingComplete() {
				loadingCompleted();
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
	private void loadingCompleted() {
		synchronized (this) {
			++this.panelsLoaded;
		}
		if (this.panelsLoaded >= 2) {
			RootPanel.get("loadingDisplay").setVisible(false);
		}
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
			loginDescrption.setVisible(true);
			loginLabel.setText(this.loginInfo.getNickname());
			// add the option to log-out
			loginLink.setHref(loginInfo.getLogoutUrl());
			loginLink.setText(EmoTrackConstants.Instance.loginSignOut());
		}
		else {
			// set the name to show
			loginDescrption.setVisible(false);
			loginLabel.setText(EmoTrackConstants.Instance.notLoggedIn());
			// add the option to log-in
			loginLink.setHref(loginInfo.getLoginUrl() == null ? "" : loginInfo.getLoginUrl());
			loginLink.setText(EmoTrackConstants.Instance.loginSignIn());
		}
	}

	public void handleError(Throwable error) {
		LOG.log(Level.SEVERE, error.getMessage());
		/*if (error instanceof NotLoggedInException) {
			if (null != loginInfo && null != loginInfo.getLoginUrl()) {
				// send them to the login URL
				try {
					Window.Location.replace(loginInfo.getLoginUrl());
				}
				catch (Exception e) {
					LOG.log(Level.SEVERE, e.getMessage());
				}
			}
		}*/
	}
	
	public static void alertWidget(final String header, final String content) {
		
		if (null == errorBoxLabel) {
			errorBoxLabel = new Label();
			errorBox.add(errorBoxLabel);
		}
		errorBoxLabel.setText(content);
        errorBox.setVisible(true);
        Timer showTimer = new Timer() {
			
			@Override
			public void run() {
				errorBox.setVisible(false);
			}
		};
		showTimer.schedule(5000);
    } 
}
