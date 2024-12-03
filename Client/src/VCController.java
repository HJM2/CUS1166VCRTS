import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VCController {
    private List<JobRequest> jobsQueue;  // In-memory job queue
    private List<CarRentals> vehiclesReady;  // In-memory vehicle list
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sys";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "HmP9HC6RAjRaSolvPqpcJmbj3wR+UuSUUywHMQgWm7M=";
    public VCController() {
        this.jobsQueue = new LinkedList<>();
        this.vehiclesReady = new ArrayList<>();
        loadJobsFromDatabase(); // Load jobs into memory from SQL
        loadVehiclesFromDatabase(); // Load vehicles into memory from SQL
    }
    public List<JobRequest> getJobsQueue() {
        return new ArrayList<>(jobsQueue);
    }
    // Load jobs from SQL database into the in-memory queue
    private void loadJobsFromDatabase() {
        String query = "SELECT * FROM JobRequests";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                JobRequest job = new JobRequest(
                    resultSet.getString("jobId"),
                    resultSet.getString("clientId"),
                    resultSet.getString("jobDescription"),
                    resultSet.getInt("duration"),
                    resultSet.getInt("redundancyLevel"),
                    resultSet.getString("jobDeadline"),
                    resultSet.getTimestamp("timestamp").toString()
                );
                jobsQueue.add(job);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading jobs from database: " + e.getMessage());
        }
    }
    public List<String[]> getVehiclesByOwnerId(String ownerId) {
        List<String[]> vehicles = new ArrayList<>();
        String query = "SELECT carId, ownerId, model, brand, plateNumber, serialNumber, vinNumber, residencyTime FROM CarRentals WHERE ownerId = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, ownerId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String[] vehicle = new String[] {
                    resultSet.getString("carId"),         // Car ID
                    resultSet.getString("ownerId"),       // Owner ID
                    resultSet.getString("model"),         // Model
                    resultSet.getString("brand"),         // Brand
                    resultSet.getString("plateNumber"),   // Plate Number
                    resultSet.getString("serialNumber"),  // Serial Number
                    resultSet.getString("vinNumber"),     // VIN
                    resultSet.getString("residencyTime")  // Residency Time
                };
                vehicles.add(vehicle);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error fetching vehicles for owner ID: " + e.getMessage());
        }
        return vehicles;
    }


    
    public List<CarRentals> getVehiclesReady() {
        return new ArrayList<>(vehiclesReady); // Return a copy to avoid modification
    }

    // Load vehicles from SQL database into the in-memory list
    private void loadVehiclesFromDatabase() {
        String query = "SELECT * FROM carrentals";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                CarRentals car = new CarRentals(
                    resultSet.getString("ownerId"),
                    resultSet.getString("model"),
                    resultSet.getString("brand"),
                    resultSet.getString("plateNumber"),
                    resultSet.getString("serialNumber"),
                    resultSet.getString("vinNumber"),
                    resultSet.getTimestamp("residencyTime").toString()
                );
                vehiclesReady.add(car);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading vehicles from database: " + e.getMessage());
        }
    }

    // Handle job submission and log it into SQL
    public String handleJobSubmission(String message) {
        System.out.println("Received job submission message: " + message);
        Pattern pattern = Pattern.compile("JOB_SUBMIT (\\S+) \"([^\"]+)\" (\\d+) (\\d+) (\\S+)");
        Matcher matcher = pattern.matcher(message);

        if (!matcher.matches()) {
            return "Invalid job submission format.";
        }

        String clientId = matcher.group(1);
        String jobDescription = matcher.group(2);
        int duration = Integer.parseInt(matcher.group(3));
        int redundancyLevel = Integer.parseInt(matcher.group(4));
        String jobDeadline = matcher.group(5);

        JobRequest job = new JobRequest(clientId, jobDescription, duration, redundancyLevel, jobDeadline);

        // Log job into the database
        String query = "INSERT INTO JobRequests (jobId, clientId, jobDescription, duration, redundancyLevel, jobDeadline, timestamp) VALUES (?, ?, ?, ?, ?, ?, NOW())";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, job.getJobId());
            statement.setString(2, clientId);
            statement.setString(3, jobDescription);
            statement.setInt(4, duration);
            statement.setInt(5, redundancyLevel);
            statement.setString(6, jobDeadline);

            statement.executeUpdate();
            jobsQueue.add(job); // Keep it in memory
            return "Job submitted and logged successfully with ID: " + job.getJobId();
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Unable to log job in database.";
        }
    }

    // Handle car readiness and log it into SQL
    public String handleCarReady(String message) {
        String[] parts = message.split(" ");
        if (parts.length < 8) {
            return "Error: Invalid car readiness format.";
        }

        String ownerId = parts[1];
        String model = parts[2];
        String brand = parts[3];
        String plateNumber = parts[4];
        String serialNumber = parts[5];
        String vinNumber = parts[6];
        String residencyDate = parts[7]; // Expected format: yyyy-MM-dd

        String query = "INSERT INTO CarRentals (ownerId, model, brand, plateNumber, serialNumber, vinNumber, residencyTime) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, ownerId);
            statement.setString(2, model);
            statement.setString(3, brand);
            statement.setString(4, plateNumber);
            statement.setString(5, serialNumber);
            statement.setString(6, vinNumber);
            statement.setDate(7, java.sql.Date.valueOf(residencyDate)); // Convert to java.sql.Date

            statement.executeUpdate();
            CarRentals car = new CarRentals(ownerId, model, brand, plateNumber, serialNumber, vinNumber, residencyDate);
            vehiclesReady.add(car); // Keep it in memory
            return "Car ready notification logged successfully.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Unable to log car readiness in database.";
        }
    }

    public String handleVehicleRemoval(String message) {
        try {
            // Parse message format: "REMOVE_VEHICLE ownerId vinNumber"
            String[] parts = message.split(" ");
            if (parts.length != 3) {
                return "Error: Invalid command format";
            }

            String ownerId = parts[1];
            String vinNumber = parts[2];

            // SQL Query to remove vehicle from database
            String query = "DELETE FROM CarRentals WHERE ownerId = ? AND vinNumber = ?";
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, ownerId);
                statement.setString(2, vinNumber);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    // Remove from the in-memory list
                    vehiclesReady.removeIf(car -> car.getOwnerId().equals(ownerId) && car.getVinNumber().equals(vinNumber));
                    return "Vehicle removed successfully.";
                } else {
                    return "Error: Vehicle not found in the database.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Unable to remove vehicle from the database.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing vehicle removal request.";
        }
    }

    // Get all jobs from memory
    public String getAllJobs() {
        StringBuilder jobInfo = new StringBuilder("All Jobs in Queue:\n");
        for (JobRequest job : jobsQueue) {
            jobInfo.append(String.format(
                "Job ID: %s, Client ID: %s, Description: %s, Duration: %d, Redundancy Level: %d, Deadline: %s\n",
                job.getJobId(), job.getClientId(), job.getJobDescription(),
                job.getDuration(), job.getRedundancyLevel(), job.getJobDeadline()
            ));
        }
        return jobInfo.toString();
    }

    // Mark job as complete and remove it from SQL and memory
    public String markJobComplete(String message) {
        String[] parts = message.split(" ");
        if (parts.length != 2) {
            return "Error: Invalid MARK_COMPLETE command format.";
        }

        String jobId = parts[1];

        String query = "DELETE FROM JobRequests WHERE jobId = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, jobId);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                jobsQueue.removeIf(job -> job.getJobId().equals(jobId)); // Remove from memory
                return "Job marked as complete and removed successfully.";
            } else {
                return "Error: Job ID not found.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Unable to mark job as complete in database.";
        }
    }
    public String displayJobsAndCompletionTimes() {

        StringBuilder jobInfo = new StringBuilder("All Assigned Jobs and Completion Times:\n");
        int cumulativeTime = 0;

        // Loop through in-memory jobsQueue for real-time data
        for (JobRequest job : jobsQueue) {
            
                cumulativeTime += job.getDuration();
                jobInfo.append(String.format(
                    "Job ID: %s, Client ID: %s, Description: %s, Duration: %d hours, Completion Time: %d hours\n",
                    job.getJobId(), job.getClientId(), job.getJobDescription(),
                    job.getDuration(), cumulativeTime
                ));
            
        }

        return jobInfo.length() > "All Assigned Jobs and Completion Times:\n".length()
                ? jobInfo.toString()
                : "No jobs found for the specified client ID.";
    }
    public String handleVehicleCompletion(String ownerId, String vinNumber) {
        // Only update fields that exist in the database
        String query = "DELETE FROM CarRentals WHERE ownerId = ? AND vinNumber = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, ownerId);
            stmt.setString(2, vinNumber);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Remove from the in-memory list
                vehiclesReady.removeIf(vehicle -> vehicle.getOwnerId().equals(ownerId) && vehicle.getVinNumber().equals(vinNumber));
                return "Vehicle marked as complete successfully.";
            } else {
                return "Error: Vehicle not found in the database.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Unable to mark vehicle as complete.";
        }
    }



}
