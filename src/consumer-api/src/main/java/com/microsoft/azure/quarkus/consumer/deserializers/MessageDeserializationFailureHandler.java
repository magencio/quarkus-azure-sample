package com.microsoft.azure.quarkus.consumer.deserializers;

import com.microsoft.azure.quarkus.consumer.model.NewOperationMessage;
import io.smallrye.common.annotation.Identifier;
import io.smallrye.reactive.messaging.kafka.DeserializationFailureHandler;
import javax.enterprise.context.ApplicationScoped;

/**
 * If we fail to deserialize a message, this handler will send a null value to
 * {@link MewOperationMessageHandler}.
 * If {@link MewOperationMessageHandler} fails to process that null value, a
 * message will be sent to the dead letter topic. The message will contain that
 * null value, some dead-letter related headers and some deserialization-failure
 * related headers.
 * The dead-letter headers will include the error that happened during the
 * processing of the null value, which caused the message to go the dead letter
 * topic.
 * The deserialization-failure headers will include the original message and the
 * cause of the deserialization error.
 */
@ApplicationScoped
@Identifier("deserialization-failure-handler")
public class MessageDeserializationFailureHandler
    implements DeserializationFailureHandler<NewOperationMessage> {
}