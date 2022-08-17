import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.Producer;

import java.util.Properties;

public class MyProducer {
    final static Properties props = new Properties();
    final static String[] msgs = {"ciao come va", "io bene grazie te come va", "io bene grazie ciao", "ciao"};

    final static String appName = "MyProducer";
    final static String topicName = "ProvaTopic";
    final static String adminAddress = "localhost:8080";

    public static void main(String[] args){
        ApplicationManager app = new ApplicationManager(appName, topicName, adminAddress);
        app.connect("0");
        setUp();
        Producer<Integer, String> producer = new org.apache.kafka.clients.producer.KafkaProducer<Integer, String>(props);
        while(true) {
            int i = 0;
            for (String e : msgs) {
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
