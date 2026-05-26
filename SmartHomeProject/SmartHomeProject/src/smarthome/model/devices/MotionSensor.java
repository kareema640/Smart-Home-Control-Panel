package smarthome.model.devices;

import smarthome.model.MQTTPublisher;

public final class MotionSensor extends Security implements MQTTPublisher {

    public MotionSensor(String name, String room) {
        super(name, room);
        setActive(true);
        updateStatusIcon();
    }

    @Override
    public void readState() {
        if (isActive() && Math.random() < 0.15) {
            triggerAlert("Motion detected in " + getRoom() + "!");
        } else {
            alertActive.set(false);
        }
        updateStatusIcon();
    }

    @Override
    public void sendCommand(String cmd) {
        switch (cmd) {
            case "ON" -> setActive(true);
            case "OFF" -> {
                setActive(false); alertActive.set(false);
            }
        }
        publish("home/security/motion/" + getName(), cmd);
    }

    @Override
    public void updateStatusIcon() {
        if (alertActive != null && alertActive.get()) statusIcon.set("🚨");
        else statusIcon.set(isActive() ? "👁️" : "😴");
    }

    @Override
    public void publish(String topic, String message) {
        System.out.println("[MQTT] " + topic + " → " + message);
    }

    @Override
    public void subscribe(String topic) {}
}
