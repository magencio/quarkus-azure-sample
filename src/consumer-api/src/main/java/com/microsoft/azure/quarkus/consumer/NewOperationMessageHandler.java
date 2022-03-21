package com.microsoft.azure.quarkus.consumer;

import com.microsoft.azure.quarkus.consumer.model.NewOperationMessage;
import com.microsoft.azure.quarkus.consumer.model.ResultEntity;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.Valid;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

/**
 * New operation message handler.
 */
@ApplicationScoped
public class NewOperationMessageHandler {

  private static final Logger LOG = Logger.getLogger(NewOperationMessageHandler.class);

  /**
   * Process a message for a new operation.
   * 
   * @param message the message with the new operation
   */
  @Incoming("new-operation")
  public CompletionStage<Void> onNewOperationMessage(@Valid NewOperationMessage message) {
    if (message == null) {
      throw new IllegalArgumentException(
          "Message failed to deserialize. Check deserialization-failure headers for details");
    }

    Double result = calculateResult(message);

    return ResultEntity.upsertResult(message, result)
        .thenAccept(ignore -> LOG.info("Message '" + message.getOperationId() + "' processed sucessfully"));
  }

  private Double calculateResult(NewOperationMessage message) {
    Double result;
    switch (message.getOperation()) {
      case "+":
        result = Double.valueOf(message.getLeftOperand()) + Double.valueOf(message.getRightOperand());
        break;
      case "-":
        result = Double.valueOf(message.getLeftOperand()) - Double.valueOf(message.getRightOperand());
        break;
      case "*":
        result = Double.valueOf(message.getLeftOperand()) * Double.valueOf(message.getRightOperand());
        break;
      case "/":
        result = Double.valueOf(message.getLeftOperand()) / Double.valueOf(message.getRightOperand());
        break;
      default:
        throw new IllegalArgumentException("Invalid operation: " + message.getOperation());
    }
    return result;
  }
}
