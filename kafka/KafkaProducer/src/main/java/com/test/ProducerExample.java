package com.test;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Serdes;

import java.io.IOException;
import java.util.Properties;


public class ProducerExample {

	final static String[] msgs = {"ciao come va", "io bene grazie te come va", "io bene grazie ciao", "ciao"};
	final static String topicName="streamTopic1";
	final static Properties props = new Properties();

	private static void setUp() {
		props.put("bootstrap.servers", "localhost:29092");
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("compression.type", "snappy");
		props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
	}

	public static void main(final String[] args) throws IOException {

		//Config Parameters
		setUp();
		//Create producer instance
	  	Producer<Integer, String> producer = new KafkaProducer<Integer, String>(props);

	    //Produce sample data
	  	int i = 0;
	    for (; i < msgs.length; i++) {
			Integer key = i;
			String value = msgs[i];
			System.out.printf("Producing record: Key: %s\tValue: %s%n", key, value);
			//Create message
			ProducerRecord<Integer, String> sendingRecord = new ProducerRecord<Integer, String>(topicName, key, value);
			//Send message
			producer.send(sendingRecord);
	    }

		producer.flush();
	    System.out.printf(i + " messages were produced to topic");
	    producer.close();

	}

}
