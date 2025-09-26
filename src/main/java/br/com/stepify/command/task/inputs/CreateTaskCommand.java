package br.com.stepify.command.task.inputs;

import br.com.stepify.command.microtask.inputs.CreateMicroTaskCommand;
import br.com.stepify.enums.ETaskPriority;
import br.com.stepify.enums.ETaskStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

public record CreateTaskCommand(
        @NotBlank
        String title,
        String description,
        ETaskStatus status,
        ETaskPriority priority,
        List<CreateMicroTaskCommand> microTaskCommands,
        LocalDateTime dueDate
) {
}
