package com.microsoft.azure.quarkus.consumer.deserializers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.quarkus.consumer.model.NewOperationMessage;
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
    super(NewOperationMessage.class,
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true));
  }
}
