package org.example;

public class Task {
    String title;
    String status;

    public Task(String title, String status) {
        this.title = title;
        this.status = status;
    }

    @Override
    public String toString() {
        return title + ":" + status;
    }
}
