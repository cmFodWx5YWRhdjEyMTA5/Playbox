package uk.co.darkerwaters.client;

import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.co.darkerwaters.client.controls.FlatUI;
import uk.co.darkerwaters.client.entry.ValueEntryPanel;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.graph.DataGraphsPanel;
import uk.co.darkerwaters.client.graph.GaugeChartPanel;
import uk.co.darkerwaters.client.html.AboutPageContainer;
import uk.co.darkerwaters.client.html.AnalysisPageContainer;
import uk.co.darkerwaters.client.html.ContactPageContainer;
import uk.co.darkerwaters.client.html.InformationPageContainer;
import uk.co.darkerwaters.client.html.SharingPageContainer;
import uk.co.darkerwaters.client.html.PageContainer;
import uk.co.darkerwaters.client.login.LoginInfo;
import uk.co.darkerwaters.client.login.LoginService;
import uk.co.darkerwaters.client.login.LoginServiceAsync;
import uk.co.darkerwaters.shared.TrackPointData;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
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
public class EmoTrack implements EntryPoint, ValueChangeHandler<String> {
	
	private LoginInfo loginInfo = null;
	private VerticalPanel loginPanel = new VerticalPanel();
	private Label loginLabel = new Label();
	private Label loginDescrption = new Label(EmoTrackConstants.Instance.loginDescription());
	private Anchor loginLink = new Anchor();

	private Timer loginCheckTimer;
	
	public static final Logger LOG = Logger.getLogger(EmoTrack.class.getName());
	
	private GaugeChartPanel variablesChartPanel;
	
	private DataGraphsPanel dataGraphsPanel;
	
	private final EmoTrackConstants constants = EmoTrackConstants.Instance;
	
	private ValueEntryPanel entryPanel;
	private int panelsLoaded = 0;
	
	private static RootPanel errorBox = null;
	private static Label errorBoxLabel = null;
	
	private enum Pages {
		home,
		analysis,
		information,
		sharing,
		about,
		contact
	}
	
	private final HashMap<Pages, PageContainer> pagesCreated = new HashMap<EmoTrack.Pages, PageContainer>();
	
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
		// create the entry panel
		this.entryPanel = new ValueEntryPanel(createValueListener());
		// handle history here
		History.addValueChangeHandler(this);
		
		// add the data graphs
		this.dataGraphsPanel = new DataGraphsPanel(createChartListener());
		RootPanel.get(EmoTrackConstants.K_CSS_ID_APPPLACEHOLDERDISPLAY).add(this.dataGraphsPanel);
		
		// setup the login panel
		loginPanel.addStyleName("login-panel");
		loginPanel.add(loginDescrption);
		loginPanel.add(loginLabel);
		loginPanel.add(loginLink);
		RootPanel.get("loginListItemPlaceholder").add(loginPanel);
		
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
		// have created the home page always
		this.pagesCreated.put(Pages.home, null);
        //when there is no token, the "home" token is set else changePage() is called.
        //this is useful if a user has bookmarked a site other than the homepage.
        if(History.getToken().isEmpty()){
            History.newItem("home");
        } else {
            changePage(History.getToken());
        }
		// perform the initial login check now
		this.loginCheckTimer.schedule(500);
	}
	
	public ValueEntryPanel getEntryPanel() {
		return this.entryPanel;
	}
	
	public void onValueChange(ValueChangeEvent<String> event) {
	    changePage(History.getToken());
	}
	
	public void changePage(String token) {
		if (token == null || token.isEmpty()) {
			token = Pages.home.toString();
		}
		Pages activePage = null;
		try {
			activePage = Pages.valueOf(token);
		}
		catch (IllegalArgumentException e) {
			// fine ignore this # we dont recognise
		}
		if (null != activePage) {
			if (false == activePage.equals(Pages.home)) {
				// is this page created?
				if (null == this.pagesCreated.get(activePage)) {
					// create this page
					createPageContents(activePage);
				}
			}
			// sort out what to show, and what not to show
			for (Pages page : Pages.values()) {
				Element linkParent = null;
				RootPanel headerLink = RootPanel.get(page.toString() + "-link");
				if (null != headerLink) {
					// need to change the style of the parent tho
					linkParent = headerLink.getElement().getParentElement();
				}
				RootPanel pagePanel = RootPanel.get(page.toString());
				if (page == activePage) {
					// this is the active page
					linkParent.addClassName("active");
					pagePanel.setVisible(true);
				}
				else {
					// this is not the active page
					linkParent.removeClassName("active");
					pagePanel.setVisible(false);
				}
			}
			if (null != this.dataGraphsPanel && activePage.equals(Pages.home)) {
				this.dataGraphsPanel.reconstructChartData();
			}
		}
	}

	private void createPageContents(Pages page) {
		RootPanel pagePanel = RootPanel.get(page.toString());
		PageContainer pageContainer = null;
		switch (page) {
		case home : 
			// no
			break;
		case analysis :
			pageContainer = new AnalysisPageContainer();
			break;
		case information :
			pageContainer = new InformationPageContainer();
			break;
		case sharing :
			pageContainer = new SharingPageContainer();
			break;
		case about :
			pageContainer = new AboutPageContainer();
			break;
		case contact :
			pageContainer = new ContactPageContainer();
			break;
		}
		if (null != pageContainer) {
			pagePanel.add(pageContainer.getPage());
			this.pagesCreated.put(page, pageContainer);
			pageContainer.initialisePage(createValueListener());
		}
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
			public void removeTrackEntry(Date pointDate) {
				// add this data to the chart
				updateChartData(pointDate);
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
				/*else if (null != loginInfo && null != loginInfo.getLoginUrl()) {
					Window.open(loginInfo.getLoginUrl(), "EmoTrack Login", "");
					return false;
				}*/
				else {
					FlatUI.createErrorMessage(EmoTrackMessages.Instance.notLoggedIn(), loginDescrption);
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
	private void loadingCompleted() {
		synchronized (this) {
			++this.panelsLoaded;
		}
		if (this.panelsLoaded >= 2) {
			RootPanel.get("loadingDisplay").setVisible(false);
		}
	}

	protected void updateChartData(TrackPointData newPoint) {
		if (null != this.dataGraphsPanel) {
			this.dataGraphsPanel.showTrackData(newPoint);
		}
	}

	protected void updateChartData(Date removalDate) {
		if (null != this.dataGraphsPanel) {
			this.dataGraphsPanel.unshowTrackData(removalDate);
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
		if (null != entryPanel) {
			entryPanel.updateTime();
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
