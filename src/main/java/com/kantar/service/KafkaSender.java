package com.kantar.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class KafkaSender {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    @Qualifier("kafkaTaskExecutor")
    private TaskExecutor taskExecutor;

    public void send(String _token, String data){
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(_token, data);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("Unable to send message=[" + data + "] due to : " + ex.getMessage());
            }else{
                System.out.println("Sent message=[" + data + "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }
        });
    }
}