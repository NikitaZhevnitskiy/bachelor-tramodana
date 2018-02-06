# How to use
From ./cassandra folder

1. Assume that data-samples locates in <project-folder>/cassandra/cassandra
2. `docker-compose up -d` - w8t for 3-5 mins
3. `docker-compose ps` - check that all containers are up
4. `docker-compose exec kafka-cluster bash` - connect to kafka-cluster bash
5. `kafka-topics --create --topic traces --partitions 3 --replication-factor 1 --zookeeper 127.0.0.1:2181` - create traces topic
6. Go to [localhost:3030](http://localhost:3030/)
7. Connectors -> New -> Sources -> Cassandra -> Replace with  -> Create
``` 
name=cassandra-source-connector
connector.class=com.datamountaineer.streamreactor.connect.cassandra.source.CassandraSourceConnector
tasks.max=1
connect.cassandra.key.space=jaeger_v1_dc1
connect.cassandra.kcql=INSERT INTO traces SELECT * FROM traces PK created
connect.cassandra.contact.points=cassandra
connect.cassandra.port=9042
```   

Check `traces` topic on [localhost:3030](http://localhost:3030/kafka-topics-ui/#/)


### KCQL (examples)
```sql
INSERT INTO kafka_topic SELECT FIELD FROM cassandra_table [PK FIELD] [INCREMENTALMODE=TIMESTAMP|TIMEUUID|TOKEN]

-- Select all columns from table orders and insert into a topic called orders-topic, use column created to track new rows.
-- Incremental mode set to TIMEUUID
INSERT INTO orders-topic SELECT * FROM orders PK created INCREMENTALMODE=TIMEUUID

-- Select created, product, price from table orders and insert into a topic called orders-topic, use column created to track new rows.
INSERT INTO orders-topic SELECT created, product, price FROM orders PK created.
```

