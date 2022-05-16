import org.apache.pulsar.client.api.*;

public class PulsarConsumer {
    public static void main(String[] args) throws PulsarClientException {
        PulsarClient client = PulsarClient.builder()
                .serviceUrl("pulsar://localhost:6650")
                .build();

        MessageListener myMessageListener = (consumer, msg) -> {
            try {
                System.out.println("Message received: " + new String(msg.getData()));
                consumer.acknowledge(msg);
            } catch (Exception e) {
                consumer.negativeAcknowledge(msg);
            }
        };

        Consumer consumer = client.newConsumer()
                .topic("persistent://public/default/my-topic")
                .subscriptionName("my-subscription")
                .messageListener(myMessageListener)
                .subscribe();
    }
}
