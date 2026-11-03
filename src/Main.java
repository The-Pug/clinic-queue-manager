public class Main {
    public static void main(String[] args) throws Exception {
        boolean showWindow = true;
        int portNumber = 8080;
        if (args.length > 0 && args[0].equals("server")) {
            showWindow = false;
        }
        if (args.length > 1) {
            portNumber = Integer.parseInt(args[1]);
        }

        final Clinic clinic = new Clinic();
        ClinicServer server = new ClinicServer(clinic, portNumber);
        server.start();
        System.out.println("Web server running at http://127.0.0.1:" + server.getPort());

        if (showWindow) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new ClinicWindow(clinic).setVisible(true);
                }
            });
        }
    }
}
