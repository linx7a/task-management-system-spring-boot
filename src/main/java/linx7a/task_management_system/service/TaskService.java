package linx7a.task_management_system.service;

import linx7a.task_management_system.model.Status;
import linx7a.task_management_system.model.Task;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {
    private final Map<Long, Task> taskMap;
    private final AtomicLong idCounter;

    public TaskService() {
        taskMap = new HashMap<>();
        idCounter = new AtomicLong();
    }

    public Task getById(Long id) {
        if (!taskMap.containsKey(id)) {
            throw new NoSuchElementException("Задача с id: " + id + " не найдена.");
        }
        return taskMap.get(id);
    }

    public List<Task> getAll() {
        return taskMap.values().stream().toList();
    }

    public Task createTask(Task taskToCreate) {
        if (taskToCreate.id() != null) {
            throw new IllegalArgumentException("id должен быть пустым.");
        }
        if (taskToCreate.status() != null) {
            throw new IllegalArgumentException("Статус должен быть пустым");
        }
        var newTask = new Task(
                idCounter.incrementAndGet(),
                taskToCreate.creatorId(),
                taskToCreate.assignedUserId(),
                Status.CREATED,
                taskToCreate.createDateTime(),
                taskToCreate.deadlineDate(),
                taskToCreate.priority()
        );
        taskMap.put(newTask.id(), newTask);
        return newTask;
    }

    public Task updateTask(Long id, Task taskToUpdate, boolean forceReopen) {
        if (!taskMap.containsKey(id)) {
            throw new NoSuchElementException("Задача с id: " + id + " не найдена.");
        }
        if (taskToUpdate.id() != null) {
            throw new IllegalArgumentException("id должен быть пустым.");
        }
        var task = taskMap.get(id);
        if (task.status() == Status.DONE) {
            if (!forceReopen) {
                throw new IllegalArgumentException(
                        "Задача с id: " + id + " завершена и не может быть изменена. " +
                                "Передайте forceReopen=true, чтобы вернуть её в IN_PROGRESS."
                );
            }
            task = new Task(
                    task.id(),
                    task.creatorId(),
                    task.assignedUserId(),
                    Status.IN_PROGRESS,
                    task.createDateTime(),
                    task.deadlineDate(),
                    task.priority()
            );
        }
        var updatedTask = new Task(
                task.id(),
                taskToUpdate.creatorId(),
                taskToUpdate.assignedUserId(),
                task.status(),
                taskToUpdate.createDateTime(),
                taskToUpdate.deadlineDate(),
                taskToUpdate.priority()
        );
        taskMap.put(id, updatedTask);
        return updatedTask;
    }
}

