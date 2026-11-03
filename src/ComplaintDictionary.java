import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ComplaintDictionary {
    private HashMap<String, Integer> priorityByComplaint = new HashMap<String, Integer>();

    public ComplaintDictionary() {
        priorityByComplaint.put("cardiac arrest", 1);
        priorityByComplaint.put("severe bleeding", 1);
        priorityByComplaint.put("breathing trouble", 1);
        priorityByComplaint.put("fracture", 2);
        priorityByComplaint.put("high fever", 3);
        priorityByComplaint.put("vomiting", 3);
        priorityByComplaint.put("minor cut", 4);
        priorityByComplaint.put("routine checkup", 5);
    }

    public void addComplaint(String complaintName, int priorityNumber) {
        if (priorityNumber < 1 || priorityNumber > 5) {
            throw new IllegalArgumentException("Priority must be between 1 and 5");
        }
        priorityByComplaint.put(complaintName.trim().toLowerCase(), priorityNumber);
    }

    public int getPriorityFor(String complaintName) {
        String complaintKey = complaintName.trim().toLowerCase();
        if (priorityByComplaint.containsKey(complaintKey)) {
            return priorityByComplaint.get(complaintKey);
        }
        return 5;
    }

    public ArrayList<String> getComplaintNames() {
        ArrayList<String> complaintNames = new ArrayList<String>(priorityByComplaint.keySet());
        Collections.sort(complaintNames);
        return complaintNames;
    }
}
