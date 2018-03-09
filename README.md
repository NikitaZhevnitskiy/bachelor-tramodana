# bachelor-tramodana

Trace, Model, Analyse - bachelor project developed in Ubuntu 16.04

## Requirements

- Java 8 JDK: `sudo apt-get update && sudo apt-get install openjdk-8-jdk`
- Gradle: [install Gradle](https://gradle.org/install/)

## running the application
1. `docker-compose -f jaeger.yml up -d`  
2. Check containers status with `docker-compose -f jaeger.yml ps` that tracing-jaeger-query and tracing-jaeger-collector are UP. 
If NOT, exec `docker-compose -f jaeger.yml up -d`  one more time
3. Up Kafka with `docker-compose -f kafka.yml up -d`, takes around 2-3 mins
4. Generate traces with examples/[TraceGenApp1](https://github.com/NikitaZhevnitskiy/TraceGenApp1) 
or examples/[TraceGenApp2](https://github.com/NikitaZhevnitskiy/TraceGenApp2)
5. Run ConnectorApp
6. Check [localhost:3030](http://localhost:3030)

## Mock backend
Install [json-server](https://github.com/typicode/json-server)
1. Exec `json-server models.json --port 3004`
2. Check [https://localhost:3004](https://localhost:3004)
## Cassandra data samples

[Cassanra docker images docs](https://hub.docker.com/_/cassandra/).
Samples contains snapshot of cassandra db, with 3 traces from [Jorge's project](https://github.com/jeqo/poc-opentrancing-jvm)
Make sure that docker user has permissions r/w/e to folder ./cassandra/data-sample: `chmod -R 777 /cassandra`
1. Exec: `docker run --name cassandra-test-db -p9042:9042 -v "$(pwd)"/cassandra/cassandra:/var/lib/cassandra -d cassandra:3.9` 
2. Exec CQLSH inside container: `docker exec -it <container_id> cqlsh`
3. Query DB with:

```mysql
DESCRIBE KEYSPACES;
USE jaeger_v1_dc1;
DESCRIBE TABLES;
SELECT * FROM traces;
```

## Connector
Check [connector documentation](./cassandra/connector.md) and [db entities](./cassandra/entities.md)

