# KANTAR-Serv

## Server Information
    BackEnd : JAVA v17
    FrontEnd : ReactJS
    DB : mariaDB

## Kafka Server Install
```
URL : https://kafka.apache.org/downloads
wget https://downloads.apache.org/kafka/3.4.0/kafka_2.13-3.4.0.tgz
tar xvf ./kafka_2.13-3.4.0
```

# Kafka zookeeper server start
/home/ec2-user/kafka_2.13-3.4.0/bin/zookeeper-server-start.sh -daemon /home/ec2-user/kafka_2.13-3.4.0/config/zookeeper.properties

# Kafka zookeeper server stop
/home/ec2-user/kafka_2.13-3.4.0/bin/zookeeper-server-stop.sh

# Kafka server start
/home/ec2-user/kafka_2.12-3.3.2/bin/kafka-server-start.sh -daemon /home/ec2-user/kafka_2.12-3.3.2/config/server.properties

# Kafka server stop
/home/ec2-user/kafka_2.12-3.3.2/bin/kafka-server-stop.sh

# create to Kafka topic
/home/ec2-user/kafka_2.12-3.3.2/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --topic kantar
