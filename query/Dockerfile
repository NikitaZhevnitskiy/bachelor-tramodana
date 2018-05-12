FROM openjdk:8

# todo: not exposed (check --network=host)
EXPOSE 8888
ADD build/install /opt

ENV PATH=/opt/query/bin:$PATH
#ENV TRAMODANA_QUERY_PORT=8888
#ENV KAFKA_BOOTSTRAP_SERVERS="localhost:9092"

CMD ["query"]