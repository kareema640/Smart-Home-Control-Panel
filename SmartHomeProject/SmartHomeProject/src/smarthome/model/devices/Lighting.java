package smarthome.model.devices;

import smarthome.model.SmartDevice;
import javafx.beans.property.*;

public abstract class Lighting extends SmartDevice {
    protected DoubleProperty brightness = new SimpleDoubleProperty(70.0);

    public Lighting(String name, String room) {
        super(name, room);
    }

    public double getBrightness() { return brightness.get(); }
    public void setBrightness(double v) { brightness.set(v); }
    public DoubleProperty brightnessProperty() { return brightness; }
}
