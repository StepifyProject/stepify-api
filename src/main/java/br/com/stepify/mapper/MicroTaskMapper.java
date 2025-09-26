package br.com.stepify.mapper;

import br.com.stepify.command.microtask.inputs.CreateMicroTaskCommand;
import br.com.stepify.mongo.entity.MicroTask;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MicroTaskMapper {
    public MicroTask fromCommand(CreateMicroTaskCommand command) {
        return MicroTask.builder()
                .id(new ObjectId().toString())
                .title(command.title())
                .description(command.description())
                .status(command.status())
                .order(command.order())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
