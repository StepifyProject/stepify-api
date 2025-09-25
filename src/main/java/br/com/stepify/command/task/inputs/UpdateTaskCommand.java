package br.com.stepify.command.task.inputs;

import br.com.stepify.enums.ETaskPriority;
import br.com.stepify.enums.ETaskStatus;

import java.time.LocalDateTime;

public record UpdateTaskCommand(
        String title,
        String description,
        ETaskStatus status,
        ETaskPriority priority,
        LocalDateTime dueDate,
        LocalDateTime completedAt
) {
}
