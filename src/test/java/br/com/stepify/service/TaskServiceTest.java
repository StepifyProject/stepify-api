package br.com.stepify.service;

import br.com.stepify.command.microtask.inputs.CreateMicroTaskCommand;
import br.com.stepify.command.task.inputs.CreateTaskCommand;
import br.com.stepify.command.task.outputs.TaskDTO;
import br.com.stepify.enums.ETaskPriority;
import br.com.stepify.enums.ETaskStatus;
import br.com.stepify.mapper.TaskMapper;
import br.com.stepify.mongo.entity.MicroTask;
import br.com.stepify.mongo.entity.Task;
import br.com.stepify.mongo.repository.TaskRepository;
import br.com.stepify.service.action.task.UpdateTaskAction;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock private TaskMapper taskMapper;
    @Mock private UpdateTaskAction updateTaskAction;
    @Mock private MicroTaskService microTaskService;
    @Mock private TaskRepository taskRepository;
    @Mock private MongoTemplate mongoTemplate;

    @InjectMocks
    private TaskService taskService;

    @Nested
    class CreateTask {
        @Test
        void shouldCreateTaskSuccessfullyWithEmptyMicroTasks() {
            CreateTaskCommand command = createTaskCommand(Collections.emptyList());
            Task expectedTask = createTask(command, Collections.emptyList());
            TaskDTO expectedTaskDTO = createTaskDTO(expectedTask);

            when(taskMapper.fromCommand(any(CreateTaskCommand.class))).thenReturn(expectedTask);
            when(taskRepository.save(any(Task.class))).thenReturn(expectedTask);
            when(taskMapper.toDTO(any(Task.class))).thenReturn(expectedTaskDTO);

            TaskDTO result = taskService.createTask(command);

            assertNotNull(result);
            assertEquals(command.title(), result.title());
            assertEquals(command.description(), result.description());
            assertEquals(command.priority(), result.priority());
            assertEquals(command.status(), result.status());
            assertEquals(command.dueDate(), result.dueDate());
            assertTrue(result.microTasks().isEmpty());

            verify(taskMapper).fromCommand(command);
            verify(taskRepository).save(expectedTask);
            verify(taskMapper).toDTO(expectedTask);
        }

        @Test
        void shouldCreateTaskSuccessfullyWithMicroTasks() {
            CreateMicroTaskCommand microTaskCommand = createMicroTaskCommand();
            CreateTaskCommand taskCommand = createTaskCommand(List.of(microTaskCommand));
            MicroTask expectedMicroTask = createMicroTask(microTaskCommand);
            Task expectedTask = createTask(taskCommand, List.of(expectedMicroTask));
            TaskDTO expectedTaskDTO = createTaskDTO(expectedTask);

            when(taskMapper.fromCommand(any(CreateTaskCommand.class))).thenReturn(expectedTask);
            when(taskRepository.save(any(Task.class))).thenReturn(expectedTask);
            when(taskMapper.toDTO(any(Task.class))).thenReturn(expectedTaskDTO);

            TaskDTO result = taskService.createTask(taskCommand);

            assertNotNull(result);
            assertEquals(taskCommand.title(), result.title());
            assertEquals(taskCommand.description(), result.description());
            assertEquals(taskCommand.priority(), result.priority());
            assertEquals(taskCommand.status(), result.status());
            assertEquals(taskCommand.dueDate(), result.dueDate());
            assertEquals(1, result.microTasks().size());

            MicroTask resultMicroTask = result.microTasks().getFirst();
            assertEquals(microTaskCommand.title(), resultMicroTask.getTitle());
            assertEquals(microTaskCommand.description(), resultMicroTask.getDescription());
            assertEquals(microTaskCommand.status(), resultMicroTask.getStatus());
            assertEquals(microTaskCommand.order(), resultMicroTask.getOrder());

            verify(taskMapper).fromCommand(taskCommand);
            verify(taskRepository).save(expectedTask);
            verify(taskMapper).toDTO(expectedTask);
        }
    }

    private CreateTaskCommand createTaskCommand(List<CreateMicroTaskCommand> microTaskCommands) {
        return new CreateTaskCommand(
                "Title",
                "Description",
                ETaskStatus.PENDING,
                ETaskPriority.HIGH,
                microTaskCommands,
                LocalDateTime.now().plusDays(7)
        );
    }

    private CreateMicroTaskCommand createMicroTaskCommand() {
        return new CreateMicroTaskCommand(
                "MicroTask title",
                "MicroTask description",
                ETaskStatus.PENDING,
                0
        );
    }

    private Task createTask(CreateTaskCommand command, List<MicroTask> microTasks) {
        return Task.builder()
                .id("1")
                .title(command.title())
                .description(command.description())
                .status(command.status())
                .priority(command.priority())
                .dueDate(command.dueDate())
                .microTasks(microTasks)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    private MicroTask createMicroTask(CreateMicroTaskCommand microTaskCommand) {
        return MicroTask.builder()
                .id("1")
                .title(microTaskCommand.title())
                .description(microTaskCommand.description())
                .status(microTaskCommand.status())
                .order(microTaskCommand.order())
                .completedAt(LocalDateTime.now().plusDays(4))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private TaskDTO createTaskDTO(Task task) {
        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getMicroTasks(),
                task.getDueDate(),
                task.getCompletedAt(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}