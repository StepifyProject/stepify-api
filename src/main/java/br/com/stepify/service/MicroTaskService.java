package br.com.stepify.service;

import br.com.stepify.command.microtask.inputs.CreateMicroTaskCommand;
import br.com.stepify.command.microtask.inputs.UpdateMicroTaskCommand;
import br.com.stepify.exception.EntityNotFoundException;
import br.com.stepify.mapper.MicroTaskMapper;
import br.com.stepify.mongo.entity.MicroTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MicroTaskService {
    private final MicroTaskMapper microTaskMapper;

    public MicroTask createMicroTask(CreateMicroTaskCommand command) {
        return microTaskMapper.fromCommand(command);
    }

    public List<MicroTask> updateMicroTaskById(List<MicroTask> microTasks, String microTaskId, UpdateMicroTaskCommand command) {
        MicroTask microTask = findMicroTaskById(microTasks, microTaskId);

        microTask.setTitle(command.title());
        microTask.setDescription(command.description());
        microTask.setCompletedAt(command.completedAt());

        if (command.status() != null) {
            microTask.setStatus(command.status());
        }

        if (command.order() != null) {
            microTask.setOrder(command.order());
        }

        return microTasks;
    }

    private MicroTask findMicroTaskById(List<MicroTask> microTasks, String id) {
        for (MicroTask microTask : microTasks) {
            if (microTask.getId().equals(id)) return microTask;
        }

        String message = String.format("Microtask with ID: %s not found while updating", id);
        throw new EntityNotFoundException(message);
    }

    public List<MicroTask> deleteMicroTaskById(List<MicroTask> microTasks, String id) {
        findMicroTaskById(microTasks, id);
        microTasks.removeIf(microTask -> microTask.getId().equals(id));
        return microTasks;
    }
}
