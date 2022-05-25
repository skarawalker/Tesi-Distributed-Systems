import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.StreamingQueryListener;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.streaming.*;
import org.apache.spark.streaming.api.java.*;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public final class MyWordCount {
	private static String output = "file:/opt/spark-data/word-count-result";
	private static String appName = "MyWordCount";
	private static String master = "spark://spark-master:7077[2]";

	public static void main(String[] args) throws TimeoutException, StreamingQueryException, InterruptedException, IOException {

		SparkSession spark = SparkSession
				.builder()
				.appName(appName)
				.getOrCreate();

		Dataset<Row> lines = spark
				.readStream()
				.format("socket")
				.option("host", "localhost")
				.option("port", 9999)
				.load();

		// Split the lines into words
		Dataset<String> words = lines
				.as(Encoders.STRING())
				.flatMap((FlatMapFunction<String, String>) x -> Arrays.asList(x.split(" ")).iterator(), Encoders.STRING());

		// Generate running word count
		Dataset<Row> wordCounts = words.groupBy("value").count();

		StreamingQuery query = wordCounts.writeStream()
				.outputMode("update")
				.format("console")
				.trigger(Trigger.ProcessingTime("0 seconds"))
				.start();
		query.awaitTermination();
	}
}
