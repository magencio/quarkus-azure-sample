package com.microsoft.azure.quarkus.producer.exceptionhandlers;

import com.fasterxml.jackson.core.JsonParseException;
import com.microsoft.azure.quarkus.producer.model.ErrorResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps errors when parsing Json of a request to a HTTP 400 error.
 */
@Provider
public class JsonParseExceptionHandler implements ExceptionMapper<JsonParseException> {
  @Override
  public Response toResponse(JsonParseException exception) {
    ErrorResponse errorResponse = new ErrorResponse()
        .setTitle("Json Parsing Error")
        .setStatus(Status.BAD_REQUEST.getStatusCode())
        .setError(exception.getOriginalMessage());
    return Response.status(Status.BAD_REQUEST).entity(errorResponse).build();
  }
}
