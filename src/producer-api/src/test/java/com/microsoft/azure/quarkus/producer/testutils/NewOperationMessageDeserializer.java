package com.microsoft.azure.quarkus.producer.testutils;

import com.microsoft.azure.quarkus.producer.model.NewOperationMessage;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

/**
 * New operation message deserializer.
 */
public class NewOperationMessageDeserializer
    extends ObjectMapperDeserializer<NewOperationMessage> {
  /**
   * Initializes a new instance of the {@link NewOperationMessageDeserializer}
   * class.
   */
  public NewOperationMessageDeserializer() {
    super(NewOperationMessage.class);
  }
}
