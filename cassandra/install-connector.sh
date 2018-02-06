#!/usr/bin/env bash

# https://github.com/Landoop/stream-reactor/releases/download/1.0.0/kafka-connect-cassandra-1.0.0-1.0.0-all.tar.gz

CWD=cassandra-source-connector
wget https://github.com/Landoop/stream-reactor/releases/download/1.0.0/kafka-connect-cassandra-1.0.0-1.0.0-all.tar.gz -P ${CWD}
#confluent-oss-4.0.0-2.11.tar.gz
tar -xzf ${CWD}/kafka-connect-cassandra-1.0.0-1.0.0-all.tar.gz -C ${CWD}
rm ${CWD}/kafka-connect-cassandra-1.0.0-1.0.0-all.tar.gz*
