package org.pt.project.event;

public record ExecutorAssignedEvent(Long taskId, String executorId)  {
}
