package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequestDTO;
import com.example.taskmanager.dto.TaskResponseDTO;
import com.example.taskmanager.entity.TaskPriority;
import com.example.taskmanager.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    TaskResponseDTO create(TaskRequestDTO requestDTO);

    TaskResponseDTO getById(Long id);

    Page<TaskResponseDTO> getAll(Pageable pageable);

    Page<TaskResponseDTO> getByStatus(TaskStatus status, Pageable pageable);

    Page<TaskResponseDTO> getByPriority(TaskPriority priority, Pageable pageable);

    Page<TaskResponseDTO> getByStatusAndPriority(TaskStatus status, TaskPriority priority, Pageable pageable);

    Page<TaskResponseDTO> search(String keyword, Pageable pageable);

    TaskResponseDTO update(Long id, TaskRequestDTO requestDTO);

    TaskResponseDTO updateStatus(Long id, TaskStatus status);

    void delete(Long id);

    long countByStatus(TaskStatus status);

    long countByPriority(TaskPriority priority);
}