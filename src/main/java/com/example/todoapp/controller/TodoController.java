package com.example.todoapp.controller;

import com.example.todoapp.entity.Todo;
import com.example.todoapp.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@Tag(name = "Todo", description = "Todo management API")
public class TodoController {

    private final TodoService todoService;

    @Operation(summary = "Get all todos", description = "Retrieves a list of all todos")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all todos")
    @GetMapping
    public ResponseEntity<List<Todo>> getAllTodos() {
        List<Todo> todos = todoService.getAllTodos();
        return ResponseEntity.ok(todos);
    }

    @Operation(summary = "Get todo by ID", description = "Retrieves a specific todo by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todo found"),
            @ApiResponse(responseCode = "404", description = "Todo not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodoById(
            @Parameter(description = "Todo ID") @PathVariable Long id) {
        return todoService.getTodoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new todo", description = "Creates a new todo item")
    @ApiResponse(responseCode = "201", description = "Todo created successfully")
    @PostMapping
    public ResponseEntity<Todo> createTodo(@Valid @RequestBody Todo todo) {
        Todo createdTodo = todoService.createTodo(todo);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTodo);
    }

    @Operation(summary = "Update a todo", description = "Updates an existing todo by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todo updated successfully"),
            @ApiResponse(responseCode = "404", description = "Todo not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(
            @Parameter(description = "Todo ID") @PathVariable Long id,
            @Valid @RequestBody Todo todoDetails) {
        return todoService.updateTodo(id, todoDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a todo", description = "Deletes a todo by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Todo deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Todo not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(
            @Parameter(description = "Todo ID") @PathVariable Long id) {
        if (todoService.deleteTodo(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get todos by completion status", description = "Retrieves todos filtered by completion status")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved todos")
    @GetMapping("/status/{completed}")
    public ResponseEntity<List<Todo>> getTodosByCompleted(
            @Parameter(description = "Completion status (true/false)") @PathVariable Boolean completed) {
        List<Todo> todos = todoService.getTodosByCompleted(completed);
        return ResponseEntity.ok(todos);
    }

    @Operation(summary = "Get todos by priority", description = "Retrieves todos filtered by priority level")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved todos")
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Todo>> getTodosByPriority(
            @Parameter(description = "Priority level (LOW, MEDIUM, HIGH)") @PathVariable Todo.Priority priority) {
        List<Todo> todos = todoService.getTodosByPriority(priority);
        return ResponseEntity.ok(todos);
    }

    @Operation(summary = "Search todos by title", description = "Searches todos by title (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved matching todos")
    @GetMapping("/search")
    public ResponseEntity<List<Todo>> searchTodosByTitle(
            @Parameter(description = "Search term for title") @RequestParam String title) {
        List<Todo> todos = todoService.searchTodosByTitle(title);
        return ResponseEntity.ok(todos);
    }

    @Operation(summary = "Toggle todo completion", description = "Toggles the completion status of a todo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todo toggled successfully"),
            @ApiResponse(responseCode = "404", description = "Todo not found")
    })
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Todo> toggleTodoCompletion(
            @Parameter(description = "Todo ID") @PathVariable Long id) {
        return todoService.toggleTodoCompletion(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
