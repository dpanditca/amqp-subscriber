package com.pandit.amqp.subscriber;

import com.pandit.amqp.config.AMQPConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageSubscriber {

    @RabbitListener(queues = AMQPConfig.INSTANT_QUEUE_NAME)
    public void instantMessage(@Payload String payload) {
        log.info("Received Instant Message: {}", payload);
    }

    @RabbitListener(queues = AMQPConfig.DELAYED_QUEUE_NAME)
    public void delayedMessage(@Payload String payload) {
        log.info("Received Delayed Message: {}", payload);
    }
}
