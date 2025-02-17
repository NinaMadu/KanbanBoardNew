package org.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
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
        broadcastTasks();
    }

    static synchronized void updateTask(String title, String newStatus) {
        for (Task task : taskList) {
            if (task.title.equals(title)) {
                task.status = newStatus;
                break;
            }
        }
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
                    String[] taskData = parts[1].split(",", 2);
                    if (taskData.length < 2) continue;

                    if (command.equals("ADD")) {
                        addTask(new Task(taskData[0], taskData[1]));
                    } else if (command.equals("UPDATE")) {
                        updateTask(taskData[0], taskData[1]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                clients.remove(this);
            }
        }

        public void sendTaskList() {
            synchronized (taskList) {
                for (Task task : taskList) {
                    out.println(task.title + ":" + task.status);
                }
                out.println("END");
            }
        }
    }
}
