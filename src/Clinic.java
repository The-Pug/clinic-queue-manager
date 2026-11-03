import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Clinic {
    private int clockMinutes = 0;
    private int nextPatientId = 1;
    private ComplaintDictionary complaintDictionary = new ComplaintDictionary();
    private ArrayList<Doctor> doctorList = new ArrayList<Doctor>();
    private ArrayList<Patient> patientList = new ArrayList<Patient>();

    private Comparator<Patient> patientOrder = new Comparator<Patient>() {
        public int compare(Patient firstPatient, Patient secondPatient) {
            if (firstPatient.getPriority() != secondPatient.getPriority()) {
                return firstPatient.getPriority() - secondPatient.getPriority();
            }
            return firstPatient.getPatientId() - secondPatient.getPatientId();
        }
    };

    private PriorityQueue<Patient> waitingQueue = new PriorityQueue<Patient>(11, patientOrder);

    private int[] seenCountByPriority = new int[6];
    private int[] totalWaitByPriority = new int[6];
    private int[] maxWaitByPriority = new int[6];

    public Clinic() {
        doctorList.add(new Doctor("Dr. Sharma", "General"));
        doctorList.add(new Doctor("Dr. Verma", "Emergency"));
        doctorList.add(new Doctor("Dr. Khan", "Paediatrics"));
    }

    public ComplaintDictionary getComplaintDictionary() { return complaintDictionary; }
    public synchronized int getClockMinutes() { return clockMinutes; }

    public synchronized ArrayList<Doctor> getDoctorList() { return new ArrayList<Doctor>(doctorList); }

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
        int priority = complaintDictionary.getPriorityFor(complaint);
        Patient patient = new Patient(nextPatientId, name.trim(), age, complaint.trim(), priority, clockMinutes);
        nextPatientId++;
        patientList.add(patient);
        waitingQueue.add(patient);
        return patient;
    }

    public synchronized ArrayList<Patient> getWaitingPatients() {
        ArrayList<Patient> sortedList = new ArrayList<Patient>(waitingQueue);
        Collections.sort(sortedList, patientOrder);
        return sortedList;
    }

    public synchronized Doctor callNextPatient() {
        Doctor freeDoctor = null;
        for (int index = 0; index < doctorList.size(); index++) {
            if (doctorList.get(index).isFree()) {
                freeDoctor = doctorList.get(index);
                break;
            }
        }
        if (freeDoctor == null) {
            throw new IllegalStateException("All doctors are busy");
        }
        if (waitingQueue.isEmpty()) {
            throw new IllegalStateException("Nobody is waiting");
        }

        Patient patient = waitingQueue.poll();

        int consultationMinutes = 35 - 5 * patient.getPriority();
        freeDoctor.startConsultation(patient, clockMinutes + consultationMinutes);
        patient.setStatus("IN_CONSULTATION");

        int waitedMinutes = clockMinutes - patient.getArrivalTime();
        int priorityLevel = patient.getPriority();
        seenCountByPriority[priorityLevel]++;
        totalWaitByPriority[priorityLevel] += waitedMinutes;
        if (waitedMinutes > maxWaitByPriority[priorityLevel]) {
            maxWaitByPriority[priorityLevel] = waitedMinutes;
        }
        return freeDoctor;
    }

    public synchronized void advanceClock(int minutesToAdd) {
        if (minutesToAdd < 1) {
            throw new IllegalArgumentException("Minutes must be at least 1");
        }
        for (int minuteCount = 0; minuteCount < minutesToAdd; minuteCount++) {
            clockMinutes++;
            for (int index = 0; index < doctorList.size(); index++) {
                Doctor doctor = doctorList.get(index);
                if (!doctor.isFree() && clockMinutes >= doctor.getConsultationEndTime()) {
                    doctor.getCurrentPatient().setStatus("DONE");
                    doctor.finishConsultation();
                }
            }
        }
    }

    public synchronized Patient escalatePatient(int patientId) {
        Patient foundPatient = null;
        for (int index = 0; index < patientList.size(); index++) {
            if (patientList.get(index).getPatientId() == patientId) {
                foundPatient = patientList.get(index);
            }
        }
        if (foundPatient == null) {
            throw new IllegalArgumentException("No patient with id " + patientId);
        }
        if (!foundPatient.getStatus().equals("WAITING")) {
            throw new IllegalStateException("Patient " + patientId + " is not waiting");
        }
        if (foundPatient.getPriority() == 1) {
            throw new IllegalStateException("Patient " + patientId + " is already at priority 1");
        }
        waitingQueue.remove(foundPatient);
        foundPatient.setPriority(foundPatient.getPriority() - 1);
        waitingQueue.add(foundPatient);
        return foundPatient;
    }

    public synchronized int getWaitingCount() { return waitingQueue.size(); }

    public synchronized int getSeenCount(int priorityLevel) { return seenCountByPriority[priorityLevel]; }

    public synchronized int getTotalSeenCount() {
        int totalSeenCount = 0;
        for (int priorityLevel = 1; priorityLevel <= 5; priorityLevel++) {
            totalSeenCount += seenCountByPriority[priorityLevel];
        }
        return totalSeenCount;
    }

    public synchronized double getAverageWaitMinutes(int priorityLevel) {
        if (seenCountByPriority[priorityLevel] == 0) {
            return 0;
        }
        return (double) totalWaitByPriority[priorityLevel] / seenCountByPriority[priorityLevel];
    }

    public synchronized int getMaxWaitMinutes(int priorityLevel) { return maxWaitByPriority[priorityLevel]; }

    public synchronized int getWaitedMinutes(Patient patient) {
        return clockMinutes - patient.getArrivalTime();
    }
}
