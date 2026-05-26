package smarthome.model;

public interface Alertable {
    void triggerAlert(String message);
    boolean isAlertActive();
}