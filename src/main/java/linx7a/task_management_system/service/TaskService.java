package linx7a.task_management_system.service;

import linx7a.task_management_system.model.Status;
import linx7a.task_management_system.model.Task;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
            throw new IllegalArgumentException("ID должен быть пустым.");
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
}
