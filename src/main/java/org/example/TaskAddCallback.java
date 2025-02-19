package org.example;

import java.util.Date;

public interface TaskAddCallback {
    void onAdd(String title, String status, String description, int priority, Date dueDate);
}
