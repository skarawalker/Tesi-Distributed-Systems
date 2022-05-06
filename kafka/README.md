# Kafka Streaming
Le tre cartelle contengono rispettivamente i codici per il producer, il consumer e l'applicazione che gestisce lo stream.
L'esempio è un semplice word count:
- il producer pubblica in un primo topic dei messaggi contenuti all'interno di un array e con una chiave numerata;
- l'applicazione di streaming riceve i messaggi, conta il numero di parole, salva i risultati in un persistent store e li pubblica in un secondo topic;
- il consumer riceve dal secondo topic i risultati e li mostra a video.

Il file docker-compose.yml contiene la struttura del container e si può creare il container tramite comando:
```
docker-compose up -d
```
