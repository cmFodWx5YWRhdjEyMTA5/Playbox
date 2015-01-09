package uk.co.darkerwaters.client.app;

import uk.co.darkerwaters.client.FuseX;
import uk.co.darkerwaters.client.NotLoggedInException;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

public abstract class ClientApp {
	
	protected final FuseX entryPoint;

	public ClientApp(FuseX entryPoint) {
		this.entryPoint = entryPoint;
	}

	public abstract void initialiseApp(EntryPoint entryPoint, RootPanel rootPanel);

	protected void handleError(Throwable error) {
		if (error instanceof NotLoggedInException) {
			this.entryPoint.handleLoginError(error);
		}
		else {
			Window.alert(error.getMessage());
		}
	}

}
