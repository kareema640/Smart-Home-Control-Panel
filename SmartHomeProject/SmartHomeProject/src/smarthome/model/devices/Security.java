package smarthome.model.devices;

import smarthome.model.SmartDevice;
import smarthome.model.Alertable;
import javafx.beans.property.*;

public abstract class Security extends SmartDevice implements Alertable {
    protected BooleanProperty alertActive = new SimpleBooleanProperty(false);
    protected StringProperty  lastAlert   = new SimpleStringProperty("");

    public Security(String name, String room) {
        super(name, room);
    }

    @Override
    public void triggerAlert(String message) {
        alertActive.set(true);
        lastAlert.set(message);
    }

    @Override
    public boolean isAlertActive() { return alertActive.get(); }

    public BooleanProperty alertActiveProperty() { return alertActive; }
    public String getLastAlert() { return lastAlert.get(); }
    public StringProperty lastAlertProperty() { return lastAlert; }
}
