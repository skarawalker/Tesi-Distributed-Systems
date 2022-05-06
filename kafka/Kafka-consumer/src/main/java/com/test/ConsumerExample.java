package com.test;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

//See https://docs.confluent.io/platform/current/installation/configuration/consumer-configs.html

public class ConsumerExample {


  public static void main(final String[] args) throws IOException {

	     //Config Parameters
	     Properties props = new Properties();
	     props.put("bootstrap.servers", "localhost:29092");
	     props.put("group.id", "mygroupid-1");
	     props.put("enable.auto.commit", "true");
	     props.put("auto.commit.interval.ms", "1000");
	     props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	     props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	     
	     //Uncomment this part to retrieve data "from the beginning"
	     //props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
	     
	     //Config properties
	     KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
	     
	     //Subscription phase
	     consumer.subscribe(Arrays.asList("mytopic"));
	     
	     while (true) {
	         ConsumerRecords<String, String> records = consumer.poll(100);
	         for (ConsumerRecord<String, String> record : records)
	             System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
	     }
  }

}