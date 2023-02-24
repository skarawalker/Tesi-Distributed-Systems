# Tesi-Distributed-Systems
In questo repository è contenuto un progetto per la gestione di un sistema distribuito di applicazioni basate su Apache Kafka e Apache Spark.
Il lavoro si divide in 3 parti:
- **Cluster Manager**, applicazione sviluppata in Java Spring Boot per la gestione dei topic in Apache Kafka, la connessione delle altre applicazioni ai topic e salvare lo stato delle applicazioni durante la lettura/scrittura su topic;
- **Docker Manager**, applicazione sviluppata in Python per la gestione dei container contenenti Apache Kafka, Apache Spark e delle applicazioni, utilizzato per controllare lo stato dei container, riavviare i container in caso di anomalia e recuperare le statistiche di ogni container;
- **Anomaly Detection**, applicazione sviluppata in Python per lo studio delle statistiche dei container all'interno del cluster e cercare di individuare un'anomalia hardware tramite un algoritmo di machine learning.

Nella cartella *Client Test* sono contenuti i codici di un semplice Consumer e Producer per testare il funzionamento del progetto.
Nella cartella *Frameworks Exmaple* sono contenuti i codici di esemnpio di utilizzo di Apache Kafka, Apache Spark e di altri framework per lo sviluppo di applicazioni distribuite (pulsar e flink).
