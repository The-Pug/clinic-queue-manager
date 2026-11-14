// One doctor. A doctor sees one patient at a time.
public class Doctor {
    private String name;
    private Patient currentPatient;   // null when the doctor is free
    private int endTime;              // clock minute when the current consultation ends

    public Doctor(String name) {
        this.name = name;
        this.currentPatient = null;
    }

    public String getName() { return name; }
    public Patient getCurrentPatient() { return currentPatient; }
    public int getEndTime() { return endTime; }

    public boolean isFree() {
        return currentPatient == null;
    }

    public void startConsultation(Patient patient, int endTime) {
        this.currentPatient = patient;
        this.endTime = endTime;
    }

    public void finishConsultation() {
        this.currentPatient = null;
    }
}
