package br.com.stepify.service;

import br.com.stepify.command.microtask.inputs.CreateMicroTaskCommand;
import br.com.stepify.command.microtask.inputs.UpdateMicroTaskCommand;
import br.com.stepify.command.microtask.outputs.MicroTaskDTO;
import br.com.stepify.exception.EntityNotFoundException;
import br.com.stepify.mapper.MicroTaskMapper;
import br.com.stepify.mongo.entity.MicroTask;
import br.com.stepify.mongo.repository.MicroTaskRepository;
import br.com.stepify.service.action.microtask.UpdateMicroTaskAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MicroTaskService {
    private final MicroTaskMapper microTaskMapper;
    private final TaskService taskService;
    private final UpdateMicroTaskAction updateMicroTaskAction;
    private final MongoTemplate mongoTemplate;
    private final MicroTaskRepository microTaskRepository;

    public MicroTaskDTO create(CreateMicroTaskCommand command) {
        log.info("Creating new micro task with title: {}", command.title());

        taskService.findTaskById(command.taskId());

        MicroTask microTask = microTaskMapper.fromCommand(command);
        MicroTask microTaskSaved = microTaskRepository.save(microTask);

        log.info("Micro task {} created successfully with ID: {}", microTaskSaved.getTitle(), microTaskSaved.getId());
        return microTaskMapper.toDTO(microTaskSaved);
    }

    public List<MicroTaskDTO> findAllMicroTasks() {
        log.info("Searching all micro tasks");
        List<MicroTask> allMicroTasks = microTaskRepository.findAllByDeletedFalse();

        return allMicroTasks.stream()
                .map(microTaskMapper::toDTO)
                .toList();
    }

    public MicroTaskDTO findMicroTaskById(String id) {
        log.info("Searching micro task with ID: {}", id);

        MicroTask microTask= getMicroTaskByIdOrThrow(id, "searching");

        log.info("Micro task with ID: {} found", id);
        return microTaskMapper.toDTO(microTask);
    }

    public MicroTaskDTO updateMicroTaskById(String id, UpdateMicroTaskCommand command) {
        log.info("Updating micro task with ID: {}", id);

        MicroTask microTask = getMicroTaskByIdOrThrow(id, "updating");

        updateMicroTaskAction.execute(microTask, command);
        log.info("Micro task with ID: {} updated successfully", id);

        microTaskRepository.save(microTask);

        return microTaskMapper.toDTO(microTask);
    }

    public void deleteMicroTaskById(String id) {
        log.info("Deleting micro task with ID: {}", id);

        getMicroTaskByIdOrThrow(id, "deleting");

        Query query = new Query(Criteria.where("id").is(id));
        Update update = Update.update("deleted", true);

        mongoTemplate.updateFirst(query, update, MicroTask.class);

        log.info("Micro task with ID: {} deleted successfully", id);
    }

    private MicroTask getMicroTaskByIdOrThrow(String id, String context) {
        return microTaskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    String message = String.format("Micro task with ID: %s not found while %s", id, context);
                    log.error(message);
                    return new EntityNotFoundException(message);
                });
    }
}
