package com.microsoft.azure.quarkus.producer.exceptionhandlers;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.microsoft.azure.quarkus.producer.model.ErrorResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps errors when mapping Json properties of a request to a HTTP 400 error.
 */
@Provider
public class JsonMappingExceptionHandler implements ExceptionMapper<JsonMappingException> {
  @Override
  public Response toResponse(JsonMappingException exception) {
    ErrorResponse errorResponse = new ErrorResponse()
        .setTitle("Json Mapping Error")
        .setStatus(Status.BAD_REQUEST.getStatusCode())
        .setError(exception.getOriginalMessage());
    return Response.status(Status.BAD_REQUEST).entity(errorResponse).build();
  }
}
