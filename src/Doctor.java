// A doctor who sees one patient at a time.
public class Doctor {
    private String name;
    private String speciality;
    private Patient current;    // null when the doctor is free
    private int endTime;        // clock minute when the current consultation finishes

    public Doctor(String name, String speciality) {
        this.name = name;
        this.speciality = speciality;
        this.current = null;
    }

    public String getName() { return name; }
    public String getSpeciality() { return speciality; }
    public Patient getCurrent() { return current; }
    public int getEndTime() { return endTime; }

    public boolean isFree() {
        return current == null;
    }

    public void start(Patient p, int endTime) {
        this.current = p;
        this.endTime = endTime;
    }

    public void finish() {
        this.current = null;
    }
}
