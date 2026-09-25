package linx7a.task_management_system.tasks;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

    @Query("""
            SELECT t FROM TaskEntity t
            WHERE (:creatorId IS NULL OR t.creatorId = :creatorId)
            AND (:assignedUserId IS NULL OR t.assignedUserId = :assignedUserId)
            AND (:status IS NULL OR t.status = :status)
            AND (:priority IS NULL OR t.priority = :priority)
            """)
    List<TaskEntity> searchByFilters(
            @Param("creatorId") Long creatorId,
            @Param("assignedUserId") Long assignedUserId,
            @Param("status") Status status,
            @Param("priority") Priority priority,
            Pageable pageable
    );
}
