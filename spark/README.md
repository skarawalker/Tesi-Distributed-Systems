# Apache Spark
Il codice implementa un esempio di wordcount.
Il file docker-compose.yml contiene la struttura del container e si può creare il container tramite comando:
```
docker-compose up -d
```
Prima di eseguire il codice è consigliato copiare il file log:
```
cp /opt/spark-data/log4j.properties /spark/conf/log4j.properties
```
Il file jar si trova nella cartella wordcout/out. Per avviarlo nel container si esegue il comando:
```
/spark/bin/spark-submit --class MyWordCount --master spark://spark-master:7077 /opt/spark-data/wordcount.jar
```
