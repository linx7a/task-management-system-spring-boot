package linx7a.task_management_system.tasks;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import linx7a.task_management_system.tasks.Priority;
import linx7a.task_management_system.tasks.Status;

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
        Priority priority,
        LocalDateTime doneDateTime
) {
}
