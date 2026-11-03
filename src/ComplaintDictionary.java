import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

// Remembers how urgent each complaint is: complaint -> priority number.
public class ComplaintDictionary {
    private HashMap<String, Integer> priorities = new HashMap<String, Integer>();

    public ComplaintDictionary() {
        priorities.put("cardiac arrest", 1);
        priorities.put("severe bleeding", 1);
        priorities.put("breathing trouble", 1);
        priorities.put("fracture", 2);
        priorities.put("high fever", 3);
        priorities.put("vomiting", 3);
        priorities.put("minor cut", 4);
        priorities.put("routine checkup", 5);
    }

    // Add a new complaint or change an old one.
    public void add(String complaint, int priority) {
        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException("Priority must be between 1 and 5");
        }
        priorities.put(complaint.trim().toLowerCase(), priority);
    }

    // Unknown complaints are treated as routine (5).
    public int getPriority(String complaint) {
        String key = complaint.trim().toLowerCase();
        if (priorities.containsKey(key)) {
            return priorities.get(key);
        }
        return 5;
    }

    // All complaint names in alphabetical order (for the drop-down list).
    public ArrayList<String> getNames() {
        ArrayList<String> names = new ArrayList<String>(priorities.keySet());
        Collections.sort(names);
        return names;
    }
}
