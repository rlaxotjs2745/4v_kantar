# KANTAR-Serv

## Server Information
    BackEnd : JAVA v17
    FrontEnd : ReactJS
    DB : mariaDB

## EC2 Server Default time zone setting
```
sudo rm /etc/localtime
sudo ln -s /usr/share/zoneinfo/Asia/Seoul /etc/localtime
```

## Kafka Server Install
```
URL : https://kafka.apache.org/downloads
wget https://downloads.apache.org/kafka/3.4.0/kafka_2.13-3.4.0.tgz
tar xvf ./kafka_2.13-3.4.0.tgz
```

### Kafka zookeeper server start
/home/ec2-user/kafka_2.13-3.4.0/bin/zookeeper-server-start.sh -daemon /home/ec2-user/kafka_2.13-3.4.0/config/zookeeper.properties

### Kafka zookeeper server stop
/home/ec2-user/kafka_2.13-3.4.0/bin/zookeeper-server-stop.sh

### Kafka server start
/home/ec2-user/kafka_2.12-3.3.2/bin/kafka-server-start.sh -daemon /home/ec2-user/kafka_2.12-3.3.2/config/server.properties

### Kafka server stop
/home/ec2-user/kafka_2.12-3.3.2/bin/kafka-server-stop.sh

### create to Kafka topic
/home/ec2-user/kafka_2.12-3.3.2/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --topic kantar


## Java Server
```
wget https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.rpm
sudo rpm -ivh jdk-17_linux-x64_bin.rpm (기본 설치 위치 : /usr/java/jdk-17.0.5/bin/java)
sudo alternatives --config java (자바 버전 선택)
java --version
rm -rf jdk-17_linux-x64_bin.rpm
wget https://dlcdn.apache.org/tomcat/tomcat-10/v10.0.27/bin/apache-tomcat-10.0.27.tar.gz
tar -xvf ./apache-tomcat-10.0.27.tar.gz
sudo mv apache-tomcat-10.0.27/ /home/ec2-user/KANTAR_SERVER
rm -rf apache-tomcat-10.0.27.tar.gz
rm -rf ./apache-tomcat-10.0.27
chmod +x /home/ec2-user/KANTAR_SERVER/bin/*.sh
rm -rf /home/ec2-user/KANTAR_SERVER/bin/*.bat
```

0. KANTAR_SERVER 프로젝트를 빌드한다.
1. kantar.war 파일을 /home/ec2-user/KANTAR_SERVER/webapps 에 복사한다.
2. setenv.sh 파일을 /home/ec2-user/KANTAR_SERVER/bin 에 복사한다.
3. server.xml 파일을 /home/ec2-user/KANTAR_SERVER/conf 에 복사한다.
4. context.xml 파일을 /home/ec2-user/KANTAR_SERVER/conf 에 복사한다.
5. chmod +x /home/ec2-user/KANTAR_SERVER/bin/*.sh

### Java Server Start
/home/ec2-user/KANTAR_SERVER/bin/startup.sh

### Java Serve Stop
/home/ec2-user/KANTAR_SERVER/bin/shutdown.sh
