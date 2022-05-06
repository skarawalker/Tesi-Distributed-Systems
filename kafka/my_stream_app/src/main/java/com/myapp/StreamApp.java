/* Demo di app di streaming di Luca Scaramuzza */

package com.myapp;

import java.util.Arrays;
import java.util.Properties;

//common libraries
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
//consumer libraries
import org.apache.kafka.clients.consumer.ConsumerConfig;

//stream libraries
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.Topology;

public class StreamApp{
    private static final String inputStream = "streamTopic1";
    private static final String outputStream = "outputTopic1";

    private static Properties streamsConfiguration = new Properties();

    private static void setUp() {
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    }

    private static void wordCountStream() throws InterruptedException{
        final StreamsBuilder builder = new StreamsBuilder();

        //Read data
        KStream<String, String> lines = builder.stream(inputStream, Consumed.with(Serdes.String(), Serdes.String()));

        //Data store
        Materialized<String, Long, KeyValueStore<Bytes, byte[]>> store = Materialized.as("counts-store");

        //Map-Reduce
        KTable<String, Long> wordCounts = lines.flatMapValues(value -> Arrays.asList(value.split("\\W+"))).map((key, word) -> KeyValue.pair(word,word))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .count(store);
        //Print result
        wordCounts.toStream().foreach((word, count) -> System.out.println("Word: " + word + " -> " + count));
        //Set the app ID
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "count-after-split-1");
        
        //Create the stream
        final Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, streamsConfiguration);

        streams.cleanUp();
        streams.start();

        Thread.sleep(60000);
        streams.close();
    }

    public static void main(String args[]) throws InterruptedException{
        setUp();
        wordCountStream();
    }
}