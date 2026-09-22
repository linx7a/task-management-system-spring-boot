package linx7a.task_management_system.controller;

import jakarta.validation.Valid;
import linx7a.task_management_system.model.Status;
import linx7a.task_management_system.model.Task;
import linx7a.task_management_system.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private static final Logger log = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(
            @PathVariable Long id) {
        log.info("Вызван getById с id={}", id);
        var task = taskService.getById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAll() {
        log.info("Вызван getAll");
        var tasks = taskService.getAll();
        log.info("getAll успешно завершён, найдено задач: {}", tasks.size());
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestBody @Valid Task taskToCreate
    ) {
        log.info("Вызван createTask");
        var newTask = taskService.createTask(taskToCreate);
        log.info("createTask успешно выполнен, id={}", newTask.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestBody @Valid Task taskToUpdate
    ) {
        log.info("Вызван updateTask id={}, taskToUpdate={}", id, taskToUpdate);
        var updated = taskService.updateTask(id, taskToUpdate);
        log.info("updateTask успешно выполнен.");
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> changeStatus(
            @PathVariable Long id,
            @RequestParam Status status
    ) {
        log.info("Вызван changeStatus id={}, taskToUpdate={}", id, status);
        var updated = taskService.changeStatus(id, status);
        log.info("changeStatus успешно выполнен. Новый статус задачи с id={}: {}", id, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id
    ) {
        log.info("Вызван deleteTask id={}", id);
        taskService.deleteTask(id);
        log.info("deleteTask успешно выполнен.");
        return ResponseEntity.noContent().build();

    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Task> startTask(
            @PathVariable Long id
    ) {
        log.info("Вызван startTask id={}", id);
        var started = taskService.startTask(id);
        log.info("startTask успешно выполнен.");
        return ResponseEntity.ok(started);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Task> completeTask(
            @PathVariable Long id
    ) {
        log.info("Вызван completeTask id={}", id);
        var completed = taskService.completeTask(id);
        log.info("completeTask успешно выполнен.");
        return ResponseEntity.ok(completed);
    }
}
