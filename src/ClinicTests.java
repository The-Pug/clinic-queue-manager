// A few simple checks. Run with: java -cp out ClinicTests
public class ClinicTests {
    static int failedCount = 0;

    static void check(String description, boolean passed) {
        if (passed) {
            System.out.println("ok    " + description);
        } else {
            System.out.println("FAIL  " + description);
            failedCount++;
        }
    }

    public static void main(String[] args) {
        // 1. The most urgent patient comes first
        Clinic clinic1 = new Clinic();
        clinic1.addPatient("Ravi", 30, "routine checkup");
        clinic1.addPatient("Hari", 61, "cardiac arrest");
        check("urgent patient is first", clinic1.getWaitingPatients().get(0).getName().equals("Hari"));

        // 2. Same priority: the one who came first goes first
        Clinic clinic2 = new Clinic();
        clinic2.addPatient("First", 20, "high fever");
        clinic2.addPatient("Second", 21, "vomiting");
        check("same priority: arrival order", clinic2.getWaitingPatients().get(0).getName().equals("First"));

        // 3. An unknown complaint gets priority 5
        Patient odd = clinic2.addPatient("Odd", 40, "strange itch");
        check("unknown complaint gets priority 5", odd.getPriority() == 5);

        // 4. Wrong age is not allowed
        boolean errorShown = false;
        try {
            clinic2.addPatient("Old", 200, "fracture");
        } catch (IllegalArgumentException e) {
            errorShown = true;
        }
        check("age 200 is rejected", errorShown);

        // 5. Three doctors are busy until the consultation time is over
        Clinic clinic3 = new Clinic();
        for (int i = 0; i < 4; i++) {
            clinic3.addPatient("P" + i, 30, "routine checkup");
        }
        clinic3.callNextPatient();
        clinic3.callNextPatient();
        clinic3.callNextPatient();
        String message = clinic3.callNextPatient();
        clinic3.advanceClock(10);
        clinic3.callNextPatient();
        check("doctors busy until time is over",
                message.equals("All doctors are busy") && clinic3.getWaitingPatients().size() == 0);

        if (failedCount == 0) {
            System.out.println("All checks passed");
        } else {
            System.out.println(failedCount + " check(s) failed");
        }
    }
}
