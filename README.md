# bachelor-tramodana

Trace, Model, Analyse - bachelor project developed in Ubuntu 16.04

## Requirements

- Java 8 JDK: `sudo apt-get update && sudo install openjdk-8-jdk`
- Gradle: [install Gradle](https://gradle.org/install/)

## Installation & running the application

1. in the root directory (_bachelor-tramodana_), run `./gradlew build`
2. after Gradle has downloaded all dependencies, run `./gradlew run`
3. [**TODO**: set up Gradle to run tests]

## Cassandra data samples

[Cassanra docker images docs](https://hub.docker.com/_/cassandra/).
Samples contains snapshot of cassandra db, with 3 traces from [Jorge's project](https://github.com/jeqo/poc-opentrancing-jvm)

Make sure that docker user has permissions r/w/e to folder ./cassandra/data-sample: `chmod -R 777 /cassandra`

1. Exec: `docker run --name cassandra-test-db -v "$(pwd)"/cassandra/data-sample:/var/lib/cassandra -d cassandra:3.9` 
2. Exec CQLSH inside container: `docker exec -it <container_id> cqlsh`
3. Query DB with:

```mysql
DESCRIBE KEYSPACES;
USE jaeger_v1_dc1;
DESCRIBE TABLES;
SELECT * FROM traces;
```

I haven't find good UI for cassandra to visualize schemas, keyspaces, tables, etc...
