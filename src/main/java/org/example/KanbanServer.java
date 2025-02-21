package org.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.*;

public class KanbanServer {
    private static final int PORT = 5000;
    private static List<Task> taskList = Collections.synchronizedList(new ArrayList<>());
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Kanban Server running on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static synchronized void broadcastTasks() {
        for (ClientHandler client : clients) {
            for (Task task : taskList) {
                client.sendMessage("TASK:" + task.getTaskId() + "," +
                        task.getTitle() + "," +
                        task.getDescription() + "," +
                        task.getStatus() + "," +
                        task.getPriority() + "," +
                        task.getDueDate());
            }
            client.sendMessage("END"); // Indicate end of data
        }
    }

    static synchronized void broadcastChatMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage("CHAT:" + message); // Prefix message with "CHAT:"
        }
    }

    static synchronized void broadcastMessage(String message) {
        for (ClientHandler client : clients) { // Assuming `clients` is a List<ClientHandler>
            client.sendMessage(message);
        }
    }


    static synchronized void addTask(Task task) {
        taskList.add(task);
        broadcastTasks();
    }

    static synchronized void updateTask(String taskId, String newStatus) {
        boolean updated = false;
        for (Task task : taskList) {
            System.out.println(task.getTaskId());
            if (task.getTaskId().equals(taskId)) {
                task.setStatus(newStatus); // Only update the status
                updated = true;
                System.out.println("Updated Task " + taskId + " to status: " + newStatus);
                break;
            }
        }
        if(updated){
            broadcastTasks();
        }
        else {
            System.out.println("ERROR: Task ID " + taskId + " not found!");
        }// Broadcast updated task list to clients
    }

    static synchronized void editTask(String taskId, String newTitle, String newDescription, String newPriority, String newDueDate) {
        boolean updated = false;
        for (Task task : taskList) {
            if (task.getTaskId().equals(taskId)) {
                // Update the task with new values
                task.setTitle(newTitle);
                task.setDescription(newDescription);
                task.setPriority(newPriority);
                task.setDueDate(LocalDate.parse(newDueDate));
                updated = true;
                System.out.println("Updated Task " + taskId);
                break;
            }
        }

        if (updated) {
            broadcastTasks();  // Broadcast updated tasks to all clients
        } else {
            System.out.println("ERROR: Task ID " + taskId + " not found!");
        }
    }


    static synchronized void deleteTask(String taskId) {
        System.out.println("Attempting to delete task: " + taskId);

        boolean found = false;
        for (Task task : taskList) {
            System.out.println("Existing Task ID: " + task.getTaskId());
            if (task.getTaskId().equals(taskId)) {
                found = true;
            }
        }

        if (!found) {
            System.out.println("ERROR: Task ID not found!");
            return;
        }

        taskList.removeIf(task -> task.getTaskId().equals(taskId));
        System.out.println("Task " + taskId + " deleted successfully.");
        broadcastMessage("DELETE:" + taskId);
        broadcastTasks();
    }

    public static synchronized List<Task> getTasks() {
        return new ArrayList<>(taskList); // Return a copy to avoid concurrency issues
    }
}
