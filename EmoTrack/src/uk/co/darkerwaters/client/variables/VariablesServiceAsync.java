package uk.co.darkerwaters.client.variables;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface VariablesServiceAsync {
	public void addVariable(String variableName, AsyncCallback<Void> async);

	public void removeVariable(String variableName, AsyncCallback<Void> async);

	public void getVariables(AsyncCallback<String[]> async);
}
