package smarthome.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AlertEntry {
    private final String message;
    private final String time;

    public AlertEntry(String message) {
        this.message = message;
        this.time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public String getMessage() { return message; }
    public String getTime()    { return time; }

    @Override
    public String toString() { return "[" + time + "] " + message; }
}