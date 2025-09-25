package br.com.stepify.service.action.task;

import br.com.stepify.command.task.inputs.UpdateTaskCommand;
import br.com.stepify.mongo.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class UpdateTaskAction {
    public void execute(Task task, UpdateTaskCommand command) {
        if (command.title() != null) {
            task.setTitle(command.title());
        }

        task.setDescription(command.description());
        task.setStatus(command.status());
        task.setPriority(command.priority());
        task.setDueDate(command.dueDate());
        task.setCompletedAt(command.completedAt());
    }
}
