package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.util.Iterator;

class KanbanGUI {
    private DefaultListModel<String> unassignedModel, openModel, priorityModel, completeModel;
    private PrintWriter out;

    public KanbanGUI(PrintWriter out) {
        this.out = out;
        JFrame frame = new JFrame("Kanban Board");
        frame.setSize(800, 400);
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

        frame.setVisible(true);
    }

    private JPanel createColumn(String title, DefaultListModel<String> model) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));

        JList<String> list = new JList<>(model);
        JScrollPane scrollPane = new JScrollPane(list);
        panel.add(scrollPane, BorderLayout.CENTER);

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = list.locationToIndex(e.getPoint());
                    if (index != -1) {
                        list.setSelectedIndex(index);
                        showTaskOptions(list.getSelectedValue(), model, e.getComponent(), e.getX(), e.getY());
                    }
                } else if (e.getClickCount() == 2) {
                    String selectedTask = list.getSelectedValue();
                    if (selectedTask != null) {
                        showMoveTaskDialog(selectedTask);
                    }
                }
            }
        });

        // **Adding the Add Task Button**
        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(e -> showAddTaskDialog(model, title));
        panel.add(addButton, BorderLayout.SOUTH);

        return panel;
    }

    private void showAddTaskDialog(DefaultListModel<String> model, String columnTitle) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add Task");
        dialog.setSize(300, 300); // Increase size for the new input field
        dialog.setLayout(new GridLayout(6, 1));

        JTextField taskTitleField = new JTextField();
        JTextField taskDescField = new JTextField();
        JTextField dueDateField = new JTextField();

        // Add a JComboBox for priority selection
        String[] priorities = {"Low", "Medium", "High"};
        JComboBox<String> priorityComboBox = new JComboBox<>(priorities);

        JButton addTaskButton = new JButton("Add Task");
        addTaskButton.addActionListener(e -> {
            String taskTitle = taskTitleField.getText().trim();
            String taskDesc = taskDescField.getText().trim();
            String dueDate = dueDateField.getText().trim();
            String priority = (String) priorityComboBox.getSelectedItem(); // Get the selected priority

            if (!taskTitle.isEmpty() && !dueDate.isEmpty()) {
                // Format the task string with pipe delimiter
                String newTask = String.format("%s|%s|Unassigned|%s|%s", taskTitle, taskDesc, priority, dueDate);
                System.out.println("Sending Task: " + newTask); // Debugging line
                out.println("ADD:" + newTask + "," + columnTitle); // Log the new task
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Task title and due date cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JLabel("Task Title:"));
        dialog.add(taskTitleField);
        dialog.add(new JLabel("Description:"));
        dialog.add(taskDescField);
        dialog.add(new JLabel("Due Date (YYYY-MM-DD):"));
        dialog.add(dueDateField);
        dialog.add(new JLabel("Priority:"));
        dialog.add(priorityComboBox);
        dialog.add(addTaskButton);

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }




    private void showTaskOptions(String task, DefaultListModel<String> model, Component comp, int x, int y) {
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
        String taskId = extractTaskId(task);
        System.out.println("Moving Task: " + taskId + " to " + newStatus);
        out.println("UPDATE:" + taskId + "," + newStatus);
        dialog.dispose();
    }

    // Extract only the task ID from the formatted task string
    private String extractTaskId(String task) {
        return task.substring(task.indexOf("[") + 1, task.indexOf("]")).trim();
    }

    public void updateTaskUI(String taskId, String title, String description, String status, String priority, String dueDate) {
        System.out.println("Updating UI for Task ID: " + taskId + " to status: " + status);
        SwingUtilities.invokeLater(() -> {
            String formattedTask = String.format("[%s] %s (%s) - %s, Due: %s", taskId, title, priority, description, dueDate);
            removeFromAllLists(taskId);
            switch (status) {
                case "Unassigned": unassignedModel.addElement(formattedTask); break;
                case "Open": openModel.addElement(formattedTask); break;
                case "Priority": priorityModel.addElement(formattedTask); break;
                case "Complete": completeModel.addElement(formattedTask); break;
            }
        });
    }

    private void removeFromAllLists(String taskId) {
        System.out.println("Removing Task ID: " + taskId + " from all lists");
        removeTaskFromModel(unassignedModel, taskId);
        removeTaskFromModel(openModel, taskId);
        removeTaskFromModel(priorityModel, taskId);
        removeTaskFromModel(completeModel, taskId);
    }

    private void removeTaskFromModel(DefaultListModel<String> model, String taskId) {
        System.out.println("Checking list for Task ID: " + taskId);
        for (int i = 0; i < model.size(); i++) {
            if (model.get(i).contains(taskId)) {
                System.out.println("Removing task from list: " + model.getElementAt(i));
                model.remove(i);
                i--;
            }
        }
    }

    public void displayError(String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE));
    }
}
