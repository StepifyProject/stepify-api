package br.com.stepify.command.task.outputs;

import br.com.stepify.enums.ETaskPriority;
import br.com.stepify.enums.ETaskStatus;
import br.com.stepify.mongo.entity.MicroTask;

import java.time.LocalDateTime;
import java.util.List;

public record TaskDTO(
        String id,
        String title,
        String description,
        ETaskStatus status,
        ETaskPriority priority,
        List<MicroTask> microTasks,
        LocalDateTime dueDate,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
