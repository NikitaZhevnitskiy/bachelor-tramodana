# bachelor-tramodana

Trace, Model, Analyse - bachelor project developed in Ubuntu 16.04

## Requirements

- Java 8 JDK: `sudo apt-get update && sudo apt-get install openjdk-8-jdk`
- Gradle: [install Gradle](https://gradle.org/install/)

## running the application

1. in the root directory (_bachelor-tramodana_), run `./gradlew build`. If you want to skip tests, append `-x test`
2. after Gradle has downloaded all dependencies, run `./gradlew run`
3. run `./gradlew test` to test all modules, and `./gradlew clean` to remove all compilation files (the _build_ folders)
4. to run a specific task for a specific project, follow the pattern `./gradlew  :<task>:<path-to-module>` - e.g: `./gradlew :module1:run` or `./gradlew :app:test`

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

I haven't find good UI for cassandra to visualize schemas, keyspaces, tables, etc...


## Connector
`https://lenses.stream/connectors/source/cassandra.html`  
```
docker run -e ADV_HOST=127.0.0.1 -e EULA="https://dl.lenses.stream/d/?id=ae1d758a-18ef-4c52-b5d7-71bdeb501a54" -e SAMPLEDATA=0 --rm -p 3030:3030 -p 9092:9092 -p 2181:2181 -p 8081:8081 -p 9581:9581 -p 9582:9582 -p 9584:9584 -p 9585:9585 landoop/kafka-lenses-dev
```

