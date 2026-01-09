package first.Project;

public class Task {
    private String id;
    private String title;
    private String description;
    private String clientName;
    private String clientAddress;
    String status;
    private String location; // Coordinates like "36.3650,6.6147"
    private String date;
    private String priority;

    public Task(String id, String title, String description, String clientName,
                String clientAddress, String status, String location, String date, String priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.clientName = clientName;
        this.clientAddress = clientAddress;
        this.status = status;
        this.location = location;
        this.date = date;
        this.priority = priority;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getClientName() { return clientName; }
    public String getClientAddress() { return clientAddress; }
    public String getStatus() { return status; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getPriority() { return priority; }

    // Status helpers
    public boolean isPending() { return "Pending".equals(status); }
    public boolean isInProgress() { return "In Progress".equals(status); }
    public boolean isCompleted() { return "Completed".equals(status); }

    // Priority helpers
    public boolean isHighPriority() { return "High".equals(priority); }
    public boolean isMediumPriority() { return "Medium".equals(priority); }
    public boolean isLowPriority() { return "Low".equals(priority); }
}