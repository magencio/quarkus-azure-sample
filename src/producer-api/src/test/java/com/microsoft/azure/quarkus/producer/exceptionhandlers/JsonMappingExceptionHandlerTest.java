package com.microsoft.azure.quarkus.producer.exceptionhandlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParseException;
import com.microsoft.azure.quarkus.producer.model.ErrorResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.jboss.resteasy.reactive.RestResponse.StatusCode;
import org.junit.jupiter.api.Test;

public class JsonMappingExceptionHandlerTest {
  @Test
  public void testToResponse() {
    // Arrange
    final String message = "Unrecognized token 'A'";

    JsonParseException jsonParseExceptionMock = mock(JsonParseException.class);
    when(jsonParseExceptionMock.getOriginalMessage()).thenReturn(message);

    // Act
    Response response = new JsonParseExceptionHandler().toResponse(jsonParseExceptionMock);

    // Assert
    ErrorResponse expectedError = new ErrorResponse("Json Parsing Error", StatusCode.BAD_REQUEST, message);
    Response expectedResponse = Response.status(Status.BAD_REQUEST).entity(expectedError).build();
    assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);
  }
}
