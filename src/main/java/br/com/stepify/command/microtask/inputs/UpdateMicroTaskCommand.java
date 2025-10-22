package br.com.stepify.command.microtask.inputs;

import br.com.stepify.enums.ETaskStatus;

import java.time.LocalDateTime;

public record UpdateMicroTaskCommand(
        String title,
        String description,
        ETaskStatus status,
        Integer order,
        LocalDateTime completedAt
) {
}
