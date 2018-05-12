FROM openjdk:8

#EXPOSE 8080

ADD build/install /opt

ENV PATH=/opt/connector/bin:$PATH
#ENV KAFKA_BOOTSTRAP_SERVERS="localhost:9092"

CMD ["connector"]