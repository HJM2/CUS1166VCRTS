import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ServerGUI extends JFrame {
    private JTabbedPane tabbedPane;
    private DefaultTableModel vehiclesTableModel;
    private DefaultTableModel jobsTableModel;
    private JTextArea logArea;
    private JLabel statusLabel;
    private Server server;

    public ServerGUI() {
        setupMainWindow();
        server = new Server(this);
        updateServerStatus(server.isRunning());
    }

    private void setupMainWindow() {
        setTitle("VCRTS Server Control Panel");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main Panel Layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add Status Panel
        mainPanel.add(createStatusPanel(), BorderLayout.NORTH);

        // Add Tabbed Panel for Vehicles and Jobs
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Registered Vehicles", createVehiclePanel());
        tabbedPane.addTab("Job Requests", createJobManagementPanel());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Add Log Panel
        mainPanel.add(createLogPanel(), BorderLayout.SOUTH);

        add(mainPanel);
        setLocationRelativeTo(null);
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Server Status"));

        statusLabel = new JLabel("Server Status: Stopped");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(Color.RED);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton startButton = new JButton("Start Server");
        JButton stopButton = new JButton("Stop Server");

        startButton.addActionListener(e -> {
            if (!server.isRunning()) {
                server.start();
                updateServerStatus(true);
            }
        });

        stopButton.addActionListener(e -> {
            if (server.isRunning()) {
                server.stop();
                updateServerStatus(false);
            }
        });

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createVehiclePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Registered Vehicles"));

        String[] columns = {"VIN", "Owner ID", "Status", "Model", "Brand", "Plate Number", "Serial Number", "Residency Time"};
        vehiclesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make the table non-editable
            }
        };

        JTable vehiclesTable = new JTable(vehiclesTableModel);
        vehiclesTable.getTableHeader().setReorderingAllowed(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        JButton markCompleteButton = new JButton("Mark Complete");

        refreshButton.addActionListener(e -> refreshVehicleTable());
        markCompleteButton.addActionListener(e -> markSelectedVehicleComplete(vehiclesTable));

        buttonPanel.add(refreshButton);
        buttonPanel.add(markCompleteButton);

        panel.add(new JScrollPane(vehiclesTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }


    private JPanel createJobManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Job Requests"));

        String[] columns = {"Job ID", "Client ID", "Description", "Duration", "Redundancy", "Deadline", "Timestamp"};
        jobsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make the table non-editable
            }
        };

        JTable jobsTable = new JTable(jobsTableModel);
        jobsTable.getTableHeader().setReorderingAllowed(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        JButton completeButton = new JButton("Mark Complete");

        refreshButton.addActionListener(e -> refreshJobRequestsTable());
        completeButton.addActionListener(e -> markSelectedJobComplete(jobsTable));

        buttonPanel.add(refreshButton);
        buttonPanel.add(completeButton);

        panel.add(new JScrollPane(jobsTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Server Log"));

        logArea = new JTextArea(6, 50);
        logArea.setEditable(false);

        JButton clearButton = new JButton("Clear Log");
        clearButton.addActionListener(e -> logArea.setText(""));

        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        panel.add(clearButton, BorderLayout.SOUTH);

        return panel;
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void refreshVehicleTable() {
        SwingUtilities.invokeLater(() -> {
            vehiclesTableModel.setRowCount(0);
            List<CarRentals> vehicles = server.getVCController().getVehiclesReady();
            for (CarRentals vehicle : vehicles) {
                vehiclesTableModel.addRow(new Object[]{
                        vehicle.getVinNumber(), vehicle.getOwnerId(), "Ready", vehicle.getVehicleModel(),
                        vehicle.getVehicleBrand(), vehicle.getPlateNumber(), vehicle.getSerialNumber(), vehicle.getResidencyTime()
                });
            }
            log("Vehicle table refreshed.");
        });
    }

    private void refreshJobRequestsTable() {
        SwingUtilities.invokeLater(() -> {
            jobsTableModel.setRowCount(0);
            List<JobRequest> jobs = server.getVCController().getJobsQueue();
            for (JobRequest job : jobs) {
                jobsTableModel.addRow(new Object[]{
                        job.getJobId(), job.getClientId(), job.getJobDescription(), job.getDuration(),
                        job.getRedundancyLevel(), job.getJobDeadline(), job.getTimestamp()
                });
            }
            log("Job requests table refreshed.");
        });
    }

    private void markSelectedJobComplete(JTable jobsTable) {
        int selectedRow = jobsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String jobId = (String) jobsTableModel.getValueAt(selectedRow, 0);
            String response = server.getVCController().markJobComplete("MARK_COMPLETE " + jobId);
            log(response);
            if (response.contains("successfully")) {
                jobsTableModel.removeRow(selectedRow);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a job to mark as complete.");
        }
    }

    public void updateServerStatus(boolean isRunning) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Server Status: " + (isRunning ? "Running" : "Stopped"));
            statusLabel.setForeground(isRunning ? Color.GREEN : Color.RED);
        });
    }

    public boolean showConfirmDialog(String message) {
        int result = JOptionPane.showConfirmDialog(this, message,
                "Server Confirmation", JOptionPane.YES_NO_OPTION);
        log("Admin confirmation: " + (result == JOptionPane.YES_OPTION ? "Approved" : "Declined"));
        return result == JOptionPane.YES_OPTION;
    }
    private void markSelectedVehicleComplete(JTable vehiclesTable) {
        int selectedRow = vehiclesTable.getSelectedRow();
        if (selectedRow >= 0) {
            String vinNumber = (String) vehiclesTableModel.getValueAt(selectedRow, 0); // VIN
            String ownerId = (String) vehiclesTableModel.getValueAt(selectedRow, 1);  // Owner ID
            System.out.println(vinNumber);
            System.out.println(ownerId);
            // Example action: Update vehicle status in the table
            String response = server.getVCController().handleVehicleCompletion(ownerId, vinNumber);
            log(response);

            if (response.contains("successfully")) {
                vehiclesTableModel.setValueAt("Completed", selectedRow, 2); // Update Status column
            } else {
                JOptionPane.showMessageDialog(this, "Failed to mark vehicle as complete: " + response);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a vehicle to mark as complete.");
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerGUI gui = new ServerGUI();
            gui.setVisible(true);
        });
    }
}
