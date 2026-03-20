package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.common.PageResponse;
import com.example.demo.dto.StatsResponse;
import com.example.demo.dto.TodoRequest;
import com.example.demo.entity.Todo;
import com.example.demo.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/todos")
@CrossOrigin(origins = "*")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Todo>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword) {
        return ApiResponse.success(todoService.findPage(page, size, keyword));
    }

    @GetMapping("/stats")
    public ApiResponse<StatsResponse> getStats() {
        return ApiResponse.success(todoService.getStats());
    }

    @GetMapping("/{id}")
    public ApiResponse<Todo> getById(@PathVariable Long id) {
        return ApiResponse.success(todoService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Todo> create(@Valid @RequestBody TodoRequest request) {
        return ApiResponse.created(todoService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Todo> update(@PathVariable Long id, @Valid @RequestBody TodoRequest request) {
        return ApiResponse.success(todoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        todoService.delete(id);
    }
}
