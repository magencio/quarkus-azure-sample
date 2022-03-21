package com.microsoft.azure.quarkus.consumer;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.azure.quarkus.consumer.model.ResultEntity;
import com.microsoft.azure.quarkus.consumer.model.ResultResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.response.Response;
import java.util.UUID;
import org.jboss.resteasy.reactive.RestResponse.StatusCode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class ResultResourceTest {

  private static final String oid = "28644956-3bfa-47e1-89a7-05b182094424";

  @Test
  @TestSecurity(user = "producer", roles = "Producer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testGetEndpointWithProducerAndExistingResultWithSameUserId() throws Exception {
    // Arrange
    String operationId = UUID.randomUUID().toString();

    ResultEntity entity = new ResultEntity(operationId, oid, 1, "+", 2, 3.0);
    ResultEntity.persistOrUpdate(entity).subscribe().asCompletionStage().join();

    // Act
    Response getResponse = given().when().get("/result/{operationId}", operationId);

    // Assert
    getResponse.then().statusCode(StatusCode.OK);

    ResultResponse response = getResponse.body().as(ResultResponse.class);
    ResultResponse expectedResponse = new ResultResponse(operationId, entity.getResult());
    assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);
  }

  @Test
  @TestSecurity(user = "producer", roles = "Producer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testGetEndpointWithProducerAndExistingResultWithDifferentUserId() throws Exception {
    // Arrange
    String operationId = UUID.randomUUID().toString();
    String anotherUserId = UUID.randomUUID().toString();

    ResultEntity entity = new ResultEntity(operationId, anotherUserId, 1, "+", 2, 3.0);
    ResultEntity.persistOrUpdate(entity).subscribe().asCompletionStage().join();

    // Act & Assert
    given().when().get("/result/{operationId}", operationId)
        .then().statusCode(StatusCode.NOT_FOUND);
  }

  @Test
  @TestSecurity(user = "producer", roles = "Producer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testGetEndpointWithProducerAndNonExistingResult() {
    given().when().get("/result/{operationId}", UUID.randomUUID().toString())
        .then().statusCode(StatusCode.NOT_FOUND);
  }

  @Test
  @TestSecurity(user = "producer", roles = "Producer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testGetEndpointWithProducerAndNoOperationId() {
    given().when().get("/result")
        .then().statusCode(StatusCode.NOT_FOUND);
  }

  @Test
  @TestSecurity(user = "producer", roles = "Producer")
  public void testGetEndpointWithProducerAndNoOidClaim() {
    given().when().get("/result/{operationId}", UUID.randomUUID().toString())
        .then().statusCode(StatusCode.FORBIDDEN);
  }

  @Test
  @TestSecurity(user = "consumer", roles = "Consumer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testGetEndpointWithConsumerAndExistingResultWithSameUserId() throws Exception {
    // Arrange
    String operationId = UUID.randomUUID().toString();

    ResultEntity entity = new ResultEntity(operationId, oid, 1, "+", 2, 3.0);
    ResultEntity.persistOrUpdate(entity).subscribe().asCompletionStage().join();

    // Act
    Response getResponse = given().when().get("/result/{operationId}", operationId);

    // Assert
    getResponse.then().statusCode(StatusCode.OK);

    ResultResponse response = getResponse.body().as(ResultResponse.class);
    ResultResponse expectedResponse = new ResultResponse(operationId, entity.getResult());
    assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);
  }

  @Test
  @TestSecurity(user = "consumer", roles = "Consumer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testGetEndpointWithConsumerAndExistingResultWithDifferentUserId() throws Exception {
    // Arrange
    String operationId = UUID.randomUUID().toString();
    String anotherUserId = UUID.randomUUID().toString();

    ResultEntity entity = new ResultEntity(operationId, anotherUserId, 1, "+", 2, 3.0);
    ResultEntity.persistOrUpdate(entity).subscribe().asCompletionStage().join();

    // Act
    Response getResponse = given().when().get("/result/{operationId}", operationId);

    // Assert
    getResponse.then().statusCode(StatusCode.OK);

    ResultResponse response = getResponse.body().as(ResultResponse.class);
    ResultResponse expectedResponse = new ResultResponse(operationId, entity.getResult());
    assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);
  }

  @Test
  @TestSecurity(user = "consumer", roles = "Consumer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testGetEndpointWithConsumerAndNonExistingResult() {
    given().when().get("/result/{operationId}", UUID.randomUUID().toString())
        .then().statusCode(StatusCode.NOT_FOUND);
  }

  @Test
  @TestSecurity(user = "consumer", roles = "Consumer")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testGetEndpointWithConsumerAndNoOperationId() {
    given().when().get("/result")
        .then().statusCode(StatusCode.NOT_FOUND);
  }

  @Test
  @TestSecurity(user = "consumer", roles = "Consumer")
  public void testGetEndpointWithConsumerAndNoOidClaim() {
    given().when().get("/result/{operationId}", UUID.randomUUID().toString())
        .then().statusCode(StatusCode.FORBIDDEN);
  }

  @Test
  @TestSecurity(user = "unknown", roles = "Unknown")
  @OidcSecurity(claims = { @Claim(key = "oid", value = oid) })
  public void testGetEndpointWithInvalidRole() {
    given().when().get("/result/{operationId}", UUID.randomUUID().toString())
        .then().statusCode(StatusCode.FORBIDDEN);
  }

  @Test
  public void testGetEndpointWithNoAuth() {
    given().when().get("/result/" + UUID.randomUUID().toString())
        .then().statusCode(StatusCode.UNAUTHORIZED);
  }
}
