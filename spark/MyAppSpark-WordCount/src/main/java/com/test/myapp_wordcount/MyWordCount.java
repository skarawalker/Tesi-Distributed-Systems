package com.test.myapp_wordcount;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import org.apache.spark.SparkConf;

import java.util.Arrays;

public class MyWordCount 
{

    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("wordcount").setMaster("spark-master:7077");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> textFile = sc.textFile("/opt/spark-data/testodiprova.txt");
        JavaPairRDD<String, Integer> counts = textFile
            .flatMap(s -> Arrays.asList(s.split(" ")).iterator())
            .mapToPair(word -> new Tuple2<>(word, 1))
            .reduceByKey((a, b) -> a + b);
        counts.saveAsTextFile("/opt/spark-data/word-count-result");
    }
}
