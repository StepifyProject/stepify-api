package br.com.stepify.command.microtask.inputs;

import br.com.stepify.enums.ETaskStatus;
import jakarta.validation.constraints.NotBlank;

public record CreateMicroTaskCommand(
        @NotBlank
        String title,
        String description,
        ETaskStatus status,
        Integer order
) {
}
