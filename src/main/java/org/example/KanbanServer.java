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

    static synchronized void addTask(Task task) {
        taskList.add(task);
        broadcastTasks();
    }

    static synchronized void updateTask(String taskId, String newStatus) {
        for (Task task : taskList) {
            if (task.getTaskId().equals(taskId)) {
                task.setStatus(newStatus); // Only update the status
                break;
            }
        }
        broadcastTasks(); // Broadcast updated task list to clients
    }

    static synchronized void deleteTask(String taskId) {
        taskList.removeIf(task -> task.getTaskId().equals(taskId));
        broadcastTasks();
    }
}
