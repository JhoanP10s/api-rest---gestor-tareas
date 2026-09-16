package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.TaskPriority;
import com.example.taskmanager.entity.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        task1 = new Task();
        task1.setTitle("Task 1");
        task1.setDescription("Description 1");
        task1.setStatus(TaskStatus.PENDING);
        task1.setPriority(TaskPriority.ALTA);

        task2 = new Task();
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        task2.setPriority(TaskPriority.MEDIA);

        task3 = new Task();
        task3.setTitle("Task 3");
        task3.setDescription("Description 3");
        task3.setStatus(TaskStatus.COMPLETED);
        task3.setPriority(TaskPriority.BAJA);

        entityManager.persistAndFlush(task1);
        entityManager.persistAndFlush(task2);
        entityManager.persistAndFlush(task3);
    }

    @Test
    void findByStatus_ShouldReturnTasksWithGivenStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> result = taskRepository.findByStatus(TaskStatus.PENDING, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Task 1");
    }

    @Test
    void findByPriority_ShouldReturnTasksWithGivenPriority() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> result = taskRepository.findByPriority(TaskPriority.ALTA, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Task 1");
    }

    @Test
    void findByStatusAndPriority_ShouldReturnTasksMatchingBoth() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> result = taskRepository.findByStatusAndPriority(TaskStatus.PENDING, TaskPriority.ALTA, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Task 1");
    }

    @Test
    void searchByKeyword_ShouldReturnTasksMatchingTitleOrDescription() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> result = taskRepository.searchByKeyword("Task 1", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Task 1");
    }

    @Test
    void searchByKeyword_ShouldMatchDescription() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> result = taskRepository.searchByKeyword("Description 2", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Task 2");
    }

    @Test
    void searchByKeyword_ShouldBeCaseInsensitive() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> result = taskRepository.searchByKeyword("task 1", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        long count = taskRepository.countByStatus(TaskStatus.PENDING);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByPriority_ShouldReturnCorrectCount() {
        long count = taskRepository.countByPriority(TaskPriority.ALTA);
        assertThat(count).isEqualTo(1);
    }
}