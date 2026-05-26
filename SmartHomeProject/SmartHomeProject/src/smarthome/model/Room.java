package smarthome.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Room {
    private final String name;
    private final String icon;
    private final ObservableList<SmartDevice> devices = FXCollections.observableArrayList();

    public Room(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() { return name; }
    public String getIcon() { return icon; }

    public ObservableList<SmartDevice> getDevices() { return devices; }

    public void addDevice(SmartDevice d) { devices.add(d); }

    public int getDeviceCount() { return devices.size(); }

    @Override
    public String toString() { return name; }
}