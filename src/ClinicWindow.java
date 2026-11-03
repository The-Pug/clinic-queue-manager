import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ClinicWindow extends JFrame {
    private Clinic clinic;

    private Color[] priorityColours = {new Color(255, 205, 210), new Color(255, 224, 178), new Color(255, 249, 196),
            new Color(220, 237, 200), new Color(187, 222, 251)};
    private Color darkBlue = new Color(31, 78, 95);
    private Color pageBackground = new Color(244, 246, 248);

    private JTextField nameField = new JTextField();
    private JTextField ageField = new JTextField();
    private JComboBox<String> complaintDropdown = new JComboBox<String>();

    private JLabel messageLabel = new JLabel(" ");
    private JLabel clockLabel = new JLabel();
    private DefaultTableModel queueTableModel;
    private JTable queueTable;
    private JTextArea statisticsArea = new JTextArea();
    private JPanel doctorPanel = new JPanel(new GridLayout(1, 0, 10, 0));
    private ArrayList<DoctorCard> doctorCards = new ArrayList<DoctorCard>();

    public ClinicWindow(Clinic clinic) {
        super("Clinic Queue - Team 29");
        this.clinic = clinic;
        try {
            for (UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels()) {
                if (lookAndFeelInfo.getName().equals("Nimbus")) {
                    UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
                }
            }
        } catch (Exception exception) {
            System.out.println("Using the default look");
        }

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(pageBackground);
        setLayout(new BorderLayout(10, 10));

        add(makeHeader(), BorderLayout.NORTH);
        add(makeLeftSide(), BorderLayout.WEST);
        add(makeQueueArea(), BorderLayout.CENTER);
        add(makeDoctorArea(), BorderLayout.SOUTH);

        Timer refreshTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                refreshScreen();
            }
        });
        refreshTimer.start();

        refreshScreen();
        setSize(1100, 700);
        setLocationRelativeTo(null);
    }

    private JPanel makeHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel titleLabel = new JLabel("Clinic Queue");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        clockLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        clockLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.WEST);
        header.add(clockLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel makeLeftSide() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("New patient"), new EmptyBorder(6, 10, 10, 10)));

        for (String complaintName : clinic.getComplaintDictionary().getComplaintNames()) {
            complaintDropdown.addItem(complaintName);
        }
        complaintDropdown.setSelectedItem("routine checkup");

        addFormRow(formPanel, "Name", nameField);
        addFormRow(formPanel, "Age", ageField);
        addFormRow(formPanel, "Complaint", complaintDropdown);

        JButton addButton = makeButton("Add to queue", new Color(46, 125, 50));
        addButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        ActionListener addAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                addPatientFromForm();
            }
        };
        addButton.addActionListener(addAction);
        nameField.addActionListener(addAction);
        ageField.addActionListener(addAction);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(addButton);

        messageLabel.setBorder(new EmptyBorder(8, 2, 0, 2));
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(messageLabel);

        statisticsArea.setEditable(false);
        statisticsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statisticsArea.setMargin(new Insets(6, 8, 6, 8));
        JScrollPane statisticsScroll = new JScrollPane(statisticsArea);
        statisticsScroll.setBorder(BorderFactory.createTitledBorder("Statistics"));

        JPanel leftSide = new JPanel(new BorderLayout(0, 10));
        leftSide.setOpaque(false);
        leftSide.setPreferredSize(new Dimension(290, 400));
        leftSide.add(formPanel, BorderLayout.NORTH);
        leftSide.add(statisticsScroll, BorderLayout.CENTER);
        return leftSide;
    }

    private void addFormRow(JPanel formPanel, String labelText, JComponent inputBox) {
        JLabel rowLabel = new JLabel(labelText);
        rowLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowLabel.setBorder(new EmptyBorder(6, 0, 2, 0));
        inputBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        inputBox.setPreferredSize(new Dimension(200, 30));
        formPanel.add(rowLabel);
        formPanel.add(inputBox);
    }

    private JPanel makeQueueArea() {
        String[] columnNames = {"#", "ID", "Name", "Age", "Complaint", "Priority", "Waited (min)"};
        queueTableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        queueTable = new JTable(queueTableModel);
        queueTable.setRowHeight(28);
        queueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        queueTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        queueTable.setDefaultRenderer(Object.class, new PriorityRowColour());
        queueTable.getColumnModel().getColumn(0).setMaxWidth(40);
        queueTable.getColumnModel().getColumn(1).setMaxWidth(50);

        JScrollPane queueScroll = new JScrollPane(queueTable);
        queueScroll.setBorder(BorderFactory.createTitledBorder("Waiting queue (first row is called next)"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttonPanel.setOpaque(false);
        JButton callNextButton = makeButton("Call next", darkBlue);
        JButton escalateButton = makeButton("Escalate selected", new Color(230, 126, 34));
        JButton tickFiveButton = makeButton("Tick +5 min", new Color(96, 125, 139));
        JButton tickFifteenButton = makeButton("Tick +15 min", new Color(96, 125, 139));
        callNextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                callNextPatient();
            }
        });
        escalateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                escalateSelectedPatient();
            }
        });
        tickFiveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                clinic.advanceClock(5);
                showMessage("Clock moved 5 minutes", true);
                refreshScreen();
            }
        });
        tickFifteenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                clinic.advanceClock(15);
                showMessage("Clock moved 15 minutes", true);
                refreshScreen();
            }
        });
        buttonPanel.add(callNextButton);
        buttonPanel.add(escalateButton);
        buttonPanel.add(tickFiveButton);
        buttonPanel.add(tickFifteenButton);

        JPanel colourKey = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        colourKey.setOpaque(false);
        String[] keyNames = {"1 urgent", "2", "3", "4", "5 routine"};
        for (int index = 0; index < 5; index++) {
            JLabel keyLabel = new JLabel(" " + keyNames[index] + " ");
            keyLabel.setOpaque(true);
            keyLabel.setBackground(priorityColours[index]);
            keyLabel.setBorder(new LineBorder(Color.GRAY));
            colourKey.add(keyLabel);
        }

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.add(buttonPanel, BorderLayout.WEST);
        bottomBar.add(colourKey, BorderLayout.EAST);

        JPanel queueArea = new JPanel(new BorderLayout(0, 6));
        queueArea.setOpaque(false);
        queueArea.add(queueScroll, BorderLayout.CENTER);
        queueArea.add(bottomBar, BorderLayout.SOUTH);
        return queueArea;
    }

    private JPanel makeDoctorArea() {
        doctorPanel.setOpaque(false);
        for (Doctor doctor : clinic.getDoctorList()) {
            DoctorCard doctorCard = new DoctorCard(doctor);
            doctorCards.add(doctorCard);
            doctorPanel.add(doctorCard);
        }
        JPanel doctorArea = new JPanel(new BorderLayout());
        doctorArea.setOpaque(false);
        doctorArea.setBorder(BorderFactory.createTitledBorder("Doctors"));
        doctorArea.setPreferredSize(new Dimension(400, 130));
        doctorArea.add(doctorPanel, BorderLayout.CENTER);
        return doctorArea;
    }

    private JButton makeButton(String text, Color colour) {
        JButton button = new JButton(text);
        button.setBackground(colour);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setFocusPainted(false);
        return button;
    }

    private void addPatientFromForm() {
        try {
            int age = Integer.parseInt(ageField.getText().trim());
            Patient patient = clinic.addPatient(nameField.getText(), age, (String) complaintDropdown.getSelectedItem());
            showMessage("Added " + patient.getName() + " (priority " + patient.getPriority() + ")", true);
            nameField.setText("");
            ageField.setText("");
            nameField.requestFocusInWindow();
        } catch (NumberFormatException exception) {
            showMessage("Age must be a number", false);
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage(), false);
        }
        refreshScreen();
    }

    private void callNextPatient() {
        try {
            Doctor doctor = clinic.callNextPatient();
            showMessage(doctor.getName() + " is now seeing " + doctor.getCurrentPatient().getName(), true);
        } catch (IllegalStateException exception) {
            showMessage(exception.getMessage(), false);
        }
        refreshScreen();
    }

    private void escalateSelectedPatient() {
        int selectedRow = queueTable.getSelectedRow();
        if (selectedRow < 0) {
            showMessage("Click a patient in the table first", false);
            return;
        }
        int patientId = (Integer) queueTableModel.getValueAt(selectedRow, 1);
        try {
            Patient patient = clinic.escalatePatient(patientId);
            showMessage(patient.getName() + " is now priority " + patient.getPriority(), true);
        } catch (IllegalStateException exception) {
            showMessage(exception.getMessage(), false);
        }
        refreshScreen();
    }

    private void showMessage(String text, boolean wentWell) {
        messageLabel.setForeground(wentWell ? new Color(27, 94, 32) : new Color(183, 28, 28));
        messageLabel.setText("<html>" + text + "</html>");
    }

    private void refreshScreen() {
        clockLabel.setText("Time: " + clinic.getClockMinutes() + " min");

        int selectedPatientId = -1;
        if (queueTable.getSelectedRow() >= 0) {
            selectedPatientId = (Integer) queueTableModel.getValueAt(queueTable.getSelectedRow(), 1);
        }
        queueTableModel.setRowCount(0);
        ArrayList<Patient> waitingPatients = clinic.getWaitingPatients();
        for (int index = 0; index < waitingPatients.size(); index++) {
            Patient patient = waitingPatients.get(index);
            Object[] rowValues = {index + 1, patient.getPatientId(), patient.getName(), patient.getAge(),
                    patient.getComplaint(), patient.getPriority(), clinic.getWaitedMinutes(patient)};
            queueTableModel.addRow(rowValues);
            if (patient.getPatientId() == selectedPatientId) {
                queueTable.setRowSelectionInterval(index, index);
            }
        }

        for (int index = 0; index < doctorCards.size(); index++) {
            doctorCards.get(index).update();
        }

        String statisticsText = "Waiting now: " + clinic.getWaitingCount() + "\n";
        statisticsText += "Seen so far:  " + clinic.getTotalSeenCount() + "\n\n";
        statisticsText += String.format("%-5s %5s %8s %5s%n", "Prio", "Seen", "AvgWait", "Max");
        for (int priorityLevel = 1; priorityLevel <= 5; priorityLevel++) {
            statisticsText += String.format("%-5d %5d %8.1f %5d%n", priorityLevel, clinic.getSeenCount(priorityLevel),
                    clinic.getAverageWaitMinutes(priorityLevel), clinic.getMaxWaitMinutes(priorityLevel));
        }
        statisticsArea.setText(statisticsText);
    }

    private class PriorityRowColour extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable tableBeingDrawn, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(tableBeingDrawn, value, isSelected,
                    hasFocus, row, column);
            if (column == 2 || column == 4) {
                setHorizontalAlignment(LEFT);
            } else {
                setHorizontalAlignment(CENTER);
            }
            if (!isSelected) {
                int priority = (Integer) tableBeingDrawn.getValueAt(row, 5);
                cellComponent.setBackground(priorityColours[priority - 1]);
                cellComponent.setForeground(Color.BLACK);
            }
            return cellComponent;
        }
    }

    private class DoctorCard extends JPanel {
        private Doctor doctor;
        private JLabel nameLabel = new JLabel();
        private JLabel statusLabel = new JLabel();
        private JProgressBar progressBar = new JProgressBar(0, 100);

        public DoctorCard(Doctor doctor) {
            this.doctor = doctor;
            setLayout(new GridLayout(3, 1, 2, 4));
            setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(6, 10, 6, 10)));
            nameLabel.setText(doctor.getName() + " (" + doctor.getSpeciality() + ")");
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            progressBar.setStringPainted(true);
            add(nameLabel);
            add(statusLabel);
            add(progressBar);
        }

        public void update() {
            Patient currentPatient = doctor.getCurrentPatient();
            if (currentPatient == null) {
                setBackground(new Color(232, 245, 233));
                statusLabel.setText("Free");
                progressBar.setValue(0);
                progressBar.setString("");
            } else {
                setBackground(new Color(255, 243, 224));
                statusLabel.setText("Seeing " + currentPatient.getName() + " (priority " + currentPatient.getPriority() + ")");
                int totalMinutes = 35 - 5 * currentPatient.getPriority();
                int minutesLeft = doctor.getConsultationEndTime() - clinic.getClockMinutes();
                progressBar.setValue(100 * (totalMinutes - minutesLeft) / totalMinutes);
                progressBar.setString(minutesLeft + " min left");
            }
        }
    }
}
