package uk.co.darkerwaters.client;

import java.io.Console;
import java.util.ArrayList;
import java.util.Date;

import uk.co.darkerwaters.client.app.ClientApp;
import uk.co.darkerwaters.client.app.FuseXClientApp;
import uk.co.darkerwaters.client.app.StockWatcherClientApp;
import uk.co.darkerwaters.shared.FieldVerifier;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class FuseX implements EntryPoint {
	
	static class MainButtonClickHandler implements EventListener {
		  static interface ClickInterface {
			  void onMainButtonClick(Element divElement, Event event);
		  }
		  private final ClickInterface handler;
		  public MainButtonClickHandler(ClickInterface handler) {
			  this.handler = handler;
		  }
		  @Override
		  public void onBrowserEvent(Event event) {
			  Element divElement = event.getEventTarget().cast();
			  switch(event.getTypeInt()) {
			  case Event.ONMOUSEOVER :
				  divElement.addClassName("mouse-over");
				  break;
			  case Event.ONMOUSEOUT :
				  divElement.removeClassName("mouse-over");
				  break;
			  case Event.ONCLICK :
				  this.handler.onMainButtonClick(divElement, event);
				  break;
			  }
		  }
	}
	
	private ClientApp activeApp = null;

	private LoginInfo loginInfo = null;
	private VerticalPanel loginPanel = new VerticalPanel();
	private Label loginLabel = new Label("Please sign in to your Google Account to access the application.");
	private Anchor signInLink = new Anchor("Sign In");
	private Anchor signOutLink = new Anchor("Sign Out");

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		// Check login status using login service.
		LoginServiceAsync loginService = GWT.create(LoginService.class);
		loginService.login(GWT.getHostPageBaseURL(),
				new AsyncCallback<LoginInfo>() {
					public void onFailure(Throwable error) {
						handleLoginError(error);
					}
					public void onSuccess(LoginInfo result) {
						loginInfo = result;
						loadLogin(loginInfo.isLoggedIn());
					}
				});
		// create the loading buttons for all the sub-components
		handleMainButton("helloworld", new HelloWorldClientApp(this));
		handleMainButton("stockwatcher", new StockWatcherClientApp(this));
		handleMainButton("fusex", new FuseXClientApp(this));
	}

	private void handleMainButton(String divId, final ClientApp clientApp) {
		Element buttonElement = RootPanel.get(divId).getElement();
		Event.sinkEvents(buttonElement, Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT);
		MainButtonClickHandler handler = new MainButtonClickHandler(new MainButtonClickHandler.ClickInterface() {
			@Override
			public void onMainButtonClick(Element divElement, Event event) {
				// set the active application
				FuseX.this.activeApp = clientApp;
				// initialise it on the screen
				clientApp.initialiseApp(FuseX.this, RootPanel.get("app-placeholder"));
			}
		});
	    DOM.setEventListener(buttonElement, handler);
	}

	private void loadLogin(boolean isLoggedIn) {
		// Assemble login panel.
		signInLink.setHref(loginInfo.getLoginUrl());
		signOutLink.setHref(loginInfo.getLogoutUrl());
		if (isLoggedIn) {
			loginPanel.add(loginLabel);
			loginPanel.add(signOutLink);
		}
		else {
			loginPanel.add(loginLabel);
			loginPanel.add(signInLink);
		}
		RootPanel.get("app-placeholder").add(loginPanel);
	}

	public void handleLoginError(Throwable error) {
		Window.alert(error.getMessage());
		if (error instanceof NotLoggedInException) {
			Window.Location.replace(loginInfo.getLogoutUrl());
		}
	}
}
