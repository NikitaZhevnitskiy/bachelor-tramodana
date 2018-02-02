job "infosak-kafka" {

  datacenters = ["dc1"]

  group "cluster" {
    count = 1

    ephemeral_disk {
      migrate = true
      sticky  = true
    }

    task "broker" {
      driver = "raw_exec"

      template {
        data = <<EOH
        ############################# Server Basics #############################
        broker.id=0
        #listeners=PLAINTEXT://:9092
        #advertised.listeners=PLAINTEXT://your.host.name:9092
        #listener.security.protocol.map=PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL
        num.network.threads=3
        num.io.threads=8
        socket.send.buffer.bytes=102400
        socket.receive.buffer.bytes=102400
        socket.request.max.bytes=104857600

        ############################# Log Basics #############################
        log.dirs=local/kafka-logs
        num.partitions=1
        num.recovery.threads.per.data.dir=1

        ############################# Internal Topic Settings  #############################
        offsets.topic.replication.factor=1
        transaction.state.log.replication.factor=1
        transaction.state.log.min.isr=1

        ############################# Log Retention Policy #############################
        log.retention.hours=168
        log.segment.bytes=1073741824
        log.retention.check.interval.ms=300000

        ############################# Zookeeper #############################
        zookeeper.connect=infosak-zookeeper-cluster-node.service.consul:2181
        zookeeper.connection.timeout.ms=6000

        ##################### Confluent Metrics Reporter #######################

        ##################### Confluent Proactive Support ######################
        confluent.support.metrics.enable=false

        ############################# Group Coordinator Settings #############################
        group.initial.rebalance.delay.ms=0
        confluent.support.customer.id=anonymous
        EOH

        destination = "local/server.properties"
      }

      artifact {
        source = "http://packages.confluent.io/archive/4.0/confluent-oss-4.0.0-2.11.zip"
      }

      config {
        command = "local/confluent-4.0.0/bin/kafka-server-start"
        args = [
          "local/server.properties"]
      }

      service {
        tags = ["confluent", "infosak", "kafka", "broker"]

        port = "broker"

        check {
          type = "tcp"
          port = "broker"
          interval = "10s"
          timeout = "2s"
        }
      }

      resources {
        network {
          port "broker" {
            static = "9092"
          }
        }
      }
    }
  }
}
