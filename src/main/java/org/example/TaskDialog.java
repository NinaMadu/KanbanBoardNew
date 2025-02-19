package org.example;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

class TaskDialog extends JDialog {
    private JTextField descriptionField;
    private JTextField priorityField;
    private JTextField dueDateField;
    private String title;
    private String status;
    private TaskAddCallback onAdd;

    public TaskDialog(String title, String status, TaskAddCallback onAdd) {
        this.title = title;
        this.status = status;
        this.onAdd = onAdd;

        setTitle("Add Task");
        setSize(300, 200);
        setLayout(new GridLayout(4, 2));

        add(new JLabel("Description:"));
        descriptionField = new JTextField();
        add(descriptionField);

        add(new JLabel("Priority (integer):"));
        priorityField = new JTextField();
        add(priorityField);

        add(new JLabel("Due Date (yyyy-MM-dd):"));
        dueDateField = new JTextField();
        add(dueDateField);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addTask());
        add(addButton);

        setLocationRelativeTo(null);
    }

    private void addTask() {
        String description = descriptionField.getText();
        int priority = Integer.parseInt(priorityField.getText());
        Date dueDate = new Date();
        try {
            dueDate = new SimpleDateFormat("yyyy-MM-dd").parse(dueDateField.getText());
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        onAdd.onAdd(title, status, description, priority, dueDate);
        dispose();
    }
}
