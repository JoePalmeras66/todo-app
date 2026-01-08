package com.example.todoapp.integration;

import com.example.todoapp.entity.Todo;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface TodoEventGateway {

    @Gateway(requestChannel = "todoCreatedChannel")
    void sendTodoCreatedEvent(Todo todo);

    @Gateway(requestChannel = "todoUpdatedChannel")
    void sendTodoUpdatedEvent(Todo todo);

    @Gateway(requestChannel = "todoDeletedChannel")
    void sendTodoDeletedEvent(Long todoId);
}
