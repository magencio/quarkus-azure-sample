package com.microsoft.azure.quarkus.producer;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.microsoft.azure.quarkus.producer.model.ErrorResponse;
import com.microsoft.azure.quarkus.producer.model.NewOperationMessage;
import com.microsoft.azure.quarkus.producer.model.NewOperationRequest;
import com.microsoft.azure.quarkus.producer.model.NewOperationResponse;
import com.microsoft.azure.quarkus.producer.testutils.ConstraintViolationError;
import com.microsoft.azure.quarkus.producer.testutils.ConstraintViolationErrorResponse;
import com.microsoft.azure.quarkus.producer.testutils.NewOperationMessageDeserializer;
import com.microsoft.azure.quarkus.producer.testutils.TestKafkaConsumer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.response.Response;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestResponse.StatusCode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@Tag("integration")
public class OperationResourceTest {

  private static final String oid = "28644956-3bfa-47e1-89a7-05b182094424";

  @ConfigProperty(name = "quarkus.kafka.devservices.port")
  private String devServicesPort;

  /** Test with all possible valid operations. */
  @ParameterizedTest
  @ValueSource(strings = { "+", "-", "*", "/" })
  @TestSecurity(user = "producer", roles = "Producer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testPostEndpointWithValidRequest(String operation) {
    // Arrange
    NewOperationRequest request = new NewOperationRequest(1, operation, 2);

    TestKafkaConsumer<NewOperationMessage> consumer = new TestKafkaConsumer<>(
        devServicesPort, "new-operation", NewOperationMessageDeserializer.class);

    // Act
    Response postResponse = postRequest(request);

    // Assert the response from the API
    postResponse.then().statusCode(StatusCode.ACCEPTED);

    NewOperationResponse response = postResponse.body().as(NewOperationResponse.class);
    assertDoesNotThrow(() -> UUID.fromString(response.getOperationId()));

    // Assert the message sent to the Kafka topic
    NewOperationMessage expectedMessage = new NewOperationMessage(response.getOperationId(), oid, request);
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      NewOperationMessage message = consumer
          .pollValue(m -> m.getOperationId().equals(expectedMessage.getOperationId()));
      assertThat(message).usingRecursiveComparison().isEqualTo(expectedMessage);
    });
  }

  @Test
  @TestSecurity(user = "producer", roles = "Producer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testPostEndpointWithNoRequest() {
    Response postResponse = postRequest("");
    assertBadRequestWithConstraintViolations(postResponse, List.of(
        new ConstraintViolationError("post.request", "must not be null")));
  }

  @Test
  @TestSecurity(user = "producer", roles = "Producer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testPostEndpointWithEmptyRequest() {
    Response postResponse = postRequest("{}");
    assertBadRequestWithConstraintViolations(postResponse, List.of(
        new ConstraintViolationError("post.request.leftOperand", "must not be null"),
        new ConstraintViolationError("post.request.operation", "must not be null"),
        new ConstraintViolationError("post.request.rightOperand", "must not be null")));
  }

  @Test
  @TestSecurity(user = "producer", roles = "Producer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testPostEndpointWithInvalidOperation() {
    Response postResponse = postRequest(new NewOperationRequest(1, "invalid", 2));
    assertBadRequestWithConstraintViolations(postResponse, List.of(
        new ConstraintViolationError("post.request.operation", "must be one of +, -, *, /")));
  }

  @Test
  @TestSecurity(user = "producer", roles = "Producer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testPostEndpointWithInvalidJsonMapping() {
    Response postResponse = postRequest("{ \"leftOperand\": \"1 }");
    assertBadRequestWithJsonError(postResponse, "Json Mapping Error",
        "Unexpected end-of-input in VALUE_STRING");
  }

  @Test
  @TestSecurity(user = "producer", roles = "Producer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testPostEndpointWithInvalidJson() {
    Response postResponse = postRequest("{ \"leftOperand\": A }");
    assertBadRequestWithJsonError(postResponse, "Json Parsing Error",
        "Unrecognized token 'A': "
            + "was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')");
  }

  @Test
  @TestSecurity(user = "producer", roles = "Producer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testPostEndpointWithInvalidPropertyType() {
    postRequest("{ \"leftOperand\": \"A\", \"operation\": \"+\", \"rightOperand\": 2 }")
        .then().statusCode(StatusCode.BAD_REQUEST);
  }

  @Test
  @TestSecurity(user = "consumer", roles = "Consumer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testPostEndpointWithInvalidRole() {
    postRequest(new NewOperationRequest(1, "+", 2))
        .then().statusCode(StatusCode.FORBIDDEN);
  }

  @Test
  @TestSecurity(user = "producer", roles = "Producer")
  public void testPostEndpointWithNoOidClaim() {
    postRequest(new NewOperationRequest(1, "+", 2))
        .then().statusCode(StatusCode.FORBIDDEN);
  }

  @Test
  public void testPostEndpointWithNoAuth() {
    postRequest(new NewOperationRequest(1, "+", 2))
        .then().statusCode(StatusCode.UNAUTHORIZED);
  }

  private Response postRequest(NewOperationRequest request) {
    return given()
        .contentType("application/json")
        .body(request)
        .when()
        .post("/operation");
  }

  private Response postRequest(String request) {
    return given()
        .contentType("application/json")
        .body(request)
        .when()
        .post("/operation");
  }

  private void assertBadRequestWithConstraintViolations(
      Response postResponse, List<ConstraintViolationError> expectedViolations) {
    postResponse.then().statusCode(StatusCode.BAD_REQUEST);

    ConstraintViolationErrorResponse expectedResponse = new ConstraintViolationErrorResponse(
        "Constraint Violation", StatusCode.BAD_REQUEST, expectedViolations);
    ConstraintViolationErrorResponse response = postResponse.body().as(ConstraintViolationErrorResponse.class);
    assertThat(response)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedResponse);
  }

  private void assertBadRequestWithJsonError(
      Response postResponse, String title, String error) {
    postResponse.then().statusCode(StatusCode.BAD_REQUEST);

    ErrorResponse expectedResponse = new ErrorResponse(title, StatusCode.BAD_REQUEST, error);
    ErrorResponse response = postResponse.body().as(ErrorResponse.class);
    assertThat(response)
        .usingRecursiveComparison()
        .isEqualTo(expectedResponse);
  }
}