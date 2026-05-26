package smarthome.model.devices;

import smarthome.model.MQTTPublisher;
import javafx.beans.property.*;

public final class Thermostat extends Climate implements MQTTPublisher {
    private final BooleanProperty heating = new SimpleBooleanProperty(true);
    private final StringProperty  airQuality = new SimpleStringProperty("Good");

    public Thermostat(String name, String room) {
        super(name, room);
        setActive(true);
        updateStatusIcon();
    }

  @Override
    public void readState() {
        if (isActive()) {
        
            double change = (Math.random() - 0.5) * 0.5;
            setTemperature(getTemperature() + change);
            
            double humChange = (Math.random() - 0.5) * 2;
            setHumidity(Math.max(30, Math.min(90, getHumidity() + humChange)));
        } else {
            
        }
        updateStatusIcon(); 
    }

    @Override
    public void sendCommand(String cmd) {
        switch (cmd) {
            case "ON" -> setActive(true);
            case "OFF" -> setActive(false);
            case "HEAT" -> heating.set(true);
            case "COOL" -> heating.set(false);
            case "TEMP_UP" -> setTemperature(getTemperature() + 1);
            case "TEMP_DN" -> setTemperature(getTemperature() - 1);
        }
        publish("home/climate/" + getName(), cmd);
    }

    @Override
    public void updateStatusIcon() {
        statusIcon.set(isActive() ? (heating.get() ? "🔥" : "❄️") : "🌡️");
    }

    @Override
    public void publish(String topic, String message) {
        System.out.println("[MQTT] " + topic + " → " + message);
    }

    @Override
    public void subscribe(String topic) {}

    public boolean isHeating() { return heating.get(); }
    public void setHeating(boolean v) { heating.set(v); updateStatusIcon(); }
    public BooleanProperty heatingProperty() { return heating; }

    public String getAirQuality() { return airQuality.get(); }
    public StringProperty airQualityProperty() { return airQuality; }
}
