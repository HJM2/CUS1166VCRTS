// VCRTSGUI.java


import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.jdatepicker.impl.DateComponentFormatter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Properties;

public class VCRTSGUI extends JFrame {
    private Client client;
    private JTextField usernameField, clientIdField, jobDurationField, jobDescriptionField;
    private JPasswordField passwordField;
    private JTextField ownerIdField, vehicleModelField, vehicleBrandField, plateNumberField, serialNumberField, vinNumberField, fNameField, lNameField, emailField, newUsernameField;
    private JComboBox<String> redundancyComboBox;
    private JDatePickerImpl residencyDatePicker, dobPicker, jobDeadlinePicker;
    private JPanel mainPanel, loginPanel, signupPanel, clientPanel, ownerPanel, mainPagePanel, jobContainer;

    private CardLayout cardLayout;
    private String accountType;
    private String currentClientId;

    private Color buttonColor;
    private Color backgroundColor;
    private Color textColor;

    public VCRTSGUI() throws IOException {
        client = new Client();
        setTitle("Vehicular Cloud Real Time System (VCRTS)");
        setSize(800, 500);
        ImageIcon logo = new ImageIcon("bin/VCRTS Logo.png");
        
        setIconImage(logo.getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buttonColor = new Color(44, 118, 220);
        backgroundColor = new Color(240, 250, 255);
        textColor = new Color(29, 42, 59);

        setupPanels();
        createMainPage();
        setVisible(true);
    }

    private void setupPanels() throws IOException {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        createWelcomePanel();
        createLoginPanel();
        createSignupPanel();
        createClientPanel();
        createOwnerPanel();
        createVCCControllerPanel();
        add(mainPanel);
    }
    private void createVCCControllerPanel() {
        // Set up the main panel for VCC Controller view
        JPanel vccControllerPanel = new JPanel(new BorderLayout());

        // Title label for the header, already hardcoded
        JLabel titleLabel = new JLabel("<html><h2>All Assigned Jobs and Completion Times:</h2></html>");
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Container to hold all job entries
        jobContainer = new JPanel();
        jobContainer.setLayout(new BoxLayout(jobContainer, BoxLayout.Y_AXIS));
        JScrollPane jobScrollPane = new JScrollPane(jobContainer);
        jobScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        jobScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Refresh button to reload the job list
        JButton refreshButton = createStyledButton("Refresh Job List");
        refreshButton.addActionListener(e -> refreshJobList());

        // Back button to return to the login screen
        JButton backButton = createStyledButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Welcome")); // Navigate to login panel
        backButton.addActionListener(e -> resizeForPanel("Welcome"));
        
        // Panel to hold the bottom buttons (Refresh and Back)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(refreshButton);
        bottomPanel.add(backButton);

        // Add components to the main VCC Controller panel
        vccControllerPanel.add(titleLabel, BorderLayout.NORTH);
        vccControllerPanel.add(jobScrollPane, BorderLayout.CENTER);
        vccControllerPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add the VCC Controller panel to the main layout
        mainPanel.add(vccControllerPanel, "VCCController");
    }


    private void refreshJobList() {
        jobContainer.removeAll(); // Clear previous entries

        String response = client.requestAllJobs();
        String[] jobs = response.split("\n(?=Job ID: )");

        if (jobs.length > 1) { // Skip header and process jobs
            for (int i = 1; i < jobs.length; i++) {
                JPanel jobPanel = new JPanel(new BorderLayout());
                jobPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

                JLabel jobLabel = new JLabel("<html>" + jobs[i].replaceAll("\n", "<br>") + "</html>");
                jobPanel.add(jobLabel, BorderLayout.CENTER);

                JButton completeButton = createStyledButton("Mark Complete");
                completeButton.setPreferredSize(new Dimension(200, completeButton.getPreferredSize().height));

                // Extract Job ID and pass it to the button's ActionListener
                String jobId = jobs[i].split(",")[0].split(": ")[1].trim();
                completeButton.addActionListener(ev -> markJobComplete(jobId));
                
                jobPanel.add(completeButton, BorderLayout.EAST);
                jobContainer.add(jobPanel);
            }
        } else {
            JLabel noJobsLabel = new JLabel("Please refresh to view available jobs if any are currently available.");
            noJobsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            jobContainer.add(noJobsLabel);
        }

        jobContainer.revalidate();
        jobContainer.repaint();
        pack();
    }




    // Helper method for marking job as complete
    private void markJobComplete(String jobId) {
        try {
            // Send the mark complete request to the server
            String response = client.sendRequest("MARK_COMPLETE " + jobId);

            // Show the server response
            JOptionPane.showMessageDialog(this, response);

            // Refresh the job list to update the GUI
            refreshJobList();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while marking the job complete.");
        }
    }


    private void resizeForPanel(String panelName) {
        switch (panelName) {
            case "Welcome":
                setSize(500, 250); // Smaller window for welcome page
                break;
            case "Login":
                setSize(300, 250); // Smaller window for login screen
                break;
            case "Signup":
                setSize(550, 410); // Medium-sized window for login/signup
                break;
            case "Client":
            	setSize(500, 410);
            	break;
            case "Owner":
            	setSize(450, 410);
            	break;
            case "VCCController":
                setSize(700, 300); // Larger window for other panels
                break;
            default:
                setSize(300, 250); // Default size
        }
        setLocationRelativeTo(null); // Re-center the window after resizing
    }

    // Helper method to send a completion request to the server


    private void createWelcomePanel() throws IOException {
        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(backgroundColor);
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel logoImage = new JLabel("");
        BufferedImage bufferedImage = ImageIO.read(this.getClass().getResource("VCRTS Logo.png"));
        Image image = bufferedImage.getScaledInstance(200, 200, Image.SCALE_DEFAULT);
        logoImage.setIcon(new ImageIcon(image));
        logoImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoImage.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        
        JLabel titleLabel = new JLabel("<html><div style='text-align:center'>Welcome to the Vehicular Cloud Real-Time System </div></html>", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 45));
        titleLabel.setForeground(textColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JLabel infoLabel = new JLabel("<html><div style='text-align:center'>This system allow car owners to share their computation power with clients to complete their jobs.<br>Join as a Job Submitter or Car Owner to participate!</div></html>", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Serif", Font.PLAIN, 20));
        infoLabel.setForeground(textColor);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 20));

        JLabel loginText = new JLabel("Returning Users");
        loginText.setFont(new Font("Serif", Font.BOLD, 20));

        JLabel signUpText = new JLabel("New Users");

        JButton loginButton = createStyledButton("Login");

        loginButton.addActionListener(e -> cardLayout.show(mainPanel, "Login")); //resizeForPanel
        loginButton.addActionListener(e -> resizeForPanel("Login"));


        JButton signupButton = createStyledButton("Sign Up");

        signupButton.addActionListener(e -> cardLayout.show(mainPanel, "Signup"));
        signupButton.addActionListener(e -> resizeForPanel("Signup"));
        buttonPanel.add(loginButton);
        buttonPanel.add(signupButton);

        // Footer component
        JLabel footerLabel = new JLabel("© 2024 Vehicular Cloud Real-Time System");
        footerLabel.setFont(new Font("Serif", Font.ITALIC, 12));
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        footerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Add components to the welcome panel with balanced spacing
        welcomePanel.add(logoImage);
        welcomePanel.add(titleLabel);
        welcomePanel.add(Box.createVerticalStrut(50));
        welcomePanel.add(infoLabel);
        welcomePanel.add(Box.createVerticalStrut(50)); // Space between info text and buttons
        welcomePanel.add(buttonPanel);
        welcomePanel.add(Box.createVerticalGlue()); // Dynamic space between buttons and footer
        welcomePanel.add(footerLabel);

        mainPanel.add(welcomePanel, "Welcome");
    }

    private void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        //gbc.insets = new Insets(2, 2, 2, 2); // Minimal spacing

        // Remove unnecessary borders
        loginPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); 

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(15);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(15);

        // Add username components
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        // Add password components
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        // Add buttons
        JButton loginButton = createStyledButton("Login");
        loginButton.addActionListener(e -> loginUser());

        JButton backButton = createStyledButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Welcome")); 
        backButton.addActionListener(e -> resizeForPanel("Welcome"));
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(loginButton, gbc);
        gbc.gridx = 1;
        loginPanel.add(backButton, gbc);

        // Add the panel to the main layout
        mainPanel.add(loginPanel, "Login");
    }


    private void createSignupPanel() {
        signupPanel = new JPanel(new GridBagLayout());
        signupPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel fNameLabel = new JLabel("First Name:");
        fNameField = new JTextField(15);
        JLabel lNameLabel = new JLabel("Last Name:");
        lNameField = new JTextField(15);
        JLabel usernameLabel = new JLabel("Username:");
        newUsernameField = new JTextField(15);
        JLabel emailLabel = new JLabel("Email Address:");
        emailField = new JTextField(15);
        JLabel dobLabel = new JLabel("Date of Birth:");

        UtilDateModel dateModel = new UtilDateModel();
        Properties properties = new Properties();
        properties.put("text.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(dateModel, properties);
        dobPicker = new JDatePickerImpl(datePanel, new DateComponentFormatter());

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField newPasswordField = new JPasswordField(15);
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        JPasswordField confirmPasswordField = new JPasswordField(15);

        JLabel accountTypeLabel = new JLabel("Account Type:");
        JRadioButton carOwnerButton = new JRadioButton("Car Owner");
        JRadioButton jobSubmitterButton = new JRadioButton("Job Submitter");
        

        ButtonGroup accountTypeGroup = new ButtonGroup();
        accountTypeGroup.add(carOwnerButton);
        accountTypeGroup.add(jobSubmitterButton);
        

        gbc.gridx = 0;
        gbc.gridy = 0;
        signupPanel.add(fNameLabel, gbc);
        gbc.gridx = 1;
        signupPanel.add(fNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        signupPanel.add(lNameLabel, gbc);
        gbc.gridx = 1;
        signupPanel.add(lNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        signupPanel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        signupPanel.add(newUsernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        signupPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        signupPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        signupPanel.add(dobLabel, gbc);
        gbc.gridx = 1;
        signupPanel.add(dobPicker, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        signupPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        signupPanel.add(newPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        signupPanel.add(confirmPasswordLabel, gbc);
        gbc.gridx = 1;
        signupPanel.add(confirmPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        signupPanel.add(accountTypeLabel, gbc);
        gbc.gridx = 1;
        JPanel accountTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        accountTypePanel.add(carOwnerButton);
        accountTypePanel.add(jobSubmitterButton);
        signupPanel.add(accountTypePanel, gbc);

        JButton signupButton = createStyledButton("Register");
        signupButton.addActionListener(e -> {
            String firstName = fNameField.getText();
            String lastName = lNameField.getText();
            String username = newUsernameField.getText();
            String email = emailField.getText();
            String dob = dobPicker.getJFormattedTextField().getText();
            String password = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            String accountType;
            if (carOwnerButton.isSelected()) {
                accountType = "CarOwner";
            } else if (jobSubmitterButton.isSelected()) {
                accountType = "JobSubmitter";
            } else {
                JOptionPane.showMessageDialog(this, "Please select an account type.");
                return;
            }

            if (password.equals(confirmPassword)) {
                String response = client.register(firstName, lastName, username, email, dob, password, accountType);
                JOptionPane.showMessageDialog(this, response);
                if (response.equals("Registration successful")) {
                    cardLayout.show(mainPanel, "Login");
                    resizeForPanel("Login");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Passwords do not match!");
            }
        });

        JButton backButton = createStyledButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Welcome"));
        backButton.addActionListener(e -> resizeForPanel("Welcome"));
        gbc.gridx = 0;
        gbc.gridy = 8;
        signupPanel.add(signupButton, gbc);
        gbc.gridx = 1;
        signupPanel.add(backButton, gbc);

        mainPanel.add(signupPanel, "Signup");
    }


    private void createMainPage() {
        mainPagePanel = new JPanel(new GridLayout(3, 1, 10, 10));
        mainPagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPagePanel.add(new JLabel("Main Page", SwingConstants.CENTER));

        JButton clientButton = createStyledButton("Client Panel");
        clientButton.addActionListener(e -> cardLayout.show(mainPanel, "Client"));
        mainPagePanel.add(clientButton);

        JButton ownerButton = createStyledButton("Owner Panel");
        ownerButton.addActionListener(e -> cardLayout.show(mainPanel, "Owner"));
        mainPagePanel.add(ownerButton);

        mainPanel.add(mainPagePanel, "MainPage");
    }

    private void createClientPanel() {
        clientPanel = new JPanel(new GridBagLayout());
        clientPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Client ID (auto-populated and uneditable)
        gbc.gridx = 0;
        gbc.gridy = 0;
        clientPanel.add(new JLabel("Client ID:"), gbc);

        clientIdField = new JTextField(15);
        clientIdField.setEditable(false);
        gbc.gridx = 1;
        clientPanel.add(clientIdField, gbc);

        // Job Description Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        clientPanel.add(new JLabel("Job Description:"), gbc);

        JTextField jobDescriptionField = new JTextField(15);
        gbc.gridx = 1;
        clientPanel.add(jobDescriptionField, gbc);

        // Job Duration Field (in hours)
        gbc.gridx = 0;
        gbc.gridy = 2;
        clientPanel.add(new JLabel("Job Duration (hours):"), gbc);

        jobDurationField = new JTextField(15);
        gbc.gridx = 1;
        clientPanel.add(jobDurationField, gbc);

        // Job Deadline (date picker)
        gbc.gridx = 0;
        gbc.gridy = 3;
        clientPanel.add(new JLabel("Job Deadline:"), gbc);

        UtilDateModel dateModel = new UtilDateModel();
        Properties properties = new Properties();
        properties.put("text.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(dateModel, properties);
        jobDeadlinePicker = new JDatePickerImpl(datePanel, new DateComponentFormatter());
        gbc.gridx = 1;
        clientPanel.add(jobDeadlinePicker, gbc);

        // Redundancy Level Selection (Number of Cars)
        gbc.gridx = 0;
        gbc.gridy = 4;
        clientPanel.add(new JLabel("Redundancy Level (Number of Cars):"), gbc);

        JComboBox<String> redundancyComboBox = new JComboBox<>(new String[] { "1", "2", "3", "4", "5" });
        gbc.gridx = 1;
        clientPanel.add(redundancyComboBox, gbc);

        // Submit Job Button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        JButton submitJobButton = createStyledButton("Submit Job");
        submitJobButton.addActionListener(e -> {
            try {
                String clientId = clientIdField.getText();
                String jobDescription = jobDescriptionField.getText();
                int jobDuration = Integer.parseInt(jobDurationField.getText());
                int redundancyLevel = Integer.parseInt((String) redundancyComboBox.getSelectedItem());
                String jobDeadline = jobDeadlinePicker.getJFormattedTextField().getText();

                if (clientId.isEmpty() || jobDescription.isEmpty() || jobDeadline.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                    return;
                }

                String response = client.submitJob(clientId, jobDescription, jobDuration, redundancyLevel, jobDeadline);
                JOptionPane.showMessageDialog(this, response);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for job duration and redundancy level.");
            }
        });
        clientPanel.add(submitJobButton, gbc);

        // Display Jobs & Completion Times Button
        gbc.gridy = 6;
        gbc.gridwidth = 1; // Reset to single column width
        gbc.gridx = 0;
        JButton displayJobsButton = createStyledButton("Display Jobs & Completion Times");
        displayJobsButton.addActionListener(e -> displayVCCJobsAndTimes());
        clientPanel.add(displayJobsButton, gbc);

        // Back Button
        gbc.gridx = 1;
        JButton backButton = createStyledButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Login")); // Adjusted to go to Login screen
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Login"));
        
        clientPanel.add(backButton, gbc);

        mainPanel.add(clientPanel, "Client");
    }




    private void createOwnerPanel() {
        ownerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Owner ID (auto-populated and uneditable)
        gbc.gridx = 0;
        gbc.gridy = 0;
        ownerPanel.add(new JLabel("Owner ID:"), gbc);
        ownerIdField = new JTextField();
        ownerIdField.setEditable(false);
        gbc.gridx = 1;
        ownerPanel.add(ownerIdField, gbc);

        // Vehicle Model
        gbc.gridx = 0;
        gbc.gridy = 1;
        ownerPanel.add(new JLabel("Vehicle Model:"), gbc);
        vehicleModelField = new JTextField(15);
        gbc.gridx = 1;
        ownerPanel.add(vehicleModelField, gbc);

        // Vehicle Brand
        gbc.gridx = 0;
        gbc.gridy = 2;
        ownerPanel.add(new JLabel("Vehicle Brand:"), gbc);
        vehicleBrandField = new JTextField(15);
        gbc.gridx = 1;
        ownerPanel.add(vehicleBrandField, gbc);

        // Plate Number
        gbc.gridx = 0;
        gbc.gridy = 3;
        ownerPanel.add(new JLabel("Plate Number:"), gbc);
        plateNumberField = new JTextField(15);
        gbc.gridx = 1;
        ownerPanel.add(plateNumberField, gbc);

        // Serial Number
        gbc.gridx = 0;
        gbc.gridy = 4;
        ownerPanel.add(new JLabel("Serial Number:"), gbc);
        serialNumberField = new JTextField(15);
        gbc.gridx = 1;
        ownerPanel.add(serialNumberField, gbc);

        // VIN Number
        gbc.gridx = 0;
        gbc.gridy = 5;
        ownerPanel.add(new JLabel("VIN Number:"), gbc);
        vinNumberField = new JTextField(15);
        gbc.gridx = 1;
        ownerPanel.add(vinNumberField, gbc);

        // Residency Date
        gbc.gridx = 0;
        gbc.gridy = 6;
        ownerPanel.add(new JLabel("Residency Date:"), gbc);

        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");

        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        residencyDatePicker = new JDatePickerImpl(datePanel, new DateComponentFormatter());
        gbc.gridx = 1;
        ownerPanel.add(residencyDatePicker, gbc);

        // Register Vehicle Button
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        JButton registerVehicleButton = new JButton("Register Vehicle");
        registerVehicleButton.addActionListener(e -> registerVehicle());
        ownerPanel.add(registerVehicleButton, gbc);

        // Back Button - now set to return to the Login panel
        gbc.gridy = 8;
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Login"));
        backButton.addActionListener(e -> resizeForPanel("Login"));// Show Login screen
        
        ownerPanel.add(backButton, gbc);

        mainPanel.add(ownerPanel, "Owner");
    }

    private void loginUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String response = client.login(username, password).trim();  // Trim the response to remove any extra whitespace

        if (response.startsWith("Login successful")) {
            String[] parts = response.split(",");
            accountType = parts[1].trim();  // Ensure no extra spaces
            currentClientId = username;
            clientIdField.setText(currentClientId);

            // Redirect based on account type
            if ("JobSubmitter".equals(accountType)) {
                cardLayout.show(mainPanel, "Client");
                resizeForPanel("Client");
            } else if ("CarOwner".equals(accountType)) {
                cardLayout.show(mainPanel, "Owner");
                ownerIdField.setText(currentClientId);
                resizeForPanel("Owner");
            } else if ("VCCController".equals(accountType)) {
                cardLayout.show(mainPanel, "VCCController");  // Show VCC Controller panel
                resizeForPanel("VCCController");
            }
            JOptionPane.showMessageDialog(this, "Login successful as " + accountType);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again.");
        }
    }



    private void submitJob() {
        try {
            String clientId = clientIdField.getText();
            String jobDescription = jobDescriptionField.getText();
            int jobDuration = Integer.parseInt(jobDurationField.getText());
            int redundancyLevel = Integer.parseInt((String) redundancyComboBox.getSelectedItem());
            String jobDeadline = jobDeadlinePicker.getJFormattedTextField().getText();  // Get jobDeadline from the date picker

            if (clientId.isEmpty() || jobDescription.isEmpty() || jobDeadline.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            String response = client.submitJob(clientId, jobDescription, jobDuration, redundancyLevel, jobDeadline);
            JOptionPane.showMessageDialog(this, response);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for job duration and redundancy level.");
        }
    }


    private void registerVehicle() {
        String ownerId = ownerIdField.getText();
        String vehicleModel = vehicleModelField.getText();
        String vehicleBrand = vehicleBrandField.getText();
        String plateNumber = plateNumberField.getText();
        String serialNumber = serialNumberField.getText();
        String vinNumber = vinNumberField.getText();
        
        // Format residency date to avoid spaces
        String residencyDate = residencyDatePicker.getJFormattedTextField().getText();
        try {
            LocalDate parsedDate = LocalDate.parse(residencyDate, DateTimeFormatter.ofPattern("MMM d, yyyy"));
            residencyDate = parsedDate.format(DateTimeFormatter.ISO_LOCAL_DATE); // Format as "YYYY-MM-DD"
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use 'MMM d, yyyy'.");
            return;
        }

        // Send car registration details to the server
        String response = client.notifyCarReady(ownerId, vehicleModel, vehicleBrand, plateNumber, serialNumber, vinNumber, residencyDate);
        JOptionPane.showMessageDialog(this, response);
    }


    private void displayVCCJobsAndTimes() {
        //String clientId = clientIdField.getText();  // Retrieve the current client ID from the text field
        String response = client.requestAllJobs();  // Send both clientId and role to server
        JOptionPane.showMessageDialog(this, response);  // Display server response in a message dialog
    }

    private static class RoundedBorder implements Border {
        private int radius;

        RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component c){
            return new Insets(this.radius + 1, this.radius + 1, this.radius + 2, this.radius);
        }

        @Override
        public boolean isBorderOpaque(){
            return true;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        //button.setPreferredSize(new Dimension(150, 50));
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBackground(buttonColor);
        button.setFocusPainted(false);
        button.setBounds(500, 500, 100, 25);
        //button.setBorder(new RoundedBorder(30));
        return button;
    }

    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(() -> {
            try {
                new VCRTSGUI();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
