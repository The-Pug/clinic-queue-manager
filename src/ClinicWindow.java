import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

// The window: a form with three boxes, the queue table, doctor panels and a statistics box.
public class ClinicWindow extends JFrame {
    private Clinic clinic;

    // colours for priority 1 (urgent) to 5 (routine)
    private Color[] rowColours = {new Color(255, 205, 210), new Color(255, 224, 178), new Color(255, 249, 196),
            new Color(220, 237, 200), new Color(187, 222, 251)};
    private Color darkBlue = new Color(31, 78, 95);
    private Color pageColour = new Color(244, 246, 248);

    // the three boxes of the form
    private JTextField nameField = new JTextField();
    private JTextField ageField = new JTextField();
    private JComboBox<String> complaintBox = new JComboBox<String>();

    private JLabel messageLabel = new JLabel(" ");
    private JLabel clockLabel = new JLabel();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextArea statsArea = new JTextArea();
    private JPanel doctorPanel = new JPanel(new GridLayout(1, 0, 10, 0));
    private ArrayList<DoctorCard> cards = new ArrayList<DoctorCard>();

    public ClinicWindow(Clinic clinic) {
        super("Clinic Queue - Team 29");
        this.clinic = clinic;
        try {
            // Nimbus looks nicer than the default grey style (if it is missing we keep the default)
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (info.getName().equals("Nimbus")) {
                    UIManager.setLookAndFeel(info.getClassName());
                }
            }
        } catch (Exception e) {
            System.out.println("Using the default look");
        }

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(pageColour);
        setLayout(new BorderLayout(10, 10));

        add(makeHeader(), BorderLayout.NORTH);
        add(makeLeftSide(), BorderLayout.WEST);
        add(makeQueueArea(), BorderLayout.CENTER);
        add(makeDoctorArea(), BorderLayout.SOUTH);

        // Redraw every second so patients added through the web address show up too.
        Timer timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        timer.start();

        refresh();
        setSize(1100, 700);
        setLocationRelativeTo(null);
    }

    // ----- building the parts of the window -----

    private JPanel makeHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(darkBlue);
        header.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("Clinic Queue");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        clockLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        clockLabel.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        header.add(clockLabel, BorderLayout.EAST);
        return header;
    }

    // Left side: the add-patient form on top, the statistics below it.
    private JPanel makeLeftSide() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("New patient"), new EmptyBorder(6, 10, 10, 10)));

        for (String name : clinic.getDictionary().getNames()) {
            complaintBox.addItem(name);
        }
        complaintBox.setSelectedItem("routine checkup");

        addFormRow(form, "Name", nameField);
        addFormRow(form, "Age", ageField);
        addFormRow(form, "Complaint", complaintBox);

        JButton addButton = makeButton("Add to queue", new Color(46, 125, 50));
        addButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        ActionListener addAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addPatient();
            }
        };
        addButton.addActionListener(addAction);
        nameField.addActionListener(addAction);     // pressing Enter in a box also adds
        ageField.addActionListener(addAction);
        form.add(Box.createVerticalStrut(10));
        form.add(addButton);

        messageLabel.setBorder(new EmptyBorder(8, 2, 0, 2));
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(messageLabel);

        statsArea.setEditable(false);
        statsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statsArea.setMargin(new Insets(6, 8, 6, 8));
        JScrollPane statsScroll = new JScrollPane(statsArea);
        statsScroll.setBorder(BorderFactory.createTitledBorder("Statistics"));

        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(290, 400));
        left.add(form, BorderLayout.NORTH);
        left.add(statsScroll, BorderLayout.CENTER);
        return left;
    }

    // One label with its box under it.
    private void addFormRow(JPanel form, String labelText, JComponent box) {
        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(6, 0, 2, 0));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        box.setPreferredSize(new Dimension(200, 30));
        form.add(label);
        form.add(box);
    }

    private JPanel makeQueueArea() {
        String[] columns = {"#", "ID", "Name", "Age", "Complaint", "Priority", "Waited (min)"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setDefaultRenderer(Object.class, new RowColour());
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setMaxWidth(50);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Waiting queue (first row is called next)"));

        // buttons under the table
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.setOpaque(false);
        JButton nextButton = makeButton("Call next", darkBlue);
        JButton escalateButton = makeButton("Escalate selected", new Color(230, 126, 34));
        JButton tick5 = makeButton("Tick +5 min", new Color(96, 125, 139));
        JButton tick15 = makeButton("Tick +15 min", new Color(96, 125, 139));
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                callNext();
            }
        });
        escalateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                escalateSelected();
            }
        });
        tick5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clinic.tick(5);
                showMessage("Clock moved 5 minutes", true);
                refresh();
            }
        });
        tick15.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clinic.tick(15);
                showMessage("Clock moved 15 minutes", true);
                refresh();
            }
        });
        buttons.add(nextButton);
        buttons.add(escalateButton);
        buttons.add(tick5);
        buttons.add(tick15);

        // small colour key
        JPanel key = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        key.setOpaque(false);
        String[] names = {"1 urgent", "2", "3", "4", "5 routine"};
        for (int i = 0; i < 5; i++) {
            JLabel box = new JLabel(" " + names[i] + " ");
            box.setOpaque(true);
            box.setBackground(rowColours[i]);
            box.setBorder(new LineBorder(Color.GRAY));
            key.add(box);
        }

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(buttons, BorderLayout.WEST);
        bottom.add(key, BorderLayout.EAST);

        JPanel area = new JPanel(new BorderLayout(0, 6));
        area.setOpaque(false);
        area.add(scroll, BorderLayout.CENTER);
        area.add(bottom, BorderLayout.SOUTH);
        return area;
    }

    private JPanel makeDoctorArea() {
        doctorPanel.setOpaque(false);
        for (Doctor d : clinic.getDoctors()) {
            DoctorCard card = new DoctorCard(d);
            cards.add(card);
            doctorPanel.add(card);
        }
        JPanel area = new JPanel(new BorderLayout());
        area.setOpaque(false);
        area.setBorder(BorderFactory.createTitledBorder("Doctors"));
        area.setPreferredSize(new Dimension(400, 130));
        area.add(doctorPanel, BorderLayout.CENTER);
        return area;
    }

    private JButton makeButton(String text, Color colour) {
        JButton b = new JButton(text);
        b.setBackground(colour);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        return b;
    }

    // ----- what the buttons do -----

    private void addPatient() {
        try {
            int age = Integer.parseInt(ageField.getText().trim());
            Patient p = clinic.addPatient(nameField.getText(), age, (String) complaintBox.getSelectedItem());
            showMessage("Added " + p.getName() + " (priority " + p.getPriority() + ")", true);
            nameField.setText("");
            ageField.setText("");
            nameField.requestFocusInWindow();
        } catch (NumberFormatException ex) {
            showMessage("Age must be a number", false);
        } catch (IllegalArgumentException ex) {
            showMessage(ex.getMessage(), false);
        }
        refresh();
    }

    private void callNext() {
        try {
            Doctor d = clinic.callNext();
            showMessage(d.getName() + " is now seeing " + d.getCurrent().getName(), true);
        } catch (IllegalStateException ex) {
            showMessage(ex.getMessage(), false);
        }
        refresh();
    }

    private void escalateSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showMessage("Click a patient in the table first", false);
            return;
        }
        int id = (Integer) tableModel.getValueAt(row, 1);
        try {
            Patient p = clinic.escalate(id);
            showMessage(p.getName() + " is now priority " + p.getPriority(), true);
        } catch (IllegalStateException ex) {
            showMessage(ex.getMessage(), false);
        }
        refresh();
    }

    private void showMessage(String text, boolean good) {
        messageLabel.setForeground(good ? new Color(27, 94, 32) : new Color(183, 28, 28));
        messageLabel.setText("<html>" + text + "</html>");
    }

    // ----- redrawing -----

    // Reads everything from the clinic again and redraws the table, doctors and statistics.
    private void refresh() {
        clockLabel.setText("Time: " + clinic.getClock() + " min");

        int selectedId = -1;
        if (table.getSelectedRow() >= 0) {
            selectedId = (Integer) tableModel.getValueAt(table.getSelectedRow(), 1);
        }
        tableModel.setRowCount(0);
        ArrayList<Patient> waiting = clinic.getWaiting();
        for (int i = 0; i < waiting.size(); i++) {
            Patient p = waiting.get(i);
            Object[] row = {i + 1, p.getId(), p.getName(), p.getAge(), p.getComplaint(),
                    p.getPriority(), clinic.getWaitedSoFar(p)};
            tableModel.addRow(row);
            if (p.getId() == selectedId) {
                table.setRowSelectionInterval(i, i);     // keep the same patient selected
            }
        }

        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).update();
        }

        String text = "Waiting now: " + clinic.getWaitingCount() + "\n";
        text += "Seen so far:  " + clinic.getTotalSeen() + "\n\n";
        text += String.format("%-5s %5s %8s %5s%n", "Prio", "Seen", "AvgWait", "Max");
        for (int level = 1; level <= 5; level++) {
            text += String.format("%-5d %5d %8.1f %5d%n", level, clinic.getSeen(level),
                    clinic.getAverageWait(level), clinic.getMaxWait(level));
        }
        statsArea.setText(text);
    }

    // Colours each row by its priority and centres the number columns.
    private class RowColour extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                boolean focus, int row, int column) {
            Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, column);
            if (column == 2 || column == 4) {
                setHorizontalAlignment(LEFT);
            } else {
                setHorizontalAlignment(CENTER);
            }
            if (!selected) {
                int priority = (Integer) t.getValueAt(row, 5);
                c.setBackground(rowColours[priority - 1]);
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }

    // One panel per doctor: name, who they are seeing and a progress bar.
    private class DoctorCard extends JPanel {
        private Doctor doctor;
        private JLabel nameLabel = new JLabel();
        private JLabel statusLabel = new JLabel();
        private JProgressBar bar = new JProgressBar(0, 100);

        public DoctorCard(Doctor doctor) {
            this.doctor = doctor;
            setLayout(new GridLayout(3, 1, 2, 4));
            setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.GRAY), new EmptyBorder(6, 10, 6, 10)));
            nameLabel.setText(doctor.getName() + " (" + doctor.getSpeciality() + ")");
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            bar.setStringPainted(true);
            add(nameLabel);
            add(statusLabel);
            add(bar);
        }

        public void update() {
            Patient p = doctor.getCurrent();
            if (p == null) {
                setBackground(new Color(232, 245, 233));
                statusLabel.setText("Free");
                bar.setValue(0);
                bar.setString("");
            } else {
                setBackground(new Color(255, 243, 224));
                statusLabel.setText("Seeing " + p.getName() + " (priority " + p.getPriority() + ")");
                int total = 35 - 5 * p.getPriority();
                int left = doctor.getEndTime() - clinic.getClock();
                bar.setValue(100 * (total - left) / total);
                bar.setString(left + " min left");
            }
        }
    }
}
