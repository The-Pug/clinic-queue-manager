import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.table.DefaultTableCellRenderer;

// The one window: add form on top, queue table in the middle, doctors at the bottom.
public class ClinicWindow extends JFrame {
    private Clinic clinic;

    private JTextField nameField = new JTextField(10);
    private JTextField ageField = new JTextField(3);
    private JComboBox<String> complaintBox = new JComboBox<String>();
    private JLabel clockLabel = new JLabel();
    private DefaultTableModel tableModel;
    private JTable table;
    private JPanel doctorPanel = new JPanel();
    private ArrayList<JLabel> doctorLabels = new ArrayList<JLabel>();

    public ClinicWindow(Clinic clinic) {
        super("Clinic Queue - Team 29");
        this.clinic = clinic;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // ----- top: add form and buttons -----
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        complaintBox.setEditable(true);      // you can also type a new complaint
        for (String name : clinic.getDictionary().getNames()) {
            complaintBox.addItem(name);
        }
        JButton addButton = new JButton("Add");
        JButton nextButton = new JButton("Call next");
        JButton tickButton = new JButton("Tick +5 min");
        top.add(new JLabel("Name"));
        top.add(nameField);
        top.add(new JLabel("Age"));
        top.add(ageField);
        top.add(new JLabel("Complaint"));
        top.add(complaintBox);
        top.add(addButton);
        top.add(nextButton);
        top.add(tickButton);
        top.add(clockLabel);
        add(top, BorderLayout.NORTH);

        // ----- middle: queue table -----
        String[] columns = {"Position", "ID", "Name", "Age", "Complaint", "Priority", "Waited (min)"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setDefaultRenderer(Object.class, new RowColour());
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ----- bottom: one label per doctor -----
        doctorPanel.setLayout(new GridLayout(0, 1));
        for (Doctor d : clinic.getDoctors()) {
            JLabel label = new JLabel();
            doctorLabels.add(label);
            doctorPanel.add(label);
        }
        add(doctorPanel, BorderLayout.SOUTH);

        // ----- button actions -----
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int age = Integer.parseInt(ageField.getText().trim());
                    clinic.addPatient(nameField.getText(), age, (String) complaintBox.getSelectedItem());
                    nameField.setText("");
                    ageField.setText("");
                } catch (NumberFormatException ex) {
                    showError("Age must be a number");
                } catch (IllegalArgumentException ex) {
                    showError(ex.getMessage());
                }
                refresh();
            }
        });
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clinic.callNext();
                } catch (IllegalStateException ex) {
                    showError(ex.getMessage());
                }
                refresh();
            }
        });
        tickButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clinic.tick(5);
                refresh();
            }
        });

        // Redraw every second so patients added through the web address show up too.
        Timer timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        timer.start();

        refresh();
        setSize(900, 500);
        setLocationRelativeTo(null);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Cannot do that", JOptionPane.WARNING_MESSAGE);
    }

    // Reads everything from the clinic again and redraws the table and doctor labels.
    private void refresh() {
        clockLabel.setText("   Time: " + clinic.getClock() + " min");

        tableModel.setRowCount(0);
        ArrayList<Patient> waiting = clinic.getWaiting();
        for (int i = 0; i < waiting.size(); i++) {
            Patient p = waiting.get(i);
            Object[] row = {i + 1, p.getId(), p.getName(), p.getAge(), p.getComplaint(),
                    p.getPriority(), clinic.getWaitedSoFar(p)};
            tableModel.addRow(row);
        }

        ArrayList<Doctor> doctors = clinic.getDoctors();
        for (int i = 0; i < doctors.size(); i++) {
            Doctor d = doctors.get(i);
            String text = d.getName() + " (" + d.getSpeciality() + "): ";
            if (d.isFree()) {
                text += "free";
            } else {
                int left = d.getEndTime() - clinic.getClock();
                text += "seeing " + d.getCurrent().getName() + ", " + left + " min left";
            }
            doctorLabels.get(i).setText(text);
        }
    }

    // Colours each row by its priority: red = urgent ... blue = routine.
    private class RowColour extends DefaultTableCellRenderer {
        private Color[] colours = {new Color(255, 205, 210), new Color(255, 224, 178), new Color(255, 249, 196),
                new Color(220, 237, 200), new Color(187, 222, 251)};

        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                boolean focus, int row, int column) {
            Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, column);
            if (!selected) {
                int priority = (Integer) t.getValueAt(row, 5);
                c.setBackground(colours[priority - 1]);
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }
}
