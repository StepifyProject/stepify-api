package br.com.stepify.service;

import br.com.stepify.command.microtask.inputs.CreateMicroTaskCommand;
import br.com.stepify.command.microtask.inputs.UpdateMicroTaskCommand;
import br.com.stepify.mapper.MicroTaskMapper;
import br.com.stepify.mongo.entity.MicroTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MicroTaskService {
    private final MicroTaskMapper microTaskMapper;

    public MicroTask createMicroTask(CreateMicroTaskCommand command) {
        return microTaskMapper.fromCommand(command);
    }

    public List<MicroTask> updateMicroTaskById(List<MicroTask> microTasks, String id, UpdateMicroTaskCommand command) {
        Optional<MicroTask> microTaskOptional = findMicroTaskById(microTasks, id);
        if (microTaskOptional.isEmpty()) return microTasks;

        MicroTask microTask = microTaskOptional.get();

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

    private Optional<MicroTask> findMicroTaskById(List<MicroTask> microTasks, String id) {
        for (MicroTask microTask : microTasks) {
            if (microTask.getId().equals(id)) return Optional.of(microTask);
        }

        return Optional.empty();
    }

    public List<MicroTask> deleteMicroTaskById(List<MicroTask> microTasks, String id) {
        microTasks.removeIf(microTask -> microTask.getId().equals(id));
        return microTasks;
    }
}
