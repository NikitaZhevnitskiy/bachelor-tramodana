FROM openjdk:8

ADD build/install /opt

ENV PATH=/opt/builder/bin:$PATH
#ENV KAFKA_BOOTSTRAP_SERVERS="localhost:9092"

CMD ["builder"]