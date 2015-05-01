package com.alonyx.client;

import org.fusesource.restygwt.client.Defaults;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TrainData implements EntryPoint {
	
	private FlexTable trainsTable;

    @Override
    public void onModuleLoad() {
    	
    	Button trainsButton = new Button("load trains");
        trainsButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                loadTrains();
            }
        });

        trainsTable = new FlexTable();
        // Put some text at the table's extremes.  This forces the table to be
        // 3 by 3.
        trainsTable.setText(0, 0, "upper-left corner");
        trainsTable.setText(2, 2, "bottom-right corner");

        // Let's put a button in the middle...
        trainsTable.setWidget(1, 0, trainsButton);

        // ...and set it's column span so that it takes up the whole row.
        trainsTable.getFlexCellFormatter().setColSpan(1, 0, 3);
        //trainsTable.setWidth("600px");

        RootPanel.get().add(trainsTable);

    }

    private void loadTrains() {
    	
    	String url = "http://www.myserver.com/getData?type=3";
    	url = GWT.getModuleBaseURL() + "v1/train";
    	url = GWT.getHostPageBaseURL() + "v1/train";
    	RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));

    	try {
    	  Request request = builder.sendRequest(null, new RequestCallback() {
    	    public void onError(Request request, Throwable exception) {
    	       // Couldn't connect to server (could be timeout, SOP violation, etc.)
    	    	exception.printStackTrace();
    	    }

			@Override
			public void onResponseReceived(Request request, Response response) {
				if (200 == response.getStatusCode()) {
	    	          // Process the response in response.getText()
					JSONValue jsonValue = JSONParser.parseStrict(response.getText());
					processTrain(jsonValue);
	    	      } else {
	    	        // Handle the error.  Can get the status text from response.getStatusText()
	    	    	  System.out.println(response.getStatusText());
	    	      }
			}
    	  });
    	} catch (Exception e) {
    	  // Couldn't connect to server
    		e.printStackTrace();
    	}
        
    }

	protected void processTrain(JSONValue jsonValue) {
		System.out.println(jsonValue.toString());
		
	}
}
