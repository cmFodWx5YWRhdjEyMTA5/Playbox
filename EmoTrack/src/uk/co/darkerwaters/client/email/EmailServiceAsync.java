package uk.co.darkerwaters.client.email;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface EmailServiceAsync {

	void sendEmail(String from, String subject, String message, AsyncCallback<String> callback);

}
