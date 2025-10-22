package br.com.stepify.controller;

import br.com.stepify.command.microtask.inputs.CreateMicroTaskCommand;
import br.com.stepify.command.microtask.inputs.UpdateMicroTaskCommand;
import br.com.stepify.command.microtask.outputs.MicroTaskDTO;
import br.com.stepify.service.MicroTaskService;
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
@RequestMapping("/microtasks")
@RequiredArgsConstructor
@Tag(name = "MicroTask Controller", description = "Operations related to the micro tasks")
public class MicroTaskController {
    private final MicroTaskService microTaskService;

    @Operation(summary = "Create a micro task")
    @ApiResponse(responseCode = "201", description = "Micro task created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid micro task input")
    @PostMapping
    public ResponseEntity<MicroTaskDTO> create(@RequestBody @Valid CreateMicroTaskCommand command) {
        MicroTaskDTO response = microTaskService.create(command);
        URI location = URI.create("/microtasks/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Find all micro tasks")
    @ApiResponse(responseCode = "200", description = "Micro tasks found successfully")
    @GetMapping
    public ResponseEntity<List<MicroTaskDTO>> findAll() {
        return ResponseEntity.ok(microTaskService.findAllMicroTasks());
    }

    @Operation(summary = "Find a micro task by ID")
    @ApiResponse(responseCode = "200", description = "Micro task found successfully")
    @ApiResponse(responseCode = "404", description = "Micro task not found")
    @GetMapping("/{microTaskId}")
    public ResponseEntity<MicroTaskDTO> findById(@PathVariable(value = "microTaskId") String microTaskId) {
        return ResponseEntity.ok(microTaskService.findMicroTaskById(microTaskId));
    }

    @Operation(summary = "Update a micro task by ID")
    @ApiResponse(responseCode = "200", description = "Micro task updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid micro task input")
    @ApiResponse(responseCode = "404", description = "Micro task not found")
    @PatchMapping("/{microTaskId}")
    public ResponseEntity<MicroTaskDTO> updateById(@PathVariable(value = "microTaskId") String microTaskId,
                                                   @RequestBody @Valid UpdateMicroTaskCommand command) {
        return ResponseEntity.ok(microTaskService.updateMicroTaskById(microTaskId, command));
    }

    @Operation(summary = "Delete a micro task by ID")
    @ApiResponse(responseCode = "204", description = "Micro task deleted successfully")
    @ApiResponse(responseCode = "404", description = "Micro task not found")
    @DeleteMapping("/{microTaskId}")
    public ResponseEntity<MicroTaskDTO> deleteById(@PathVariable(value = "microTaskId") String microTaskId) {
        microTaskService.deleteMicroTaskById(microTaskId);
        return ResponseEntity.noContent().build();
    }
}
