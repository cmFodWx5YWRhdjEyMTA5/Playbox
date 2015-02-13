package uk.co.darkerwaters.server;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import uk.co.darkerwaters.client.email.EmailService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class EmailServiceImpl extends RemoteServiceServlet implements EmailService {

	private static final long serialVersionUID = -1891679506633866883L;

	@Override
	public String sendEmail(String from, String subject, String message) {
		String output=null;
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
 
        try { 
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.addRecipient(Message.RecipientType.TO,
                             new InternetAddress("stereobrain@blueyonder.co.uk", "Emotion Tracker Admin"));
            msg.setSubject(subject);
            msg.setText(message);
            msg.setReplyTo(new InternetAddress[]{new InternetAddress(from)});
            Transport.send(msg);
 
        } catch (Exception e) {
            output=e.toString();                
        }    
        return output;
	}

}
