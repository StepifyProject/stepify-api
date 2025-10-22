package br.com.stepify.controller;

import br.com.stepify.command.task.inputs.CreateTaskCommand;
import br.com.stepify.command.task.inputs.UpdateTaskCommand;
import br.com.stepify.command.task.outputs.TaskDTO;
import br.com.stepify.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Task Controller", description = "Operations related to the tasks")
public class TaskController {
    private final TaskService taskService;

    @Operation(summary = "Create a task")
    @ApiResponse(responseCode = "201", description = "Task created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid task input")
    @PostMapping
    public ResponseEntity<TaskDTO> create(@RequestBody @Valid CreateTaskCommand command) {
        TaskDTO response = taskService.createTask(command);
        URI location = URI.create("/tasks/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Find all tasks")
    @ApiResponse(responseCode = "200", description = "Tasks found successfully")
    @GetMapping
    public ResponseEntity<List<TaskDTO>> findAll() {
        return ResponseEntity.ok(taskService.findAllTasks());
    }

    @Operation(summary = "Find a task by ID")
    @ApiResponse(responseCode = "200", description = "Task found successfully")
    @ApiResponse(responseCode = "404", description = "Task not found")
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> findById(@PathVariable(value = "taskId") String taskId) {
        return ResponseEntity.ok(taskService.findTaskById(taskId));
    }

    @Operation(summary = "Update a task by ID")
    @ApiResponse(responseCode = "200", description = "Task updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid task input")
    @ApiResponse(responseCode = "404", description = "Task not found")
    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateById(@PathVariable(value = "taskId") String taskId,
                                              @RequestBody @Valid UpdateTaskCommand command) {
        return ResponseEntity.ok(taskService.updateTaskById(taskId, command));
    }

    @Operation(summary = "Delete a task by ID")
    @ApiResponse(responseCode = "204", description = "Task deleted successfully")
    @ApiResponse(responseCode = "404", description = "Task not found")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<TaskDTO> deleteTaskById(@PathVariable(value = "taskId") String taskId) {
        taskService.deleteTaskById(taskId);
        return ResponseEntity.noContent().build();
    }
}
