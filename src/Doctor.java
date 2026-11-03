public class Doctor {
    private String name;
    private String speciality;
    private Patient currentPatient;
    private int consultationEndTime;

    public Doctor(String name, String speciality) {
        this.name = name;
        this.speciality = speciality;
        this.currentPatient = null;
    }

    public String getName() { return name; }
    public String getSpeciality() { return speciality; }
    public Patient getCurrentPatient() { return currentPatient; }
    public int getConsultationEndTime() { return consultationEndTime; }

    public boolean isFree() {
        return currentPatient == null;
    }

    public void startConsultation(Patient patient, int consultationEndTime) {
        this.currentPatient = patient;
        this.consultationEndTime = consultationEndTime;
    }

    public void finishConsultation() {
        this.currentPatient = null;
    }
}
