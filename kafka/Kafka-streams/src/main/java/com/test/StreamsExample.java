package com.test;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;


import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;


//Michele Stecca

//Original version taken from this repository:
//https://github.com/eugenp/tutorials/blob/master/apache-kafka/src/test/java/com/baeldung/kafka/streamsvsconsumer/KafkaStreamsLiveTest.java

public class StreamsExample {

    private static final String LEFT_TOPIC = "left-stream-topic";
    private static final String RIGHT_TOPIC = "right-stream-topic";

    private static Properties streamsConfiguration = new Properties();

    static final String TEXT_LINES_TOPIC = "TextLinesTopic";
	
    //System configuration
    public static void setUp() {
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    }
 
    //Apply the toUpperCase() method to all values & filter (Stateless)
    public static void simpleStream() throws InterruptedException {
        String topicName = "streamTopic1";

        //Read data: topic name + Serdes
        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream(topicName,
          Consumed.with(Serdes.String(), Serdes.String()));

        final KStream<String, String> textLinesUpperCase =
          textLines
            .map((key, value) -> KeyValue.pair(value, value.toUpperCase()));
            //.filter((key, value) -> value.contains("FILTER"));
        
        textLinesUpperCase.foreach((word, capital) -> System.out.println("Word: " + word + " -> " + capital));
 
        textLinesUpperCase.to("outputTopic1",
                Produced.with(Serdes.String(), Serdes.String()));
        
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "capital-filter-map-id");
        
        final Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, streamsConfiguration);

        streams.cleanUp();
        streams.start();

        Thread.sleep(120000);
        
        streams.close();

    }
  
    //Count splitted words (Stateful)
    public static void countSplittedText() throws InterruptedException {
        String wordCountTopic = "wordCountTopic";

        //Read data: topic name + Serdes
        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream(wordCountTopic,
          Consumed.with(Serdes.String(), Serdes.String()));
 
        //Data store definition
        Materialized<String, Long, KeyValueStore<Bytes, byte[]>> myStore = Materialized.as("counts-store");
        
        KTable<String, Long> wordCounts = textLines
                .flatMapValues(value -> Arrays.asList(value.split("\\W+")))//Split words. E.g., <1,"hello world"> --> <1,"hello">,<1,"world">
                .map((key, word) -> KeyValue.pair(word,word))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .count(myStore);

        wordCounts.toStream().foreach((word, count) -> System.out.println("Word: " + word + " -> " + count));

        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "count-after-split-1");
        final Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, streamsConfiguration);

        streams.cleanUp();
        streams.start();

        Thread.sleep(60000);
        streams.close();

        /*Alternative solution
        KTable<String, Long> wordCounts = textLines
          .flatMapValues(value -> Arrays.asList(value.split("\\W+")))//Split words
          .groupBy((key, word) -> word)
          .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>> as("counts-store"));
		*/
        
    }
 
    //Join + Window 
    public static void windowingJoinStatefulTransformations() throws InterruptedException {
       
       final StreamsBuilder builder = new StreamsBuilder();

       KStream<String, String> leftSource = builder.stream(LEFT_TOPIC);
       KStream<String, String> rightSource = builder.stream(RIGHT_TOPIC);

        KStream<String, String> leftRightSource = leftSource.outerJoin(rightSource,
         (leftValue, rightValue) -> "left=" + leftValue + ", right=" + rightValue,
         JoinWindows.of(Duration.ofSeconds(10)))
           .groupByKey()
           .reduce(((key, lastValue) -> lastValue))
           .toStream();

        leftRightSource.foreach((key, value) -> System.out.println("(key= " + key + ") -> (" + value + ")"));

        final Topology topology = builder.build();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "windowing-join-id");
        KafkaStreams streams = new KafkaStreams(topology, streamsConfiguration);

        streams.cleanUp();
        streams.start();

        Thread.sleep(300000);
        streams.close();
    }
    
    
	  public static void main(final String[] args) throws InterruptedException {

		  setUp();
		  simpleStream();
		  //countSplittedText(); 
		  //windowingJoinStatefulTransformations();

	  }
	
}

