import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.client.api.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class PulsarProducer {
    public static void main(String[] args) throws PulsarClientException, UnknownHostException, PulsarAdminException {
        PulsarClient client = PulsarClient.builder()
                .serviceUrl("pulsar://localhost:6650")
                .build();
        Producer<byte[]> producer = client.newProducer()
                .topic("persistent://public/default/test")
                .create();
        producer.send("My message".getBytes());
        System.out.println("Message sent");
        producer.close();
        client.close();
    }
}
