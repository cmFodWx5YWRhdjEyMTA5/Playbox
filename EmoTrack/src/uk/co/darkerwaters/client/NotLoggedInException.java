package uk.co.darkerwaters.client;

import java.io.Serializable;

public class NotLoggedInException extends Exception implements Serializable {

	private static final long serialVersionUID = -3522662786904463795L;

	public NotLoggedInException() {
		super();
	}

	public NotLoggedInException(String message) {
		super(message);
	}

}
