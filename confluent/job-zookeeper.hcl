job "infosak-zookeeper" {

  datacenters = ["dc1"]

  group "cluster" {
    count = 1

    ephemeral_disk {
      migrate = true
      sticky  = true
    }

    task "node" {
      driver = "raw_exec"

      template {
        data = <<EOH
        dataDir=local/zookeeper
        clientPort=2181
        maxClientCnxns=0
        EOH

        destination = "local/zookeeper.properties"
      }

      artifact {
        source = "http://packages.confluent.io/archive/4.0/confluent-oss-4.0.0-2.11.zip"
      }

      config {
        command = "local/confluent-4.0.0/bin/zookeeper-server-start"
        args = [
          "local/zookeeper.properties"]
      }

      service {
        tags = ["confluent", "infosak", "zookeeper"]

        port = "zk"

        check {
          type     = "script"
          command  = "/bin/bash"
          args     = ["-c", "echo stat | nc localhost 2181"]
          interval = "10s"
          timeout  = "5s"

          #check_restart {
          #  limit = 3
          #  grace = "90s"
          #  ignore_warnings = false
          #}
        }
      }

      resources {
        network {
          port "zk" {
            static = "2181"
          }
        }
      }
    }
  }
}
