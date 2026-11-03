// Starts the web server and the window.
//   java -cp out Main              window + web server on port 8080
//   java -cp out Main server 9000  web server only, on port 9000
public class Main {
    public static void main(String[] args) throws Exception {
        boolean windowOn = true;
        int port = 8080;
        if (args.length > 0 && args[0].equals("server")) {
            windowOn = false;
        }
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        final Clinic clinic = new Clinic();
        ClinicServer server = new ClinicServer(clinic, port);
        server.start();
        System.out.println("Web server running at http://127.0.0.1:" + server.getPort());

        if (windowOn) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new ClinicWindow(clinic).setVisible(true);
                }
            });
        }
    }
}
