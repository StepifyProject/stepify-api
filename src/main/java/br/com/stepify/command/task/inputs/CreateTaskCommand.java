package br.com.stepify.command.task.inputs;

import br.com.stepify.enums.ETaskPriority;
import br.com.stepify.enums.ETaskStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record CreateTaskCommand(
        @NotBlank
        String title,
        String description,
        ETaskStatus status,
        ETaskPriority priority,
        LocalDateTime dueDate
) {
}
