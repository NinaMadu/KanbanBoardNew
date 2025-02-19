package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KanbanClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private DefaultListModel<String> todoModel, inProgressModel, doneModel;

    public KanbanClient() {
        JFrame frame = new JFrame("Kanban Board");
        frame.setSize(700, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(1, 3));

        todoModel = new DefaultListModel<>();
        inProgressModel = new DefaultListModel<>();
        doneModel = new DefaultListModel<>();

        frame.add(createColumn("TODO", todoModel));
        frame.add(createColumn("IN_PROGRESS", inProgressModel));
        frame.add(createColumn("DONE", doneModel));

        try {
            socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println("Received: " + response); // Debugging
                        if (response.equals("END")) continue;
                        String[] taskData = response.split(":");
                        if (taskData.length == 2) {
                            String title = taskData[0];
                            String[] details = taskData[1].split(",");
                            if (details.length == 4) {
                                String status = details[0];
                                String description = details[1];
                                int priority = Integer.parseInt(details[2]);
                                String dueDate = details[3];
                                System.out.println("Updating UI with task: " + title + ", " + status + ", " + description + ", " + priority + ", " + dueDate); // Debugging
                                updateTaskUI(title, status, description, priority, dueDate);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

        frame.setVisible(true);
    }

    private JPanel createColumn(String title, DefaultListModel<String> model) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));

        JList<String> list = new JList<>(model);
        JScrollPane scrollPane = new JScrollPane(list);
        panel.add(scrollPane, BorderLayout.CENTER);

        JTextField taskField = new JTextField();
        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(e -> {
            String task = taskField.getText();
            if (!task.isEmpty()) {
                TaskDialog taskDialog = new TaskDialog(task, title, this::sendAddTask);
                taskDialog.setVisible(true);
                taskField.setText("");
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(taskField, BorderLayout.CENTER);
        bottomPanel.add(addButton, BorderLayout.EAST);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = list.locationToIndex(e.getPoint());
                    if (index != -1) {
                        list.setSelectedIndex(index);
                        showTaskOptions(list.getSelectedValue(), list, model, e.getComponent(), e.getX(), e.getY());
                    }
                } else if (e.getClickCount() == 2) {
                    String selectedTask = list.getSelectedValue();
                    if (selectedTask != null) {
                        showMoveTaskDialog(selectedTask);
                    }
                }
            }
        });

        return panel;
    }

    private void showTaskOptions(String task, JList<String> list, DefaultListModel<String> model, Component comp, int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete Task");

        deleteItem.addActionListener(e -> deleteTask(task, model));
        menu.add(deleteItem);
        menu.show(comp, x, y);
    }

    private void deleteTask(String task, DefaultListModel<String> model) {
        out.println("DELETE:" + task);
        model.removeElement(task);
    }

    private void showMoveTaskDialog(String task) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Move Task");
        dialog.setSize(300, 200);
        dialog.setLayout(new GridLayout(4, 1));

        JLabel label = new JLabel("Move task: " + task, SwingConstants.CENTER);
        dialog.add(label);

        JButton todoButton = new JButton("Move to TODO");
        JButton inProgressButton = new JButton("Move to IN_PROGRESS");
        JButton doneButton = new JButton("Move to DONE");

        todoButton.addActionListener(e -> moveTask(task, "TODO", dialog));
        inProgressButton.addActionListener(e -> moveTask(task, "IN_PROGRESS", dialog));
        doneButton.addActionListener(e -> moveTask(task, "DONE", dialog));

        dialog.add(todoButton);
        dialog.add(inProgressButton);
        dialog.add(doneButton);

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void moveTask(String task, String newStatus, JDialog dialog) {
        out.println("UPDATE:" + task + "," + newStatus);
        System.out.println("Moving task: " + task + " to " + newStatus); // Debugging
        dialog.dispose();
    }

    private void updateTaskUI(String title, String status, String description, int priority, String dueDate) {
        SwingUtilities.invokeLater(() -> {
            removeFromAllLists(title);
            String taskDetails = title + " (" + description + ", " + priority + ", " + dueDate + ")";
            switch (status) {
                case "TODO": todoModel.addElement(taskDetails); break;
                case "IN_PROGRESS": inProgressModel.addElement(taskDetails); break;
                case "DONE": doneModel.addElement(taskDetails); break;
            }
        });
    }

    private void removeFromAllLists(String title) {
        removeElementsFromModel(todoModel, title);
        removeElementsFromModel(inProgressModel, title);
        removeElementsFromModel(doneModel, title);
    }

    private void removeElementsFromModel(DefaultListModel<String> model, String title) {
        for (int i = model.getSize() - 1; i >= 0; i--) {
            String element = model.getElementAt(i);
            if (element.startsWith(title + " (")) {
                model.remove(i);
            }
        }
    }

    public void sendAddTask(String title, String status, String description, int priority, Date dueDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String taskDetails = title + "," + status + "," + description + "," + priority + "," + sdf.format(dueDate);
        System.out.println("Sending ADD: " + taskDetails); // Debugging
        out.println("ADD:" + taskDetails);
    }

    public static void main(String[] args) {
        new KanbanClient();
    }
}
