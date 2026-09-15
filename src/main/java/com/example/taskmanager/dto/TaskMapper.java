package com.example.taskmanager.dto;

import com.example.taskmanager.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponseDTO toResponseDTO(Task task) {
        if (task == null) {
            return null;
        }
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getCompletedAt()
        );
    }

    public Task toEntity(TaskRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }
        Task task = new Task();
        task.setTitle(requestDTO.getTitle());
        task.setDescription(requestDTO.getDescription());
        task.setStatus(requestDTO.getStatus());
        task.setPriority(requestDTO.getPriority());
        return task;
    }

    public void updateEntityFromDTO(TaskRequestDTO requestDTO, Task task) {
        if (requestDTO == null || task == null) {
            return;
        }
        task.setTitle(requestDTO.getTitle());
        task.setDescription(requestDTO.getDescription());
        task.setStatus(requestDTO.getStatus());
        task.setPriority(requestDTO.getPriority());
    }
}