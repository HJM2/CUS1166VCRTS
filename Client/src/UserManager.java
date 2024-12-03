// UserManager.java

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;
import java.sql.Date;

public class UserManager {
    private static final String USER_DATA_FILE = "UserInformation.csv";
    private Map<String, String[]> users;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sys";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "HmP9HC6RAjRaSolvPqpcJmbj3wR+UuSUUywHMQgWm7M=";

    public UserManager() {
        users = new HashMap<>();
        loadUsersFromDatabase();
    }

    private void loadUsersFromDatabase() {
        String query = "SELECT * FROM users";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String userId = resultSet.getString("userId");
                String username = resultSet.getString("username");

                String[] userData = new String[]{
                    userId,
                    resultSet.getString("firstName"),
                    resultSet.getString("lastName"),
                    username,
                    resultSet.getString("email"),
                    resultSet.getString("dob"),
                    resultSet.getString("password"),
                    resultSet.getString("accountType")
                };

                users.put(username, userData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading users from database: " + e.getMessage());
        }
    }

    public List<String[]> getAllUsers() {
        List<String[]> users = new ArrayList<>();
        String query = "SELECT userId, username, email, accountType FROM users";
        
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            
            while (resultSet.next()) {
                String[] user = new String[4];
                user[0] = resultSet.getString("userId");
                user[1] = resultSet.getString("username");
                user[2] = resultSet.getString("email");
                user[3] = resultSet.getString("accountType");
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error retrieving users from database: " + e.getMessage());
        }
        
        return users;
    }

    public String registerUser(String message) {
        String[] parts = message.split(" ");
        if (parts.length != 8) {
            return "Error: Invalid registration command";
        }

        String userId = UUID.randomUUID().toString();
        String firstName = parts[1];
        String lastName = parts[2];
        String username = parts[3];
        String email = parts[4];
        String dob = parts[5].trim();  // Trim whitespace
        String password = parts[6];
        String accountType = parts[7];

        // Debugging
        System.out.println("Original DOB value: [" + dob + "]");

        // Remove surrounding quotes, if any
        dob = dob.replaceAll("^\"|\"$", "");
        System.out.println("Processed DOB value: [" + dob + "]");

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO Users (userId, firstName, lastName, username, email, dob, password, accountType) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
             )) {

            statement.setString(1, userId);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, username);
            statement.setString(5, email);

            try {
                // Parse the date and convert to java.sql.Date
                LocalDate parsedDate = LocalDate.parse(dob); // Ensures valid format
                Date sqlDate = Date.valueOf(parsedDate);    // Converts to java.sql.Date
                statement.setDate(6, sqlDate);
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: Invalid date format for DOB. Ensure it is yyyy-MM-dd.";
            }

            statement.setString(7, password);
            statement.setString(8, accountType);

            statement.executeUpdate();
            return "Registration successful";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Unable to register user";
        }
    }






    public String loginUser(String message) {
        String[] parts = message.split(" ");
        if (parts.length != 3) {
            return "Error: Invalid login command";
        }

        String username = parts[1];
        String password = parts[2];

        System.out.printf("Attempting login: Username=%s, Password=%s\n", username, password);

        // Hardcoded credentials for "vcc"
        if (username.equals("vcc") && password.equals("vccadmin")) {
            System.out.println("Hardcoded VCC login successful");
            String vccUserId = "vcc-unique-id"; // Assign a fixed or generated userId
            return "Login successful,VCCController," + vccUserId;
        }

        // SQL Query to validate user credentials
        String query = "SELECT userId, password, accountType FROM users WHERE username = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, "\"" + username + "\""); // Add quotes to match the stored format

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    // Username not found in database
                    System.out.println("Error: Username not found in database");
                    return "Error: Username not found";
                }

                // Retrieve user details from database
                String storedPassword = resultSet.getString("password");
                String accountType = resultSet.getString("accountType");
                String userId = resultSet.getString("userId");

                // Remove quotes from stored values for comparison
                storedPassword = storedPassword.replaceAll("^\"|\"$", "");
                accountType = accountType.replaceAll("^\"|\"$", "");

                System.out.printf("Found user: StoredPassword=%s, AccountType=%s, userId=%s\n", storedPassword, accountType, userId);

                if (!storedPassword.equals(password)) {
                    // Password mismatch
                    System.out.println("Error: Incorrect password");
                    return "Error: Incorrect password";
                }

                // Login successful
                return "Login successful," + accountType + "," + userId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Unable to process login";
        }
    }


    }




   
    

