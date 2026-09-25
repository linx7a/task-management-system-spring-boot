package linx7a.task_management_system.tasks;

public record TaskSearchFilter(
        Long creatorId,
        Long assignedUserId,
        Status status,
        Priority priority,
        Integer pageSize,
        Integer pageNumber
) {
}
