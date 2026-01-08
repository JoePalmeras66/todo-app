package com.example.todoapp.config;

import com.example.todoapp.entity.Todo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableIntegration
@Slf4j
public class IntegrationConfig {

    @Bean
    public MessageChannel todoCreatedChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel todoUpdatedChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel todoDeletedChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "todoCreatedChannel")
    public MessageHandler todoCreatedHandler() {
        return message -> {
            Todo todo = (Todo) message.getPayload();
            log.info("Todo created event: id={}, title={}", todo.getId(), todo.getTitle());
        };
    }

    @Bean
    @ServiceActivator(inputChannel = "todoUpdatedChannel")
    public MessageHandler todoUpdatedHandler() {
        return message -> {
            Todo todo = (Todo) message.getPayload();
            log.info("Todo updated event: id={}, title={}, completed={}",
                    todo.getId(), todo.getTitle(), todo.getCompleted());
        };
    }

    @Bean
    @ServiceActivator(inputChannel = "todoDeletedChannel")
    public MessageHandler todoDeletedHandler() {
        return message -> {
            Long todoId = (Long) message.getPayload();
            log.info("Todo deleted event: id={}", todoId);
        };
    }
}
