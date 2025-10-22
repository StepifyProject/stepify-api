package br.com.stepify.command.microtask.outputs;

import br.com.stepify.enums.ETaskStatus;

import java.time.LocalDateTime;

public record MicroTaskDTO(
        String id,
        String taskId,
        String title,
        String description,
        ETaskStatus status,
        Integer order,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
