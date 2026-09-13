package linx7a.task_management_system.controller;

import linx7a.task_management_system.model.Status;
import linx7a.task_management_system.model.Task;
import linx7a.task_management_system.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

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
        try {
            var task = taskService.getById(id);
            log.info("getById успешно завершён, id={}", id);
            return ResponseEntity.ok(task);

        } catch (NoSuchElementException e) {
            log.warn("getById: задача с id={} не найдена", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
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
            @RequestBody Task taskToCreate
    ) {
        log.info("Вызван createTask");
        try {
            var newTask = taskService.createTask(taskToCreate);
            log.info("createTask успешно выполнен, id={}", newTask.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
        } catch (IllegalArgumentException e) {
            log.warn("Не удалось создать задачу: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestBody Task taskToUpdate,
            @RequestParam(defaultValue = "false") boolean forceOpen
    ) {
        log.info("Вызван updateTask id={}, taskToUpdate={}", id, taskToUpdate);
        try {
            var updated = taskService.updateTask(id, taskToUpdate, forceOpen);
            log.info("updateTask успешно выполнен.");
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            log.warn("Задача с id={} не найдена.", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            log.warn("Не удалось обновить задачу: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> changeStatus(
            @PathVariable Long id,
            @RequestParam Status status
    ) {
        log.info("Вызван changeStatus id={}, taskToUpdate={}", id, status);
        try {
            var updated = taskService.changeStatus(id, status);
            log.info("changeStatus успешно выполнен. Новый статус задачи с id={}: {}", id, status);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            log.warn("Задача с id={} не найдена.", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            log.warn("Не удалось обновить статус задачи: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
