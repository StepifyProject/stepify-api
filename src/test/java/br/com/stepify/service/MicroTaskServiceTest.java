package br.com.stepify.service;

import br.com.stepify.command.microtask.inputs.CreateMicroTaskCommand;
import br.com.stepify.command.microtask.inputs.UpdateMicroTaskCommand;
import br.com.stepify.command.microtask.outputs.MicroTaskDTO;
import br.com.stepify.command.task.outputs.TaskDTO;
import br.com.stepify.enums.ETaskPriority;
import br.com.stepify.enums.ETaskStatus;
import br.com.stepify.exception.EntityNotFoundException;
import br.com.stepify.mapper.MicroTaskMapper;
import br.com.stepify.mongo.entity.MicroTask;
import br.com.stepify.mongo.repository.MicroTaskRepository;
import br.com.stepify.service.action.microtask.UpdateMicroTaskAction;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MicroTaskServiceTest {
    @Mock
    private MicroTaskMapper microTaskMapper;
    @Mock
    private TaskService taskService;
    @Mock
    private UpdateMicroTaskAction updateMicroTaskAction;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private MicroTaskRepository microTaskRepository;

    @InjectMocks
    private MicroTaskService microTaskService;

    @Nested
    class Create {
        @Test
        void shouldCreateMicroTaskSuccessfullyWhenTaskExists() {
            TaskDTO mockTaskDTO = createMockTaskDTO();
            CreateMicroTaskCommand microTaskCommand = createMicroTaskCommand();
            MicroTask expectedMicroTask = createMicroTask(microTaskCommand);
            MicroTaskDTO expectedMicroTaskDTO = createMicroTaskDTO(expectedMicroTask);

            when(taskService.findTaskById(anyString())).thenReturn(mockTaskDTO);
            when(microTaskMapper.fromCommand(microTaskCommand)).thenReturn(expectedMicroTask);
            when(microTaskRepository.save(expectedMicroTask)).thenReturn(expectedMicroTask);
            when(microTaskMapper.toDTO(expectedMicroTask)).thenReturn(expectedMicroTaskDTO);

            MicroTaskDTO result = microTaskService.create(microTaskCommand);

            assertNotNull(result);
            assertMicroTaskDTO(expectedMicroTask, result);

            verify(taskService).findTaskById(microTaskCommand.taskId());
            verify(microTaskMapper).fromCommand(microTaskCommand);
            verify(microTaskRepository).save(expectedMicroTask);
            verify(microTaskMapper).toDTO(expectedMicroTask);
        }

        @Test
        void shouldNotCreateMicroTaskWhenTaskDoesNotExists() {
            CreateMicroTaskCommand microTaskCommand = createMicroTaskCommand();

            doThrow(EntityNotFoundException.class).when(taskService).findTaskById(anyString());

            assertThrows(EntityNotFoundException.class, () -> microTaskService.create(microTaskCommand));

            verify(microTaskRepository, never()).save(any(MicroTask.class));
        }
    }

    @Nested
    class FindAllMicroTasks {
        @Test
        void shouldRetrieveAllMicroTasksSuccessfully() {
            MicroTask microTask1 = createMicroTask("1", 1);
            MicroTask microTask2 = createMicroTask("2", 2);
            MicroTaskDTO microTaskDTO1 = createMicroTaskDTO(microTask1);
            MicroTaskDTO microTaskDTO2 = createMicroTaskDTO(microTask2);
            List<MicroTask> expectedMicroTasks = List.of(microTask1, microTask2);

            when(microTaskRepository.findAllByDeletedFalse()).thenReturn(expectedMicroTasks);
            when(microTaskMapper.toDTO(microTask1)).thenReturn(microTaskDTO1);
            when(microTaskMapper.toDTO(microTask2)).thenReturn(microTaskDTO2);

            List<MicroTaskDTO> result = microTaskService.findAllMicroTasks();

            assertNotNull(result);
            assertMicroTaskDTO(expectedMicroTasks, result);
        }

        @Test
        void shouldReturnEmptyListWhenNoMicroTaskIsFound() {
            when(microTaskRepository.findAllByDeletedFalse()).thenReturn(Collections.emptyList());

            List<MicroTaskDTO> result = microTaskService.findAllMicroTasks();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class FindMicroTaskById {
        @Test
        void shouldReturnMicroTaskSuccessfullyWhenExists() {
            String microTaskId = "1";
            MicroTask expectedMicroTask = createMicroTask(microTaskId, 1);
            MicroTaskDTO expectedMicroTaskDTO = createMicroTaskDTO(expectedMicroTask);

            when(microTaskRepository.findByIdAndDeletedFalse(microTaskId)).thenReturn(Optional.of(expectedMicroTask));
            when(microTaskMapper.toDTO(expectedMicroTask)).thenReturn(expectedMicroTaskDTO);

            MicroTaskDTO result = microTaskService.findMicroTaskById(microTaskId);

            assertNotNull(result);
            assertMicroTaskDTO(expectedMicroTask, result);

            verify(microTaskRepository).findByIdAndDeletedFalse(microTaskId);
            verify(microTaskMapper).toDTO(expectedMicroTask);
        }

        @Test
        void shouldThrowEntityNotFoundExceptionWhenMicroTaskDoesNotExists() {
            when(microTaskRepository.findByIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> microTaskService.findMicroTaskById("1"));
        }

        @Nested
        class UpdateMicroTaskById {
            @Test
            void shouldUpdateMicroTaskSuccessfullyWhenExists() {
                String microTaskId = "1";
                UpdateMicroTaskCommand command = updateMicroTaskCommand();
                MicroTask existingMicroTask = createMicroTask(microTaskId, 1);
                MicroTask updatedMicroTask = createMicroTask(microTaskId, existingMicroTask.getTaskId(), command);
                MicroTaskDTO expectedMicroTaskDTO = createMicroTaskDTO(updatedMicroTask);

                when(microTaskRepository.findByIdAndDeletedFalse(microTaskId)).thenReturn(Optional.of(existingMicroTask));
                doNothing().when(updateMicroTaskAction).execute(existingMicroTask, command);
                when(microTaskRepository.save(existingMicroTask)).thenReturn(updatedMicroTask);
                when(microTaskMapper.toDTO(existingMicroTask)).thenReturn(expectedMicroTaskDTO);

                MicroTaskDTO result = microTaskService.updateMicroTaskById(microTaskId, command);

                assertNotNull(result);
                assertMicroTaskDTO(updatedMicroTask, result);

                verify(microTaskRepository).findByIdAndDeletedFalse(microTaskId);
                verify(updateMicroTaskAction).execute(existingMicroTask, command);
                verify(microTaskRepository).save(existingMicroTask);
                verify(microTaskMapper).toDTO(existingMicroTask);
            }

            @Test
            void shouldThrowEntityNotFoundExceptionWhenMicroTaskDoesNotExists() {
                UpdateMicroTaskCommand command = updateMicroTaskCommand();

                when(microTaskRepository.findByIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());

                assertThrows(EntityNotFoundException.class, () -> microTaskService.updateMicroTaskById("1", command));
            }
        }
    }

    @Nested
    class DeleteMicroTaskById {
        @Test
        void shouldDeleteMicroTaskSuccessfullyWhenExists() {
            String microTaskId = "1";
            MicroTask expectedMicroTask = createMicroTask(microTaskId, 1);
            UpdateResult updateResult = mock(UpdateResult.class);

            ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
            ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);

            when(microTaskRepository.findByIdAndDeletedFalse(microTaskId)).thenReturn(Optional.of(expectedMicroTask));
            when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(MicroTask.class))).thenReturn(updateResult);

            assertDoesNotThrow(() -> microTaskService.deleteMicroTaskById(microTaskId));

            verify(microTaskRepository).findByIdAndDeletedFalse(microTaskId);
            verify(mongoTemplate).updateFirst(queryCaptor.capture(), updateCaptor.capture(), eq(MicroTask.class));

            Query capturedQuery = queryCaptor.getValue();
            assertEquals(microTaskId, capturedQuery.getQueryObject().get("id"));

            Update capturedUpdate = updateCaptor.getValue();
            assertEquals(true, capturedUpdate.getUpdateObject().get("$set", Document.class).get("deleted"));
        }

        @Test
        void shouldThrowEntityNotFoundExceptionWhenMicroTaskDoesNotExists() {
            when(microTaskRepository.findByIdAndDeletedFalse(anyString())).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> microTaskService.deleteMicroTaskById("1"));

            verify(mongoTemplate, never()).updateFirst(any(Query.class), any(Update.class), eq(MicroTask.class));
        }
    }

    private void assertMicroTaskDTO(MicroTask expected, MicroTaskDTO actual) {
        assertEquals(expected.getId(), actual.id());
        assertEquals(expected.getTaskId(), actual.taskId());
        assertEquals(expected.getTitle(), actual.title());
        assertEquals(expected.getDescription(), actual.description());
        assertEquals(expected.getStatus(), actual.status());
        assertEquals(expected.getOrder(), actual.order());
        assertEquals(expected.getCreatedAt(), actual.createdAt());
        assertEquals(expected.getUpdatedAt(), actual.updatedAt());
    }

    private void assertMicroTaskDTO(List<MicroTask> expected, List<MicroTaskDTO> actual) {
        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertMicroTaskDTO(expected.get(i), actual.get(i));
        }
    }

    private CreateMicroTaskCommand createMicroTaskCommand() {
        return new CreateMicroTaskCommand(
                "taskId",
                "title",
                "description",
                ETaskStatus.IN_PROGRESS,
                0
        );
    }

    private UpdateMicroTaskCommand updateMicroTaskCommand() {
        return new UpdateMicroTaskCommand(
                "title",
                "description",
                ETaskStatus.COMPLETED,
                3,
                LocalDateTime.now()
        );
    }

    private MicroTask createMicroTask(String id, int order) {
        return MicroTask.builder()
                .id(id)
                .taskId("taskId")
                .title("title")
                .description("description")
                .status(ETaskStatus.IN_PROGRESS)
                .order(order)
                .deleted(false)
                .completedAt(LocalDateTime.now().plusDays(3))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private MicroTask createMicroTask(CreateMicroTaskCommand command) {
        return MicroTask.builder()
                .id("1")
                .taskId(command.taskId())
                .title(command.title())
                .description(command.description())
                .status(command.status())
                .order(command.order())
                .deleted(false)
                .completedAt(LocalDateTime.now().plusDays(3))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private MicroTask createMicroTask(String id, String taskId, UpdateMicroTaskCommand command) {
        return MicroTask.builder()
                .id(id)
                .taskId(taskId)
                .title(command.title())
                .description(command.description())
                .status(command.status())
                .order(command.order())
                .deleted(false)
                .completedAt(LocalDateTime.now().plusDays(3))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private MicroTaskDTO createMicroTaskDTO(MicroTask expectedMicroTask) {
        return new MicroTaskDTO(
                expectedMicroTask.getId(),
                expectedMicroTask.getTaskId(),
                expectedMicroTask.getTitle(),
                expectedMicroTask.getDescription(),
                expectedMicroTask.getStatus(),
                expectedMicroTask.getOrder(),
                expectedMicroTask.getCreatedAt(),
                expectedMicroTask.getUpdatedAt()
        );
    }

    private TaskDTO createMockTaskDTO() {
        return new TaskDTO(
                "taskId",
                "Task Title",
                "Task Description",
                ETaskStatus.IN_PROGRESS,
                ETaskPriority.HIGH,
                LocalDateTime.now().plusDays(7),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}