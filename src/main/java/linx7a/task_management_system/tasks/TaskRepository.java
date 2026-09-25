package linx7a.task_management_system.tasks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    @Query("""
            SELECT COUNT(t) FROM TaskEntity t 
            WHERE t.assignedUserId = :assignedUserId
            AND t.status = :status
            """)
    long countActiveTasks(
            @Param("assignedUserId") Long assignedUserId,
            @Param("status") Status status
    );
}
