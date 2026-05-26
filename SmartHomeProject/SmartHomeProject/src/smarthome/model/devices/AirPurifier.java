package smarthome.model.devices;

import smarthome.model.MQTTPublisher;
import javafx.beans.property.*;

public final class AirPurifier extends Climate implements MQTTPublisher {
    private final DoubleProperty  purity = new SimpleDoubleProperty(64.0);
    private final StringProperty  speed  = new SimpleStringProperty("medium");

    public AirPurifier(String name, String room) {
        super(name, room);
        setActive(false);
        updateStatusIcon();
    }

   @Override
    public void readState() {
        
        if (isActive()) {
            
            setPurity(40 + Math.random() * 60);
        } 
        
        updateStatusIcon();
    }
    @Override
    public void sendCommand(String cmd) {
        switch (cmd) {
            case "ON" -> setActive(true);
            case "OFF" -> setActive(false);
            case "LOW" -> speed.set("low");
            case "MEDIUM" -> speed.set("medium");
            case "HIGH" -> speed.set("high");
        }
        publish("home/air/" + getName(), cmd);
    }

    @Override
    public void updateStatusIcon() {
        statusIcon.set(isActive() ? "💨" : "🍃");
    }

    @Override
    public void publish(String topic, String message) {
        System.out.println("[MQTT] " + topic + " → " + message);
    }

    @Override
    public void subscribe(String topic) {}

    public double getPurity() { return purity.get(); }
    public void setPurity(double v) { purity.set(Math.round(v)); }
    public DoubleProperty purityProperty() { return purity; }

    public String getSpeed() { return speed.get(); }
    public void setSpeed(String v) { speed.set(v); }
    public StringProperty speedProperty() { return speed; }
}
