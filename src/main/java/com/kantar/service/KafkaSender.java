package com.kantar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaSender {
    @Autowired
    private KafkaTemplate<String, Double> kafkaTemplate;

    public void send(Double data){
        // LOG.info("sending data='{}' to topic='{}'", data, "msg");
        kafkaTemplate.send("msg", data);
    }
}