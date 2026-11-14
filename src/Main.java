public class Main {
    public static void main(String[] args) {
        Clinic clinic = new Clinic();

        ClinicWindow window = new ClinicWindow(clinic);
        window.setVisible(true);

        // Start the clock in its own thread
        ClockThread clockThread = new ClockThread(clinic, window);
        clockThread.start();
    }
}
