# bachelor-tramodana

Trace, Model, Analyse - bachelor project developed in Ubuntu 16.04

## Requirements

- Java 8 JDK: `sudo apt-get update && sudo install openjdk-8-jdk`
- SBT:

```shell
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
sudo apt-get update
sudo apt-get install sbt
```

## Installation

    1. 
    2.

## Cassandra data samples

[Cassanra docker images docs](https://hub.docker.com/_/cassandra/).  
Samples contains snapshot of cassandra db, with 3 traces from [Jorge's project](https://github.com/jeqo/poc-opentrancing-jvm)

Make sure that docker user has permissions r/w/e to folder ./cassandra/data-sample: `chmod -R 777 /cassandra`

1. Exec
`docker run --name cassandra-test-db -v <absolute path>/cassandra/data-sample:/var/lib/cassandra -d cassandra:3.9`

For me its
`docker run --name cassandra-test-db -v /home/nikita/IdeaProjects/bachelor-tramodana/cassandra/data-sample:/var/lib/cassandra -d cassandra:3.9`

2. Exec CQLSH inside container `docker exec -it <container_id> cqlsh`
3. Query DB

```mysql
DESCRIBE KEYSPACES;
USE jaeger_v1_dc1;
DESCRIBE TABLES;
SELECT * FROM traces;
```

I haven't find good UI for cassandra to visualize schemas, keyspaces, tables, etc...