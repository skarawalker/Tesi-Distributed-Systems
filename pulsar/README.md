## Apache Pulsar
Le tre cartelle contengono il codice per il producer, il consumer e una funzione per l'elaborazione dei dati dello streaming.
L'esempio è un semplice word count:
- il producer pubblica in un primo topic dei messaggi contenuti all'interno di un array e con una chiave numerata;
- la funzione di streaming riceve i messaggi, conta il numero di parole, salva i risultati in un persistent store e li pubblica in un secondo topic;
- il consumer riceve dal secondo topic i risultati e li mostra a video.
Differentemente da Apache Kafka, la funzione viene caricata nel broker e agisce su ogni messaggio che riceve in input dal primo topic.

I due file docker-compose presentano due diverse opzioni per l'architettura del cluster:
- il file docker-compose_standalone implementa tutti e 3 gli elementi principali del cluster (zookeeper, bookkeeper e broker) nello stesso container, non può essere espanso con ulteriori nodi ma non richiede ulteriori configurazioni per essere utilizzato;
- il file docker-compose_cluster implementa 3 container, uno per ogni elemento principale del cluster, può essere espanso ma richiede ulteriori procedure di configurazioni.
Le istruzioni di configurazione per la seconda opzione sono presenti nel file initialize.txt.
Per creare il cluster bisogna scaricare uno dei due file sopra indicati, rinominarlo in docker-compose.yml ed eseguire tramite terminale il comando:
```
docker-compose up -d
```
Per la seconda opzione è necessario scaricare anche il file DockerFile.
