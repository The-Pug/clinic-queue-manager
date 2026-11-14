# Clinic Queue

Team 29, Section B (OOP with Java)

Team: Koustubh (team leader), Ojas Rawat, Masihullah, Haisiyat Ali

## What it does

A small clinic simulation. Patients arrive with a complaint, and each complaint has a priority number from 1
(most urgent) to 5 (routine). The most urgent patient is called first. If two patients have the same priority, the
one who came first goes first. Three doctors see one patient at a time.

A clock runs in its own thread: every 1 second of real time is 1 minute in the clinic.

Plain Java only: no libraries and no build tool.

## Build and run

```
javac -d out src/*.java
java -cp out Main            # open the window
java -cp out ClinicTests     # run the checks
```

## The window

- Type a **Name** and **Age**, pick a **Complaint**, and press **Add patient**.
- Press **Call next** to send the most urgent patient to a free doctor.
- The **Waiting queue** box shows patients in the order they will be called.
- The **Doctors** box shows who each doctor is seeing and how many minutes are left.
- The bottom line shows the time and a message about the last action.

Consultation time = 35 - 5 x priority minutes (priority 1 = 30 min, priority 5 = 10 min).

## Java topics used

| Topic | Where |
|---|---|
| Classes and objects | `Patient`, `Doctor`, `Clinic` |
| Encapsulation (private fields, getters) | `Patient`, `Doctor` |
| Arrays | the three doctors in `Clinic` |
| `ArrayList` | the waiting list in `Clinic` |
| `HashMap` | complaint to priority in `ComplaintDictionary` |
| Bubble sort | `Clinic.getWaitingPatients()` |
| Exception handling (`try` / `catch` / `throw`) | `Clinic.addPatient()`, `ClinicWindow.addPatient()` |
| Swing and AWT (`JFrame`, layouts, `ActionListener`) | `ClinicWindow` |
| Multithreading (`extends Thread`, `sleep`) | `ClockThread` |
| Synchronization (`synchronized` methods) | `Clinic`, shared by the window and the clock thread |

## Files

| File | What it is |
|---|---|
| `Patient.java` | One patient |
| `Doctor.java` | One doctor and the patient they are seeing |
| `ComplaintDictionary.java` | `HashMap` of complaint to priority number |
| `Clinic.java` | The waiting list, doctors and clock |
| `ClinicWindow.java` | The Swing window |
| `ClockThread.java` | The thread that moves the clock forward |
| `Main.java` | Starts the window and the clock thread |
| `ClinicTests.java` | A few checks with a `main` method |
