package com.example.todoapp.controller;

import com.example.todoapp.entity.Todo;
import com.example.todoapp.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = TodoController.class, excludeAutoConfiguration = SpringDocConfiguration.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    private Todo sampleTodo;

    @BeforeEach
    void setUp() {
        sampleTodo = Todo.builder()
                .id(1L)
                .title("Test Todo")
                .description("Test Description")
                .completed(false)
                .priority(Todo.Priority.MEDIUM)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("GET /api/todos")
    class GetAllTodosTests {

        @Test
        @DisplayName("should return all todos with 200 status")
        void shouldReturnAllTodos() throws Exception {
            Todo todo2 = Todo.builder()
                    .id(2L)
                    .title("Second Todo")
                    .completed(true)
                    .priority(Todo.Priority.HIGH)
                    .build();
            List<Todo> todos = Arrays.asList(sampleTodo, todo2);
            when(todoService.getAllTodos()).thenReturn(todos);

            mockMvc.perform(get("/api/todos"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("Test Todo"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].title").value("Second Todo"));

            verify(todoService, times(1)).getAllTodos();
        }

        @Test
        @DisplayName("should return empty list when no todos exist")
        void shouldReturnEmptyList() throws Exception {
            when(todoService.getAllTodos()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/todos"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/todos/{id}")
    class GetTodoByIdTests {

        @Test
        @DisplayName("should return todo when found")
        void shouldReturnTodoWhenFound() throws Exception {
            when(todoService.getTodoById(1L)).thenReturn(Optional.of(sampleTodo));

            mockMvc.perform(get("/api/todos/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Test Todo"))
                    .andExpect(jsonPath("$.description").value("Test Description"))
                    .andExpect(jsonPath("$.completed").value(false))
                    .andExpect(jsonPath("$.priority").value("MEDIUM"));

            verify(todoService, times(1)).getTodoById(1L);
        }

        @Test
        @DisplayName("should return 404 when todo not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(todoService.getTodoById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/todos/99"))
                    .andExpect(status().isNotFound());

            verify(todoService, times(1)).getTodoById(99L);
        }
    }

    @Nested
    @DisplayName("POST /api/todos")
    class CreateTodoTests {

        @Test
        @DisplayName("should create todo and return 201 status")
        void shouldCreateTodo() throws Exception {
            Todo newTodo = Todo.builder()
                    .title("New Todo")
                    .description("New Description")
                    .priority(Todo.Priority.HIGH)
                    .build();
            Todo savedTodo = Todo.builder()
                    .id(1L)
                    .title("New Todo")
                    .description("New Description")
                    .completed(false)
                    .priority(Todo.Priority.HIGH)
                    .createdAt(LocalDateTime.now())
                    .build();
            when(todoService.createTodo(any(Todo.class))).thenReturn(savedTodo);

            mockMvc.perform(post("/api/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newTodo)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("New Todo"))
                    .andExpect(jsonPath("$.priority").value("HIGH"));

            verify(todoService, times(1)).createTodo(any(Todo.class));
        }

        @Test
        @DisplayName("should return 400 when title is blank")
        void shouldReturn400WhenTitleIsBlank() throws Exception {
            Todo invalidTodo = Todo.builder()
                    .title("")
                    .description("Description")
                    .build();

            mockMvc.perform(post("/api/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidTodo)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.title").exists());

            verify(todoService, never()).createTodo(any(Todo.class));
        }

        @Test
        @DisplayName("should return 400 when title is missing")
        void shouldReturn400WhenTitleIsMissing() throws Exception {
            String jsonWithoutTitle = "{\"description\": \"Description\"}";

            mockMvc.perform(post("/api/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonWithoutTitle))
                    .andExpect(status().isBadRequest());

            verify(todoService, never()).createTodo(any(Todo.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/todos/{id}")
    class UpdateTodoTests {

        @Test
        @DisplayName("should update todo and return 200 status")
        void shouldUpdateTodo() throws Exception {
            Todo updateDetails = Todo.builder()
                    .title("Updated Title")
                    .description("Updated Description")
                    .completed(true)
                    .priority(Todo.Priority.LOW)
                    .build();
            Todo updatedTodo = Todo.builder()
                    .id(1L)
                    .title("Updated Title")
                    .description("Updated Description")
                    .completed(true)
                    .priority(Todo.Priority.LOW)
                    .build();
            when(todoService.updateTodo(eq(1L), any(Todo.class))).thenReturn(Optional.of(updatedTodo));

            mockMvc.perform(put("/api/todos/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Updated Title"))
                    .andExpect(jsonPath("$.completed").value(true))
                    .andExpect(jsonPath("$.priority").value("LOW"));

            verify(todoService, times(1)).updateTodo(eq(1L), any(Todo.class));
        }

        @Test
        @DisplayName("should return 404 when todo not found")
        void shouldReturn404WhenNotFound() throws Exception {
            Todo updateDetails = Todo.builder()
                    .title("Updated Title")
                    .build();
            when(todoService.updateTodo(eq(99L), any(Todo.class))).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/todos/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDetails)))
                    .andExpect(status().isNotFound());

            verify(todoService, times(1)).updateTodo(eq(99L), any(Todo.class));
        }

        @Test
        @DisplayName("should return 400 when validation fails")
        void shouldReturn400WhenValidationFails() throws Exception {
            Todo invalidUpdate = Todo.builder()
                    .title("")
                    .build();

            mockMvc.perform(put("/api/todos/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidUpdate)))
                    .andExpect(status().isBadRequest());

            verify(todoService, never()).updateTodo(anyLong(), any(Todo.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/todos/{id}")
    class DeleteTodoTests {

        @Test
        @DisplayName("should delete todo and return 204 status")
        void shouldDeleteTodo() throws Exception {
            when(todoService.deleteTodo(1L)).thenReturn(true);

            mockMvc.perform(delete("/api/todos/1"))
                    .andExpect(status().isNoContent());

            verify(todoService, times(1)).deleteTodo(1L);
        }

        @Test
        @DisplayName("should return 404 when todo not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(todoService.deleteTodo(99L)).thenReturn(false);

            mockMvc.perform(delete("/api/todos/99"))
                    .andExpect(status().isNotFound());

            verify(todoService, times(1)).deleteTodo(99L);
        }
    }

    @Nested
    @DisplayName("GET /api/todos/status/{completed}")
    class GetTodosByCompletedTests {

        @Test
        @DisplayName("should return completed todos")
        void shouldReturnCompletedTodos() throws Exception {
            Todo completedTodo = Todo.builder()
                    .id(2L)
                    .title("Completed Todo")
                    .completed(true)
                    .build();
            when(todoService.getTodosByCompleted(true)).thenReturn(List.of(completedTodo));

            mockMvc.perform(get("/api/todos/status/true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].completed").value(true));

            verify(todoService, times(1)).getTodosByCompleted(true);
        }

        @Test
        @DisplayName("should return incomplete todos")
        void shouldReturnIncompleteTodos() throws Exception {
            when(todoService.getTodosByCompleted(false)).thenReturn(List.of(sampleTodo));

            mockMvc.perform(get("/api/todos/status/false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].completed").value(false));

            verify(todoService, times(1)).getTodosByCompleted(false);
        }
    }

    @Nested
    @DisplayName("GET /api/todos/priority/{priority}")
    class GetTodosByPriorityTests {

        @Test
        @DisplayName("should return todos by priority")
        void shouldReturnTodosByPriority() throws Exception {
            Todo highPriorityTodo = Todo.builder()
                    .id(2L)
                    .title("High Priority Todo")
                    .priority(Todo.Priority.HIGH)
                    .build();
            when(todoService.getTodosByPriority(Todo.Priority.HIGH)).thenReturn(List.of(highPriorityTodo));

            mockMvc.perform(get("/api/todos/priority/HIGH"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].priority").value("HIGH"));

            verify(todoService, times(1)).getTodosByPriority(Todo.Priority.HIGH);
        }

        @Test
        @DisplayName("should return empty list for priority with no todos")
        void shouldReturnEmptyListForPriorityWithNoTodos() throws Exception {
            when(todoService.getTodosByPriority(Todo.Priority.LOW)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/todos/priority/LOW"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/todos/search")
    class SearchTodosByTitleTests {

        @Test
        @DisplayName("should return todos matching search term")
        void shouldReturnTodosMatchingSearchTerm() throws Exception {
            when(todoService.searchTodosByTitle("Test")).thenReturn(List.of(sampleTodo));

            mockMvc.perform(get("/api/todos/search")
                            .param("title", "Test"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title").value("Test Todo"));

            verify(todoService, times(1)).searchTodosByTitle("Test");
        }

        @Test
        @DisplayName("should return empty list when no match")
        void shouldReturnEmptyListWhenNoMatch() throws Exception {
            when(todoService.searchTodosByTitle("NonExistent")).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/todos/search")
                            .param("title", "NonExistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("PATCH /api/todos/{id}/toggle")
    class ToggleTodoCompletionTests {

        @Test
        @DisplayName("should toggle completion and return 200 status")
        void shouldToggleCompletion() throws Exception {
            Todo toggledTodo = Todo.builder()
                    .id(1L)
                    .title("Test Todo")
                    .completed(true)
                    .build();
            when(todoService.toggleTodoCompletion(1L)).thenReturn(Optional.of(toggledTodo));

            mockMvc.perform(patch("/api/todos/1/toggle"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.completed").value(true));

            verify(todoService, times(1)).toggleTodoCompletion(1L);
        }

        @Test
        @DisplayName("should return 404 when todo not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(todoService.toggleTodoCompletion(99L)).thenReturn(Optional.empty());

            mockMvc.perform(patch("/api/todos/99/toggle"))
                    .andExpect(status().isNotFound());

            verify(todoService, times(1)).toggleTodoCompletion(99L);
        }
    }
}
