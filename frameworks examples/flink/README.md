# Apache Flink
Il codice implementa un esempio di wordcount.
Il file docker-compose.yml contiene la struttura del container e si può creare il container tramite comando:
```
docker-compose up -d
```
Il file jar si trova in questa cartella e per eseguirlo bisogna aprire un terminale di un worker e scrivere il comando:
```
/usr/local/flink/bin/flink run --class MyWordCount /opt/flink-data/MyWordCount.jar --hostname <ip_host> --port 9000
```
Come argomento da passare bisogna inserire l'indirizzo IP del worker da cui si inviano i messaggi. Per vedere l'indirizzo IP bisogna aprire un terminale e controllare nel file /etc/hosts il valore vicino alla stringa localhost.
Per inviare messaggi al servizio, scrivere nello stesso terminale usato per controllare l'indirizzo IP il seguente comando:
```
nc -l -vv -9000
```
L'output dei risultati non viene stampato nel terminale in cui viene eseguito il servizio, ma si trova nel file flink-root-taskexecutor-0-<worker_name>.out all'interno del worker che ha eseguito il file jar nella cartella /usr/local/flink/log. Il l'identificativo del worker che ha eseguito il servizio e che gli è stato dato nel file docker-compose.yml.
