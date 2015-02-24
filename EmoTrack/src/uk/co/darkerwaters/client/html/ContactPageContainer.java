package uk.co.darkerwaters.client.html;

import java.util.logging.Level;

import uk.co.darkerwaters.client.EmoTrack;
import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.EmoTrackResources;
import uk.co.darkerwaters.client.FlatUI;
import uk.co.darkerwaters.client.email.EmailService;
import uk.co.darkerwaters.client.email.EmailServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

public class ContactPageContainer extends PageContainer {

	private TextBox nameEntry;
	private TextBox telEntry;
	private TextBox emailEntry;
	private TextArea contentEntry;
	private Button submitButton;
	
	final EmailServiceAsync emailService = GWT.create(EmailService.class);

	public ContactPageContainer() {
		super(EmoTrackResources.INSTANCE.contactPage().getText());
		
		nameEntry = new TextBox();
		FlatUI.makeEntryText(nameEntry, "inputName", "Enter name...");
		getPage().add(nameEntry, "contactInputName");
		
		telEntry = new TextBox();
		FlatUI.makeEntryText(telEntry, "inputTel", "Enter telephone number...");
		getPage().add(telEntry, "contactInputTel");
		
		emailEntry = new TextBox();
		FlatUI.makeEntryText(emailEntry, "inputEmail", "Enter email address...", "email");
		getPage().add(emailEntry, "contactInputEmail");
		
		contentEntry = new TextArea();
		FlatUI.makeEntryTextArea(contentEntry, "inputContent", "Enter comments...");
		getPage().add(contentEntry, "contactInputContent");
		
		submitButton = new Button("Submit");
		FlatUI.makeButton(submitButton, "sendButton", EmoTrackConstants.Instance.tipSendEmail());
		getPage().add(submitButton, "contactInputButton");
	}
	
	public void initialisePage() {
		nameEntry.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				validatePage();
			}
		});
		emailEntry.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				validatePage();
			}
		});
		submitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
	            String subject = "Comments on Emotion Tracker";
	            String content = "From: " + nameEntry.getText() + "  " +
				            		"Tel: " + telEntry.getText() + "  " + 
				            		"Email: " + emailEntry.getText() + "  " + 
				            		"Comments: " + contentEntry.getText();
	            emailService.sendEmail(emailEntry.getText(), subject, content, new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						EmoTrack.alertWidget("Email sent", "Thankyou for your comments, we will try to get back to you as soon as possable");
					}
					@Override
					public void onFailure(Throwable caught) {
						EmoTrack.alertWidget("Email failed", "Sorry, for some reason this email could not be sent, try again later?...");
						EmoTrack.LOG.log(Level.SEVERE, "Email failure " + caught.getMessage());
					}
				});
			}
		});
		validatePage();
	}

	protected void validatePage() {
		boolean containsError = false;
		String name = this.nameEntry.getText();
		if (name == null || name.isEmpty()) {
			this.nameEntry.getElement().getParentElement().addClassName("has-error");
			containsError = true;
		}
		else {
			this.nameEntry.getElement().getParentElement().removeClassName("has-error");
		}
		String email = this.emailEntry.getText();
		
		boolean isValid = email != null &&
				email.matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
		if (false == isValid) {
			this.emailEntry.getElement().getParentElement().addClassName("has-error");
			containsError = true;
		}
		else {
			this.emailEntry.getElement().getParentElement().removeClassName("has-error");
		}
		submitButton.setEnabled(false == containsError);
	}
}
