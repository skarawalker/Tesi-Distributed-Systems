import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.protocol.types.Field;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class KafkaProducer {
    //Variable for Kafka
    final static Properties props = new Properties();
    //Variables for the communication
    final static int partitions = 1;
    final static short repFactor = 1;
    //Messages to send
    final static String[] msgs = {"ciao come va", "io bene grazie te come va", "io bene grazie ciao", "ciao"};
    public static void main(String[] args) {
        String topicName = "stream1";
        String appName = "app1";
        ApplicationManager app = null;
        try{
            app = new ApplicationManager("localhost", 9991);
            app.initApp(appName, topicName, "produce");
            app.connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //Config Parameters
        setUp();
        //Create producer instance
        Producer<Integer, String> producer = new org.apache.kafka.clients.producer.KafkaProducer<Integer, String>(props);
        while(true) {
            int i = 0;
            for (String e : msgs) {
                app.imhere();
                System.out.printf("Producing record: Key: %s\tValue: %s%n", i, e);
                //Create message
                ProducerRecord<Integer, String> sendingRecord = new ProducerRecord<Integer, String>(topicName, i, e);
                //Send message
                producer.send(sendingRecord);
                producer.flush();
                i++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }
        //producer.close();
        //app.disconnect();
    }
    private static void setUp() {
        props.put("bootstrap.servers", "localhost:29092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("compression.type", "snappy");
        props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    }


}
