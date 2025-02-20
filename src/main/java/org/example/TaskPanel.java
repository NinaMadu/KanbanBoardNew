package org.example;

import javax.swing.*;
import java.awt.*;

class TaskPanel extends JPanel {
    private String taskDetails; // Store the original task details string

    public TaskPanel(String taskDetails) {
        this.taskDetails = taskDetails; // Save task details

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 2)); // Border for each task

        // Hover effect
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackground(new Color(220, 220, 220)); // Change background color on hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                setBackground(Color.WHITE); // Restore original background color
            }
        });

        String[] details = taskDetails.split(" \\| "); // Split task details
        String title = details[0];
        String description = details[1];
        String priority = details[2];
        String dueDate = details[3];

        if (priority.equals("High")) {
            setBackground(new Color(255, 0, 0)); // Red for high priority
        } else if (priority.equals("Medium")) {
            setBackground(new Color(255, 165, 0)); // Orange for medium priority
        } else {
            setBackground(new Color(0, 255, 0)); // Green for low priority
        }

        JPanel taskDetailsPanel = new JPanel();
        taskDetailsPanel.setLayout(new BoxLayout(taskDetailsPanel, BoxLayout.Y_AXIS));

        // Add title
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        taskDetailsPanel.add(titleLabel);

        // Add description
        JLabel descriptionLabel = new JLabel("<html><i>" + description + "</i></html>");
        taskDetailsPanel.add(descriptionLabel);

        // Add priority and due date in a more readable format
        JPanel priorityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        priorityPanel.add(new JLabel("Priority: " + priority));
        priorityPanel.add(new JLabel("Due: " + dueDate));
        taskDetailsPanel.add(priorityPanel);

        add(taskDetailsPanel, BorderLayout.CENTER);
    }

    // Method to return the task details as a string
    public String getTaskDetails() {
        return taskDetails;
    }
}
