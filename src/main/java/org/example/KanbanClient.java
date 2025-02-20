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
            socket = new Socket("localhost", 5000);
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
                String[] taskData = response.split(":");
                if (taskData.length == 2) {
                    gui.updateTaskUI(taskData[0], taskData[1]);
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

