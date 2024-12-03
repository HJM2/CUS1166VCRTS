// CarRentals.java

import java.util.ArrayList;
import java.util.List;

public class CarRentals {
    private String ownerId;
    private String model;
    private String brand;
    private String plateNumber;
    private String serialNumber;
    private String vinNum;
    private String residencyTime;
    private List<JobRequest> assignedJobs;

    // Constructor
    public CarRentals(String ownerId, String model, String brand, String plateNumber, String serialNumber, String vinNum, String residencyTime) {
        this.ownerId = ownerId;
        this.model = model;
        this.brand = brand;
        this.plateNumber = plateNumber;
        this.serialNumber = serialNumber;
        this.vinNum = vinNum;
        this.residencyTime = residencyTime;
        this.assignedJobs = new ArrayList<>();
    }

    // Getter methods
    public String getOwnerId() {
        return ownerId;
    }

    public String getVehicleModel() {
        return model;
    }

    public String getVehicleBrand() {
        return brand;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getVinNumber() {
        return vinNum;
    }

    public String getResidencyTime() {
        return residencyTime;
    }

    // Method to assign a job to this vehicle
    public void assignJob(JobRequest job) {
        assignedJobs.add(job);
    }

    // Optional: Method to get all assigned jobs for logging or debugging
    public List<JobRequest> getAssignedJobs() {
        return assignedJobs;
    }
}
