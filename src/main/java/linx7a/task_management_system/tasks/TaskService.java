package linx7a.task_management_system.tasks;

import linx7a.task_management_system.tasks.TaskEntity;
import linx7a.task_management_system.tasks.Status;
import linx7a.task_management_system.tasks.Task;
import linx7a.task_management_system.tasks.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task getById(Long id) {
        TaskEntity taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Задача с id: " + id + " не найдена."));
        return toDomainTask(taskEntity);
    }

    public List<Task> getAll() {
        List<TaskEntity> allEntities = taskRepository.findAll();
        return allEntities.stream()
                .map(it -> toDomainTask(it))
                .toList();
    }

    public Task createTask(Task taskToCreate) {
        if (taskToCreate.id() != null) {
            throw new IllegalArgumentException("id должен быть пустым.");
        }
        if (taskToCreate.status() != null) {
            throw new IllegalArgumentException("Статус должен быть пустым");
        }
        var newTaskEntity = new TaskEntity(
                null,
                taskToCreate.creatorId(),
                taskToCreate.assignedUserId(),
                Status.CREATED,
                taskToCreate.createDateTime(),
                taskToCreate.deadlineDate(),
                taskToCreate.priority(),
                null
        );
        var saved = taskRepository.save(newTaskEntity);
        return toDomainTask(saved);
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
        var updatedTaskEntity = new TaskEntity(
                taskEntity.getId(),
                taskToUpdate.creatorId(),
                taskToUpdate.assignedUserId(),
                taskEntity.getStatus(),
                taskToUpdate.createDateTime(),
                taskToUpdate.deadlineDate(),
                taskToUpdate.priority(),
                taskEntity.getDoneDateTime()
        );
        var saved = taskRepository.save(updatedTaskEntity);
        return toDomainTask(saved);
    }

    public Task changeStatus(Long id, Status newStatus) {
        TaskEntity taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Задача с id: " + id + " не найдена."));

        if (!isValidTransition(taskEntity.getStatus(), newStatus)) {
            throw new IllegalStateException("Недопустимый переход статуса: " + taskEntity.getStatus()
                    + " -> " + newStatus);
        }
        var updatedTaskEntity = new TaskEntity(
                taskEntity.getId(),
                taskEntity.getCreatorId(),
                taskEntity.getAssignedUserId(),
                newStatus,
                taskEntity.getCreateDateTime(),
                taskEntity.getDeadlineDate(),
                taskEntity.getPriority(),
                null
        );
        var saved = taskRepository.save(updatedTaskEntity);
        return toDomainTask(saved);
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
                .countByAssignedUserIdAndStatus(taskEntity.getAssignedUserId(), Status.IN_PROGRESS);
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
        var doneTaskEntity = new TaskEntity(
                taskEntity.getId(),
                taskEntity.getCreatorId(),
                taskEntity.getAssignedUserId(),
                Status.DONE,
                taskEntity.getCreateDateTime(),
                taskEntity.getDeadlineDate(),
                taskEntity.getPriority(),
                LocalDateTime.now()
        );
        var completed = taskRepository.save(doneTaskEntity);
        return toDomainTask(completed);
    }

    private Task toDomainTask(
            TaskEntity taskEntity
    ) {
        return new Task(
                taskEntity.getId(),
                taskEntity.getCreatorId(),
                taskEntity.getAssignedUserId(),
                taskEntity.getStatus(),
                taskEntity.getCreateDateTime(),
                taskEntity.getDeadlineDate(),
                taskEntity.getPriority(),
                taskEntity.getDoneDateTime()
        );
    }


}

