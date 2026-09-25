package linx7a.task_management_system.tasks;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper mapper;

    public TaskService(TaskRepository taskRepository, TaskMapper mapper) {
        this.taskRepository = taskRepository;
        this.mapper = mapper;
    }

    public Task getById(Long id) {
        TaskEntity taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Задача с id: " + id + " не найдена."));
        return mapper.toDomain(taskEntity);
    }

    public List<Task> searchAllByFilter(TaskSearchFilter filter) {
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        List<TaskEntity> allEntities = taskRepository.searchByFilters(
                filter.creatorId(),
                filter.assignedUserId(),
                filter.status(),
                filter.priority(),
                pageable
        );
        return allEntities.stream()
                .map(it -> mapper.toDomain(it))
                .toList();
    }

    public Task createTask(Task taskToCreate) {
        if (taskToCreate.id() != null) {
            throw new IllegalArgumentException("id должен быть пустым.");
        }
        if (taskToCreate.status() != null) {
            throw new IllegalArgumentException("Статус должен быть пустым");
        }
        var newTaskEntity = mapper.toEntity(taskToCreate);
        newTaskEntity.setId(null);
        newTaskEntity.setStatus(Status.CREATED);
        newTaskEntity.setDoneDateTime(null);
        var saved = taskRepository.save(newTaskEntity);
        return mapper.toDomain(saved);
    }

    public Task updateTask(Long id, Task taskToUpdate) {
        TaskEntity taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Задача с id: " + id + " не найдена."));
        if (taskToUpdate.id() != null) {
            throw new IllegalArgumentException("id должен быть пустым.");
        }
        if (taskEntity.getStatus() == Status.DONE) {
            throw new IllegalArgumentException(
                    "Задача с id: " + id + " завершена и не может быть изменена. " +
                            "Измените статус задачи на IN_PROGRESS, чтобы продолжить ее редактирование."
            );
        }
        var updatedTaskEntity = mapper.toEntity(taskToUpdate);
        updatedTaskEntity.setId(taskEntity.getId());
        updatedTaskEntity.setStatus(taskEntity.getStatus());
        updatedTaskEntity.setDoneDateTime(taskEntity.getDoneDateTime());
        var saved = taskRepository.save(updatedTaskEntity);
        return mapper.toDomain(saved);
    }

    public Task changeStatus(Long id, Status newStatus) {
        TaskEntity taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Задача с id: " + id + " не найдена."));

        if (!isValidTransition(taskEntity.getStatus(), newStatus)) {
            throw new IllegalStateException("Недопустимый переход статуса: " + taskEntity.getStatus()
                    + " -> " + newStatus);
        }
        taskEntity.setStatus(newStatus);
        taskEntity.setDoneDateTime(null);
        var saved = taskRepository.save(taskEntity);
        return mapper.toDomain(saved);
    }

    private boolean isValidTransition(Status current, Status next) {
        return switch (current) {
            case CREATED -> next == Status.IN_PROGRESS;
            case IN_PROGRESS -> next == Status.CREATED;
            case DONE -> next == Status.IN_PROGRESS;
        };
    }

    public void deleteTask(Long id) {
        TaskEntity taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Задача с id: " + id + " не найдена."));
        taskRepository.deleteById(id);
    }

    public Task startTask(Long id) {
        TaskEntity taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Задача с id: " + id + " не найдена."));
        if (taskEntity.getAssignedUserId() == null) {
            throw new IllegalArgumentException("У задачи не назначен исполнитель.");
        }
        long activeCount = taskRepository
                .countActiveTasks(taskEntity.getAssignedUserId(), Status.IN_PROGRESS);
        if (activeCount >= 5) {
            throw new IllegalArgumentException("У пользователя уже 4 активные задачи в статусе IN_PROGRESS.");
        }
        return changeStatus(id, Status.IN_PROGRESS);
    }

    public Task completeTask(Long id) {
        TaskEntity taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Задача с id: " + id + " не найдена."));
        if (taskEntity.getAssignedUserId() == null) {
            throw new IllegalArgumentException("Нельзя завершить задачу: не назначен исполнитель.");
        }
        if (taskEntity.getDeadlineDate() == null) {
            throw new IllegalArgumentException("Нельзя завершить задачу: не указан дедлайн.");
        }
        if (taskEntity.getStatus() != Status.IN_PROGRESS) {
            throw new IllegalStateException("Недопустимый переход статуса: "
                    + taskEntity.getStatus() + " -> DONE.");
        }
        taskEntity.setStatus(Status.DONE);
        taskEntity.setDoneDateTime(LocalDateTime.now());
        var completed = taskRepository.save(taskEntity);
        return mapper.toDomain(completed);
    }
}

