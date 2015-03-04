package uk.co.darkerwaters.client.html.sharing;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.controls.FlatUI;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.variables.VariablesServiceAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class SharingPageSetupPanel {
	
	private final DisclosurePanel mainPanel = new DisclosurePanel(EmoTrackConstants.Instance.setupSharingData());
	
	private final ValueEntryListener listener;

	private final VariablesServiceAsync variablesService;

	private TextBox newUserIdText;
	
	private FlowPanel gridPanel;
	
	private Label userIdLabel;
	
	public SharingPageSetupPanel(VariablesServiceAsync variablesService, ValueEntryListener listener) {
		this.listener = listener;
		this.variablesService = variablesService;
		
		mainPanel.addStyleName("sub-page-section");
		
		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("sharing-add-panel");
		this.newUserIdText = new TextBox();
		FlatUI.makeEntryText(this.newUserIdText, "newSharedUserText", "Enter the User ID of the person to share your data with");
		contentPanel.add(this.newUserIdText);
		
		Button addButton = new Button(EmoTrackConstants.Instance.addShareUser());
		FlatUI.makeButton(addButton, null, EmoTrackConstants.Instance.tipAddShareUser());
		contentPanel.add(addButton);
		addButton.addStyleName("entryValue");
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addUser(newUserIdText.getText());
			}
		});
		
		Button refreshButton = new Button(EmoTrackConstants.Instance.refreshUsers());
		FlatUI.makeButton(refreshButton, null, EmoTrackConstants.Instance.tipRefreshUsers());
		refreshButton.addStyleName("entryValue");
		contentPanel.add(refreshButton);
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refreshSharedUsers();
			}
		});
		
		FlowPanel userIdPanel = new FlowPanel();
		userIdPanel.addStyleName("sharing-user-id-panel");
		userIdPanel.add(FlatUI.createLabel(EmoTrackConstants.Instance.ownUserIdExplan(), null));
		this.userIdLabel = FlatUI.createLabel("", "sharingUserId", false);
		userIdPanel.add(userIdLabel);
		
		mainPanel.add(contentPanel);
		
		gridPanel = new FlowPanel();
		gridPanel.addStyleName("sharing-user-grid");
		contentPanel.add(gridPanel);
		
		contentPanel.add(userIdPanel);
	}

	public void initialisePanel() {
		refreshSharedUsers();
	}
	
	protected void addUser(String userId) {
		if (false == this.listener.checkLoginStatus()) {
			return;
		}
		if (null == userId || userId.isEmpty()) {
			FlatUI.createErrorMessage("Please enter a valid user ID which which to share data", this.newUserIdText);
		}
		else {
			this.variablesService.addSharedUser(userId, new AsyncCallback<String[]>() {
				@Override
				public void onFailure(Throwable caught) {
					FlatUI.createErrorMessage("Sorry faied to add this user, please try again", newUserIdText);
				}
				@Override
				public void onSuccess(String[] result) {
					populateGrid(result);
				}
			});
		}
	}
	
	protected void removeUser(String userId) {
		if (false == this.listener.checkLoginStatus()) {
			return;
		}
		this.variablesService.removeSharedUser(userId, new AsyncCallback<String[]>() {
			@Override
			public void onFailure(Throwable caught) {
				FlatUI.createErrorMessage("Sorry failed to remove this user, please try again", newUserIdText);
			}
			@Override
			public void onSuccess(String[] result) {
				populateGrid(result);
			}
		});
	}

	private FlowPanel createUserEntry(final String userId) {
		FlowPanel userPanel = new FlowPanel();
		userPanel.addStyleName("share-user-panel");
		TextBox userIdText = new TextBox();
		FlatUI.makeEntryText(userIdText, null, null);
		userIdText.setStylePrimaryName("share-text");
		FlowPanel textPanel = new FlowPanel();
		textPanel.addStyleName("share-text");
		userIdText.setText(userId);
		userIdText.setReadOnly(true);
		textPanel.add(userIdText);
		userPanel.add(textPanel);
		
		final TextBox userNameText = new TextBox();
		textPanel = new FlowPanel();
		textPanel.addStyleName("share-text");
		FlatUI.makeEntryText(userNameText, null, "Fetching name");
		userNameText.setReadOnly(true);
		textPanel.add(userNameText);
		userPanel.add(textPanel);
		this.variablesService.resolveUserIdToName(userId, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				userNameText.setText("Failed to resolve...");
			}
			@Override
			public void onSuccess(String result) {
				userNameText.setText(result == null || result.isEmpty() ? "unknown" : result);
			}
		});
		
		Button deleteButton = new Button("X");
		FlatUI.makeButton(deleteButton, null, EmoTrackConstants.Instance.deleteShareUserButton());
		deleteButton.addStyleName("entryValue");
		userPanel.add(deleteButton);
		deleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				removeUser(userId);
			}
		});
		return userPanel;
	}

	private void refreshSharedUsers() {
		if (false == this.listener.checkLoginStatus()) {
			return;
		}
		this.variablesService.getSharedUsers(new AsyncCallback<String[]>() {
			@Override
			public void onSuccess(String[] result) {
				// create the users with whom to share data with
				populateGrid(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				populateGrid(null);
			}
		});
		this.variablesService.getOwnUserId(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				SharingPageSetupPanel.this.userIdLabel.setText(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				SharingPageSetupPanel.this.userIdLabel.setText("fetching...");
			}
		});
	}

	protected void populateGrid(String[] result) {
		if (false == this.listener.checkLoginStatus()) {
			return;
		}
		this.gridPanel.clear();
		if (null == result) {
			FlatUI.createErrorMessage("Failed to get the users with which you are sharing", newUserIdText);
		}
		else {
			for (String userId : result) {
				FlowPanel panel = createUserEntry(userId);
				this.gridPanel.add(panel);
			}
		}
	}

	public DisclosurePanel getContent() {
		return mainPanel;
	}
	
}
