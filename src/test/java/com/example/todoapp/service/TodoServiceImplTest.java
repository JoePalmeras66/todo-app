package com.example.todoapp.service;

import com.example.todoapp.entity.Todo;
import com.example.todoapp.integration.TodoEventGateway;
import com.example.todoapp.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private TodoEventGateway todoEventGateway;

    @InjectMocks
    private TodoServiceImpl todoService;

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
    @DisplayName("getAllTodos")
    class GetAllTodosTests {

        @Test
        @DisplayName("should return all todos")
        void shouldReturnAllTodos() {
            Todo todo2 = Todo.builder()
                    .id(2L)
                    .title("Second Todo")
                    .completed(true)
                    .priority(Todo.Priority.HIGH)
                    .build();
            List<Todo> todos = Arrays.asList(sampleTodo, todo2);
            when(todoRepository.findAll()).thenReturn(todos);

            List<Todo> result = todoService.getAllTodos();

            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(sampleTodo, todo2);
            verify(todoRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("should return empty list when no todos exist")
        void shouldReturnEmptyListWhenNoTodos() {
            when(todoRepository.findAll()).thenReturn(Collections.emptyList());

            List<Todo> result = todoService.getAllTodos();

            assertThat(result).isEmpty();
            verify(todoRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("getTodoById")
    class GetTodoByIdTests {

        @Test
        @DisplayName("should return todo when found")
        void shouldReturnTodoWhenFound() {
            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodo));

            Optional<Todo> result = todoService.getTodoById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getTitle()).isEqualTo("Test Todo");
            verify(todoRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("should return empty when todo not found")
        void shouldReturnEmptyWhenNotFound() {
            when(todoRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Todo> result = todoService.getTodoById(99L);

            assertThat(result).isEmpty();
            verify(todoRepository, times(1)).findById(99L);
        }
    }

    @Nested
    @DisplayName("createTodo")
    class CreateTodoTests {

        @Test
        @DisplayName("should create todo and send event")
        void shouldCreateTodoAndSendEvent() {
            Todo newTodo = Todo.builder()
                    .title("New Todo")
                    .description("New Description")
                    .priority(Todo.Priority.HIGH)
                    .build();
            Todo savedTodo = Todo.builder()
                    .id(1L)
                    .title("New Todo")
                    .description("New Description")
                    .priority(Todo.Priority.HIGH)
                    .completed(false)
                    .build();
            when(todoRepository.save(newTodo)).thenReturn(savedTodo);

            Todo result = todoService.createTodo(newTodo);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("New Todo");
            verify(todoRepository, times(1)).save(newTodo);
            verify(todoEventGateway, times(1)).sendTodoCreatedEvent(savedTodo);
        }
    }

    @Nested
    @DisplayName("updateTodo")
    class UpdateTodoTests {

        @Test
        @DisplayName("should update todo and send event when found")
        void shouldUpdateTodoAndSendEventWhenFound() {
            Todo updateDetails = Todo.builder()
                    .title("Updated Title")
                    .description("Updated Description")
                    .completed(true)
                    .priority(Todo.Priority.HIGH)
                    .dueDate(LocalDateTime.now().plusDays(1))
                    .build();
            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodo));
            when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Optional<Todo> result = todoService.updateTodo(1L, updateDetails);

            assertThat(result).isPresent();
            assertThat(result.get().getTitle()).isEqualTo("Updated Title");
            assertThat(result.get().getCompleted()).isTrue();
            assertThat(result.get().getPriority()).isEqualTo(Todo.Priority.HIGH);
            verify(todoRepository, times(1)).findById(1L);
            verify(todoRepository, times(1)).save(any(Todo.class));
            verify(todoEventGateway, times(1)).sendTodoUpdatedEvent(any(Todo.class));
        }

        @Test
        @DisplayName("should return empty when todo not found")
        void shouldReturnEmptyWhenTodoNotFound() {
            Todo updateDetails = Todo.builder().title("Updated").build();
            when(todoRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Todo> result = todoService.updateTodo(99L, updateDetails);

            assertThat(result).isEmpty();
            verify(todoRepository, times(1)).findById(99L);
            verify(todoRepository, never()).save(any(Todo.class));
            verify(todoEventGateway, never()).sendTodoUpdatedEvent(any(Todo.class));
        }
    }

    @Nested
    @DisplayName("deleteTodo")
    class DeleteTodoTests {

        @Test
        @DisplayName("should delete todo and send event when found")
        void shouldDeleteTodoAndSendEventWhenFound() {
            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodo));
            doNothing().when(todoRepository).delete(sampleTodo);

            boolean result = todoService.deleteTodo(1L);

            assertThat(result).isTrue();
            verify(todoRepository, times(1)).findById(1L);
            verify(todoRepository, times(1)).delete(sampleTodo);
            verify(todoEventGateway, times(1)).sendTodoDeletedEvent(1L);
        }

        @Test
        @DisplayName("should return false when todo not found")
        void shouldReturnFalseWhenTodoNotFound() {
            when(todoRepository.findById(99L)).thenReturn(Optional.empty());

            boolean result = todoService.deleteTodo(99L);

            assertThat(result).isFalse();
            verify(todoRepository, times(1)).findById(99L);
            verify(todoRepository, never()).delete(any(Todo.class));
            verify(todoEventGateway, never()).sendTodoDeletedEvent(anyLong());
        }
    }

    @Nested
    @DisplayName("getTodosByCompleted")
    class GetTodosByCompletedTests {

        @Test
        @DisplayName("should return completed todos")
        void shouldReturnCompletedTodos() {
            Todo completedTodo = Todo.builder()
                    .id(2L)
                    .title("Completed Todo")
                    .completed(true)
                    .build();
            when(todoRepository.findByCompleted(true)).thenReturn(List.of(completedTodo));

            List<Todo> result = todoService.getTodosByCompleted(true);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCompleted()).isTrue();
            verify(todoRepository, times(1)).findByCompleted(true);
        }

        @Test
        @DisplayName("should return incomplete todos")
        void shouldReturnIncompleteTodos() {
            when(todoRepository.findByCompleted(false)).thenReturn(List.of(sampleTodo));

            List<Todo> result = todoService.getTodosByCompleted(false);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCompleted()).isFalse();
            verify(todoRepository, times(1)).findByCompleted(false);
        }
    }

    @Nested
    @DisplayName("getTodosByPriority")
    class GetTodosByPriorityTests {

        @Test
        @DisplayName("should return todos by priority")
        void shouldReturnTodosByPriority() {
            Todo highPriorityTodo = Todo.builder()
                    .id(2L)
                    .title("High Priority")
                    .priority(Todo.Priority.HIGH)
                    .build();
            when(todoRepository.findByPriority(Todo.Priority.HIGH)).thenReturn(List.of(highPriorityTodo));

            List<Todo> result = todoService.getTodosByPriority(Todo.Priority.HIGH);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPriority()).isEqualTo(Todo.Priority.HIGH);
            verify(todoRepository, times(1)).findByPriority(Todo.Priority.HIGH);
        }
    }

    @Nested
    @DisplayName("searchTodosByTitle")
    class SearchTodosByTitleTests {

        @Test
        @DisplayName("should return todos matching title")
        void shouldReturnTodosMatchingTitle() {
            when(todoRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(List.of(sampleTodo));

            List<Todo> result = todoService.searchTodosByTitle("Test");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).contains("Test");
            verify(todoRepository, times(1)).findByTitleContainingIgnoreCase("Test");
        }

        @Test
        @DisplayName("should return empty list when no match")
        void shouldReturnEmptyListWhenNoMatch() {
            when(todoRepository.findByTitleContainingIgnoreCase("NonExistent")).thenReturn(Collections.emptyList());

            List<Todo> result = todoService.searchTodosByTitle("NonExistent");

            assertThat(result).isEmpty();
            verify(todoRepository, times(1)).findByTitleContainingIgnoreCase("NonExistent");
        }
    }

    @Nested
    @DisplayName("toggleTodoCompletion")
    class ToggleTodoCompletionTests {

        @Test
        @DisplayName("should toggle from incomplete to complete")
        void shouldToggleFromIncompleteToComplete() {
            sampleTodo.setCompleted(false);
            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodo));
            when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Optional<Todo> result = todoService.toggleTodoCompletion(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getCompleted()).isTrue();
            verify(todoRepository, times(1)).findById(1L);
            verify(todoRepository, times(1)).save(any(Todo.class));
            verify(todoEventGateway, times(1)).sendTodoUpdatedEvent(any(Todo.class));
        }

        @Test
        @DisplayName("should toggle from complete to incomplete")
        void shouldToggleFromCompleteToIncomplete() {
            sampleTodo.setCompleted(true);
            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodo));
            when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Optional<Todo> result = todoService.toggleTodoCompletion(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getCompleted()).isFalse();
            verify(todoEventGateway, times(1)).sendTodoUpdatedEvent(any(Todo.class));
        }

        @Test
        @DisplayName("should return empty when todo not found")
        void shouldReturnEmptyWhenTodoNotFound() {
            when(todoRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Todo> result = todoService.toggleTodoCompletion(99L);

            assertThat(result).isEmpty();
            verify(todoRepository, never()).save(any(Todo.class));
            verify(todoEventGateway, never()).sendTodoUpdatedEvent(any(Todo.class));
        }
    }
}
