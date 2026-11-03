public class Patient {
    private int patientId;
    private String name;
    private int age;
    private String complaint;
    private int priority;
    private int arrivalTime;
    private String status;

    public Patient(int patientId, String name, int age, String complaint, int priority, int arrivalTime) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.complaint = complaint;
        this.priority = priority;
        this.arrivalTime = arrivalTime;
        this.status = "WAITING";
    }

    public int getPatientId() { return patientId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getComplaint() { return complaint; }
    public int getPriority() { return priority; }
    public int getArrivalTime() { return arrivalTime; }
    public String getStatus() { return status; }

    public void setPriority(int priority) { this.priority = priority; }
    public void setStatus(String status) { this.status = status; }

    public String toString() {
        return "#" + patientId + " " + name;
    }
}
