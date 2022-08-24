import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

public class MyConsumer {
    final static Properties props = new Properties();
    final static String[] msgs = {"ciao come va", "io bene grazie te come va", "io bene grazie ciao", "ciao"};

    final static String appName = "MyConsumer";
    final static String topicName = "ProvaTopic";
    final static String adminAddress = "localhost:8080";

    public static void main(String[] args){
        ApplicationManager app = new ApplicationManager(appName, topicName, adminAddress);
        app.connect("0");

        //Config Parameters
        setUp();

        KafkaConsumer<String, Long> consumer = new KafkaConsumer<String, Long>(props);

        long offset = app.getOffset();
        int partition = app.getPartition();
        System.out.println(partition);
        ConsumerRecords<String, Long> records = null;
        if(partition==-1) {
            //Subscription phase
            consumer.subscribe(Arrays.asList(topicName));
            records = consumer.poll(100);
            Set<TopicPartition> topicPartition = consumer.assignment();
            consumer.seek((TopicPartition) topicPartition.stream().toArray()[0], offset);
            partition = ((TopicPartition) topicPartition.stream().toArray()[0]).partition();
            System.out.println("got partition");
            app.updatePartition(partition);
            System.out.println("partition updated");
        } else {
            TopicPartition topicPartition = new TopicPartition(topicName, partition);
            consumer.assign(Arrays.asList(topicPartition));
            records = consumer.poll(100);
            consumer.seek(topicPartition, offset);
        }

        System.out.println("Consuming from: partition "+partition+" and offset "+offset);


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
