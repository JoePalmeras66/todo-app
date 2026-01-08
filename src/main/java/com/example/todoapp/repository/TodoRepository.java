package com.example.todoapp.repository;

import com.example.todoapp.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByCompleted(Boolean completed);

    List<Todo> findByPriority(Todo.Priority priority);

    List<Todo> findByTitleContainingIgnoreCase(String title);

    List<Todo> findByCompletedAndPriority(Boolean completed, Todo.Priority priority);
}
