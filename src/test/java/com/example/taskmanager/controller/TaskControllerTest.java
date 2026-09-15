package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequestDTO;
import com.example.taskmanager.dto.TaskResponseDTO;
import com.example.taskmanager.entity.TaskPriority;
import com.example.taskmanager.entity.TaskStatus;
import com.example.taskmanager.service.TaskService;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        // Register a simple Page serializer for testing
        SimpleModule pageModule = new SimpleModule();
        @SuppressWarnings("unchecked")
        Class<? extends Page<?>> pageClass = (Class<? extends Page<?>>) (Class<?>) Page.class;
        pageModule.addSerializer(pageClass, new JsonSerializer<Page<?>>() {
            @Override
            public void serialize(Page<?> page, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeStartObject();
                gen.writeArrayFieldStart("content");
                for (Object item : page.getContent()) {
                    provider.defaultSerializeValue(item, gen);
                }
                gen.writeEndArray();
                gen.writeNumberField("totalElements", page.getTotalElements());
                gen.writeNumberField("totalPages", page.getTotalPages());
                gen.writeNumberField("size", page.getSize());
                gen.writeNumberField("number", page.getNumber());
                gen.writeBooleanField("first", page.isFirst());
                gen.writeBooleanField("last", page.isLast());
                gen.writeNumberField("numberOfElements", page.getNumberOfElements());
                gen.writeBooleanField("empty", page.isEmpty());
                gen.writeEndObject();
            }
        });
        objectMapper.registerModule(pageModule);
    }

    @Test
    void create_ShouldReturnCreatedTask() throws Exception {
        TaskResponseDTO responseDTO = new TaskResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle("Test Task");
        responseDTO.setStatus(TaskStatus.PENDING);
        responseDTO.setPriority(TaskPriority.MEDIA);

        when(taskService.create(any(TaskRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Test Task",
                                    "description": "Test Description",
                                    "status": "PENDING",
                                    "priority": "MEDIA"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void create_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "",
                                    "status": "PENDING",
                                    "priority": "MEDIA"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    void getById_WhenTaskExists_ShouldReturnTask() throws Exception {
        TaskResponseDTO responseDTO = new TaskResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle("Test Task");
        responseDTO.setStatus(TaskStatus.PENDING);
        responseDTO.setPriority(TaskPriority.MEDIA);

        when(taskService.getById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void getById_WhenTaskNotExists_ShouldReturnNotFound() throws Exception {
        when(taskService.getById(1L)).thenThrow(new com.example.taskmanager.exception.TaskNotFoundException(1L));

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id: 1"));
    }

    @Test
    void getAll_ShouldReturnPageOfTasks() throws Exception {
        TaskResponseDTO responseDTO = new TaskResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle("Test Task");
        responseDTO.setStatus(TaskStatus.PENDING);
        responseDTO.setPriority(TaskPriority.MEDIA);

        Page<TaskResponseDTO> page = new PageImpl<>(List.of(responseDTO));
        when(taskService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Test Task"));
    }

    @Test
    void getAll_WithStatusFilter_ShouldReturnFilteredTasks() throws Exception {
        TaskResponseDTO responseDTO = new TaskResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle("Test Task");
        responseDTO.setStatus(TaskStatus.PENDING);
        responseDTO.setPriority(TaskPriority.MEDIA);

        Page<TaskResponseDTO> page = new PageImpl<>(List.of(responseDTO));
        when(taskService.getByStatus(eq(TaskStatus.PENDING), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/tasks")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void update_ShouldReturnUpdatedTask() throws Exception {
        TaskResponseDTO responseDTO = new TaskResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle("Updated Task");
        responseDTO.setStatus(TaskStatus.IN_PROGRESS);
        responseDTO.setPriority(TaskPriority.ALTA);

        when(taskService.update(eq(1L), any(TaskRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Updated Task",
                                    "description": "Updated Description",
                                    "status": "IN_PROGRESS",
                                    "priority": "ALTA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateStatus_ShouldReturnUpdatedTask() throws Exception {
        TaskResponseDTO responseDTO = new TaskResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle("Test Task");
        responseDTO.setStatus(TaskStatus.COMPLETED);
        responseDTO.setPriority(TaskPriority.MEDIA);

        when(taskService.updateStatus(1L, TaskStatus.COMPLETED)).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/tasks/1/status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        doNothing().when(taskService).delete(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).delete(1L);
    }

    @Test
    void delete_WhenTaskNotExists_ShouldReturnNotFound() throws Exception {
        doThrow(new com.example.taskmanager.exception.TaskNotFoundException(1L)).when(taskService).delete(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNotFound());
    }
}