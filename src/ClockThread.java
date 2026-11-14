import javax.swing.SwingUtilities;

// A separate thread that runs the clinic clock.
// Every 1 second of real time, the clinic clock moves forward by 1 minute.
public class ClockThread extends Thread {
    private Clinic clinic;
    private ClinicWindow window;

    public ClockThread(Clinic clinic, ClinicWindow window) {
        this.clinic = clinic;
        this.window = window;
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
            clinic.advanceClock(1);

            // Swing screens should only be changed from the Swing thread,
            // so we ask Swing to run refreshScreen() for us.
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    window.refreshScreen();
                }
            });
        }
    }
}
