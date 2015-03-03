package uk.co.darkerwaters.server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import uk.co.darkerwaters.client.login.NotLoggedInException;
import uk.co.darkerwaters.client.variables.Variables;
import uk.co.darkerwaters.client.variables.VariablesService;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class VariablesServiceImpl extends RemoteServiceServlet implements VariablesService {
	private static final long serialVersionUID = 3564108017056663579L;

	private static final Logger LOG = Logger.getLogger(VariablesServiceImpl.class.getName());
	private static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");

	public String[] addVariable(String variableName) throws NotLoggedInException {
		// get the current variables item
		PersistenceManager pm = getPersistenceManager();
		Variables variables = getVariables(true, pm);
		if (null == variables) {
			variables = new Variables(getUser());
		}
		else {
			variables = new Variables(variables);
		}
		// add the variable
		variables.addVariableName(variableName);
		try {
			// and add the thing into the store
			pm.makePersistent(variables);
		} finally {
			pm.close();
		}
		return variablesNames(variables);
	}

	public String[] removeVariable(String variableName) throws NotLoggedInException {
		// get the current variables item
		PersistenceManager pm = getPersistenceManager();
		Variables variables = getVariables(true, pm);
		if (null == variables) {
			variables = new Variables(getUser());
		}
		else {
			variables = new Variables(variables);
		}
		// remove the variable
		variables.removeVariableName(variableName);
		try {
			// now we can put the variables instance we want back in
			pm.makePersistent(variables);
		} finally {
			pm.close();
		}
		return variablesNames(variables);
	}
	
	public String[] addSharedUser(String userId) throws NotLoggedInException {
		// get the current variables item
		PersistenceManager pm = getPersistenceManager();
		Variables variables = getVariables(true, pm);
		if (null == variables) {
			variables = new Variables(getUser());
		}
		else {
			variables = new Variables(variables);
		}
		// add the variable
		variables.addSharedUser(userId);
		try {
			// and add the thing into the store
			pm.makePersistent(variables);
		} finally {
			pm.close();
		}
		return sharedUsers(variables);
	}

	public String[] removeSharedUser(String userId) throws NotLoggedInException {
		// get the current variables item
		PersistenceManager pm = getPersistenceManager();
		Variables variables = getVariables(true, pm);
		if (null == variables) {
			variables = new Variables(getUser());
		}
		else {
			variables = new Variables(variables);
		}
		// remove the variable
		variables.removeSharedUser(userId);
		try {
			// now we can put the variables instance we want back in
			pm.makePersistent(variables);
		} finally {
			pm.close();
		}
		return sharedUsers(variables);
	}
	
	public String[] getSharedUsers() throws NotLoggedInException {
		PersistenceManager pm = getPersistenceManager();
		Variables variables = null;
		try {
			variables = getVariables(false, pm);
		}
		finally {
			pm.close();
		}
		if (null == variables) {
			return new String[0];
		}
		else {
			return sharedUsers(variables);
		}
	}
	
	private String[] sharedUsers(Variables variables) {
		String[] names = new String[variables.getNumberSharedUsers()];
		for (int i = 0; i < names.length; ++i) {
			names[i] = variables.getSharedUser(i);
		}
		return names;
	}
	
	private String[] variablesNames(Variables variables) {
		String[] names = new String[variables.getNumberVariables()];
		for (int i = 0; i < names.length; ++i) {
			names[i] = variables.getVariableName(i);
		}
		return names;
	}

	public Integer getNumberUsers() throws NotLoggedInException {
		PersistenceManager pm = getPersistenceManager();
		Integer noUsers = new Integer(0);
		try {
			noUsers = new Integer(getAllUsers(pm).length);
		}
		finally {
			pm.close();
		}
		return noUsers;
	}
	
	public String resolveUserIdToName(String userId) throws NotLoggedInException {
		checkLoggedIn();
		String userName = "";
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Variables.class);
			Object executeResult = q.execute(getUser());
			if (null != executeResult && executeResult instanceof List<?>) {
				// get all the users
				List<?> executeList = (List<?>) executeResult;
				for (Object item : executeList) {
					if (null != item && item instanceof Variables) {
						// result is as expected
						Variables variable = (Variables)item;
						// get the user
						User user = variable.getUser();
						if (null != user && user.getUserId().equals(userId)) {
							// this is the one
							userName = user.getNickname();
							break;
						}
					}
					else {
						// not expected
						LOG.log(Level.WARNING, "resolveUserIdToName not returning expected object type:" + item);
					}
				}
			}
			else {
				LOG.log(Level.WARNING, "resolveUserIdToName not returning expected execution object type:" + executeResult);
			}
		}
		finally {
			pm.close();
		}
		return userName;
	}
	
	public User[] getAllUsers(PersistenceManager pm) throws NotLoggedInException {
		checkLoggedIn();
		ArrayList<User> users = new ArrayList<User>();
		
		Query q = pm.newQuery(Variables.class);
		Object executeResult = q.execute(getUser());
		if (null != executeResult && executeResult instanceof List<?>) {
			// get all the users
			List<?> executeList = (List<?>) executeResult;
			for (Object item : executeList) {
				if (null != item && item instanceof Variables) {
					// result is as expected
					Variables variable = (Variables)item;
					// get the user
					User user = variable.getUser();
					if (null != user && false == users.contains(user)) {
						users.add(user);
					}
				}
				else {
					// not expected
					LOG.log(Level.WARNING, "getAllUsers not returning expected object type:" + item);
				}
			}
		}
		else {
			LOG.log(Level.WARNING, "getAllUsers not returning expected execution object type:" + executeResult);
		}
		return users.toArray(new User[users.size()]);
	}
	
	public Variables getVariables(boolean deleteVariablesFound, PersistenceManager pm) throws NotLoggedInException {
		checkLoggedIn();
		Variables variablesFound = null;
		
		Query q = pm.newQuery(Variables.class, "user == u");
		q.declareParameters("com.google.appengine.api.users.User u");
		q.setOrdering("createDate");
		Object executeResult = q.execute(getUser());
		if (null != executeResult && executeResult instanceof List<?>) {
			// convert the execute result list into a returnable result
			List<?> executeList = (List<?>) executeResult;
			for (Object item : executeList) {
				if (null != item && item instanceof Variables) {
					// result is as expected
					if (null == variablesFound) {
						// just set this
						variablesFound = (Variables)item;
					}
					else {
						// merge
						variablesFound.addVariableName(((Variables)item).getVariablesNames());
					}
					if (deleteVariablesFound) {
						pm.deletePersistent(item);
					}
				}
				else {
					// not expected
					LOG.log(Level.WARNING, "getVariables not returning expected object type:" + item);
				}
			}
		}
		else {
			LOG.log(Level.WARNING, "getVariables not returning expected execution object type:" + executeResult);
		}
		return variablesFound;
	}

	public String[] getVariableNames() throws NotLoggedInException {
		PersistenceManager pm = getPersistenceManager();
		String[] names = new String[0];
		try {
			Variables variables = getVariables(false, pm);
			if (null != variables) {
				names = variablesNames(variables);
			}
		}
		finally {
			pm.close();
		}
		return names;
	}

	private void checkLoggedIn() throws NotLoggedInException {
		if (getUser() == null) {
			throw new NotLoggedInException("Not logged in.");
		}
	}

	private User getUser() {
		UserService userService = UserServiceFactory.getUserService();
		return userService.getCurrentUser();
	}

	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}
}
