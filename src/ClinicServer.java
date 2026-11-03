import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class ClinicServer {
    private Clinic clinic;
    private HttpServer httpServer;

    public ClinicServer(Clinic clinic, int portNumber) throws IOException {
        this.clinic = clinic;
        httpServer = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), portNumber), 0);

        httpServer.createContext("/queue", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                if (!isCorrectMethod(exchange, "GET")) return;
                sendReply(exchange, 200, queueToJson());
            }
        });

        httpServer.createContext("/patients", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                if (!isCorrectMethod(exchange, "POST")) return;
                try {
                    HashMap<String, String> inputValues = readInputValues(exchange);
                    int age = Integer.parseInt(inputValues.get("age"));
                    Patient patient = ClinicServer.this.clinic.addPatient(
                            inputValues.get("name"), age, inputValues.get("complaint"));
                    sendReply(exchange, 201, patientToJson(patient));
                } catch (NumberFormatException exception) {
                    sendReply(exchange, 400, errorToJson("age must be a number"));
                } catch (IllegalArgumentException exception) {
                    sendReply(exchange, 400, errorToJson(exception.getMessage()));
                }
            }
        });

        httpServer.createContext("/next", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                if (!isCorrectMethod(exchange, "POST")) return;
                try {
                    Doctor doctor = ClinicServer.this.clinic.callNextPatient();
                    sendReply(exchange, 200, "{\"doctor\":" + putInQuotes(doctor.getName())
                            + ",\"patient\":" + patientToJson(doctor.getCurrentPatient()) + "}");
                } catch (IllegalStateException exception) {
                    sendReply(exchange, 409, errorToJson(exception.getMessage()));
                }
            }
        });

        httpServer.createContext("/escalate", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                if (!isCorrectMethod(exchange, "POST")) return;
                try {
                    HashMap<String, String> inputValues = readInputValues(exchange);
                    Patient patient = ClinicServer.this.clinic.escalatePatient(Integer.parseInt(inputValues.get("id")));
                    sendReply(exchange, 200, patientToJson(patient));
                } catch (NumberFormatException exception) {
                    sendReply(exchange, 400, errorToJson("id must be a number"));
                } catch (IllegalArgumentException exception) {
                    sendReply(exchange, 404, errorToJson(exception.getMessage()));
                } catch (IllegalStateException exception) {
                    sendReply(exchange, 409, errorToJson(exception.getMessage()));
                }
            }
        });

        httpServer.createContext("/stats", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                if (!isCorrectMethod(exchange, "GET")) return;
                sendReply(exchange, 200, statsToJson());
            }
        });
    }

    public void start() { httpServer.start(); }
    public int getPort() { return httpServer.getAddress().getPort(); }

    private String putInQuotes(String text) {
        String escapedText = text.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escapedText + "\"";
    }

    private String errorToJson(String message) {
        return "{\"error\":" + putInQuotes(message) + "}";
    }

    private String patientToJson(Patient patient) {
        return "{\"id\":" + patient.getPatientId() + ",\"name\":" + putInQuotes(patient.getName())
                + ",\"age\":" + patient.getAge()
                + ",\"complaint\":" + putInQuotes(patient.getComplaint())
                + ",\"priority\":" + patient.getPriority()
                + ",\"status\":" + putInQuotes(patient.getStatus()) + "}";
    }

    private String queueToJson() {
        String json = "{\"clock\":" + clinic.getClockMinutes() + ",\"waiting\":[";
        ArrayList<Patient> waitingPatients = clinic.getWaitingPatients();
        for (int index = 0; index < waitingPatients.size(); index++) {
            if (index > 0) json += ",";
            json += patientToJson(waitingPatients.get(index));
        }
        json += "],\"doctors\":[";
        ArrayList<Doctor> doctorList = clinic.getDoctorList();
        for (int index = 0; index < doctorList.size(); index++) {
            Doctor doctor = doctorList.get(index);
            if (index > 0) json += ",";
            json += "{\"name\":" + putInQuotes(doctor.getName()) + ",\"free\":" + doctor.isFree();
            if (!doctor.isFree()) {
                json += ",\"patient\":" + putInQuotes(doctor.getCurrentPatient().getName())
                        + ",\"endsAt\":" + doctor.getConsultationEndTime();
            }
            json += "}";
        }
        return json + "]}";
    }

    private String statsToJson() {
        double totalWaitMinutes = 0;
        int maxWaitMinutes = 0;
        String priorityLevelsJson = "";
        for (int priorityLevel = 1; priorityLevel <= 5; priorityLevel++) {
            totalWaitMinutes += clinic.getAverageWaitMinutes(priorityLevel) * clinic.getSeenCount(priorityLevel);
            if (clinic.getMaxWaitMinutes(priorityLevel) > maxWaitMinutes) {
                maxWaitMinutes = clinic.getMaxWaitMinutes(priorityLevel);
            }
            if (priorityLevel > 1) priorityLevelsJson += ",";
            priorityLevelsJson += "{\"priority\":" + priorityLevel
                    + ",\"seen\":" + clinic.getSeenCount(priorityLevel)
                    + ",\"averageWait\":" + roundOneDecimal(clinic.getAverageWaitMinutes(priorityLevel))
                    + ",\"maxWait\":" + clinic.getMaxWaitMinutes(priorityLevel) + "}";
        }
        int seenCount = clinic.getTotalSeenCount();
        double averageWaitMinutes = 0;
        if (seenCount > 0) {
            averageWaitMinutes = totalWaitMinutes / seenCount;
        }
        return "{\"clock\":" + clinic.getClockMinutes() + ",\"waiting\":" + clinic.getWaitingCount()
                + ",\"seen\":" + seenCount + ",\"averageWait\":" + roundOneDecimal(averageWaitMinutes)
                + ",\"maxWait\":" + maxWaitMinutes
                + ",\"byPriority\":[" + priorityLevelsJson + "]}";
    }

    private double roundOneDecimal(double number) {
        return Math.round(number * 10) / 10.0;
    }

    private boolean isCorrectMethod(HttpExchange exchange, String wantedMethod) throws IOException {
        if (exchange.getRequestMethod().equals(wantedMethod)) {
            return true;
        }
        sendReply(exchange, 405, errorToJson("use " + wantedMethod + " here"));
        return false;
    }

    private HashMap<String, String> readInputValues(HttpExchange exchange) throws IOException {
        String inputText = "";
        String addressPart = exchange.getRequestURI().getRawQuery();
        if (addressPart != null) inputText = addressPart;
        String bodyPart = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
        if (!bodyPart.equals("")) inputText = inputText + "&" + bodyPart;

        HashMap<String, String> inputValues = new HashMap<String, String>();
        String[] inputPairs = inputText.split("&");
        for (int index = 0; index < inputPairs.length; index++) {
            String[] nameAndValue = inputPairs[index].split("=", 2);
            if (nameAndValue.length == 2) {
                inputValues.put(URLDecoder.decode(nameAndValue[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(nameAndValue[1], StandardCharsets.UTF_8));
            }
        }
        return inputValues;
    }

    private void sendReply(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] replyBytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, replyBytes.length);
        OutputStream replyStream = exchange.getResponseBody();
        replyStream.write(replyBytes);
        replyStream.close();
    }
}
