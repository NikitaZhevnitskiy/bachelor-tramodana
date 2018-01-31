# bachelor-tramodana
Trace, Model, Analyse - bachelor project


# Cassandra data samples
[Cassanra docker images docs](https://hub.docker.com/_/cassandra/).  
Samples contains snapshot of cassandra db, with 3 traces from [Jorge's project](https://github.com/jeqo/poc-opentrancing-jvm)

Make sure that docker user has permissions r/w/e to folder ./cassandra/data-sample 
`chmod -R 777 /cassandra`  
1. Exec 
`docker run --name cassandra-test-db -v <absolute path>/cassandra/data-sample:/var/lib/cassandra -d cassandra:3.9`  

For me its `docker run --name cassandra-test-db -v /home/nikita/IdeaProjects/bachelor-tramodana/cassandra/data-sample:/var/lib/cassandra -d cassandra:3.9`

2. Exec CQLSH inside container 
`docker exec -it <container_id> cqlsh`

3. Query DB
```mysql
DESCRIBE KEYSPACES;
USE jaeger_v1_dc1;
DESCRIBE TABLES;
SELECT * FROM traces;

```

I haven't find good UI for cassandra to visualize schemas, keyspaces, tables, etc...

