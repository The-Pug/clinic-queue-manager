// Small checks. Run with: java -cp out ClinicTests
public class ClinicTests {
    static int failed = 0;

    static void check(String what, boolean ok) {
        if (ok) {
            System.out.println("ok    " + what);
        } else {
            System.out.println("FAIL  " + what);
            failed++;
        }
    }

    public static void main(String[] args) {
        // 1. urgent patient comes first
        Clinic c = new Clinic();
        c.addPatient("Ravi", 30, "routine checkup");
        c.addPatient("Hari", 61, "cardiac arrest");
        check("urgent patient is first", c.getWaiting().get(0).getName().equals("Hari"));

        // 2. same priority: who came first goes first
        Clinic c2 = new Clinic();
        c2.addPatient("First", 20, "high fever");
        c2.addPatient("Second", 21, "vomiting");
        check("tie: arrival order", c2.getWaiting().get(0).getName().equals("First"));

        // 3. unknown complaint is routine
        Patient odd = c2.addPatient("Odd", 40, "strange itch");
        check("unknown complaint gets priority 5", odd.getPriority() == 5);

        // 4. escalate moves a patient up the queue
        Clinic c3 = new Clinic();
        c3.addPatient("A", 20, "high fever");        // priority 3
        Patient b = c3.addPatient("B", 20, "minor cut");   // priority 4
        c3.escalate(b.getId());                       // now 3, but A came first
        c3.escalate(b.getId());                       // now 2, ahead of A
        check("escalate moves patient to front", c3.getWaiting().get(0) == b);

        // 5. a doctor is busy until the time is over
        Clinic c4 = new Clinic();
        for (int i = 0; i < 4; i++) {
            c4.addPatient("P" + i, 30, "routine checkup");   // 10 minutes each
        }
        c4.callNext();
        c4.callNext();
        c4.callNext();
        boolean noFreeDoctor = false;
        try {
            c4.callNext();
        } catch (IllegalStateException e) {
            noFreeDoctor = true;
        }
        c4.tick(10);
        c4.callNext();
        check("doctors busy until time is over", noFreeDoctor && c4.getWaitingCount() == 0);

        System.out.println(failed == 0 ? "All checks passed" : failed + " check(s) failed");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
