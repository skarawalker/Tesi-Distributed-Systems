import org.apache.pulsar.client.api.*;

import java.nio.charset.StandardCharsets;

public class PulsarProducer {
    public static void main(String[] args) throws PulsarClientException {
        PulsarClient client = PulsarClient.builder()
                .serviceUrl("pulsar://localhost:6650")
                .build();
        Producer<byte[]> producer = client.newProducer()
                .topic("persistent://public/default/my-topic")
                .create();
        producer.send("My message".getBytes());
        producer.close();
        client.close();
    }
}
