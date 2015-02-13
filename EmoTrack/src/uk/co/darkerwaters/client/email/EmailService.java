package uk.co.darkerwaters.client.email;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("email")
public interface EmailService extends RemoteService {
	public String sendEmail(String from, String subject, String message);
}
