import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.IntegerType;

import javax.xml.crypto.Data;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.col;

public class MyKafkaSparkStream {
    private static String appName = "MyKafkaSparkStream";
    private static String master = "spark://spark-master:7077[2]";
    final static String inputTopic="streamTopic1";
    final static String outputTopic="streamTopic2";
    final static String kafkaHost="172.29.0.4:9092";

    public static void main(String args[]) throws StreamingQueryException, TimeoutException {
        SparkSession spark = SparkSession
                .builder()
                .appName(appName)
                .getOrCreate();

        Dataset<Row> df = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", kafkaHost)
                .option("subscribe", inputTopic)
                .option("checkpointLocation", "/tmp/vaquarkhan/checkpoint")
                .load();
        Dataset<Row> lines = df.select(col("value").cast("String").as("value"));

        // Split the lines into words
        Dataset<String> words = lines
                .as(Encoders.STRING())
                .flatMap((FlatMapFunction<String, String>) x -> Arrays.asList(x.split(" ")).iterator(), Encoders.STRING());

        // Generate running word count
        Dataset<Row> wordCounts = words.groupBy("value").count();

        /*StreamingQuery query = wordCounts
                .writeStream()
                .outputMode("update")
                .format("console")
                .trigger(Trigger.ProcessingTime("0 seconds"))
                .start();
        query.awaitTermination();*/
        StreamingQuery ds = wordCounts
                .selectExpr("CAST(value AS STRING) as key","CAST(count AS BINARY) as value")
                .writeStream()
                .outputMode("complete")
                .format("kafka")
                .option("kafka.bootstrap.servers", kafkaHost)
                .option("topic", outputTopic)
                .option("checkpointLocation", "/tmp/vaquarkhan/checkpoint")
                .start();
        ds.awaitTermination();
    }
}
