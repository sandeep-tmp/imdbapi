/**
 * 
 */
package com.sandeepkaul.imdbapi;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * @author sandeep.kaul
 *
 */
public class ImdbExceptionMapper implements ExceptionMapper<Exception> {

	public Response toResponse(Exception exception) {

		if (exception instanceof RuntimeException) {
			Integer statusCode = 400;
			String errorMessage = exception.getMessage();
			return Response.status(statusCode).entity("{\"message\":\"" + errorMessage + "\"}")
					.type(MediaType.APPLICATION_JSON).build();
		} else if (exception instanceof Exception) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"message\":\"Something went wrong.\"}").build();
		}
		return null;
	}

}
