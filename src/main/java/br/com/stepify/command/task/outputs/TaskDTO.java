package br.com.stepify.command.task.outputs;

import br.com.stepify.enums.ETaskPriority;
import br.com.stepify.enums.ETaskStatus;

import java.time.LocalDateTime;

public record TaskDTO(
        String id,
        String title,
        String description,
        ETaskStatus status,
        ETaskPriority priority,
        LocalDateTime dueDate,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
