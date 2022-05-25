# Apache Spark
Nella cartella "Examples" si trovano due esempi di implementazione del wordcount:
- in MyMicroBatchStream si trova l'implementazione tramite la classe di default di Apache Spark per la gestione degli streams, in cui l'input stream viene suddiviso in batches e poi elaborato.
- in MyStructuredStream si trova l'implementazione tramite la classe secondaria di Apache Spark per la gestione degli stream, in cui l'input stream può essere gestito in maniera continua o in batches, in particolare in questo esempio viene gestita in maniera continua.
Il file docker-compose.yml contiene la struttura del container e si può creare il container tramite comando:
```
docker-compose up -d
```
Prima di eseguire il codice è consigliato copiare il file log:
```
cp /opt/spark-data/log4j.properties /spark/conf/log4j.properties
```
I file jar dei due esempi si trovano nella cartella "jar". Per avviare il servizio, aprire il terminale di uno dei 3 worker e scrivere il comando:
```
/spark/bin/spark-submit --class MyWordCount --master spark://spark-master:7077 /opt/spark-data/jars/MyMicroBatchStream.jar
/spark/bin/spark-submit --class MyWordCount --master spark://spark-master:7077 /opt/spark-data/jars/MyStructuredStream.jar
```
Per inviare messaggi al servizio, aprire un altro terminale del worker e scrivere il comando:
```
nc -l -vv -p 9999
```
