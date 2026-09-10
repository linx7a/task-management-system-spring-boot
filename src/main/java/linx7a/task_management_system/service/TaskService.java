package linx7a.task_management_system.service;

import linx7a.task_management_system.model.Priority;
import linx7a.task_management_system.model.Status;
import linx7a.task_management_system.model.Task;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

}
