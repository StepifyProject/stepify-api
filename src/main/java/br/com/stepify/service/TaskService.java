package br.com.stepify.service;

import br.com.stepify.command.microtask.inputs.CreateMicroTaskCommand;
import br.com.stepify.command.microtask.inputs.UpdateMicroTaskCommand;
import br.com.stepify.command.task.inputs.CreateTaskCommand;
import br.com.stepify.command.task.inputs.UpdateTaskCommand;
import br.com.stepify.command.task.outputs.TaskDTO;
import br.com.stepify.exception.EntityNotFoundException;
import br.com.stepify.mapper.TaskMapper;
import br.com.stepify.mongo.entity.MicroTask;
import br.com.stepify.mongo.entity.Task;
import br.com.stepify.mongo.repository.TaskRepository;
import br.com.stepify.service.action.task.UpdateTaskAction;
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
public class TaskService {
    private final TaskMapper taskMapper;
    private final UpdateTaskAction updateTaskAction;
    private final MicroTaskService microTaskService;
    private final TaskRepository taskRepository;
    private final MongoTemplate mongoTemplate;

    public TaskDTO createTask(CreateTaskCommand command) {
        log.info("Creating new task with title: {}", command.title());

        Task task = taskMapper.fromCommand(command);
        Task taskSaved = taskRepository.save(task);

        log.info("Task {} created successfully with ID: {}", task.getTitle(), task.getId());
        return taskMapper.toDTO(taskSaved);
    }

    public List<TaskDTO> findAllTasks() {
        log.info("Searching all tasks");
        List<Task> allTasks = taskRepository.findAllByDeletedFalse();

        return allTasks.stream()
                .map(taskMapper::toDTO)
                .toList();
    }

    public TaskDTO findTaskById(String id) {
        log.info("Searching task with ID: {}", id);

        Task task = getTaskByIdOrThrow(id, "searching");

        log.info("Task with ID: {} found", id);
        return taskMapper.toDTO(task);
    }

    public TaskDTO updateTaskById(String id, UpdateTaskCommand command) {
        log.info("Updating task with ID: {}", id);

        Task task = getTaskByIdOrThrow(id, "updating");

        updateTaskAction.execute(task, command);
        log.info("Task with ID: {} updated successfully", id);

        taskRepository.save(task);

        return taskMapper.toDTO(task);
    }

    public void deleteTaskById(String id) {
        log.info("Deleting task with ID: {}", id);

        getTaskByIdOrThrow(id, "deleting");

        Query query = new Query(Criteria.where("id").is(id));
        Update update = Update.update("deleted", true);

        mongoTemplate.updateFirst(query, update, Task.class);

        log.info("Task with ID: {} deleted successfully", id);
    }

    public TaskDTO addMicroTask(CreateMicroTaskCommand command, String taskId) {
        log.info("Adding microtask to task with ID: {}", taskId);

        Task existingTask = getTaskByIdOrThrow(taskId, "adding microtask");
        MicroTask microTask = microTaskService.createMicroTask(command);

        existingTask.getMicroTasks().add(microTask);
        Task taskSaved = taskRepository.save(existingTask);

        log.info("Microtask {} added successfully with ID: {}", microTask.getTitle(), microTask.getId());

        return taskMapper.toDTO(taskSaved);
    }

    public TaskDTO updateMicroTaskById(UpdateMicroTaskCommand command, String microTaskId, String taskId) {
        log.info("Updating microtask with ID: {}", microTaskId);

        Task existingTask = getTaskByIdOrThrow(taskId, "updating microtask");

        List<MicroTask> updatedMicroTasks = microTaskService.updateMicroTaskById(existingTask.getMicroTasks(), microTaskId, command);
        existingTask.setMicroTasks(updatedMicroTasks);
        Task taskSaved = taskRepository.save(existingTask);

        log.info("Microtask with ID: {} updated successfully", microTaskId);

        return taskMapper.toDTO(taskSaved);
    }

    public TaskDTO deleteMicroTaskById(String microTaskId, String taskId) {
        log.info("Deleting microtask with ID: {}", microTaskId);

        Task existingTask = getTaskByIdOrThrow(taskId, "deleting microtask");

        List<MicroTask> updatedMicroTasks = microTaskService.deleteMicroTaskById(existingTask.getMicroTasks(), microTaskId);
        existingTask.setMicroTasks(updatedMicroTasks);
        Task taskSaved = taskRepository.save(existingTask);

        log.info("Microtask with ID: {} deleted successfully", microTaskId);

        return taskMapper.toDTO(taskSaved);
    }

    private Task getTaskByIdOrThrow(String id, String context) {
        return taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    String message = String.format("Task with ID: %s not found while %s", id, context);
                    log.error(message);
                    return new EntityNotFoundException(message);
                });
    }
}
