package smarthome.model;

import javafx.beans.property.*;

public abstract class SmartDevice {
    protected StringProperty name = new SimpleStringProperty();
    protected BooleanProperty active = new SimpleBooleanProperty(false);
    protected StringProperty statusIcon = new SimpleStringProperty();
    protected StringProperty room = new SimpleStringProperty();

    public SmartDevice(String name, String room) {
        this.name.set(name);
        this.room.set(room);
    }

    public abstract void readState();
    public abstract void sendCommand(String cmd);
    public abstract void updateStatusIcon();

    // Name
    public String getName() { return name.get(); }
    public void setName(String v) { name.set(v); }
    public StringProperty nameProperty() { return name; }

    // Active
    public boolean isActive() { return active.get(); }
    public void setActive(boolean v) { active.set(v); updateStatusIcon();}
    public BooleanProperty activeProperty() { return active; }

    // Status Icon
    public String getStatusIcon() { return statusIcon.get(); }
    public StringProperty statusIconProperty() { return statusIcon; }

    // Room
    public String getRoom() { return room.get(); }
    public void setRoom(String v) { room.set(v); }
    public StringProperty roomProperty() { return room; }

    @Override
    public String toString() { return getName(); }
}