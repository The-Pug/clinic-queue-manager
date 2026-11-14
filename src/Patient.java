// One patient waiting at the clinic
public class Patient {
    private int patientId;
    private String name;
    private int age;
    private String complaint;
    private int priority;      // 1 = most urgent, 5 = routine
    private int arrivalTime;   // clock minute when the patient arrived

    public Patient(int patientId, String name, int age, String complaint, int priority, int arrivalTime) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.complaint = complaint;
        this.priority = priority;
        this.arrivalTime = arrivalTime;
    }

    public int getPatientId() { return patientId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getComplaint() { return complaint; }
    public int getPriority() { return priority; }
    public int getArrivalTime() { return arrivalTime; }
}
