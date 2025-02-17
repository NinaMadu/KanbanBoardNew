package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
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
                    String[] taskData = parts[1].split(",", 2);
                    String title = taskData[0];
                    String status = taskData[1];
                    taskList.add(new Task(title, status));
                } else if (command.equals("UPDATE")) {
                    String[] taskData = parts[1].split(",", 2);
                    String title = taskData[0];
                    String newStatus = taskData[1];
                    for (Task task : taskList) {
                        if (task.title.equals(title)) {
                            task.status = newStatus;
                            break;
                        }
                    }
                }

                // Broadcast updated task list to all clients
                for (Task task : taskList) {
                    out.println("TASK:" + task.toString());
                }
                out.println("END"); // Indicate end of data
            }
        } catch (IOException e) {
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

