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

// Web interface with five addresses. Answers are JSON text that we build by hand.
// Inputs are sent like a web form:  name=Ravi&age=30&complaint=high+fever  (in the address after ? or in the body).
public class ClinicServer {
    private Clinic clinic;
    private HttpServer server;

    public ClinicServer(Clinic clinic, int port) throws IOException {
        this.clinic = clinic;
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);

        server.createContext("/queue", new HttpHandler() {
            public void handle(HttpExchange ex) throws IOException {
                if (!isMethod(ex, "GET")) return;
                send(ex, 200, queueJson());
            }
        });
        server.createContext("/patients", new HttpHandler() {
            public void handle(HttpExchange ex) throws IOException {
                if (!isMethod(ex, "POST")) return;
                try {
                    HashMap<String, String> in = readInput(ex);
                    int age = Integer.parseInt(in.get("age"));
                    Patient p = ClinicServer.this.clinic.addPatient(in.get("name"), age, in.get("complaint"));
                    send(ex, 201, patientJson(p));
                } catch (NumberFormatException e) {
                    send(ex, 400, error("age must be a number"));
                } catch (IllegalArgumentException e) {
                    send(ex, 400, error(e.getMessage()));
                }
            }
        });
        server.createContext("/next", new HttpHandler() {
            public void handle(HttpExchange ex) throws IOException {
                if (!isMethod(ex, "POST")) return;
                try {
                    Doctor d = ClinicServer.this.clinic.callNext();
                    send(ex, 200, "{\"doctor\":" + quote(d.getName()) + ",\"patient\":" + patientJson(d.getCurrent()) + "}");
                } catch (IllegalStateException e) {
                    send(ex, 409, error(e.getMessage()));
                }
            }
        });
        server.createContext("/escalate", new HttpHandler() {
            public void handle(HttpExchange ex) throws IOException {
                if (!isMethod(ex, "POST")) return;
                try {
                    HashMap<String, String> in = readInput(ex);
                    Patient p = ClinicServer.this.clinic.escalate(Integer.parseInt(in.get("id")));
                    send(ex, 200, patientJson(p));
                } catch (NumberFormatException e) {
                    send(ex, 400, error("id must be a number"));
                } catch (IllegalArgumentException e) {
                    send(ex, 404, error(e.getMessage()));
                } catch (IllegalStateException e) {
                    send(ex, 409, error(e.getMessage()));
                }
            }
        });
        server.createContext("/stats", new HttpHandler() {
            public void handle(HttpExchange ex) throws IOException {
                if (!isMethod(ex, "GET")) return;
                send(ex, 200, statsJson());
            }
        });
    }

    public void start() { server.start(); }
    public int getPort() { return server.getAddress().getPort(); }

    // ----- building the JSON text -----

    private String quote(String text) {
        String safe = text.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + safe + "\"";
    }

    private String error(String message) {
        return "{\"error\":" + quote(message) + "}";
    }

    private String patientJson(Patient p) {
        return "{\"id\":" + p.getId() + ",\"name\":" + quote(p.getName()) + ",\"age\":" + p.getAge()
                + ",\"complaint\":" + quote(p.getComplaint()) + ",\"priority\":" + p.getPriority()
                + ",\"status\":" + quote(p.getStatus()) + "}";
    }

    private String queueJson() {
        String json = "{\"clock\":" + clinic.getClock() + ",\"waiting\":[";
        ArrayList<Patient> waiting = clinic.getWaiting();
        for (int i = 0; i < waiting.size(); i++) {
            if (i > 0) json += ",";
            json += patientJson(waiting.get(i));
        }
        json += "],\"doctors\":[";
        ArrayList<Doctor> doctors = clinic.getDoctors();
        for (int i = 0; i < doctors.size(); i++) {
            Doctor d = doctors.get(i);
            if (i > 0) json += ",";
            json += "{\"name\":" + quote(d.getName()) + ",\"free\":" + d.isFree();
            if (!d.isFree()) {
                json += ",\"patient\":" + quote(d.getCurrent().getName()) + ",\"endsAt\":" + d.getEndTime();
            }
            json += "}";
        }
        return json + "]}";
    }

    private String statsJson() {
        double total = 0;
        int max = 0;
        String levels = "";
        for (int level = 1; level <= 5; level++) {
            total += clinic.getAverageWait(level) * clinic.getSeen(level);
            if (clinic.getMaxWait(level) > max) max = clinic.getMaxWait(level);
            if (level > 1) levels += ",";
            levels += "{\"priority\":" + level + ",\"seen\":" + clinic.getSeen(level)
                    + ",\"averageWait\":" + round(clinic.getAverageWait(level))
                    + ",\"maxWait\":" + clinic.getMaxWait(level) + "}";
        }
        int seen = clinic.getTotalSeen();
        double average = seen == 0 ? 0 : total / seen;
        return "{\"clock\":" + clinic.getClock() + ",\"waiting\":" + clinic.getWaitingCount()
                + ",\"seen\":" + seen + ",\"averageWait\":" + round(average) + ",\"maxWait\":" + max
                + ",\"byPriority\":[" + levels + "]}";
    }

    private double round(double x) {
        return Math.round(x * 10) / 10.0;
    }

    // ----- reading the request and sending the answer -----

    private boolean isMethod(HttpExchange ex, String wanted) throws IOException {
        if (ex.getRequestMethod().equals(wanted)) {
            return true;
        }
        send(ex, 405, error("use " + wanted + " here"));
        return false;
    }

    // Reads name=value pairs from the address (after ?) and from the body.
    private HashMap<String, String> readInput(HttpExchange ex) throws IOException {
        String text = "";
        String query = ex.getRequestURI().getRawQuery();
        if (query != null) text = query;
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
        if (!body.equals("")) text = text + "&" + body;

        HashMap<String, String> map = new HashMap<String, String>();
        String[] pairs = text.split("&");
        for (int i = 0; i < pairs.length; i++) {
            String[] parts = pairs[i].split("=", 2);
            if (parts.length == 2) {
                map.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private void send(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        OutputStream out = ex.getResponseBody();
        out.write(bytes);
        out.close();
    }
}
