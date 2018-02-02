#!/usr/bin/env bash
CWD=`dirname $0`

CONFLUENT_VERSION=4.0.0

${CWD}/confluent-${CONFLUENT_VERSION}/bin/kafka-server-stop