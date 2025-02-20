package org.example;

import java.time.LocalDate;
import java.util.UUID;

public class Task {
    private final String taskId;  // Unique identifier
    private String title;
    private String description;
    private String status; // "Unassigned", "Open", "Priority", "Complete"
    private String priority; // "Low", "Medium", "High"
    private LocalDate dueDate;

    // Constructor
    public Task(String title, String description, String status, String priority, LocalDate dueDate) {
        this.taskId = UUID.randomUUID().toString(); // Generate unique ID
        this.title = title;
        this.description = description;
        setStatus(status);
        this.priority = priority;
        this.dueDate = dueDate;
    }

    // Getters
    public String getTaskId() {
        return taskId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    // Setters with validation
    public void setTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title;
        } else {
            throw new IllegalArgumentException("Title cannot be empty");
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        if (status.equals("Unassigned") || status.equals("Open") ||
                status.equals("Priority") || status.equals("Complete")) {
            this.status = status;
        } else {
            throw new IllegalArgumentException("Invalid status");
        }
    }

    public void setPriority(String priority) {
        if (priority.equals("Low") || priority.equals("Medium") || priority.equals("High")) {
            this.priority = priority;
        } else {
            throw new IllegalArgumentException("Invalid priority level");
        }
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    // Override toString for better task representation
    @Override
    public String toString() {
        return String.format("Task ID: %s\nTitle: %s\nDescription: %s\nStatus: %s\nPriority: %s\nDue Date: %s\n",
                taskId, title, description, status, priority, dueDate);
    }
}
