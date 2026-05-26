package smarthome.model;

public interface MQTTPublisher {
    void publish(String topic, String message);
    void subscribe(String topic);
}