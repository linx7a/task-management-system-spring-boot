package linx7a.task_management_system.model;

import java.time.LocalDateTime;

public record Task(
        Long id,
        Long creatorId,
        Long assignedUserId,
        Status status,
        LocalDateTime createDateTime,
        LocalDateTime deadlineDate,
        Priority priority
) {
}
