package linx7a.task_management_system.model;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record Task(
        Long id,
        @NotNull
        Long creatorId,
        Long assignedUserId,
        Status status,
        LocalDateTime createDateTime,
        @NotNull
        @Future
        LocalDateTime deadlineDate,
        @NotNull
        Priority priority
) {
}
