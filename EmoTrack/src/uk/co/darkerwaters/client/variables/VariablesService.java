package uk.co.darkerwaters.client.variables;

import uk.co.darkerwaters.client.login.NotLoggedInException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("variables")
public interface VariablesService extends RemoteService {
	public String[] addVariable(String variable) throws NotLoggedInException;

	public String[] removeVariable(String variable) throws NotLoggedInException;

	public String[] getVariableNames() throws NotLoggedInException;
	
	public String[] addSharedUser(String userId) throws NotLoggedInException;

	public String[] removeSharedUser(String userId) throws NotLoggedInException;

	public String[] getSharedUsers() throws NotLoggedInException;
	
	public String resolveUserIdToName(String userId) throws NotLoggedInException;
	
	public String getOwnUserId() throws NotLoggedInException;
	
	public Integer getNumberUsers() throws NotLoggedInException;
	
	public String[] getUsersSharing() throws NotLoggedInException;
}
