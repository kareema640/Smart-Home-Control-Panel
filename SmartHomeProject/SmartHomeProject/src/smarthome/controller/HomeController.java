package smarthome.controller;

import smarthome.model.*;
import smarthome.model.devices.*;
import javafx.collections.*;
import java.util.Timer;
import java.util.TimerTask;

public class HomeController {

    private final ObservableList<Room>       rooms       = FXCollections.observableArrayList();
    private final ObservableList<AlertEntry> alertLog    = FXCollections.observableArrayList();
    private Room   selectedRoom;
    private Timer  sensorTimer;

    public HomeController() {
        buildDemoData();
        startSensorTimer();
    }

    // ---------------------------------------------------------------
    // Demo data
    // ---------------------------------------------------------------
    private void buildDemoData() {

        // Living Room
        Room living = new Room("Living room", "🛋️");
        SmartBulb   bulb1      = new SmartBulb("Smart Lamp", "Living room");
        bulb1.setActive(true);
        Camera      cam1       = new Camera("Camera", "Living room");
        MotionSensor motion1   = new MotionSensor("Motion Sensor", "Living room");
        motion1.setActive(true);
        Thermostat  thermo1    = new Thermostat("Air Conditioner", "Living room");
        thermo1.setActive(true);
        thermo1.setTemperature(21);
        living.addDevice(bulb1);
        living.addDevice(cam1);
        living.addDevice(motion1);
        living.addDevice(thermo1);
        rooms.add(living);

        // Kitchen
        Room kitchen = new Room("Kitchen", "🍳");
        AirPurifier  purifier  = new AirPurifier("Air Purifier", "Kitchen");
        purifier.setActive(false);
        Thermostat   thermo2   = new Thermostat("Temperature", "Kitchen");
        thermo2.setActive(true);
        thermo2.setTemperature(24);
        SmartBulb    bulb2     = new SmartBulb("ES light", "Kitchen");
        bulb2.setActive(true);
        bulb2.setBrightness(36);
        kitchen.addDevice(purifier);
        kitchen.addDevice(thermo2);
        kitchen.addDevice(bulb2);
        rooms.add(kitchen);

        // Bed Room
        Room bedroom = new Room("Bed room", "🛏️");
        Thermostat thermo3 = new Thermostat("Air Conditioner", "Bed room");
        thermo3.setActive(false);
        thermo3.setTemperature(21);
        RGBStrip rgb = new RGBStrip("RGB Light", "Bed room");
        rgb.setActive(true);
        MotionSensor motion2 = new MotionSensor("Motion Sensor", "Bed room");
        bedroom.addDevice(thermo3);
        bedroom.addDevice(rgb);
        bedroom.addDevice(motion2);
        rooms.add(bedroom);

        selectedRoom = living;
    }

    // ---------------------------------------------------------------
    // Sensor polling timer
    // ---------------------------------------------------------------
    private void startSensorTimer() {
        sensorTimer = new Timer(true);
        sensorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Room r : rooms) {
                    for (SmartDevice d : r.getDevices()) {
                        d.readState();
                        // Collect alerts
                        if (d instanceof Alertable a) {
                            if (a.isAlertActive()) {
                                final String msg = ((smarthome.model.devices.Security) d).getLastAlert();
                                javafx.application.Platform.runLater(() ->
                                    addAlert(msg));
                            }
                        }
                    }
                }
            }
        }, 2000, 3000);
    }

    public void stopTimer() {
        if (sensorTimer != null) sensorTimer.cancel();
    }

    // ---------------------------------------------------------------
    // Alerts
    // ---------------------------------------------------------------
    public void addAlert(String msg) {
        alertLog.add(0, new AlertEntry(msg));
        if (alertLog.size() > 50) alertLog.remove(alertLog.size() - 1);
    }

    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------
    public ObservableList<Room>       getRooms()    { return rooms; }
    public ObservableList<AlertEntry> getAlertLog() { return alertLog; }

    public Room getSelectedRoom() { return selectedRoom; }
    public void setSelectedRoom(Room r) { selectedRoom = r; }

    public double getTemperature() {
        if (selectedRoom == null) return 24;
        return selectedRoom.getDevices().stream()
            .filter(d -> d instanceof smarthome.model.devices.Climate)
            .mapToDouble(d -> ((smarthome.model.devices.Climate) d).getTemperature())
            .average().orElse(24.0);
    }

    public double getHumidity() {
        if (selectedRoom == null) return 75;
        return selectedRoom.getDevices().stream()
            .filter(d -> d instanceof smarthome.model.devices.Climate)
            .mapToDouble(d -> ((smarthome.model.devices.Climate) d).getHumidity())
            .average().orElse(75.0);
    }

    public double getBrightness() {
        if (selectedRoom == null) return 70;
        return selectedRoom.getDevices().stream()
            .filter(d -> d instanceof smarthome.model.devices.Lighting)
            .mapToDouble(d -> ((smarthome.model.devices.Lighting) d).getBrightness())
            .average().orElse(70.0);
    }

    public double getEnergyUsed() { return 13.0; }
}