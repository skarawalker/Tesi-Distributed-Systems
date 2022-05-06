package com.test;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.Producer;
import java.io.IOException;
import java.util.Properties;


public class ProducerExample {


  public static void main(final String[] args) throws IOException {

	  //Config Parameters
	  Properties props = new Properties();
      props.put("bootstrap.servers", "localhost:29092");
	  props.put("acks", "all");
	  props.put("retries", 0);
	  props.put("compression.type", "snappy");
	  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
	  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
	  
	  //Create a producer instance
      Producer<String, String> producer = new KafkaProducer<String, String>(props);

	    // Produce sample data
	    int numMessages = 10;
	    String topicName="mytopic";
	    for (int i = 0; i < numMessages; i++) {
	    	
	      String padding ="";
	      if (i<10) padding ="00";
	      else if (i<100) padding ="0";
	      
	      String key = padding + i;
	      String value="Msg n. " + padding + i;
	
	      System.out.printf("Producing record: Key: %s\tValue: %s%n", key, value);
	      
	      //Create a kafka message...
	      ProducerRecord<String, String> sendingRecord = new ProducerRecord<String, String>(topicName, key, value);
	      
	      //...and send it!
	      producer.send(sendingRecord);
	      
	    }
	
	    producer.flush();
	
	    System.out.printf(numMessages + " messages were produced to topic");
	
	    producer.close();
	  }

}
