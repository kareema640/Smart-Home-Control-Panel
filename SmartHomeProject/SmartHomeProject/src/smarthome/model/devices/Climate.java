package smarthome.model.devices;

import smarthome.model.SmartDevice;
import javafx.beans.property.*;

public abstract class Climate extends SmartDevice {
    protected DoubleProperty temperature = new SimpleDoubleProperty(22.0);
    protected DoubleProperty humidity    = new SimpleDoubleProperty(75.0);

    public Climate(String name, String room) {
        super(name, room);
    }

    public double getTemperature() { return temperature.get(); }
    public void setTemperature(double v) { temperature.set(v); }
    public DoubleProperty temperatureProperty() { return temperature; }

    public double getHumidity() { return humidity.get(); }
    public void setHumidity(double v) { humidity.set(v); }
    public DoubleProperty humidityProperty() { return humidity; }
}