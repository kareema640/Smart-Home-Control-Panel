package smarthome.model.devices;

import smarthome.model.MQTTPublisher;
import javafx.beans.property.*;

public final class Camera extends Security implements MQTTPublisher {
    private final BooleanProperty recording = new SimpleBooleanProperty(true);

    public Camera(String name, String room) {
        super(name, room);
        setActive(true);
        updateStatusIcon();
    }

    @Override
    public void readState() {
        updateStatusIcon();
    }

    @Override
    public void sendCommand(String cmd) {
        switch (cmd) {
            case "ON" -> setActive(true);
            case "OFF" -> setActive(false);
            case "REC" -> recording.set(true);
            case "STOP" -> recording.set(false);
        }
        publish("home/security/camera/" + getName(), cmd);
    }

    @Override
    public void updateStatusIcon() {
        statusIcon.set(isActive() ? "📷" : "📵");
    }

    @Override
    public void publish(String topic, String message) {
        System.out.println("[MQTT] " + topic + " → " + message);
    }

    @Override
    public void subscribe(String topic) {}

    public boolean isRecording() { return recording.get(); }
    public BooleanProperty recordingProperty() { return recording; }
}
