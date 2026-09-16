package com.example.taskmanager.exception;

import com.example.taskmanager.entity.TaskStatus;

public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(TaskStatus currentStatus, TaskStatus newStatus) {
        super(String.format("Invalid state transition from %s to %s. Allowed transitions from %s: %s",
                currentStatus, newStatus, currentStatus, getAllowedTransitions(currentStatus)));
    }

    private static String getAllowedTransitions(TaskStatus status) {
        return switch (status) {
            case PENDING -> "IN_PROGRESS, COMPLETED, CANCELLED";
            case IN_PROGRESS -> "COMPLETED, CANCELLED, PENDING";
            case COMPLETED -> "IN_PROGRESS";
            case CANCELLED -> "PENDING";
        };
    }
}