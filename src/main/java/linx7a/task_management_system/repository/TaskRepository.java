package linx7a.task_management_system.repository;

import linx7a.task_management_system.entity.TaskEntity;
import linx7a.task_management_system.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    long countByAssignedUserIdAndStatus(Long assignedUserId, Status status);
}
