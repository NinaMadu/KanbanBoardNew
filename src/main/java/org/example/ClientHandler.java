package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

class ClientHandler extends Thread {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private static List<Task> taskList = Collections.synchronizedList(new ArrayList<>());

    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public void run() {
        try {
            String input;
            while ((input = in.readLine()) != null) {
                String[] parts = input.split(":", 2);
                String command = parts[0];

                if (command.equals("ADD")) {
                    String[] taskData = parts[1].split(",", 5);
                    String title = taskData[0];
                    Task.Status status = Task.Status.valueOf(taskData[1]);
                    String description = taskData[2];
                    int priority = Integer.parseInt(taskData[3]);
                    Date dueDate = new SimpleDateFormat("yyyy-MM-dd").parse(taskData[4]);
                    taskList.add(new Task(title, status, description, priority, dueDate));
                } else if (command.equals("UPDATE")) {
                    String[] taskData = parts[1].split(",", 2);
                    String title = taskData[0];
                    Task.Status newStatus = Task.Status.valueOf(taskData[1]);
                    for (Task task : taskList) {
                        if (task.getTitle().equals(title)) {
                            task.setStatus(newStatus);
                            break;
                        }
                    }
                } else if (command.equals("DELETE")) {
                    String title = parts[1];
                    taskList.removeIf(task -> task.getTitle().equals(title));
                }

                // Broadcast updated task list to all clients
                for (Task task : taskList) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    out.println("TASK:" + task.getTitle() + ":" + task.getStatus() + "," + task.getDescription() + "," + task.getPriority() + "," + sdf.format(task.getDueDate()));
                }
                out.println("END"); // Indicate end of data
            }
        } catch (IOException | java.text.ParseException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
