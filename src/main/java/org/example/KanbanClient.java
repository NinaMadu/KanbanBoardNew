package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class KanbanClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private DefaultListModel<String> unassignedModel, openModel, priorityModel, completeModel;

    public KanbanClient() {
        JFrame frame = new JFrame("Kanban Board");
        frame.setSize(700, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(1, 4));

        unassignedModel = new DefaultListModel<>();
        openModel = new DefaultListModel<>();
        priorityModel = new DefaultListModel<>();
        completeModel = new DefaultListModel<>();

        frame.add(createColumn("Unassigned", unassignedModel));
        frame.add(createColumn("Open", openModel));
        frame.add(createColumn("Priority", priorityModel));
        frame.add(createColumn("Complete", completeModel));

        try {
            socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        if (response.equals("END")) continue;
                        String[] taskData = response.split(":");
                        if (taskData.length == 2) {
                            updateTaskUI(taskData[0], taskData[1]);
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
                out.println("ADD:" + task + "," + title);
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
        dialog.setLayout(new GridLayout(5, 1));

        JLabel label = new JLabel("Move task: " + task, SwingConstants.CENTER);
        dialog.add(label);

        JButton openButton = new JButton("Move to Open");
        JButton priorityButton = new JButton("Move to Priority");
        JButton completeButton = new JButton("Move to Complete");
        JButton unassignedButton = new JButton("Move to Unassigned");

        openButton.addActionListener(e -> moveTask(task, "Open", dialog));
        priorityButton.addActionListener(e -> moveTask(task, "Priority", dialog));
        completeButton.addActionListener(e -> moveTask(task, "Complete", dialog));
        unassignedButton.addActionListener(e -> moveTask(task, "Unassigned", dialog));

        dialog.add(openButton);
        dialog.add(priorityButton);
        dialog.add(completeButton);
        dialog.add(unassignedButton);

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void moveTask(String task, String newStatus, JDialog dialog) {
        out.println("UPDATE:" + task + "," + newStatus);
        dialog.dispose();
    }

    private void updateTaskUI(String title, String status) {
        SwingUtilities.invokeLater(() -> {
            removeFromAllLists(title);
            switch (status) {
                case "Unassigned": unassignedModel.addElement(title); break;
                case "Open": openModel.addElement(title); break;
                case "Priority": priorityModel.addElement(title); break;
                case "Complete": completeModel.addElement(title); break;
            }
        });
    }

    private void removeFromAllLists(String title) {
        unassignedModel.removeElement(title);
        openModel.removeElement(title);
        priorityModel.removeElement(title);
        completeModel.removeElement(title);
    }

    public static void main(String[] args) {
        new KanbanClient();
    }
}
