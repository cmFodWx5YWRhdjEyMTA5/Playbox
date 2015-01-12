package uk.co.darkerwaters.client;

import java.util.logging.Logger;

import uk.co.darkerwaters.client.login.LoginInfo;
import uk.co.darkerwaters.client.login.LoginService;
import uk.co.darkerwaters.client.login.LoginServiceAsync;
import uk.co.darkerwaters.client.login.NotLoggedInException;
import uk.co.darkerwaters.client.variables.VariablesPanel;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
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
	private Anchor loginLink = new Anchor();

	private Timer loginCheckTimer;
	private VariablesPanel variablesPanel;
	
	public static final Logger LOG = Logger.getLogger(EmoTrack.class.getName());
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		// add the main application components to show the user
		AppearingPanel appearingPanel = new AppearingPanel();
		this.variablesPanel = new VariablesPanel(appearingPanel);
		appearingPanel.add(this.variablesPanel);
		appearingPanel.setCellWidth(this.variablesPanel, "100%");
		RootPanel.get().add(appearingPanel);
		
		// setup the login panel
		loginPanel.addStyleName("login-panel");
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

	private void updateLoginDetails() {
		// Assemble login panel.
		if (null != this.loginInfo && this.loginInfo.isLoggedIn()) {
			// set the name to show
			loginLabel.setText(this.loginInfo.getNickname());
			// add the option to log-out
			loginLink.setHref(loginInfo.getLogoutUrl());
			loginLink.setText("Sign Out");
		}
		else {
			// set the name to show
			loginLabel.setText("Please sign in to your Google Account to access the application.");
			// add the option to log-in
			loginLink.setHref(loginInfo.getLoginUrl());
			loginLink.setText("Sign In");
		}
	}

	public void handleLoginError(Throwable error) {
		Window.alert(error.getMessage());
		if (error instanceof NotLoggedInException) {
			Window.Location.replace(loginInfo.getLogoutUrl());
		}
	}
}
