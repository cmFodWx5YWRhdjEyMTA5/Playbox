package uk.co.darkerwaters.client.variables;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface VariablesServiceAsync {
	public void addVariable(String variableName, AsyncCallback<String[]> async);

	public void removeVariable(String variableName, AsyncCallback<String[]> async);

	public void getVariableNames(AsyncCallback<String[]> async);
	
	public void addSharedUser(String userId, AsyncCallback<String[]> async);

	public void removeSharedUser(String userId, AsyncCallback<String[]> async);

	public void getSharedUsers(AsyncCallback<String[]> async);
	
	public void resolveUserIdToName(String userId, AsyncCallback<String> async);
	
	public void getOwnUserId(AsyncCallback<String> async);
	
	public void getNumberUsers(AsyncCallback<Integer> async);
	
	public void getUsersSharing(AsyncCallback<String[]> async);
}
