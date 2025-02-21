package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TaskRenderer extends JLabel implements ListCellRenderer<String> {

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String task, int index, boolean isSelected, boolean cellHasFocus) {
        // Initialize task details
        String title = "";
        String description = "";
        String priority = "";
        String dueDate = "";

        // Regex to parse the task format
        Pattern pattern = Pattern.compile("\\[(.*?)\\] (.*?) \\((.*?)\\) - (.*?), Due: (\\d{4}-\\d{2}-\\d{2})");
        Matcher matcher = pattern.matcher(task);

        if (!matcher.matches()) {
            // Handle invalid task format
            setText("<html><b>Invalid task format!</b></html>");
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            setBackground(Color.RED);
            return this;
        }

        // Extract task details
        try {
            title = matcher.group(2).trim();
            priority = matcher.group(3).trim().toLowerCase(); // Convert to lowercase for comparison
            description = matcher.group(4).trim();
            dueDate = matcher.group(5).trim();
        } catch (Exception e) {
            title = "Error parsing task";
            description = "Error parsing task description";
            priority = "N/A";
            dueDate = "N/A";
        }

        // Determine background color based on priority
        Color bgColor;
        switch (priority) {
            case "high":
                bgColor = new Color(255, 102, 102); // Light Red
                break;
            case "medium":
                bgColor = new Color(255, 204, 102); // Light Orange
                break;
            case "low":
                bgColor = new Color(153, 204, 255); // Light Blue
                break;
            default:
                bgColor = list.getBackground(); // Default background
                break;
        }

        // Set text formatting
        setText("<html>" +
                "<b>" + title + "</b><br>" +
                "<i>Description:</i> " + description + "<br>" +
                "<i>Priority:</i> " + priority + "<br>" +
                "<i>Due Date:</i> " + dueDate + "<br>" +
                "</html>");

        // Apply background color
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(bgColor);
            setForeground(Color.BLACK);
        }

        // Ensure background is visible
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return this;
    }
}
