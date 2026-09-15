package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskMapper;
import com.example.taskmanager.dto.TaskRequestDTO;
import com.example.taskmanager.dto.TaskResponseDTO;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.TaskPriority;
import com.example.taskmanager.entity.TaskStatus;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task task;
    private TaskRequestDTO requestDTO;
    private TaskResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(TaskPriority.MEDIA);

        requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Test Task");
        requestDTO.setDescription("Test Description");
        requestDTO.setStatus(TaskStatus.PENDING);
        requestDTO.setPriority(TaskPriority.MEDIA);

        responseDTO = new TaskResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle("Test Task");
        responseDTO.setDescription("Test Description");
        responseDTO.setStatus(TaskStatus.PENDING);
        responseDTO.setPriority(TaskPriority.MEDIA);
    }

    @Test
    void create_ShouldReturnCreatedTask() {
        when(taskMapper.toEntity(requestDTO)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponseDTO(task)).thenReturn(responseDTO);

        TaskResponseDTO result = taskService.create(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Task");
        verify(taskRepository).save(task);
    }

    @Test
    void getById_WhenTaskExists_ShouldReturnTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponseDTO(task)).thenReturn(responseDTO);

        TaskResponseDTO result = taskService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_WhenTaskNotExists_ShouldThrowException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getById(1L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("Task not found with id: 1");
    }

    @Test
    void getAll_ShouldReturnPageOfTasks() {
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        when(taskRepository.findAll(any(Pageable.class))).thenReturn(taskPage);
        when(taskMapper.toResponseDTO(task)).thenReturn(responseDTO);

        Page<TaskResponseDTO> result = taskService.getAll(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void update_WhenTaskExists_ShouldReturnUpdatedTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponseDTO(task)).thenReturn(responseDTO);

        TaskResponseDTO result = taskService.update(1L, requestDTO);

        assertThat(result).isNotNull();
        verify(taskMapper).updateEntityFromDTO(requestDTO, task);
        verify(taskRepository).save(task);
    }

    @Test
    void update_WhenTaskNotExists_ShouldThrowException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(1L, requestDTO))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void updateStatus_WhenTaskExists_ShouldReturnUpdatedTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponseDTO(task)).thenReturn(responseDTO);

        TaskResponseDTO result = taskService.updateStatus(1L, TaskStatus.COMPLETED);

        assertThat(result).isNotNull();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }

    @Test
    void delete_WhenTaskExists_ShouldDeleteTask() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.delete(1L);

        verify(taskRepository).deleteById(1L);
    }

    @Test
    void delete_WhenTaskNotExists_ShouldThrowException() {
        when(taskRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.delete(1L))
                .isInstanceOf(TaskNotFoundException.class);
    }
}