package com.microsoft.azure.quarkus.producer.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

public class NewOperationMessageTest {
  @Test
  public void testConstructor() {
    // Arrange
    final String operationId = UUID.randomUUID().toString();
    final String userId = UUID.randomUUID().toString();

    NewOperationRequest request = new NewOperationRequest(1, "+", 2);

    // Act
    NewOperationMessage message = new NewOperationMessage(operationId, userId, request);

    // Assert
    NewOperationMessage expectedMessage = new NewOperationMessage(operationId, userId, 1, "+", 2);
    assertThat(message).usingRecursiveComparison().isEqualTo(expectedMessage);
  }
}
