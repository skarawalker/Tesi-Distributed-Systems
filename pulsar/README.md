# Apache Pulsar
Le tre cartelle contengono il codice per il producer, il consumer e una funzione per l'elaborazione dei dati dello streaming.
L'esempio è un semplice word count:
- il producer pubblica in un primo topic dei messaggi contenuti all'interno di un array e con una chiave numerata;
- la funzione di streaming riceve i messaggi, conta il numero di parole, salva i risultati in un persistent store e li pubblica in un secondo topic;
- il consumer riceve dal secondo topic i risultati e li mostra a video.
Differentemente da Apache Kafka, la funzione viene caricata nel broker e agisce su ogni messaggio che riceve in input dal primo topic.

I due file docker-compose presentano due diverse opzioni per l'architettura del cluster:
- il file docker-compose_standalone implementa tutti e 3 gli elementi principali del cluster (zookeeper, bookkeeper e broker) nello stesso container, non può essere espanso con ulteriori nodi ma non richiede ulteriori configurazioni per essere utilizzato;
- il file docker-compose_cluster implementa 3 container, uno per ogni elemento principale del cluster, può essere espanso ma richiede ulteriori procedure di configurazioni.
Per creare il cluster bisogna scaricare uno dei due file sopra indicati, rinominarlo in docker-compose.yml ed eseguire tramite terminale il comando:
```
docker-compose up -d
```
Per la seconda opzione è necessario scaricare anche il file DockerFile.

## Configurazione cluster di Apache Pulsar
1. Aprire un terminale per ogni container
2. Estrarre gli indirizzi IP dei 3 container tramite comando ``` cat /etc/hosts ```
3. Nel terminale di zookeeper:
    1. cd /pulsar
    2. mkdir -p data/zookeeper
    3. echo 1 > data/zookeeper/myid
    4. /pulsar/bin/pulsar-daemon start zookeeper (CHECK dell'operazione: nano logs/pulsar-zookeeper-zookeper.log)
    5. /pulsar/bin/pulsar initialize-cluster-metadata --cluster pulsar-cluster-1 --zookeeper <indirizzo_ip_zookeeper>:<n_porta_zookeeper> --configuration-store <indirizzo_ip_zookeeper>:<n_porta_zookeeper> --web-service-url http://<indirizzo_ip_broker>:8080
4. Nel terminale di bookkeeper:
    1. cd /pulsar
    2. nano conf/bookkeeper.conf
    3. modificare valore di zkServers in zkServer=<indirizzo_ip_zookeeper>:<n_porta_zookeeper>
    4. /pulsar/bin/pulsar-daemon start bookie (CHECK dell'operazione: nano logs/pulsar-bookie-bookkeeper.log)
5. Nel terminale di broker:
    1. cd /pulsar
    2. nano conf/broker.conf
    3. modificare valore di zookeeperServers in zookeeperServers=<indirizzo_ip_zookeeper>:<n_porta_zookeeper>
    4. modificare il valore di clusterName in clusterName=pulsar-cluster-1
    5. modificare il valore di functionsWorkerEnabled in functionsWorkerEnabled=true
    6. NEL CASO DI SINGOLO NODO: modificare le seguenti variabili con i relatvi valori indicati
        - managedLedgerDefaultEnsembleSize=1
        - managedLedgerDefaultWriteQuorum=1
        - managedLedgerDefaultAckQuorum=1
    7. nano conf/functions_worker.yml
    8. modificare il valore di pulsarFunctionsCluster in pulsarFunctionsCluster: pulsar-cluster-1
    9. /pulsar/bin/pulsar-daemon start broker (CHECK dell'operazione: nano logs/pulsar-broker-broker.log)
Per controllare l'avvenuta configurazione eseguire nel terminale del broker il comando:
```
/pulsar/bin/pulsar-client consume persistent://public/default/test -n 100 -s "consumer-test" -t "Exclusive"
```
poi aprire un altro terminale del broker ed eseguire il comando:
```
/pulsar/bin/pulsar-client produce persistent://public/default/test -n 1 -m "Hello Pulsar"
```
