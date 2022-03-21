package com.microsoft.azure.quarkus.producer.exceptionhandlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.microsoft.azure.quarkus.producer.model.ErrorResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.jboss.resteasy.reactive.RestResponse.StatusCode;
import org.junit.jupiter.api.Test;

public class JsonParseExceptionHandlerTest {
  @Test
  public void testToResponse() {
    // Arrange
    final String message = "Unexpected end-of-input in VALUE_STRING";

    JsonMappingException jsonMappingExceptionMock = mock(JsonMappingException.class);
    when(jsonMappingExceptionMock.getOriginalMessage()).thenReturn(message);

    // Act
    Response response = new JsonMappingExceptionHandler().toResponse(jsonMappingExceptionMock);

    // Assert
    ErrorResponse expectedError = new ErrorResponse("Json Mapping Error", StatusCode.BAD_REQUEST, message);
    Response expectedResponse = Response.status(Status.BAD_REQUEST).entity(expectedError).build();
    assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);
  }
}
