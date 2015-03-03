package uk.co.darkerwaters.client.variables;

import uk.co.darkerwaters.client.login.NotLoggedInException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("variables")
public interface VariablesService extends RemoteService {
	public void addVariable(String variable) throws NotLoggedInException;

	public void removeVariable(String variable) throws NotLoggedInException;

	public String[] getVariables() throws NotLoggedInException;
	
	public String[] getAllUsers() throws NotLoggedInException;
}
