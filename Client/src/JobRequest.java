// JobRequest.java

import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JobRequest {
    private final String jobId;           // Unique job identifier
    private String clientId;
    private String jobDescription;
    private int duration;
    private int redundancyLevel;
    private String jobDeadline;
    private String timestamp;             // Timestamp when the job request was created

    // Constructor that generates a unique jobId and timestamp
    public JobRequest(String clientId, String jobDescription, int duration, int redundancyLevel, String jobDeadline) {
        this.jobId = UUID.randomUUID().toString();  // Use UUID for unique job ID
        this.clientId = clientId;
        this.jobDescription = jobDescription;
        this.duration = duration;
        this.redundancyLevel = redundancyLevel;
        this.jobDeadline = jobDeadline;
        this.timestamp = getCurrentTimestamp();     // Generate current timestamp
    }

    // Constructor that accepts existing jobId and timestamp (e.g., when loading from CSV)
    public JobRequest(String jobId, String clientId, String jobDescription, int duration, int redundancyLevel, String jobDeadline, String timestamp) {
        this.jobId = jobId;                         // Use existing jobId
        this.clientId = clientId;
        this.jobDescription = jobDescription;
        this.duration = duration;
        this.redundancyLevel = redundancyLevel;
        this.jobDeadline = jobDeadline;
        this.timestamp = timestamp;                 // Use existing timestamp
    }

    // Getter methods
    public String getJobId() {
        return jobId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public int getDuration() {
        return duration;
    }

    public int getRedundancyLevel() {
        return redundancyLevel;
    }

    public String getJobDeadline() {
        return jobDeadline;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // Helper method to generate current timestamp
    private String getCurrentTimestamp() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(dtf);
    }

    @Override
    public String toString() {
        return "JobRequest{" +
                "jobId='" + jobId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", jobDescription='" + jobDescription + '\'' +
                ", duration=" + duration +
                ", redundancyLevel=" + redundancyLevel +
                ", jobDeadline='" + jobDeadline + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
