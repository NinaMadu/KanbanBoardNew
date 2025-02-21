package org.example;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class KanbanGUI {
    private DefaultListModel<String> unassignedModel, openModel, priorityModel, completeModel;
    private PrintWriter out;
    private String clientName;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    public KanbanGUI(PrintWriter out) {
        this.out = out;
        JFrame frame = new JFrame("Kanban Board");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // First, prompt the user to enter their name
        clientName = getClientName();

        // Show client name in the header
        JLabel clientNameLabel = new JLabel("Welcome " + clientName, SwingConstants.LEFT);
        clientNameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(clientNameLabel, BorderLayout.NORTH);

        // Kanban Board Section
        JPanel kanbanContainer = new JPanel(new BorderLayout());
        kanbanContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2), "Kanban Board",
                TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 16)));

        // Create Kanban board columns
        JPanel contentPanel = new JPanel(new GridLayout(1, 4, 10, 10)); // Spacing between columns
        unassignedModel = new DefaultListModel<>();
        openModel = new DefaultListModel<>();
        priorityModel = new DefaultListModel<>();
        completeModel = new DefaultListModel<>();

        // Add the Kanban columns to the content panel
        contentPanel.add(createColumn("Unassigned", unassignedModel));
        contentPanel.add(createColumn("Open", openModel));
        contentPanel.add(createColumn("Review", priorityModel));
        contentPanel.add(createColumn("Complete", completeModel));

        // Add content panel (Kanban board) to the container
        kanbanContainer.add(contentPanel, BorderLayout.CENTER);
        frame.add(kanbanContainer, BorderLayout.CENTER);

        // Create a container panel for both the button and chat panels
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS)); // Stack components vertically

        // Create the button panel for "Add Task"
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Task");
        addButton.setPreferredSize(new Dimension(150, 30)); // Increase height
        addButton.setBackground(new Color(34, 177, 76)); // Beautiful green color
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        buttonPanel.add(addButton);

        // Add the button panel to the south container
        southPanel.add(buttonPanel);

        // Chat Section
        JPanel chatContainer = new JPanel(new BorderLayout());
        chatContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2), "Chat",
                TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 16)));

        // Create the chat panel
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea(10, 50);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        messageField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(150, 30));
        sendButton.setBackground(new Color(34, 177, 76));
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.addActionListener(e -> sendMessage());
        JPanel inputPanel = new JPanel(new BorderLayout());

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // Add the chat panel to the chat container
        chatContainer.add(chatPanel, BorderLayout.CENTER);

        // Add the chat container to the south container
        southPanel.add(chatContainer);

        // Add the south container to the frame
        frame.add(southPanel, BorderLayout.SOUTH);

        // Add listener to the "Add Task" button
        addButton.addActionListener(e -> showAddTaskDialog(unassignedModel, "test"));

        // Revalidate and repaint the frame to make sure everything is updated
        frame.revalidate();
        frame.repaint();

        // Make the frame visible
        frame.setVisible(true);
    }




    private JPanel createColumn(String title, DefaultListModel<String> model) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));

        JList<String> list = new JList<>(model);
        list.setCellRenderer(new TaskRenderer());
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

//        // **Adding the Add Task Button**
//        JButton addButton = new JButton("Add Task");
//        addButton.addActionListener(e -> showAddTaskDialog(model, title));
//        panel.add(addButton, BorderLayout.SOUTH);

        return panel;
    }

    private String getClientName() {
        // Show a dialog asking the user for their name
        String name = JOptionPane.showInputDialog(null, "Please enter your name:", "Client Name", JOptionPane.PLAIN_MESSAGE);
        return name != null ? name : "Anonymous";  // If they don't provide a name, set it to "Anonymous"
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

        JMenuItem editItem = new JMenuItem("Edit Task");
        editItem.addActionListener(e -> showEditTaskDialog(task, model));
        menu.add(editItem);

        JMenuItem deleteItem = new JMenuItem("Delete Task");
        deleteItem.addActionListener(e -> deleteTask(task, model));
        menu.add(deleteItem);

        menu.show(comp, x, y);
    }

    private void showEditTaskDialog(String task, DefaultListModel<String> model) {
        // Debugging
        System.out.println("Task String: " + task);

        // Extract task ID
        String taskId = extractTaskId(task);

        // Regex to parse the task format
        Pattern pattern = Pattern.compile("\\[(.*?)\\] (.*?) \\((.*?)\\) - (.*?), Due: (\\d{4}-\\d{2}-\\d{2})");
        Matcher matcher = pattern.matcher(task);

        if (!matcher.matches()) {
            JOptionPane.showMessageDialog(null, "Invalid task format!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Extract details
        String title = matcher.group(2).trim();
        String priority = matcher.group(3).trim();
        String description = matcher.group(4).trim();
        String dueDate = matcher.group(5).trim();

        // Create Dialog
        JDialog dialog = new JDialog();
        dialog.setTitle("Edit Task");
        dialog.setSize(350, 300);
        dialog.setLayout(new GridLayout(5, 2, 5, 5)); // Grid layout with spacing

        // Create input fields
        JLabel titleLabel = new JLabel("Title:");
        JTextField titleField = new JTextField(title);

        JLabel descLabel = new JLabel("Description:");
        JTextField descField = new JTextField(description);

        JLabel dueDateLabel = new JLabel("Due Date:");
        JTextField dueDateField = new JTextField(dueDate);

        JLabel priorityLabel = new JLabel("Priority:");
        String[] priorities = {"Low", "Medium", "High"};
        JComboBox<String> priorityComboBox = new JComboBox<>(priorities);
        priorityComboBox.setSelectedItem(priority);

        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> {
            String newTitle = titleField.getText().trim();
            String newDescription = descField.getText().trim();
            String newDueDate = dueDateField.getText().trim();
            String newPriority = (String) priorityComboBox.getSelectedItem();

            if (!newTitle.isEmpty() && !newDueDate.isEmpty()) {
                // Format the updated task
                String updatedTask = String.format("[%s] %s (%s) - %s, Due: %s",
                        taskId, newTitle, newPriority, newDescription, newDueDate);

                // Prepare the message to send to the server for updating the task
                String updatedTaskMessage = String.format("EDIT:%s,%s,%s,%s,%s", taskId, newTitle, newDescription, newPriority, newDueDate);

                // Send the edit command to the server
                out.println(updatedTaskMessage);

                // Remove the old task from the model and add the updated one
                model.removeElement(task);
                model.addElement(updatedTask);

                // Close the edit dialog
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Task title and due date cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Add components to dialog
        dialog.add(titleLabel);
        dialog.add(titleField);
        dialog.add(descLabel);
        dialog.add(descField);
        dialog.add(dueDateLabel);
        dialog.add(dueDateField);
        dialog.add(priorityLabel);
        dialog.add(priorityComboBox);
        dialog.add(new JLabel()); // Empty placeholder for spacing
        dialog.add(saveButton);

        dialog.setVisible(true);
    }



    private void deleteTask(String task, DefaultListModel<String> model) {
        System.out.println("Deleting Task: " + task);
        String taskId = extractTaskId(task);
        out.println("DELETE:" + taskId);
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
        JButton priorityButton = new JButton("Move to Review");
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

    public void removeFromAllLists(String taskId) {
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


    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            // Display the message in the chat area with the client name
            //chatArea.append(clientName + ": " + message + "\n");
            messageField.setText("");  // Clear the input field

            // Send message to the server with "CHAT:" prefix
            out.println("CHAT: " + clientName + ": " + message);  // Send formatted message with client name
            out.flush();
        }
    }

    // Update chat area with a new message
    public void updateChatArea(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append("  " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength()); // Scroll to bottom
        });
    }

    // Handling incoming messages from the server
    public void handleIncomingMessage(String message) {
        updateChatArea(message); // Update chat area with incoming message
    }

}
