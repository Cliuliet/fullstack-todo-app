package com.example.demo.service;

import com.example.demo.common.PageResponse;
import com.example.demo.dto.StatsResponse;
import com.example.demo.dto.TodoRequest;
import com.example.demo.entity.Todo;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoService todoService;

    private Todo todo1;
    private Todo todo2;

    @BeforeEach
    void setUp() {
        todo1 = new Todo();
        todo1.setId(1L);
        todo1.setTitle("Learn Spring Boot");
        todo1.setCompleted(false);
        todo1.setPriority(1);

        todo2 = new Todo();
        todo2.setId(2L);
        todo2.setTitle("Learn Vue3");
        todo2.setCompleted(true);
        todo2.setPriority(2);
    }

    // --- findPage ---

    @Test
    void findPage_withoutKeyword_returnsPageResponse() {
        Page<Todo> page = new PageImpl<>(List.of(todo1, todo2), PageRequest.of(0, 10), 2);
        when(todoRepository.findByKeyword(eq(""), any(Pageable.class))).thenReturn(page);

        PageResponse<Todo> result = todoService.findPage(0, 10, "");

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getPage()).isEqualTo(0);
        verify(todoRepository).findByKeyword(eq(""), any(Pageable.class));
    }

    @Test
    void findPage_withKeyword_returnsFilteredPageResponse() {
        Page<Todo> page = new PageImpl<>(List.of(todo1), PageRequest.of(0, 10), 1);
        when(todoRepository.findByKeyword(eq("Spring"), any(Pageable.class))).thenReturn(page);

        PageResponse<Todo> result = todoService.findPage(0, 10, "Spring");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Learn Spring Boot");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // --- findById ---

    @Test
    void findById_existingId_returnsTodo() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));

        Todo result = todoService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Learn Spring Boot");
    }

    @Test
    void findById_nonExistingId_throwsResourceNotFoundException() {
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- create ---

    @Test
    void create_validRequest_returnsSavedTodo() {
        TodoRequest request = new TodoRequest();
        request.setTitle("New Task");
        request.setPriority(1);

        Todo saved = new Todo();
        saved.setId(3L);
        saved.setTitle("New Task");
        saved.setPriority(1);

        when(todoRepository.save(any(Todo.class))).thenReturn(saved);

        Todo result = todoService.create(request);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getTitle()).isEqualTo("New Task");
        assertThat(result.getPriority()).isEqualTo(1);
        verify(todoRepository).save(any(Todo.class));
    }

    // --- update ---

    @Test
    void update_existingId_returnsUpdatedTodo() {
        TodoRequest request = new TodoRequest();
        request.setTitle("Updated Title");
        request.setCompleted(true);
        request.setPriority(3);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));
        when(todoRepository.save(any(Todo.class))).thenAnswer(inv -> inv.getArgument(0));

        Todo result = todoService.update(1L, request);

        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getPriority()).isEqualTo(3);
    }

    @Test
    void update_nonExistingId_throwsResourceNotFoundException() {
        TodoRequest request = new TodoRequest();
        request.setTitle("Updated Title");
        request.setPriority(2);

        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- delete ---

    @Test
    void delete_existingId_deletesSuccessfully() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));
        doNothing().when(todoRepository).deleteById(1L);

        assertThatCode(() -> todoService.delete(1L)).doesNotThrowAnyException();
        verify(todoRepository).deleteById(1L);
    }

    @Test
    void delete_nonExistingId_throwsResourceNotFoundException() {
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(todoRepository, never()).deleteById(anyLong());
    }

    // --- getStats ---

    @Test
    void getStats_returnsCorrectStats() {
        when(todoRepository.count()).thenReturn(5L);
        when(todoRepository.countByCompletedTrue()).thenReturn(2L);
        when(todoRepository.countByCompletedFalse()).thenReturn(3L);
        when(todoRepository.countByPriorityAndCompletedFalse(1)).thenReturn(1L);

        StatsResponse stats = todoService.getStats();

        assertThat(stats.getTotal()).isEqualTo(5L);
        assertThat(stats.getCompleted()).isEqualTo(2L);
        assertThat(stats.getActive()).isEqualTo(3L);
        assertThat(stats.getHighPriority()).isEqualTo(1L);
    }
}
