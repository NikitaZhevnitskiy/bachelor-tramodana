#!/usr/bin/env bash

# http://packages.confluent.io/archive/4.0/confluent-oss-4.0.0-2.11.zip

CONFLUENT_MAJOR_VERSION=4.0
CONFLUENT_VERSION=4.0.0
KAFKA_SCALA_VERSION=2.11

CWD=`dirname $0`

wget http://packages.confluent.io/archive/${CONFLUENT_MAJOR_VERSION}/confluent-oss-${CONFLUENT_VERSION}-${KAFKA_SCALA_VERSION}.tar.gz -P ${CWD}

#confluent-oss-4.0.0-2.11.tar.gz
tar -xzf ${CWD}/confluent-oss-${CONFLUENT_VERSION}-${KAFKA_SCALA_VERSION}.tar.gz -C ${CWD}

rm ${CWD}/confluent-oss-${CONFLUENT_VERSION}-${KAFKA_SCALA_VERSION}.tar.gz*