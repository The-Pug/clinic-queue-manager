public class ClinicTests {
    static int failedCount = 0;

    static void checkResult(String description, boolean passed) {
        if (passed) {
            System.out.println("ok    " + description);
        } else {
            System.out.println("FAIL  " + description);
            failedCount++;
        }
    }

    public static void main(String[] args) {
        Clinic orderClinic = new Clinic();
        orderClinic.addPatient("Ravi", 30, "routine checkup");
        orderClinic.addPatient("Hari", 61, "cardiac arrest");
        checkResult("urgent patient is first", orderClinic.getWaitingPatients().get(0).getName().equals("Hari"));

        Clinic tieClinic = new Clinic();
        tieClinic.addPatient("First", 20, "high fever");
        tieClinic.addPatient("Second", 21, "vomiting");
        checkResult("tie: arrival order", tieClinic.getWaitingPatients().get(0).getName().equals("First"));

        Patient unknownComplaintPatient = tieClinic.addPatient("Odd", 40, "strange itch");
        checkResult("unknown complaint gets priority 5", unknownComplaintPatient.getPriority() == 5);

        Clinic escalateClinic = new Clinic();
        escalateClinic.addPatient("A", 20, "high fever");
        Patient cutPatient = escalateClinic.addPatient("B", 20, "minor cut");
        escalateClinic.escalatePatient(cutPatient.getPatientId());
        escalateClinic.escalatePatient(cutPatient.getPatientId());
        checkResult("escalate moves patient to front", escalateClinic.getWaitingPatients().get(0) == cutPatient);

        Clinic doctorClinic = new Clinic();
        for (int number = 0; number < 4; number++) {
            doctorClinic.addPatient("P" + number, 30, "routine checkup");
        }
        doctorClinic.callNextPatient();
        doctorClinic.callNextPatient();
        doctorClinic.callNextPatient();
        boolean noFreeDoctor = false;
        try {
            doctorClinic.callNextPatient();
        } catch (IllegalStateException exception) {
            noFreeDoctor = true;
        }
        doctorClinic.advanceClock(10);
        doctorClinic.callNextPatient();
        checkResult("doctors busy until time is over", noFreeDoctor && doctorClinic.getWaitingCount() == 0);

        System.out.println(failedCount == 0 ? "All checks passed" : failedCount + " check(s) failed");
        if (failedCount > 0) {
            System.exit(1);
        }
    }
}
