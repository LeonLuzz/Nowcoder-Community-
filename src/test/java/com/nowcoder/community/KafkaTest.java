package com.nowcoder.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@SpringBootTest
public class KafkaTest {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void testKafka(){
        kafkaProducer.sendMessage("test", "Hello");
        kafkaProducer.sendMessage("test", "Are you here?");
        try{
            Thread.sleep(100*10);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}

@Component
class KafkaProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, String content){
        kafkaTemplate.send(topic, content);
    }
}

@Component
class Consumer {
    // 消费者提取数据是被动的，无需显式调用
    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record){
        System.out.println(record.value());
    }
}