package org.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
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
            client.sendTaskList();
        }
    }

    static synchronized void addTask(Task task) {
        taskList.add(task);
        System.out.println("Task Added: " + task); // Debugging
        broadcastTasks();
    }

    static synchronized void updateTask(String title, String newStatus) {
        for (Task task : taskList) {
            if (task.getTitle().equals(title)) {
                task.setStatus(Task.Status.valueOf(newStatus));
                System.out.println("Updated Task: " + task);
                break;
            }
        }
        broadcastTasks();
    }

    static synchronized void deleteTask(String title) {
        taskList.removeIf(task -> task.getTitle().equals(title));
        broadcastTasks();
    }

    static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try (Scanner in = new Scanner(clientSocket.getInputStream())) {
                sendTaskList();  // Send existing tasks to new client

                while (in.hasNextLine()) {
                    String input = in.nextLine();
                    String[] parts = input.split(":", 2);
                    if (parts.length < 2) continue;

                    String command = parts[0];
                    String[] taskData = parts[1].split(",", 5);

                    if (command.equals("ADD") && taskData.length == 5) {
                        String title = taskData[0];
                        Task.Status status = Task.Status.valueOf(taskData[1]);
                        String description = taskData[2];
                        int priority = Integer.parseInt(taskData[3]);
                        Date dueDate = new SimpleDateFormat("yyyy-MM-dd").parse(taskData[4]);
                        addTask(new Task(title, status, description, priority, dueDate));
                    } else if (command.equals("UPDATE") && taskData.length == 2) {
                        updateTask(taskData[0], taskData[1]);
                    } else if (command.equals("DELETE")) {
                        deleteTask(parts[1]);
                    }
                }
            } catch (IOException | java.text.ParseException e) {
                e.printStackTrace();
            } finally {
                clients.remove(this);
            }
        }

        public void sendTaskList() {
            synchronized (taskList) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                for (Task task : taskList) {
                    out.println(task.getTitle() + ":" + task.getStatus() + "," + task.getDescription() + "," + task.getPriority() + "," + sdf.format(task.getDueDate()));
                }
                out.println("END");
            }
        }
    }
}
