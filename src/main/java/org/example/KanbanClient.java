package org.example;

import java.io.*;
import java.net.*;

public class KanbanClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private KanbanGUI gui;

    public KanbanClient() {
        try {
            socket = new Socket( "192.168.195.198", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            gui = new KanbanGUI(out);
            new Thread(this::listenToServer).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenToServer() {
        try {
            String response;
            while ((response = in.readLine()) != null) {
                if (response.equals("END")) continue;

                if (response.startsWith("TASK:")) {
                    String[] taskData = response.substring(5).split(",", 6);
                    if (taskData.length == 6) {
                        String taskId = taskData[0].trim();
                        String title = taskData[1].trim();
                        String description = taskData[2].trim();
                        String status = taskData[3].trim();
                        String priority = taskData[4].trim();
                        String dueDate = taskData[5].trim();

                        gui.updateTaskUI(taskId, title, description, status, priority, dueDate);
                    }
                } else if (response.startsWith("DELETE:")) {
                    String taskId = response.substring(7).trim();
                    gui.removeFromAllLists(taskId);
                } else if (response.startsWith("ERROR:")) {
                    gui.displayError(response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new KanbanClient();
    }
}
