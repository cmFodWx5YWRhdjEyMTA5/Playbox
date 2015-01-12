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
import uk.co.darkerwaters.client.variables.Variable;
import uk.co.darkerwaters.client.variables.VariablesService;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class VariablesServiceImpl extends RemoteServiceServlet implements VariablesService {
	private static final long serialVersionUID = 3564108017056663579L;

	private static final Logger LOG = Logger.getLogger(VariablesServiceImpl.class.getName());
	private static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");

	public void addVariable(String variableName) throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.makePersistent(new Variable(getUser(), variableName));
		} finally {
			pm.close();
		}
	}

	public void removeVariable(String variableName) throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		try {
			long deleteCount = 0;
			Query q = pm.newQuery(Variable.class, "user == u");
			q.declareParameters("com.google.appengine.api.users.User u");
			List<Variable> variablesList = (List<Variable>) q.execute(getUser());
			for (Variable variable : variablesList) {
				// delete everything like this name (ignoring case)
				if (0 == variableName.compareToIgnoreCase(variable.getVariableName())) {
					++deleteCount;
					pm.deletePersistent(variable);
				}
			}
			if (deleteCount != 1) {
				LOG.log(Level.WARNING, "removeVariable deleted " + deleteCount + " Stocks");
			}
		} finally {
			pm.close();
		}
	}

	public String[] getVariables() throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		List<String> variables = new ArrayList<String>();
		try {
			Query q = pm.newQuery(Variable.class, "user == u");
			q.declareParameters("com.google.appengine.api.users.User u");
			q.setOrdering("createDate");
			List<Variable> variablesList = (List<Variable>) q.execute(getUser());
			for (Variable variable : variablesList) {
				variables.add(variable.getVariableName());
			}
		} finally {
			pm.close();
		}
		return (String[]) variables.toArray(new String[0]);
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
