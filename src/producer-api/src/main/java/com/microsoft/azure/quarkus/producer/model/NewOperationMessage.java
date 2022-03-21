package com.microsoft.azure.quarkus.producer.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * New operation message.
 */
@Getter
@Setter
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
@AllArgsConstructor
@NoArgsConstructor
public class NewOperationMessage {
  private String operationId;
  private String userId;
  private Integer leftOperand;
  private String operation;
  private Integer rightOperand;

  /**
   * Initializes a new instance of the {@link NewOperationMessage} class.
   * 
   * @param operationId the operation id
   * @param userId      the user id
   * @param request     the request with the new operation
   */
  public NewOperationMessage(String operationId, String userId, NewOperationRequest request) {
    this.operationId = operationId;
    this.userId = userId;
    this.leftOperand = request.getLeftOperand();
    this.operation = request.getOperation();
    this.rightOperand = request.getRightOperand();
  }
}