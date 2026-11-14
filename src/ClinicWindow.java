import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

// The window: a form at the top, the queue and doctors in the middle, time and messages at the bottom.
public class ClinicWindow extends JFrame implements ActionListener {
    private Clinic clinic;

    private JTextField nameField = new JTextField(10);
    private JTextField ageField = new JTextField(3);
    private JComboBox<String> complaintBox = new JComboBox<String>();
    private JButton addButton = new JButton("Add patient");
    private JButton callButton = new JButton("Call next");

    private JTextArea queueArea = new JTextArea(12, 25);
    private JTextArea doctorArea = new JTextArea(12, 25);

    private JLabel timeLabel = new JLabel("Time: 0 min");
    private JLabel messageLabel = new JLabel("Welcome!");

    public ClinicWindow(Clinic clinic) {
        this.clinic = clinic;
        setTitle("Clinic Queue - Team 29");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Fill the drop-down with the complaint names
        ArrayList<String> complaints = clinic.getComplaintDictionary().getComplaintNames();
        for (int i = 0; i < complaints.size(); i++) {
            complaintBox.addItem(complaints.get(i));
        }

        // Top: the form
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Name:"));
        topPanel.add(nameField);
        topPanel.add(new JLabel("Age:"));
        topPanel.add(ageField);
        topPanel.add(new JLabel("Complaint:"));
        topPanel.add(complaintBox);
        topPanel.add(addButton);
        topPanel.add(callButton);
        add(topPanel, BorderLayout.NORTH);

        // Middle: queue on the left, doctors on the right
        queueArea.setEditable(false);
        doctorArea.setEditable(false);
        JScrollPane queueScroll = new JScrollPane(queueArea);
        JScrollPane doctorScroll = new JScrollPane(doctorArea);
        queueScroll.setBorder(BorderFactory.createTitledBorder("Waiting queue"));
        doctorScroll.setBorder(BorderFactory.createTitledBorder("Doctors"));
        JPanel middlePanel = new JPanel(new GridLayout(1, 2));
        middlePanel.add(queueScroll);
        middlePanel.add(doctorScroll);
        add(middlePanel, BorderLayout.CENTER);

        // Bottom: time and messages
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(timeLabel);
        bottomPanel.add(new JLabel("  |  "));
        bottomPanel.add(messageLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        addButton.addActionListener(this);
        callButton.addActionListener(this);

        refreshScreen();
    }

    // Runs when a button is clicked
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            addPatient();
        } else if (e.getSource() == callButton) {
            messageLabel.setText(clinic.callNextPatient());
        }
        refreshScreen();
    }

    private void addPatient() {
        try {
            int age = Integer.parseInt(ageField.getText().trim());
            String complaint = (String) complaintBox.getSelectedItem();
            Patient patient = clinic.addPatient(nameField.getText(), age, complaint);
            messageLabel.setText("Added " + patient.getName() + " with priority " + patient.getPriority());
            nameField.setText("");
            ageField.setText("");
        } catch (NumberFormatException e) {
            messageLabel.setText("Age must be a number");
        } catch (IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    // Shows the latest time, queue and doctors on the screen
    public void refreshScreen() {
        int now = clinic.getClockMinutes();
        timeLabel.setText("Time: " + now + " min");

        String queueText = "";
        ArrayList<Patient> waiting = clinic.getWaitingPatients();
        for (int i = 0; i < waiting.size(); i++) {
            Patient p = waiting.get(i);
            queueText += (i + 1) + ". " + p.getName() + " (" + p.getAge() + ") - " + p.getComplaint()
                    + " - priority " + p.getPriority() + " - waited " + (now - p.getArrivalTime()) + " min\n";
        }
        if (waiting.size() == 0) {
            queueText = "Nobody is waiting";
        }
        queueArea.setText(queueText);

        String doctorText = "";
        Doctor[] doctors = clinic.getDoctors();
        for (int i = 0; i < doctors.length; i++) {
            Doctor d = doctors[i];
            Patient p = d.getCurrentPatient();   // read once: the clock thread may free the doctor any time
            if (p == null) {
                doctorText += d.getName() + ": free\n";
            } else {
                doctorText += d.getName() + ": seeing " + p.getName()
                        + " (" + (d.getEndTime() - now) + " min left)\n";
            }
        }
        doctorText += "\nPatients seen: " + clinic.getPatientsSeen();
        doctorArea.setText(doctorText);
    }
}
