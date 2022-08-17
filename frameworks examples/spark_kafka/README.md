# Kafka-Spark
In questa cartella è implementato un semplice esempio di collegamento tramite i nodi di Apache Kafka e Apache Spark. Il codice di esempio contenuto nelle due cartelle "kafka_programs" e "spark_programs" è quello del word count e funziona similmente a quello presente nelle cartelle relative ad Apache Spark e Apache Kafka:
- Nella cartella "kafka_programs" sono presenti il producer (che pubblica in un topic una serie di stringhe) e il consumer (che si iscrive al topic per ricevere delle tuple di parola-conteggio);
- Nella cartella "spark_programs" è presente un programma che si iscrive al topic dove il producer pubblica le stringhe, conta il numero di parole e salva il conteggio in un persistence storage, pubblica il conteggio nel topci dove è iscritto il consumer.
Per eseguire il codice prima bisogna eseguire il seguente comando per creare la rete di cluster:
```
docker-compose up -d
```
Prima di eseguire il codice conviene aprire il terminale dello spark master ed eseguire il comando:
```
cp /opt/spark-data/log4j.properties /spark/conf/log4j.properties
```
Aprire poi il terminale del kafka-1 ed eseguire il comando ``` cat /etc/hosts ``` ed estrarre l'indirizzo IP del worker e modificare quello presente nel codice del wordcount di spark. Dopo questa modifica bisogna rieseguire la build del file jar del progetto e conviene inserirla nella cartella "spark_env" per averlo visibile all'interno del master node di spark.
Per eseguire il programma del wordcount bisogna scrivere il seguente comando nel terminale del worker:
```
/spark/bin/spark-submit --class MyKafkaSparkStream --master spark://spark-master:7077 --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.1.1 /opt/spark-data/KafkaStream.jar
```
Poi eseguire il codice del producer e del consumer.
