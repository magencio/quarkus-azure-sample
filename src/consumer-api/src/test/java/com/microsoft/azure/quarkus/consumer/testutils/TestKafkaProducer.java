package com.microsoft.azure.quarkus.consumer.testutils;

import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * Generic Kafka producer that we can use to send messages to local dev services
 * for Kafka.
 */
public class TestKafkaProducer<T> {
  private KafkaProducer<String, T> producer;

  /**
   * Initialize the Kafka producer.
   * 
   * @param devServicesPort the port of the dev services for Kafka topic
   */
  public TestKafkaProducer(String devServicesPort) {
    Properties properties = new Properties();
    // Target local dev services.
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + devServicesPort);
    properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ObjectMapperSerializer.class.getName());
    properties.setProperty(ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());

    producer = new KafkaProducer<>(properties);
  }

  /**
   * Send a message to the topic.
   * 
   * @param topic   the kafka topic to be used
   * @param message the message
   */
  public void sendMessage(String topic, T message) {
    ProducerRecord<String, T> record = new ProducerRecord<String, T>(topic, UUID.randomUUID().toString(), message);
    producer.send(record);
    producer.close();
  }
}
