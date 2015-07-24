package com.alonyx.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public abstract class JacksonResource {
	
	private final ObjectMapper objectMapper;

	public static final Logger LOG = Logger.getLogger(JacksonResource.class.getName());
	
	protected JacksonResource() {
		this.objectMapper = new ObjectMapper();
	}
	
	protected String toJsonString(Object object) {
		String jsonString = "error";
		try {
			jsonString = this.objectMapper.writeValueAsString(object);
		} catch (JsonGenerationException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		} catch (JsonMappingException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
		return jsonString;
	}
	
	public <T> T createJacksonObject(Class<T> jacksonClass, String jsonString) {
		// create the specified object from the database value
		T result = null;
		try {
			// read the value using jackson
			result = this.objectMapper.readValue(jsonString, jacksonClass);
		} catch (IOException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
		return result;
	}
}
