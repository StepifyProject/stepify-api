package br.com.stepify.service;

import br.com.stepify.command.task.inputs.CreateTaskCommand;
import br.com.stepify.command.task.inputs.UpdateTaskCommand;
import br.com.stepify.command.task.outputs.TaskDTO;
import br.com.stepify.exception.EntityNotFoundException;
import br.com.stepify.mapper.TaskMapper;
import br.com.stepify.mongo.entity.Task;
import br.com.stepify.mongo.repository.TaskRepository;
import br.com.stepify.service.action.task.UpdateTaskAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {
    private final TaskMapper taskMapper;
    private final UpdateTaskAction updateTaskAction;
    private final TaskRepository taskRepository;

    public TaskDTO createTask(CreateTaskCommand command) {
        log.info("Creating new task with title: {}", command.title());

        Task task = taskMapper.fromCommand(command);
        Task taskSaved = taskRepository.save(task);

        log.info("Task {} created successfully with ID: {}", task.getTitle(), task.getId());
        return taskMapper.toDTO(taskSaved);
    }

    public List<TaskDTO> findAllTasks() {
        log.info("Searching all tasks");
        List<Task> allTasks = taskRepository.findAll();

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

        return taskMapper.toDTO(task);
    }

    public void deleteTaskById(String id) {
        log.info("Deleting task with ID: {}", id);

        getTaskByIdOrThrow(id, "deleting");

        taskRepository.deleteById(id);
        log.info("Task with ID: {} deleted successfully", id);
    }

    private Task getTaskByIdOrThrow(String id, String context) {
        return taskRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Task with ID: %s not found while %s", id, context);
                    log.error(message);
                    return new EntityNotFoundException(message);
                });
    }
}
