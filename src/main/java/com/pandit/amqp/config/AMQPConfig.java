package com.pandit.amqp.config;

import lombok.Data;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("amqp")
public class AMQPConfig {
    private String exchange;

    public static final String INSTANT_QUEUE_NAME = "instant";
    public static final String DELAYED_QUEUE_NAME = "delayed";

    private static final String ROUTING_KEY_INSTANT = "registry.instant";
    private static final String ROUTING_KEY_DELAYED = "registry.delayed";
    private static final String ROUTING_KEY_ERRORS = "error.#";

    @Bean
    CustomExchange exchange() {
        return new CustomExchange(exchange, "x-delayed-message");
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    Queue instantQueue() {
        return QueueBuilder.durable(INSTANT_QUEUE_NAME)
                .deadLetterExchange(exchange)
                .deadLetterRoutingKey(ROUTING_KEY_ERRORS)
                .build();
    }

    @Bean
    Binding binding1(@Qualifier("instantQueue") Queue queue, Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_INSTANT).noargs();
    }

    @Bean
    Queue delayedQueue() {
        return QueueBuilder.durable(DELAYED_QUEUE_NAME)
                .deadLetterExchange(exchange)
                .deadLetterRoutingKey(ROUTING_KEY_ERRORS)
                .build();
    }

    @Bean
    Binding binding2(@Qualifier("delayedQueue") Queue queue, Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_DELAYED).noargs();
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                        SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        // setting a global error handler
        factory.setErrorHandler(throwable -> {
            throw new AmqpRejectAndDontRequeueException("Error Handler converted exception to fatal", throwable);
        });
        return factory;
    }

}
