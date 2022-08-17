import org.apache.pulsar.functions.api.*;

public class PulsarFunction implements Function<String, String> {
    @Override
    public String process(String input, Context context) {
        return String.format("%s!", input);
    }
}
