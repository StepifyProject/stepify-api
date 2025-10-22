package br.com.stepify.mapper;

import br.com.stepify.command.microtask.inputs.CreateMicroTaskCommand;
import br.com.stepify.command.microtask.outputs.MicroTaskDTO;
import br.com.stepify.mongo.entity.MicroTask;
import org.springframework.stereotype.Component;

@Component
public class MicroTaskMapper {
    public MicroTask fromCommand(CreateMicroTaskCommand command) {
        return MicroTask.builder()
                .taskId(command.taskId())
                .title(command.title())
                .description(command.description())
                .status(command.status())
                .order(command.order())
                .build();
    }

    public MicroTaskDTO toDTO(MicroTask microTask) {
        return new MicroTaskDTO(
                microTask.getId(),
                microTask.getTaskId(),
                microTask.getTitle(),
                microTask.getDescription(),
                microTask.getStatus(),
                microTask.getOrder(),
                microTask.getCreatedAt(),
                microTask.getUpdatedAt()
        );
    }
}
