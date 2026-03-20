package com.example.demo.service;

import com.example.demo.common.PageResponse;
import com.example.demo.dto.StatsResponse;
import com.example.demo.dto.TodoRequest;
import com.example.demo.entity.Todo;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.TodoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<Todo> findAll() {
        return todoRepository.findAllByOrderByCreatedAtDesc();
    }

    public PageResponse<Todo> findPage(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.from(todoRepository.findByKeyword(keyword, pageable));
    }

    public Todo findById(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo", id));
    }

    public Todo create(TodoRequest request) {
        Todo todo = new Todo();
        todo.setTitle(request.getTitle());
        todo.setCompleted(request.isCompleted());
        todo.setPriority(request.getPriority());
        return todoRepository.save(todo);
    }

    public Todo update(Long id, TodoRequest request) {
        Todo existing = findById(id);
        existing.setTitle(request.getTitle());
        existing.setCompleted(request.isCompleted());
        existing.setPriority(request.getPriority());
        return todoRepository.save(existing);
    }

    public void delete(Long id) {
        findById(id);
        todoRepository.deleteById(id);
    }

    public StatsResponse getStats() {
        long total = todoRepository.count();
        long completed = todoRepository.countByCompletedTrue();
        long active = todoRepository.countByCompletedFalse();
        long highPriority = todoRepository.countByPriorityAndCompletedFalse(1);
        return new StatsResponse(total, completed, active, highPriority);
    }
}
