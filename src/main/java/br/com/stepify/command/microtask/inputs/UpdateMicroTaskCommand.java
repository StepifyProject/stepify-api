package br.com.stepify.command.microtask.inputs;

import br.com.stepify.enums.ETaskStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record UpdateMicroTaskCommand(
        @NotBlank
        String title,
        String description,
        ETaskStatus status,
        Integer order,
        LocalDateTime completedAt
) {
}
