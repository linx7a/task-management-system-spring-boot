package linx7a.task_management_system.tasks;

import linx7a.task_management_system.tasks.TaskEntity;
import linx7a.task_management_system.tasks.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    long countByAssignedUserIdAndStatus(Long assignedUserId, Status status);
}
