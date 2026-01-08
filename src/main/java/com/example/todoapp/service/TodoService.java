package com.example.todoapp.service;

import com.example.todoapp.entity.Todo;

import java.util.List;
import java.util.Optional;

public interface TodoService {

    List<Todo> getAllTodos();

    Optional<Todo> getTodoById(Long id);

    Todo createTodo(Todo todo);

    Optional<Todo> updateTodo(Long id, Todo todoDetails);

    boolean deleteTodo(Long id);

    List<Todo> getTodosByCompleted(Boolean completed);

    List<Todo> getTodosByPriority(Todo.Priority priority);

    List<Todo> searchTodosByTitle(String title);

    Optional<Todo> toggleTodoCompletion(Long id);
}
