package com.example.todoapp.service;

import com.example.todoapp.entity.Todo;
import com.example.todoapp.integration.TodoEventGateway;
import com.example.todoapp.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final TodoEventGateway todoEventGateway;

    @Override
    @Transactional(readOnly = true)
    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Todo> getTodoById(Long id) {
        return todoRepository.findById(id);
    }

    @Override
    public Todo createTodo(Todo todo) {
        Todo savedTodo = todoRepository.save(todo);
        todoEventGateway.sendTodoCreatedEvent(savedTodo);
        return savedTodo;
    }

    @Override
    public Optional<Todo> updateTodo(Long id, Todo todoDetails) {
        return todoRepository.findById(id)
                .map(existingTodo -> {
                    existingTodo.setTitle(todoDetails.getTitle());
                    existingTodo.setDescription(todoDetails.getDescription());
                    existingTodo.setCompleted(todoDetails.getCompleted());
                    existingTodo.setPriority(todoDetails.getPriority());
                    existingTodo.setDueDate(todoDetails.getDueDate());
                    Todo updatedTodo = todoRepository.save(existingTodo);
                    todoEventGateway.sendTodoUpdatedEvent(updatedTodo);
                    return updatedTodo;
                });
    }

    @Override
    public boolean deleteTodo(Long id) {
        return todoRepository.findById(id)
                .map(todo -> {
                    todoRepository.delete(todo);
                    todoEventGateway.sendTodoDeletedEvent(id);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Todo> getTodosByCompleted(Boolean completed) {
        return todoRepository.findByCompleted(completed);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Todo> getTodosByPriority(Todo.Priority priority) {
        return todoRepository.findByPriority(priority);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Todo> searchTodosByTitle(String title) {
        return todoRepository.findByTitleContainingIgnoreCase(title);
    }

    @Override
    public Optional<Todo> toggleTodoCompletion(Long id) {
        return todoRepository.findById(id)
                .map(todo -> {
                    todo.setCompleted(!todo.getCompleted());
                    Todo updatedTodo = todoRepository.save(todo);
                    todoEventGateway.sendTodoUpdatedEvent(updatedTodo);
                    return updatedTodo;
                });
    }
}
