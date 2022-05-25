import org.apache.spark.SparkConf;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.streaming.*;
import org.apache.spark.streaming.api.java.*;
import scala.Tuple2;

import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public final class MyWordCount {
	private static String output = "file:/opt/spark-data/word-count-result";
	private static String appName = "MyWordCount";
	private static String master = "spark://spark-master:7077[2]";

	public static void main(String[] args) throws InterruptedException, TimeoutException, StreamingQueryException {
		SparkConf conf = new SparkConf().setAppName(appName).setMaster("local[2]"); //.setMaster(master);
		//cambiare con il continuous
		try (JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(10))) {
			JavaRDD<Tuple2<String, Integer>> countList = jssc.sparkContext().emptyRDD();
			JavaReceiverInputDStream<String> lines = jssc.socketTextStream(
					"localhost", 9999, StorageLevels.MEMORY_AND_DISK_SER);
			JavaPairDStream<String, Integer> counts = lines
					.flatMap(x -> Arrays.asList(x.split(" ")).iterator())
					.mapToPair(s -> new Tuple2<String, Integer>(s, 1))
					.reduceByKey((a, b) -> a + b);
			counts.foreachRDD(rdd -> {
				System.out.println(rdd.collect());
			});
			jssc.start();
			jssc.awaitTermination();
		}
	}
}
