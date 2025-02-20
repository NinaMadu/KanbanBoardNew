package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
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

                switch (command) {
                    case "ADD":
                        addTask(parts[1]);
                        break;
                    case "UPDATE":
                        updateTask(parts[1]);
                        break;
                    case "DELETE":
                        deleteTask(parts[1]);
                        break;
                    case "EDIT":
                        editTask(parts[1]);  // Handle edit task request
                        break;
                    default:
                        out.println("ERROR: Invalid command");
                        break;
                }

                // Broadcast updated task list to all clients
                KanbanServer.broadcastTasks();
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

    public void sendMessage(String message) {
        out.println(message);
    }


    private void addTask(String taskData) {
        try {
            // Debugging: Log the raw task data received
            System.out.println("Raw task data received: " + taskData);

            // Split the task data by pipe delimiter
            String[] data = taskData.split("\\|", -1);

            // Check if the split has 5 parts
            if (data.length != 5) {
                out.println("ERROR: Invalid ADD format. Expected 5 fields separated by '|'. Received: " + data.length);
                return;
            }

            // Extract individual task details
            String title = data[0].trim();
            String description = data[1].trim();
            String status = data[2].trim();  // Default status is "Unassigned"
            String priority = data[3].trim();
            LocalDate dueDate = LocalDate.parse(data[4].split(",")[0].trim());  // Expecting format "YYYY-MM-DD"

            // Debugging output to verify parsed values
            System.out.println("Parsed Task: Title = " + title + ", Description = " + description + ", Status = " + status + ", Priority = " + priority + ", DueDate = " + dueDate);

            // Validate status and priority values with detailed error messages
            try {
                if (!(status.equals("Unassigned") || status.equals("Open") || status.equals("Priority") || status.equals("Complete"))) {
                    out.println("ERROR: Invalid status value.");
                    return;
                }
            } catch (IllegalArgumentException e) {
                out.println("ERROR: Invalid status value. " + e.getMessage());
                return;
            }

            try {
                if (!(priority.equals("Low") || priority.equals("Medium") || priority.equals("High"))) {
                    out.println("ERROR: Invalid priority value.");
                    return;
                }
            } catch (IllegalArgumentException e) {
                out.println("ERROR: Invalid priority value. " + e.getMessage());
                return;
            }

            // Create the Task object
            Task task = new Task(title, description, status, priority, dueDate);
            KanbanServer.addTask(task); // Add the task to the task list on the server

        } catch (IllegalArgumentException e) {
            out.println("ERROR: Invalid task data. " + e.getMessage());
        } catch (Exception e) {
            out.println("ERROR: Invalid task data format.");
            e.printStackTrace(); // Log detailed exception info for debugging
        }
    }

    private void updateTask(String taskData) {
        System.out.println("Received UPDATE request: " + taskData); // Debugging log
        try {
            String[] data = taskData.split(",", 2);
            if (data.length < 2) {
                out.println("ERROR: Invalid UPDATE format. Use TaskID,Status");
                return;
            }

            String taskId = data[0].trim();
            System.out.println("taskId "+taskId  );
            String newStatus = data[1].trim();
            System.out.println("new status "+ newStatus);

            KanbanServer.updateTask(taskId, newStatus);

        } catch (Exception e) {
            out.println("ERROR: Failed to update task.");
        }
    }

    private void editTask(String taskData) {
        System.out.println("Received EDIT request: " + taskData); // Debugging log
        try {
            String[] data = taskData.split(",", 5);
            if (data.length < 5) {
                out.println("ERROR: Invalid EDIT format. Use TaskID,NewTitle,NewDescription,NewPriority,NewDueDate");
                return;
            }

            String taskId = data[0].trim();
            String newTitle = data[1].trim();
            String newDescription = data[2].trim();
            String newPriority = data[3].trim();
            String newDueDate = data[4].trim();

            // Call the server's edit task method
            KanbanServer.editTask(taskId, newTitle, newDescription, newPriority, newDueDate);
        } catch (Exception e) {
            out.println("ERROR: Failed to edit task.");
        }
    }



    private void deleteTask(String taskId) {
        System.out.println("Received DELETE request: " + taskId);
        try {
            KanbanServer.deleteTask(taskId);
        }
        catch (Exception e){
            out.println("ERROR: Failed to delete task.");
        }
    }
}
