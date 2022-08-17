import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class MyConsumer {
    //Variable for Kafka
    final static Properties props = new Properties();
    //Variables for the communication
    final static String applicationName = "consumer1";
    final static String topicName = "stream1";
    final static int partitions = 1;
    final static short repFactor = 1;
    //Messages to send
    final static String[] msgs = {"ciao come va", "io bene grazie te come va", "io bene grazie ciao", "ciao"};
    public static void main(String[] args) throws Exception {
        ApplicationManager app = null;
        try {
            app = new ApplicationManager("localhost", 9991);
            app.initApp(applicationName, topicName, "consume");
            app.connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Long offset = app.getOffset();
        System.out.println("start from "+offset);

        //Config Parameters
        setUp();
        //Create consumer instance
        KafkaConsumer<String, Long> consumer = new KafkaConsumer<String, Long>(props);

        int partition = app.getPartition();
        ConsumerRecords<String, Long> records = null;
        if(partition==-1) {
            //Subscription phase
            consumer.subscribe(Arrays.asList(topicName));
            records = consumer.poll(100);
            Set<TopicPartition> topicPartition = consumer.assignment();
            consumer.seek((TopicPartition) topicPartition.stream().toArray()[0], offset);
            System.out.println(((TopicPartition) topicPartition.stream().toArray()[0]).partition());
        } else {
            TopicPartition topicPartition = new TopicPartition(topicName, partition);
            consumer.assign(Arrays.asList(topicPartition));
            records = consumer.poll(100);
            consumer.seek(topicPartition, offset);
        }


        while (true) {
            records = consumer.poll(100);
            for (ConsumerRecord<String, Long> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                app.updateOffset(record.offset());
            }
        }
    }
    private static void setUp() {
        props.put("bootstrap.servers", "localhost:29092");
        props.put("group.id", "mygroupid-1");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "100");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    }
}
