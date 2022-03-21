package com.microsoft.azure.quarkus.consumer.testutils;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * Generic Kafka consumer that we can use to consume messages from local dev
 * services for Kafka.
 */
public class TestKafkaConsumer<T> {
  private KafkaConsumer<String, T> consumer;

  /**
   * Initialize the Kafka consumer.
   * 
   * @param devServicesPort   the port of the dev services for Kafka topic
   * @param topic             the topic to consume from
   * @param deserializerClass the class of the deserializer to use
   */
  public TestKafkaConsumer(String devServicesPort, String topic, Class<?> deserializerClass) {
    Properties properties = new Properties();
    // Target local dev services.
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + devServicesPort);
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializerClass.getName());
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    consumer = new KafkaConsumer<>(properties);
    consumer.subscribe(List.of(topic));
  }

  /**
   * Try to get the value of a message from the Kafka topic
   * based on the contents of the value.
   * 
   * @param predicate the condition that the value should match
   * @return the value if the message is found, null otherwise
   */
  public T pollValue(Predicate<? super T> predicate) {
    ConsumerRecords<String, T> records = consumer.poll(Duration.ofMillis(1000));
    return StreamSupport
        .stream(records.spliterator(), false)
        .map(r -> r.value())
        .filter(predicate)
        .findFirst().orElse(null);
  }

  /**
   * Try to get the record (header and value) of the message from the Kafka topic
   * based on the contents of the record.
   * 
   * @param predicate the condition that the record should match
   * @return the record if the message is found, null otherwise
   */
  public ConsumerRecord<String, T> pollRecord(Predicate<ConsumerRecord<String, T>> predicate) {
    ConsumerRecords<String, T> records = consumer.poll(Duration.ofMillis(1000));
    return StreamSupport
        .stream(records.spliterator(), false)
        .filter(predicate)
        .findFirst().orElse(null);
  }
}
