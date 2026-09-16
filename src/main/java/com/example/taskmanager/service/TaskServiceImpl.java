package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskMapper;
import com.example.taskmanager.dto.TaskRequestDTO;
import com.example.taskmanager.dto.TaskResponseDTO;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.TaskPriority;
import com.example.taskmanager.entity.TaskStatus;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.exception.InvalidStateTransitionException;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    // Valid state transitions: currentStatus -> Set of allowed next statuses
    private static final Map<TaskStatus, Set<TaskStatus>> VALID_TRANSITIONS = Map.of(
            TaskStatus.PENDING, EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED, TaskStatus.CANCELLED),
            TaskStatus.IN_PROGRESS, EnumSet.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED, TaskStatus.PENDING),
            TaskStatus.COMPLETED, EnumSet.of(TaskStatus.IN_PROGRESS),
            TaskStatus.CANCELLED, EnumSet.of(TaskStatus.PENDING)
    );

    public TaskServiceImpl(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public TaskResponseDTO create(TaskRequestDTO requestDTO) {
        Task task = taskMapper.toEntity(requestDTO);
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDTO(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDTO getById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return taskMapper.toResponseDTO(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getAll(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(taskMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getByStatus(TaskStatus status, Pageable pageable) {
        return taskRepository.findByStatus(status, pageable)
                .map(taskMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getByPriority(TaskPriority priority, Pageable pageable) {
        return taskRepository.findByPriority(priority, pageable)
                .map(taskMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getByStatusAndPriority(TaskStatus status, TaskPriority priority, Pageable pageable) {
        return taskRepository.findByStatusAndPriority(status, priority, pageable)
                .map(taskMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> search(String keyword, Pageable pageable) {
        return taskRepository.searchByKeyword(keyword, pageable)
                .map(taskMapper::toResponseDTO);
    }

    @Override
    public TaskResponseDTO update(Long id, TaskRequestDTO requestDTO) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        // Validate status transition if status is being changed
        TaskStatus newStatus = requestDTO.getStatus();
        if (newStatus != null && !newStatus.equals(existingTask.getStatus())) {
            validateTransition(existingTask.getStatus(), newStatus);
        }

        taskMapper.updateEntityFromDTO(requestDTO, existingTask);
        Task updatedTask = taskRepository.save(existingTask);
        return taskMapper.toResponseDTO(updatedTask);
    }

    @Override
    public TaskResponseDTO updateStatus(Long id, TaskStatus status) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        validateTransition(existingTask.getStatus(), status);

        existingTask.setStatus(status);
        Task updatedTask = taskRepository.save(existingTask);
        return taskMapper.toResponseDTO(updatedTask);
    }

    @Override
    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(TaskStatus status) {
        return taskRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByPriority(TaskPriority priority) {
        return taskRepository.countByPriority(priority);
    }

    private void validateTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        Set<TaskStatus> allowedTransitions = VALID_TRANSITIONS.get(currentStatus);
        if (allowedTransitions == null || !allowedTransitions.contains(newStatus)) {
            throw new InvalidStateTransitionException(currentStatus, newStatus);
        }
    }
}