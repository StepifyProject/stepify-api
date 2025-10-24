package br.com.stepify.service;

import br.com.stepify.command.task.inputs.CreateTaskCommand;
import br.com.stepify.command.task.inputs.UpdateTaskCommand;
import br.com.stepify.command.task.outputs.TaskDTO;
import br.com.stepify.enums.ETaskPriority;
import br.com.stepify.enums.ETaskStatus;
import br.com.stepify.exception.EntityNotFoundException;
import br.com.stepify.mapper.TaskMapper;
import br.com.stepify.mongo.entity.Task;
import br.com.stepify.mongo.repository.TaskRepository;
import br.com.stepify.service.action.task.UpdateTaskAction;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private UpdateTaskAction updateTaskAction;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Nested
    class CreateTask {
        @Test
        void shouldCreateTaskSuccessfully() {
            CreateTaskCommand taskCommand = createTaskCommand("Title");
            Task expectedTask = createTask("1", taskCommand);
            TaskDTO expectedTaskDTO = createTaskDTO(expectedTask);

            when(taskMapper.fromCommand(any(CreateTaskCommand.class))).thenReturn(expectedTask);
            when(taskRepository.save(any(Task.class))).thenReturn(expectedTask);
            when(taskMapper.toDTO(any(Task.class))).thenReturn(expectedTaskDTO);

            TaskDTO result = taskService.createTask(taskCommand);

            assertNotNull(result);
            assertTaskDTO(taskCommand, result);

            verify(taskMapper).fromCommand(taskCommand);
            verify(taskRepository).save(expectedTask);
            verify(taskMapper).toDTO(expectedTask);
        }
    }

    @Nested
    class FindAllTasks {
        @Test
        void shouldReturnAllTasksSuccessfully() {
            CreateTaskCommand taskCommand1 = createTaskCommand("Task 1");
            CreateTaskCommand taskCommand2 = createTaskCommand("Task 2");
            Task expectedTask1 = createTask("1", taskCommand1);
            Task expectedTask2 = createTask("2", taskCommand2);
            List<Task> expectedTaskList = List.of(expectedTask1, expectedTask2);
            TaskDTO expectedTaskDTO1 = createTaskDTO(expectedTask1);
            TaskDTO expectedTaskDTO2 = createTaskDTO(expectedTask2);

            when(taskRepository.findAllByDeletedFalse()).thenReturn(expectedTaskList);
            when(taskMapper.toDTO(expectedTask1)).thenReturn(expectedTaskDTO1);
            when(taskMapper.toDTO(expectedTask2)).thenReturn(expectedTaskDTO2);

            List<TaskDTO> result = taskService.findAllTasks();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("1", result.get(0).id());
            assertEquals("2", result.get(1).id());

            verify(taskRepository).findAllByDeletedFalse();
            verify(taskMapper, times(2)).toDTO(any(Task.class));
        }

        @Test
        void shouldReturnEmptyListWhenNoTasksExist() {
            when(taskRepository.findAllByDeletedFalse()).thenReturn(Collections.emptyList());

            List<TaskDTO> result = taskService.findAllTasks();

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(taskRepository).findAllByDeletedFalse();
            verify(taskMapper, never()).toDTO(any());
        }
    }

    @Nested
    class FindTaskById {
        @Test
        void shouldReturnTaskSuccessfullyWhenExists() {
            String taskId = "1";
            Task expectedTask = createTask(taskId);
            TaskDTO expectedTaskDTO = createTaskDTO(expectedTask);

            when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(expectedTask));
            when(taskMapper.toDTO(expectedTask)).thenReturn(expectedTaskDTO);

            TaskDTO result = taskService.findTaskById(taskId);

            assertNotNull(result);
            assertTaskDTO(expectedTask, result);

            verify(taskRepository).findByIdAndDeletedFalse(taskId);
            verify(taskMapper).toDTO(expectedTask);
        }

        @Test
        void shouldThrowEntityNotFoundExceptionWhenTaskDoesNotExists() {
            when(taskRepository.findByIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> taskService.findTaskById("1"));
        }
    }

    @Nested
    class UpdateTaskById {
        @Test
        void shouldUpdateTaskSuccessfullyWhenExists() {
            String taskId = "1";
            UpdateTaskCommand command = createUpdateTaskCommand();
            Task existingTask = createTask(taskId);
            Task updatedTask = createTask(command);
            TaskDTO expectedTaskDTO = createTaskDTO(updatedTask);

            when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(existingTask));
            doNothing().when(updateTaskAction).execute(existingTask, command);
            when(taskRepository.save(existingTask)).thenReturn(updatedTask);
            when(taskMapper.toDTO(existingTask)).thenReturn(expectedTaskDTO);

            TaskDTO result = taskService.updateTaskById(taskId, command);

            assertNotNull(result);
            assertTaskDTO(updatedTask, result);

            verify(taskRepository).findByIdAndDeletedFalse(taskId);
            verify(updateTaskAction).execute(existingTask, command);
            verify(taskRepository).save(existingTask);
            verify(taskMapper).toDTO(existingTask);
        }

        @Test
        void shouldThrowEntityNotFoundExceptionWhenTaskDoesNotExists() {
            UpdateTaskCommand command = createUpdateTaskCommand();

            when(taskRepository.findByIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> taskService.updateTaskById("1", command));
        }
    }

    @Nested
    class DeleteTaskById {
        @Test
        void shouldDeleteTaskSuccessfullyWhenExists() {
            String taskId = "1";
            Task expectedTask = createTask(taskId);
            UpdateResult updateResult = mock(UpdateResult.class);

            ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
            ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);

            when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(expectedTask));
            when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Task.class))).thenReturn(updateResult);

            assertDoesNotThrow(() -> taskService.deleteTaskById(taskId));

            verify(taskRepository).findByIdAndDeletedFalse(taskId);
            verify(mongoTemplate).updateFirst(queryCaptor.capture(), updateCaptor.capture(), eq(Task.class));

            Query capturedQuery = queryCaptor.getValue();
            assertEquals(taskId, capturedQuery.getQueryObject().get("id"));

            Update capturedUpdate = updateCaptor.getValue();
            assertEquals(true, capturedUpdate.getUpdateObject().get("$set", Document.class).get("deleted"));
        }

        @Test
        void shouldThrowEntityNotFoundExceptionWhenTaskDoesNotExists() {
            when(taskRepository.findByIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> taskService.deleteTaskById("1"));

            verify(mongoTemplate, never()).updateFirst(any(Query.class), any(Update.class), eq(Task.class));
        }
    }

    private void assertTaskDTO(CreateTaskCommand expected, TaskDTO actual) {
        assertEquals(expected.title(), actual.title());
        assertEquals(expected.description(), actual.description());
        assertEquals(expected.priority(), actual.priority());
        assertEquals(expected.status(), actual.status());
        assertEquals(expected.dueDate(), actual.dueDate());
    }

    private void assertTaskDTO(Task expected, TaskDTO actual) {
        assertEquals(expected.getTitle(), actual.title());
        assertEquals(expected.getDescription(), actual.description());
        assertEquals(expected.getPriority(), actual.priority());
        assertEquals(expected.getStatus(), actual.status());
        assertEquals(expected.getDueDate(), actual.dueDate());
    }

    private CreateTaskCommand createTaskCommand(String title) {
        return new CreateTaskCommand(
                title,
                "Description",
                ETaskStatus.PENDING,
                ETaskPriority.HIGH,
                LocalDateTime.now().plusDays(7)
        );
    }

    private UpdateTaskCommand createUpdateTaskCommand() {
        return new UpdateTaskCommand(
                "Title Updated",
                "Description Updated",
                ETaskStatus.IN_PROGRESS,
                ETaskPriority.LOW,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now()
        );
    }

    private Task createTask(String id, CreateTaskCommand command) {
        return Task.builder()
                .id(id)
                .title(command.title())
                .description(command.description())
                .status(command.status())
                .priority(command.priority())
                .dueDate(command.dueDate())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    private Task createTask(String id) {
        return Task.builder()
                .id(id)
                .title("Task " + id)
                .description("Description " + id)
                .status(ETaskStatus.PENDING)
                .priority(ETaskPriority.MEDIUM)
                .dueDate(LocalDateTime.now().plusDays(3))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    private Task createTask(UpdateTaskCommand command) {
        return Task.builder()
                .id("1")
                .title(command.title())
                .description(command.description())
                .status(command.status())
                .priority(command.priority())
                .dueDate(command.dueDate())
                .completedAt(command.completedAt())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    private TaskDTO createTaskDTO(Task task) {
        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCompletedAt(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}