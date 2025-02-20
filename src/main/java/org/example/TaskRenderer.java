package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TaskRenderer extends JLabel implements ListCellRenderer<String> {

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String task, int index, boolean isSelected, boolean cellHasFocus) {
        // Set the background color if selected
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

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
            return this;
        }

        // Extract task details using the regex groups
        try {
            title = matcher.group(2).trim();
            priority = matcher.group(3).trim();
            description = matcher.group(4).trim();
            dueDate = matcher.group(5).trim();
        } catch (Exception e) {
            // Fallback in case of unexpected errors
            title = "Error parsing task";
            description = "Error parsing task description";
            priority = "N/A";
            dueDate = "N/A";
        }

        // Display the task with improved formatting
        setText("<html>" +
                "<b>" + title + "</b><br>" +
                "<i>Description:</i> " + description + "<br>" +
                "<i>Priority:</i> " + priority + "<br>" +
                "<i>Due Date:</i> " + dueDate + "<br>" +
                "</html>");

        // Set alignment and style
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return this;
    }
}
