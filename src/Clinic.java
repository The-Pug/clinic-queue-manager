import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

// The clinic: one priority queue of waiting patients, some doctors and a clock.
// The methods are "synchronized" so the window and the web server never change things at the same moment.
public class Clinic {
    private int clock = 0;                 // minutes
    private int nextId = 1;
    private ComplaintDictionary dictionary = new ComplaintDictionary();
    private ArrayList<Doctor> doctors = new ArrayList<Doctor>();
    private ArrayList<Patient> allPatients = new ArrayList<Patient>();

    // Lowest priority number first. If it is a tie, the patient who came first (smaller id) goes first.
    private Comparator<Patient> order = new Comparator<Patient>() {
        public int compare(Patient a, Patient b) {
            if (a.getPriority() != b.getPriority()) {
                return a.getPriority() - b.getPriority();
            }
            return a.getId() - b.getId();
        }
    };
    private PriorityQueue<Patient> queue = new PriorityQueue<Patient>(11, order);

    // numbers for the statistics, index 1..5 = priority when the patient was called
    private int[] seenByPriority = new int[6];
    private int[] waitByPriority = new int[6];
    private int[] maxWaitByPriority = new int[6];

    public Clinic() {
        doctors.add(new Doctor("Dr. Sharma", "General"));
        doctors.add(new Doctor("Dr. Verma", "Emergency"));
        doctors.add(new Doctor("Dr. Khan", "Paediatrics"));
    }

    public ComplaintDictionary getDictionary() { return dictionary; }
    public synchronized int getClock() { return clock; }
    public synchronized ArrayList<Doctor> getDoctors() { return new ArrayList<Doctor>(doctors); }

    public synchronized Patient addPatient(String name, int age, String complaint) {
        if (name == null || name.trim().equals("")) {
            throw new IllegalArgumentException("Name is required");
        }
        if (age < 0 || age > 120) {
            throw new IllegalArgumentException("Age must be between 0 and 120");
        }
        if (complaint == null || complaint.trim().equals("")) {
            throw new IllegalArgumentException("Complaint is required");
        }
        int priority = dictionary.getPriority(complaint);
        Patient p = new Patient(nextId, name.trim(), age, complaint.trim(), priority, clock);
        nextId++;
        allPatients.add(p);
        queue.add(p);
        return p;
    }

    // Waiting patients in the order they will be called.
    public synchronized ArrayList<Patient> getWaiting() {
        ArrayList<Patient> list = new ArrayList<Patient>(queue);
        Collections.sort(list, order);
        return list;
    }

    // Sends the most urgent patient to the first free doctor.
    public synchronized Doctor callNext() {
        Doctor free = null;
        for (int i = 0; i < doctors.size(); i++) {
            if (doctors.get(i).isFree()) {
                free = doctors.get(i);
                break;
            }
        }
        if (free == null) {
            throw new IllegalStateException("All doctors are busy");
        }
        if (queue.isEmpty()) {
            throw new IllegalStateException("Nobody is waiting");
        }

        Patient p = queue.poll();
        int minutes = 35 - 5 * p.getPriority();     // priority 1 = 30 min ... priority 5 = 10 min
        free.start(p, clock + minutes);
        p.setStatus("IN_CONSULTATION");

        int waited = clock - p.getArrivalTime();
        int level = p.getPriority();
        seenByPriority[level]++;
        waitByPriority[level] += waited;
        if (waited > maxWaitByPriority[level]) {
            maxWaitByPriority[level] = waited;
        }
        return free;
    }

    // Moves the clock forward one minute at a time and frees doctors who are finished.
    public synchronized void tick(int minutes) {
        if (minutes < 1) {
            throw new IllegalArgumentException("Minutes must be at least 1");
        }
        for (int m = 0; m < minutes; m++) {
            clock++;
            for (int i = 0; i < doctors.size(); i++) {
                Doctor d = doctors.get(i);
                if (!d.isFree() && clock >= d.getEndTime()) {
                    d.getCurrent().setStatus("DONE");
                    d.finish();
                }
            }
        }
    }

    // Makes a waiting patient one step more urgent.
    public synchronized Patient escalate(int id) {
        Patient p = null;
        for (int i = 0; i < allPatients.size(); i++) {
            if (allPatients.get(i).getId() == id) {
                p = allPatients.get(i);
            }
        }
        if (p == null) {
            throw new IllegalArgumentException("No patient with id " + id);
        }
        if (!p.getStatus().equals("WAITING")) {
            throw new IllegalStateException("Patient " + id + " is not waiting");
        }
        if (p.getPriority() == 1) {
            throw new IllegalStateException("Patient " + id + " is already at priority 1");
        }
        // The queue sorts only when a patient is added, so take the patient out and put back in.
        queue.remove(p);
        p.setPriority(p.getPriority() - 1);
        queue.add(p);
        return p;
    }

    // ----- numbers for /stats -----

    public synchronized int getWaitingCount() { return queue.size(); }

    public synchronized int getSeen(int level) { return seenByPriority[level]; }

    public synchronized int getTotalSeen() {
        int total = 0;
        for (int level = 1; level <= 5; level++) {
            total += seenByPriority[level];
        }
        return total;
    }

    public synchronized double getAverageWait(int level) {
        if (seenByPriority[level] == 0) {
            return 0;
        }
        return (double) waitByPriority[level] / seenByPriority[level];
    }

    public synchronized int getMaxWait(int level) { return maxWaitByPriority[level]; }

    public synchronized int getWaitedSoFar(Patient p) {
        return clock - p.getArrivalTime();
    }
}
