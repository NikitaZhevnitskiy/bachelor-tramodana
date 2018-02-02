job "infosak-zookeeper" {

  datacenters = [
    "dc1"]

  group "zookeeper-cluster" {
    count = 1

    ephemeral_disk {
      sticky = true
    }

    task "zookeeper" {
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
        command = "local/confluent-4.0.0/bin/windows/zookeeper-server-start.bat"
        args = [
          "local/zookeeper.properties"]
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
