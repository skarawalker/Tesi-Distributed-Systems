package com.test;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;


public class ConsumerExample {

	final static String topicName="streamTopic1";
	final static Properties props = new Properties();

	private static void setUp() {
		props.put("bootstrap.servers", "localhost:29092");
		props.put("group.id", "mygroupid-1");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "100");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
	}

	public static void main(final String[] args) throws IOException {

	    //Config Parameters
		setUp();
	    //Create consumer instance
	    KafkaConsumer<String, Integer> consumer = new KafkaConsumer<String, Integer>(props);

		//Subscription phase
		consumer.subscribe(Arrays.asList("outputTopic1"));
	     
	    while (true) {
	    	ConsumerRecords<String, Integer> records = consumer.poll(100);
	        for (ConsumerRecord<String, Integer> record : records)
				System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
	    }
  }

}