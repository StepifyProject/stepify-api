package br.com.stepify.mapper;

import br.com.stepify.command.task.inputs.CreateTaskCommand;
import br.com.stepify.command.task.outputs.TaskDTO;
import br.com.stepify.mongo.entity.Task;
import br.com.stepify.service.MicroTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskMapper {
    private final MicroTaskService microTaskService;

    public Task fromCommand(CreateTaskCommand command) {
        return Task.builder()
                .title(command.title())
                .description(command.description())
                .status(command.status())
                .priority(command.priority())
                .microTasks(
                        command.microTaskCommands().stream()
                                .map(microTaskService::createMicroTask)
                                .toList()
                )
                .dueDate(command.dueDate())
                .build();
    }

    public TaskDTO toDTO(Task task) {
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
