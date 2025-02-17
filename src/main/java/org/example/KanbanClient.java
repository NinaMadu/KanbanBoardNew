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
            socket = new Socket("192.168.132.198"
                    , 5000);
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
                if (e.getClickCount() == 2) {
                    String selectedTask = list.getSelectedValue();
                    String newStatus = JOptionPane.showInputDialog("Move to (Unassigned, Open, Priority, Complete):");
                    if (newStatus != null) {
                        out.println("UPDATE:" + selectedTask + "," + newStatus);
                    }
                }
            }
        });

        return panel;
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
