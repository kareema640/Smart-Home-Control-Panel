package smarthome.model.devices;

import smarthome.model.MQTTPublisher;

public final class SmartBulb extends Lighting implements MQTTPublisher {

    public SmartBulb(String name, String room) {
        super(name, room);
        updateStatusIcon();
    }

    @Override
    public void readState() {
        if (isActive()) { 
            
            double newBrightness = 30 + Math.random() * 70;
            setBrightness(Math.round(newBrightness));
        } else {
            setBrightness(0.0);
        }
        updateStatusIcon();
    }

    @Override
    public void sendCommand(String cmd) {
        switch (cmd) {
            case "ON" -> setActive(true);
            case "OFF" -> setActive(false);
            default -> {
                if (cmd.startsWith("BRIGHTNESS:")) {
                    double val = Double.parseDouble(cmd.split(":")[1]);
                    setBrightness(val);
                }
            }
        }
        publish("home/lighting/" + getName(), cmd);
    }

    @Override
    public void updateStatusIcon() {
        statusIcon.set(isActive() ? "💡" : "🔦");
    }

    @Override
    public void publish(String topic, String message) {
        System.out.println("[MQTT] " + topic + " → " + message);
    }

    @Override
    public void subscribe(String topic) {
        System.out.println("[MQTT] Subscribed to: " + topic);
    }
}
