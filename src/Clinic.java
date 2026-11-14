import java.util.ArrayList;

// The clinic: waiting list, doctors and clock.
// The window and the ClockThread both use the same Clinic object at the same time,
// so every method that reads or changes the data is "synchronized".
// That means only one thread can be inside these methods at a time.
public class Clinic {
    private int clockMinutes = 0;
    private int nextPatientId = 1;
    private int patientsSeen = 0;
    private ComplaintDictionary complaintDictionary = new ComplaintDictionary();
    private ArrayList<Patient> waitingList = new ArrayList<Patient>();
    private Doctor[] doctors = new Doctor[3];

    public Clinic() {
        doctors[0] = new Doctor("Dr. Sharma");
        doctors[1] = new Doctor("Dr. Verma");
        doctors[2] = new Doctor("Dr. Khan");
    }

    public ComplaintDictionary getComplaintDictionary() {
        return complaintDictionary;
    }

    public synchronized int getClockMinutes() {
        return clockMinutes;
    }

    public synchronized int getPatientsSeen() {
        return patientsSeen;
    }

    public synchronized Doctor[] getDoctors() {
        return doctors;
    }

    // Adds a patient. Throws an exception if the input is wrong.
    public synchronized Patient addPatient(String name, int age, String complaint) {
        if (name == null || name.trim().equals("")) {
            throw new IllegalArgumentException("Name is required");
        }
        if (age < 0 || age > 120) {
            throw new IllegalArgumentException("Age must be between 0 and 120");
        }
        int priority = complaintDictionary.getPriorityFor(complaint);
        Patient patient = new Patient(nextPatientId, name.trim(), age, complaint, priority, clockMinutes);
        nextPatientId++;
        waitingList.add(patient);
        return patient;
    }

    // Returns the waiting patients in the order they will be called.
    // Bubble sort: lower priority number first; if equal, the one who came first.
    public synchronized ArrayList<Patient> getWaitingPatients() {
        ArrayList<Patient> sorted = new ArrayList<Patient>(waitingList);
        for (int i = 0; i < sorted.size() - 1; i++) {
            for (int j = 0; j < sorted.size() - 1 - i; j++) {
                Patient a = sorted.get(j);
                Patient b = sorted.get(j + 1);
                boolean swap = a.getPriority() > b.getPriority()
                        || (a.getPriority() == b.getPriority() && a.getPatientId() > b.getPatientId());
                if (swap) {
                    sorted.set(j, b);
                    sorted.set(j + 1, a);
                }
            }
        }
        return sorted;
    }

    // Sends the most urgent patient to a free doctor and returns a message.
    public synchronized String callNextPatient() {
        Doctor freeDoctor = null;
        for (int i = 0; i < doctors.length; i++) {
            if (doctors[i].isFree()) {
                freeDoctor = doctors[i];
                break;
            }
        }
        if (freeDoctor == null) {
            return "All doctors are busy";
        }
        if (waitingList.size() == 0) {
            return "Nobody is waiting";
        }

        Patient patient = getWaitingPatients().get(0);
        waitingList.remove(patient);

        // priority 1 = 30 minutes ... priority 5 = 10 minutes
        int consultationMinutes = 35 - 5 * patient.getPriority();
        freeDoctor.startConsultation(patient, clockMinutes + consultationMinutes);
        patientsSeen++;
        return freeDoctor.getName() + " is now seeing " + patient.getName();
    }

    // Moves the clock forward. A doctor becomes free when the consultation time is over.
    public synchronized void advanceClock(int minutes) {
        for (int m = 0; m < minutes; m++) {
            clockMinutes++;
            for (int i = 0; i < doctors.length; i++) {
                if (!doctors[i].isFree() && clockMinutes >= doctors[i].getEndTime()) {
                    doctors[i].finishConsultation();
                }
            }
        }
    }
}
