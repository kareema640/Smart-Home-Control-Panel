package smarthome.model.devices;

import smarthome.model.MQTTPublisher;
import javafx.beans.property.*;

public final class RGBStrip extends Lighting implements MQTTPublisher {
    private final BooleanProperty redOn   = new SimpleBooleanProperty(true);
    private final BooleanProperty greenOn = new SimpleBooleanProperty(false);
    private final BooleanProperty blueOn  = new SimpleBooleanProperty(true);

    public RGBStrip(String name, String room) {
        super(name, room);
        setActive(true);
        setBrightness(55);
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
            case "RED" -> redOn.set(!redOn.get());
            case "GREEN" -> greenOn.set(!greenOn.get());
            case "BLUE" -> blueOn.set(!blueOn.get());
        }
        publish("home/rgb/" + getName(), cmd);
    }

    @Override
    public void updateStatusIcon() {
        statusIcon.set(isActive() ? "🌈" : "⬛");
    }

    @Override
    public void publish(String topic, String message) {
        System.out.println("[MQTT] " + topic + " → " + message);
    }

    @Override
    public void subscribe(String topic) {}

    public BooleanProperty redOnProperty()   { return redOn; }
    public BooleanProperty greenOnProperty() { return greenOn; }
    public BooleanProperty blueOnProperty()  { return blueOn; }

    public boolean isRedOn()   { return redOn.get(); }
    public boolean isGreenOn() { return greenOn.get(); }
    public boolean isBlueOn()  { return blueOn.get(); }
    public void setRedOn(boolean v)   { redOn.set(v); }
    public void setGreenOn(boolean v) { greenOn.set(v); }
    public void setBlueOn(boolean v)  { blueOn.set(v); }
}
