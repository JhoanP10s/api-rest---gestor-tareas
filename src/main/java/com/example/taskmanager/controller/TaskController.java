package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequestDTO;
import com.example.taskmanager.dto.TaskResponseDTO;
import com.example.taskmanager.entity.TaskPriority;
import com.example.taskmanager.entity.TaskStatus;
import com.example.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management API")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskResponseDTO> create(@Valid @RequestBody TaskRequestDTO requestDTO) {
        TaskResponseDTO created = taskService.create(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by ID")
    public ResponseEntity<TaskResponseDTO> getById(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        TaskResponseDTO task = taskService.getById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping
    @Operation(summary = "Get all tasks with pagination and optional filters")
    public ResponseEntity<Page<TaskResponseDTO>> getAll(
            @Parameter(description = "Filter by status") @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Filter by priority") @RequestParam(required = false) TaskPriority priority,
            @Parameter(description = "Search keyword") @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<TaskResponseDTO> tasks;
        
        if (search != null && !search.trim().isEmpty()) {
            tasks = taskService.search(search.trim(), pageable);
        } else if (status != null && priority != null) {
            tasks = taskService.getByStatusAndPriority(status, priority, pageable);
        } else if (status != null) {
            tasks = taskService.getByStatus(status, pageable);
        } else if (priority != null) {
            tasks = taskService.getByPriority(priority, pageable);
        } else {
            tasks = taskService.getAll(pageable);
        }
        
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task completely")
    public ResponseEntity<TaskResponseDTO> update(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Valid @RequestBody TaskRequestDTO requestDTO) {
        TaskResponseDTO updated = taskService.update(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status")
    public ResponseEntity<TaskResponseDTO> updateStatus(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam TaskStatus status) {
        TaskResponseDTO updated = taskService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/status/{status}")
    @Operation(summary = "Get count of tasks by status")
    public ResponseEntity<Long> countByStatus(
            @Parameter(description = "Task status") @PathVariable TaskStatus status) {
        long count = taskService.countByStatus(status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/priority/{priority}")
    @Operation(summary = "Get count of tasks by priority")
    public ResponseEntity<Long> countByPriority(
            @Parameter(description = "Task priority") @PathVariable TaskPriority priority) {
        long count = taskService.countByPriority(priority);
        return ResponseEntity.ok(count);
    }
}