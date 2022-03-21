package com.microsoft.azure.quarkus.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.microsoft.azure.quarkus.consumer.deserializers.NewOperationMessageDeserializer;
import com.microsoft.azure.quarkus.consumer.model.NewOperationMessage;
import com.microsoft.azure.quarkus.consumer.model.ResultEntity;
import com.microsoft.azure.quarkus.consumer.testutils.TestKafkaConsumer;
import com.microsoft.azure.quarkus.consumer.testutils.TestKafkaProducer;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
@Tag("integration")
public class NewOperationMessageHandlerTest {

  @ConfigProperty(name = "quarkus.kafka.devservices.port")
  private String devServicesPort;

  /** Test with all possible valid operations. */
  @ParameterizedTest
  @CsvSource({ "+,3.0", "-,-1.0", "*,2.0", "/,0.5" })
  public void testOnNewOperationMessageWithValidMessage(String operation, String result) {
    // Arrange
    final String operationId = UUID.randomUUID().toString();
    final String userId = UUID.randomUUID().toString();

    NewOperationMessage message = new NewOperationMessage(operationId, userId, 1, operation, 2);

    TestKafkaProducer<NewOperationMessage> kafkaProducer = new TestKafkaProducer<>(devServicesPort);

    // Act
    kafkaProducer.sendMessage("new-operation", message);

    // Assert
    Double expectedResult = Double.valueOf(result);
    ResultEntity expectedEntity = new ResultEntity(operationId, userId, 1, operation, 2, expectedResult);
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      // Entity should be in the database
      ResultEntity resultEntity = ResultEntity.findByOperationIdAndUserId(operationId, userId)
          .toCompletableFuture().get();
      assertThat(resultEntity).usingRecursiveComparison().isEqualTo(expectedEntity);
    });
  }

  @Test
  public void testOnNewOperationMessageWithoutMandatoryFields() {
    // Arrange
    NewOperationMessage invalidMessage = new NewOperationMessage();

    TestKafkaProducer<NewOperationMessage> kafkaProducer = new TestKafkaProducer<>(devServicesPort);
    TestKafkaConsumer<NewOperationMessage> kafkaConsumer = new TestKafkaConsumer<>(devServicesPort,
        "new-operation-dead-letter", NewOperationMessageDeserializer.class);

    // Act
    kafkaProducer.sendMessage("new-operation", invalidMessage);

    // Assert
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      // Message should be in the dead letter queue
      ConsumerRecord<String, NewOperationMessage> record = kafkaConsumer
          .pollRecord(r -> headerContainsString(r, "dead-letter-reason", "5 constraint violation(s)"));
      assertThat(record).isNotNull();
      assertThat(record.value()).usingRecursiveComparison().isEqualTo(invalidMessage);

      // Reason for failure should be in the headers of the message
      Header header = getHeader(record, "dead-letter-reason");
      assertThat(header).isNotNull();
      assertThat(new String(header.value()))
          .contains("5 constraint violation(s) occurred during method validation")
          .contains("must not be blank")
          .contains("onNewOperationMessage.message.operationId")
          .contains("onNewOperationMessage.message.userId")
          .contains("must not be null")
          .contains("onNewOperationMessage.message.leftOperand")
          .contains("onNewOperationMessage.message.operation")
          .contains("onNewOperationMessage.message.rightOperand");
    });
  }

  @Test
  public void testOnNewOperationMessageWithInvalidFields() {
    // Arrange
    NewOperationMessage invalidMessage = new NewOperationMessage(" ", " ", 1, " ", 2);

    TestKafkaProducer<NewOperationMessage> kafkaProducer = new TestKafkaProducer<>(devServicesPort);
    TestKafkaConsumer<NewOperationMessage> kafkaConsumer = new TestKafkaConsumer<>(devServicesPort,
        "new-operation-dead-letter", NewOperationMessageDeserializer.class);

    // Act
    kafkaProducer.sendMessage("new-operation", invalidMessage);

    // Assert
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      // Message should be in the dead letter queue
      ConsumerRecord<String, NewOperationMessage> record = kafkaConsumer
          .pollRecord(r -> headerContainsString(r, "dead-letter-reason", "3 constraint violation(s)"));
      assertThat(record).isNotNull();
      assertThat(record.value())
          .usingRecursiveComparison()
          .isEqualTo(invalidMessage);

      // Reason for failure should be in the headers of the message
      Header header = getHeader(record, "dead-letter-reason");
      assertThat(header).isNotNull();
      assertThat(new String(header.value()))
          .contains("3 constraint violation(s) occurred during method validation")
          .contains("must not be blank")
          .contains("onNewOperationMessage.message.operationId")
          .contains("onNewOperationMessage.message.userId")
          .contains("must be one of +, -, *, /")
          .contains("onNewOperationMessage.message.operation");
    });
  }

  @Test
  public void testOnNewOperationMessageWithFieldsNotInSchema() {
    // Arrange
    final String operationId = UUID.randomUUID().toString();
    final String userId = UUID.randomUUID().toString();

    NewOperationMessage message = new NewOperationMessage(operationId, userId, 1, "+", 2);
    JSONObject invalidMessage = new JSONObject(message).put("dummy", "dummy");

    TestKafkaProducer<Map<String, Object>> kafkaProducer = new TestKafkaProducer<>(devServicesPort);
    TestKafkaConsumer<NewOperationMessage> kafkaConsumer = new TestKafkaConsumer<>(devServicesPort,
        "new-operation-dead-letter", NewOperationMessageDeserializer.class);

    // Act
    kafkaProducer.sendMessage("new-operation", invalidMessage.toMap());

    // Assert
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      // Entity shouldn't be in the database
      ResultEntity resultEntity = ResultEntity.findByOperationIdAndUserId(operationId, userId)
          .toCompletableFuture().get();
      assertThat(resultEntity).isNull();

      // Null message with deserialization-failure headers should be in the dead
      // letter queue. Headers of the message should include original message
      ConsumerRecord<String, NewOperationMessage> record = kafkaConsumer
          .pollRecord(r -> headerContainsString(r, "deserialization-failure-data", operationId));
      assertThat(record).isNotNull();
      assertThat(record.value()).isNull();

      // Reason for failure should be in the headers of the message
      Header reasonHeader = getHeader(record, "dead-letter-reason");
      assertThat(reasonHeader).isNotNull();
      assertThat(new String(reasonHeader.value()))
          .isEqualTo("Message failed to deserialize. Check deserialization-failure headers for details");

      Header causeHeader = getHeader(record, "deserialization-failure-cause");
      assertThat(causeHeader).isNotNull();
      assertThat(new String(causeHeader.value()))
          .contains("Unrecognized field \"dummy\"");
    });
  }

  @Test
  public void testOnNewOperationMessageWithWrongFieldType() {
    // Arrange
    final String operationId = UUID.randomUUID().toString();
    final String userId = UUID.randomUUID().toString();

    NewOperationMessage message = new NewOperationMessage(operationId, userId, 1, "+", 2);
    JSONObject invalidMessage = new JSONObject(message).put("leftOperand", "A");

    TestKafkaProducer<Map<String, Object>> kafkaProducer = new TestKafkaProducer<>(devServicesPort);
    TestKafkaConsumer<NewOperationMessage> kafkaConsumer = new TestKafkaConsumer<>(devServicesPort,
        "new-operation-dead-letter", NewOperationMessageDeserializer.class);

    // Act
    kafkaProducer.sendMessage("new-operation", invalidMessage.toMap());

    // Assert
    await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      // Entity shouldn't be in the database
      ResultEntity resultEntity = ResultEntity.findByOperationIdAndUserId(operationId, userId)
          .toCompletableFuture().get();
      assertThat(resultEntity).isNull();

      // Null message with deserialization-failure headers should be in the dead
      // letter queue. Headers of the message should include original message
      ConsumerRecord<String, NewOperationMessage> record = kafkaConsumer
          .pollRecord(r -> headerContainsString(r, "deserialization-failure-data", operationId));
      assertThat(record).isNotNull();
      assertThat(record.value()).isNull();

      // Reason for failure should be in the headers of the message
      Header reasonHeader = getHeader(record, "dead-letter-reason");
      assertThat(reasonHeader).isNotNull();
      assertThat(new String(reasonHeader.value()))
          .isEqualTo("Message failed to deserialize. Check deserialization-failure headers for details");

      Header causeHeader = getHeader(record, "deserialization-failure-cause");
      assertThat(causeHeader).isNotNull();
      assertThat(new String(causeHeader.value()))
          .contains("Cannot deserialize value of type `int` from String \"A\"");
    });
  }

  private boolean headerContainsString(ConsumerRecord<String, NewOperationMessage> record, String key, String value) {
    return StreamSupport.stream(record.headers().spliterator(), false)
        .anyMatch(h -> h.key().equals(key) && new String(h.value()).contains(value));
  }

  private Header getHeader(ConsumerRecord<String, NewOperationMessage> record, String key) {
    return StreamSupport.stream(record.headers().spliterator(), false)
        .filter(h -> h.key().equals(key)).findFirst().orElse(null);
  }
}
