package linx7a.task_management_system.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper mapper;

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    public TaskService(TaskRepository taskRepository, TaskMapper mapper) {
        this.taskRepository = taskRepository;
        this.mapper = mapper;
    }

    public Task getById(Long id) {
        log.info("Поиск задачи: id={}.", id);
        return mapper.toDomain(getEntityOrThrow(id));
    }

    public List<Task> searchAllByFilter(TaskSearchFilter filter) {
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        log.info("Поиск задач: pageSize={}, pageNumber={}.", pageSize, pageNumber);
        List<TaskEntity> allEntities = taskRepository.searchByFilters(
                filter.creatorId(),
                filter.assignedUserId(),
                filter.status(),
                filter.priority(),
                pageable
        );
        log.info("Из БД получено сущностей: {}.", allEntities.size());
        return allEntities.stream()
                .map(mapper::toDomain)
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
        newTaskEntity.setCreateDateTime(LocalDateTime.now());
        newTaskEntity.setDoneDateTime(null);
        var saved = taskRepository.save(newTaskEntity);
        log.info("Создана задача: id={}, status=CREATED.", saved.getId());
        return mapper.toDomain(saved);
    }

    public Task updateTask(Long id, Task taskToUpdate) {
        TaskEntity taskEntity = getEntityOrThrow(id);
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
        updatedTaskEntity.setCreateDateTime(taskEntity.getCreateDateTime());
        updatedTaskEntity.setDoneDateTime(taskEntity.getDoneDateTime());
        var saved = taskRepository.save(updatedTaskEntity);
        log.info("Обновлена задача: id={}, status={}, doneDateTime={}",
                updatedTaskEntity.getId(), updatedTaskEntity.getStatus(), updatedTaskEntity.getDoneDateTime());
        return mapper.toDomain(saved);
    }

    public Task changeStatus(Long id, Status newStatus) {
        TaskEntity taskEntity = getEntityOrThrow(id);

        Status oldStatus = taskEntity.getStatus();
        if (!isValidTransition(oldStatus, newStatus)) {
            throw new IllegalStateException("Недопустимый переход статуса: " + oldStatus
                    + " -> " + newStatus);
        }
        taskEntity.setStatus(newStatus);
        taskEntity.setDoneDateTime(null);
        var saved = taskRepository.save(taskEntity);
        log.info("Статус задачи id={} изменён: {} -> {}", id, oldStatus, newStatus);
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
        if (!taskRepository.existsById(id)) {
            throw new NoSuchElementException("Задача с id: " + id + " не найдена.");
        }
        taskRepository.deleteById(id);
        log.info("Задача с id={} удалена.", id);
    }

    public Task startTask(Long id) {
        TaskEntity taskEntity = getEntityOrThrow(id);
        if (taskEntity.getAssignedUserId() == null) {
            throw new IllegalArgumentException("У задачи не назначен исполнитель.");
        }
        long activeCount = taskRepository
                .countActiveTasks(taskEntity.getAssignedUserId(), Status.IN_PROGRESS);
        if (activeCount >= 5) {
            throw new IllegalArgumentException("У пользователя уже 5 активных задач в статусе IN_PROGRESS.");
        }
        return changeStatus(id, Status.IN_PROGRESS);
    }

    public Task completeTask(Long id) {
        TaskEntity taskEntity = getEntityOrThrow(id);
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
        log.info("Задача id={} завершена, дата и время завершения={}.", completed.getId(), completed.getDoneDateTime());
        return mapper.toDomain(completed);
    }

    private TaskEntity getEntityOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Задача с id: " + id + " не найдена."));
    }
}

