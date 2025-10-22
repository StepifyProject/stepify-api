package br.com.stepify.service.action.microtask;

import br.com.stepify.command.microtask.inputs.UpdateMicroTaskCommand;
import br.com.stepify.mongo.entity.MicroTask;
import org.springframework.stereotype.Component;

@Component
public class UpdateMicroTaskAction {
    public void execute(MicroTask microTask, UpdateMicroTaskCommand command) {
        if (command.title() != null && !command.title().isBlank()) {
            microTask.setTitle(command.title());
        }

        if (command.description() != null) {
            microTask.setDescription(command.description());
        }

        if (command.status() != null) {
            microTask.setStatus(command.status());
        }

        if (command.order() != null) {
            microTask.setOrder(command.order());
        }

        if (command.completedAt() != null) {
            microTask.setCompletedAt(command.completedAt());
        }
    }
}
