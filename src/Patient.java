// One patient in the clinic.
public class Patient {
    private int id;
    private String name;
    private int age;
    private String complaint;
    private int priority;       // 1 = most urgent, 5 = routine
    private int arrivalTime;    // clock minute when the patient came in
    private String status;      // WAITING, IN_CONSULTATION or DONE

    public Patient(int id, String name, int age, String complaint, int priority, int arrivalTime) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.complaint = complaint;
        this.priority = priority;
        this.arrivalTime = arrivalTime;
        this.status = "WAITING";
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getComplaint() { return complaint; }
    public int getPriority() { return priority; }
    public int getArrivalTime() { return arrivalTime; }
    public String getStatus() { return status; }

    public void setPriority(int priority) { this.priority = priority; }
    public void setStatus(String status) { this.status = status; }

    public String toString() {
        return "#" + id + " " + name;
    }
}
