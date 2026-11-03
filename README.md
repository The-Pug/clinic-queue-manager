# Clinic Queue

Team 29, Section B (OOP with Java)

Team: Koustubh (team leader), Ojas Rawat, Masihullah, Haisiyat Ali

## What it does

A small clinic simulation. Patients arrive with a complaint, and each complaint has a priority number from 1
(most urgent) to 5 (routine). The most urgent patient is called first. If two patients have the same priority, the
one who came first goes first. Three doctors see one patient at a time, and a clock (in minutes) moves the day forward.

Plain Java only: no libraries and no build tool.

## Build and run

```
javac -d out src/*.java
java -cp out Main                  # window + web server on port 8080
java -cp out Main server 9000      # web server only, on port 9000
java -cp out ClinicTests           # run the checks
```

## The window

- Fill in name, age and complaint, then press **Add**. You can also type a new complaint.
- **Call next** sends the most urgent patient to the first free doctor.
- **Tick +5 min** moves the clock forward. A doctor becomes free when the consultation time is over.
- Rows are coloured by priority (red = urgent, blue = routine).

Consultation time = 35 - 5 x priority minutes (priority 1 = 30 min, priority 5 = 10 min).

## Web addresses

The server listens on 127.0.0.1 only. Answers are JSON. Inputs are sent like a web form, either after the `?` or in the body.

| Method | Address | Inputs | What it does |
|---|---|---|---|
| GET | `/queue` | | Waiting patients in call order and the doctors |
| POST | `/patients` | `name`, `age`, `complaint` | Add a patient |
| POST | `/next` | | Send the most urgent patient to a free doctor |
| POST | `/escalate` | `id` | Make a waiting patient one priority step more urgent |
| GET | `/stats` | | Seen patients, average and maximum waiting time per priority |

Examples:

```
curl -X POST "http://127.0.0.1:8080/patients?name=Hari&age=61&complaint=cardiac%20arrest"
curl http://127.0.0.1:8080/queue
curl -X POST http://127.0.0.1:8080/next
curl -X POST "http://127.0.0.1:8080/escalate?id=3"
curl http://127.0.0.1:8080/stats
```

Errors come back as `{"error": "..."}` with status 400 (bad input), 404 (no such patient), 405 (wrong method)
or 409 (no free doctor, nobody waiting, or patient not waiting).

## Files

| File | What it is |
|---|---|
| `Patient.java` | One patient |
| `Doctor.java` | One doctor and the patient they are seeing |
| `ComplaintDictionary.java` | `HashMap` of complaint to priority number |
| `Clinic.java` | The queue (`PriorityQueue`), doctors, clock and statistics |
| `ClinicWindow.java` | The Swing window |
| `ClinicServer.java` | The five web addresses |
| `Main.java` | Starts everything |
| `ClinicTests.java` | A few checks with a `main` method |
