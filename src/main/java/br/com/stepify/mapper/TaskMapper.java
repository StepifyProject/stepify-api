package br.com.stepify.mapper;

import br.com.stepify.command.task.inputs.CreateTaskCommand;
import br.com.stepify.command.task.outputs.TaskDTO;
import br.com.stepify.mongo.entity.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskMapper {
    public Task fromCommand(CreateTaskCommand command) {
        return Task.builder()
                .title(command.title())
                .description(command.description())
                .status(command.status())
                .priority(command.priority())
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
                task.getDueDate(),
                task.getCompletedAt(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
