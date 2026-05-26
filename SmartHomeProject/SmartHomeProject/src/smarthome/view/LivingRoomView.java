package smarthome.view;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import smarthome.controller.HomeController;
import smarthome.model.devices.*;

public class LivingRoomView extends BaseRoomView {

    private Thermostat   thermostat;
    private SmartBulb    bulb;
    private MotionSensor motionSensor;

    // Alarm switch
    private ToggleSwitch alarmSwitch;

    public LivingRoomView(HomeController ctrl, MainView mv) {
        super(ctrl, mv);
        resolveDevices();
        build();
    }

    private void resolveDevices() {
        for (var d : controller.getRooms().get(0).getDevices()) {
            if (d instanceof Thermostat   t) thermostat   = t;
            if (d instanceof SmartBulb    b) bulb         = b;
            if (d instanceof MotionSensor m) motionSensor = m;
        }
    }

    private void build() {
        buildShell("Room Temp",
            thermostat != null ? thermostat.getTemperature() : 24,
            thermostat != null ? thermostat.getHumidity()    : 75,
            bulb       != null ? bulb.getBrightness()        : 70,
            controller.getEnergyUsed());

        AnchorPane canvas = new AnchorPane();
        canvas.setStyle("-fx-background-color: #F2F2F2;");
        canvas.setPrefSize(1334, 870);

        // Stat cards
        canvas.getChildren().addAll(
            statPane(45,  15, "Room Temp",
                thermostat != null ? String.format("%.0f °C", thermostat.getTemperature()) : "24 °C"),
            statPane(296, 15, "Humidity",
                thermostat != null ? String.format("%.0f %%", thermostat.getHumidity()) : "75 %"),
            statPane(554, 15, "Brightness",
                bulb != null ? String.format("%.0f %%", bulb.getBrightness()) : "70 %"),
            statPane(804, 15, "Energy Used",
                String.format("%.0f Kwh", controller.getEnergyUsed()))
        );

        // Rooms panel 
        VBox roomsPanel = buildRoomsPanel();
        roomsPanel.setPrefSize(259, 298);
        AnchorPane.setLeftAnchor(roomsPanel, 1047.0);
        AnchorPane.setTopAnchor(roomsPanel,  15.0);
        canvas.getChildren().add(roomsPanel);

        // Camera feed 
        Pane cameraPane = cameraCard(539, 298);
        AnchorPane.setLeftAnchor(cameraPane, 45.0);
        AnchorPane.setTopAnchor(cameraPane,  146.0);
        canvas.getChildren().add(cameraPane);

        // Motion sensor card 
        Pane motionPane = motionCard(274, 386);
        AnchorPane.setLeftAnchor(motionPane, 45.0);
        AnchorPane.setTopAnchor(motionPane,  466.0);
        canvas.getChildren().add(motionPane);

        // Security card 
        Pane secPane = securityCard(235, 152);
        AnchorPane.setLeftAnchor(secPane, 343.0);
        AnchorPane.setTopAnchor(secPane,  463.0);
        canvas.getChildren().add(secPane);

        // AC dial card 
        Pane acPane = acDialCard(415, 463);
        AnchorPane.setLeftAnchor(acPane, 610.0);
        AnchorPane.setTopAnchor(acPane,  144.0);
        canvas.getChildren().add(acPane);

        // Alarm card 
        Pane alarmPane = alarmCard(242, 228);
        AnchorPane.setLeftAnchor(alarmPane, 343.0);
        AnchorPane.setTopAnchor(alarmPane,  627.0);
        canvas.getChildren().add(alarmPane);

        // Smart lamp card 
        Pane lampPane = smartLampCard(251, 270);
        AnchorPane.setLeftAnchor(lampPane, 1052.0);
        AnchorPane.setTopAnchor(lampPane,  329.0);
        canvas.getChildren().add(lampPane);

        // Alert history
        Pane alertPane = alertHistoryCard(730, 243);
        AnchorPane.setLeftAnchor(alertPane, 604.0);
        AnchorPane.setTopAnchor(alertPane,  626.0);
        canvas.getChildren().add(alertPane);

        attachDeviceListeners();

        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setFitToWidth(false);
        scroll.setFitToHeight(false);
        scroll.setStyle("-fx-background-color:#F2F2F2; -fx-background:#F2F2F2;");
        root.setCenter(scroll);
        root.setRight(null);
    }

    // All device listeners in one place
    private void attachDeviceListeners() {
        if (thermostat != null) {
            thermostat.temperatureProperty().addListener((ob, ov, nv) -> {
                logAlert("Living Room: Temperature changed to " + String.format("%.0f°C", nv.doubleValue()));
                if (nv.doubleValue() > 35 && alarmSwitch != null && !alarmSwitch.isSelected()) {
                    Platform.runLater(() -> {
                        alarmSwitch.setSelected(true);
                        logAlert("Living Room: Alarm triggered — temperature exceeded 35°C");
                    });
                }
            });
            thermostat.humidityProperty().addListener((ob, ov, nv) ->
                logAlert("Living Room: Humidity changed to " + String.format("%.0f%%", nv.doubleValue())));
            // Single AC active listener 
            thermostat.activeProperty().addListener((ob, ov, nv) -> {
                logAlert("Living Room: AC " + (nv ? "turned ON" : "turned OFF"));
                if (nv && alarmSwitch != null && !alarmSwitch.isSelected()) {
                    Platform.runLater(() -> {
                        alarmSwitch.setSelected(true);
                        logAlert("Living Room: Alarm triggered — AC turned on");
                    });
                }
            });
        }
        if (bulb != null) {
            bulb.activeProperty().addListener((ob, ov, nv) -> {
                logAlert("Living Room: Smart Lamp " + (nv ? "turned ON" : "turned OFF"));
                if (nv && alarmSwitch != null && !alarmSwitch.isSelected()) {
                    Platform.runLater(() -> {
                        alarmSwitch.setSelected(true);
                        logAlert("Living Room: Alarm triggered — Smart Lamp turned on");
                    });
                }
            });
            bulb.brightnessProperty().addListener((ob, ov, nv) ->
                logAlert("Living Room: Lamp brightness changed to " + String.format("%.0f%%", nv.doubleValue())));
        }
        if (motionSensor != null) {
            motionSensor.activeProperty().addListener((ob, ov, nv) -> {
                logAlert("Living Room: Motion Sensor " + (nv ? "activated" : "deactivated"));
                if (nv && alarmSwitch != null && !alarmSwitch.isSelected()) {
                    Platform.runLater(() -> {
                        alarmSwitch.setSelected(true);
                        logAlert("Living Room: Alarm triggered — motion detected");
                    });
                }
            });
        }
    }
    
    private ImageView loadImage(String path) {
        try {
            var s = getClass().getResourceAsStream(path);
            if (s == null) return null;
            return new ImageView(new Image(s));
        } catch (Exception e) { return null; }
    }

    // White stat card 
    private Pane statPane(double x, double y, String label, String value) {
        Pane pane = new Pane();
        pane.setPrefSize(226, 99);
        pane.setStyle("-fx-background-color:WHITE; -fx-background-radius:20;");
        AnchorPane.setLeftAnchor(pane, x);
        AnchorPane.setTopAnchor(pane,  y);

        Label lbl = new Label(label);
        lbl.setLayoutX(28); lbl.setLayoutY(14);
        lbl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#888888;");

        Label val = new Label(value);
        val.setLayoutX(28); val.setLayoutY(46);
        val.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#111111;");

        pane.getChildren().addAll(lbl, val);
        return pane;
    }

    // Camera card
    private Pane cameraCard(double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);

        ImageView img = loadImage("/smarthome/images/living_room.jpg");
        if (img == null) img = loadImage("/smarthome/images/living_room.png");
        if (img != null) {
            img.setFitWidth(w); img.setFitHeight(h);
            img.setPreserveRatio(false); img.setSmooth(true);
            Rectangle clip = new Rectangle(w, h);
            clip.setArcWidth(20); clip.setArcHeight(20);
            img.setClip(clip);
            pane.getChildren().add(img);
        } else {
            pane.setStyle("-fx-background-color:#c4a882; -fx-background-radius:20;");
        }

        Pane badge = new Pane();
        badge.setLayoutX(22); badge.setLayoutY(14);
        badge.setPrefSize(73, 25);
        badge.setStyle("-fx-background-color:WHITE; -fx-background-radius:20;");
        Pane dot = new Pane();
        dot.setLayoutX(8); dot.setLayoutY(7);
        dot.setPrefSize(10, 10);
        dot.setStyle("-fx-background-color:RED; -fx-background-radius:10;");
        Label liveLbl = new Label("Live");
        liveLbl.setLayoutX(24); liveLbl.setLayoutY(2);
        liveLbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold;");
        badge.getChildren().addAll(dot, liveLbl);
        pane.getChildren().add(badge);
        return pane;
    }

    // Motion sensor card
    private Pane motionCard(double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);
        pane.setStyle("-fx-background-color:#918A78; -fx-background-radius:20;");

        ImageView img = loadImage("/smarthome/images/motion_sensor.jpg");
        if (img == null) img = loadImage("/smarthome/images/motion_sensor.png");
        if (img != null) {
            img.setLayoutX(0); img.setLayoutY(0);
            img.setFitWidth(w); img.setFitHeight(228);
            img.setPreserveRatio(false); img.setSmooth(true);
            Rectangle clip = new Rectangle(w, 228);
            clip.setArcWidth(20); clip.setArcHeight(20);
            img.setClip(clip);
            pane.getChildren().add(img);
        } else {
            Pane fallback = new Pane();
            fallback.setPrefSize(w, 228);
            fallback.setStyle("-fx-background-color:#7a7568; -fx-background-radius:20 20 0 0;");
            Label person = new Label("🚶");
            person.setLayoutX(95); person.setLayoutY(75);
            person.setStyle("-fx-font-size:65px; -fx-opacity:0.5;");
            fallback.getChildren().add(person);
            pane.getChildren().add(fallback);
        }

        Label title = new Label("Motion Sensor");
        title.setLayoutX(19); title.setLayoutY(234);
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:WHITE;");

        ToggleSwitch sw = new ToggleSwitch(motionSensor != null && motionSensor.isActive());
        if (motionSensor != null) sw.selectedProperty().bindBidirectional(motionSensor.activeProperty());
        sw.setLayoutX(215); sw.setLayoutY(236);

        Pane wavePane = new Pane();
        wavePane.setLayoutX(0); wavePane.setLayoutY(275);
        wavePane.setPrefSize(w, 100);
        buildAnimatedWave(wavePane, w, 100, sw);

        pane.getChildren().addAll(title, sw, wavePane);
        return pane;
    }

    // Animated red wave
    private void buildAnimatedWave(Pane pane, double w, double h, ToggleSwitch sw) {
        int seg = 24;
        Line[] lines = new Line[seg];
        for (int i = 0; i < seg; i++) {
            Line l = new Line(i * (w / seg), h / 2, (i + 1) * (w / seg), h / 2);
            l.setStroke(Color.web("#e74c3c"));
            l.setStrokeWidth(2.0);
            lines[i] = l;
            pane.getChildren().add(l);
        }
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(180), e -> {
            for (Line l : lines) {
                double a = 3 + Math.random() * 22;
                l.setStartY(h / 2 - a);
                l.setEndY(h / 2 + a);
            }
        }));
        tl.setCycleCount(Timeline.INDEFINITE);
        sw.selectedProperty().addListener((obs, ov, nv) -> {
            if (nv) tl.play(); else tl.stop();
        });
        if (sw.isSelected()) tl.play();
    }

    // Security card
    private Pane securityCard(double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);
        pane.setStyle("-fx-background-color:#918A78; -fx-background-radius:20;");

        Label title = new Label("Security");
        title.setLayoutX(23); title.setLayoutY(9);
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:WHITE;");

        Label shield = new Label("🛡");
        shield.setLayoutX(195); shield.setLayoutY(12);
        shield.setStyle("-fx-font-size:16px;");

        Label idLbl = new Label("ID:");
        idLbl.setLayoutX(24); idLbl.setLayoutY(43);
        idLbl.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#eeeeee;");

        TextField idField = new TextField();
        idField.setLayoutX(26); idField.setLayoutY(71);
        idField.setPrefSize(185, 30);
        idField.setPromptText("Enter your id....");
        idField.setStyle("-fx-background-radius:8; -fx-background-color:#f8f8f8;");

        Label errLbl = new Label();
        errLbl.setLayoutX(26); errLbl.setLayoutY(108);
        errLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:RED;");
        errLbl.setVisible(false);

        idField.setOnAction((ActionEvent e) -> {
            boolean ok = idField.getText().equals("1234");
            errLbl.setText(ok ? "Access Granted!" : "The id is wrong!!!");
            errLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:"
                    + (ok ? "GREEN" : "RED") + ";");
            errLbl.setVisible(true);
            logAlert("Living Room: Security ID attempt — " + (ok ? "granted" : "denied"));
            
            if (ok) {
                if (motionSensor != null && motionSensor.isActive()) {
                    motionSensor.setActive(false);
                    logAlert("Living Room: Motion sensor deactivated — access granted");
                }
            } else {
                if (motionSensor != null && !motionSensor.isActive()) {
                    motionSensor.setActive(true);
                    logAlert("Living Room: Motion sensor activated — unauthorized ID attempt");
                }
            }
            
            idField.clear();
        });

        pane.getChildren().addAll(title, shield, idLbl, idField, errLbl);
        return pane;
    }

    // AC dial card
    private Pane acDialCard(double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);
        pane.setStyle("-fx-background-color:#B4BBC2; -fx-background-radius:20;");

        Label title = new Label("Air Conditioner");
        title.setLayoutX(22); title.setLayoutY(18);
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#111111;");

        ToggleSwitch sw = new ToggleSwitch(thermostat != null && thermostat.isActive());
        if (thermostat != null) sw.selectedProperty().bindBidirectional(thermostat.activeProperty());
        sw.setLayoutX(355); sw.setLayoutY(20);

        double dialSize = 300;
        Pane dialPane = buildArcDial(dialSize);
        dialPane.setLayoutX((w - dialSize) / 2.0);
        dialPane.setLayoutY(60);
        dialPane.disableProperty().bind(sw.selectedProperty().not());
        dialPane.opacityProperty().bind(
            javafx.beans.binding.Bindings.when(sw.selectedProperty()).then(1.0).otherwise(0.5));

        double btnY = 60 + dialSize + 12;
        Button minus = new Button("−");
        minus.setLayoutX(w / 2.0 - 72); minus.setLayoutY(btnY);
        minus.setPrefSize(46, 46);
        minus.setStyle("-fx-background-color:#dcdcdc; -fx-background-radius:23; -fx-font-size:22px; -fx-font-weight:bold; -fx-cursor:hand;");

        Button plus = new Button("+");
        plus.setLayoutX(w / 2.0 + 26); plus.setLayoutY(btnY);
        plus.setPrefSize(46, 46);
        plus.setStyle("-fx-background-color:#dcdcdc; -fx-background-radius:23; -fx-font-size:22px; -fx-font-weight:bold; -fx-cursor:hand;");

        plus.disableProperty().bind(sw.selectedProperty().not());
        minus.disableProperty().bind(sw.selectedProperty().not());
        plus.opacityProperty().bind(
            javafx.beans.binding.Bindings.when(sw.selectedProperty()).then(1.0).otherwise(0.5));
        minus.opacityProperty().bind(
            javafx.beans.binding.Bindings.when(sw.selectedProperty()).then(1.0).otherwise(0.5));

        if (thermostat != null) {
            minus.setOnAction(e -> thermostat.sendCommand("TEMP_DN"));
            plus.setOnAction(e  -> thermostat.sendCommand("TEMP_UP"));
        }

        double modeY = btnY + 58;
        Label auto  = modeLabel("☀  Auto");
        auto.setLayoutX(w / 2.0 - 125); auto.setLayoutY(modeY);
        Label timer = modeLabel("⏱  1:37min");
        timer.setLayoutX(w / 2.0 + 18);  timer.setLayoutY(modeY);

        pane.getChildren().addAll(title, sw, dialPane, minus, plus, auto, timer);
        return pane;
    }

    // Arc dial
    private Pane buildArcDial(double size) {
        Pane pane = new Pane();
        pane.setPrefSize(size, size);

        double cx = size / 2.0, cy = size / 2.0;
        double r  = size / 2.0 - 20;
        double startDeg = -220.0, totalDeg = 260.0;
        double minTemp = 16.0, maxTemp = 30.0;

        Arc track = new Arc(cx, cy, r, r, startDeg, totalDeg);
        track.setType(ArcType.OPEN);
        track.setStroke(Color.web("#d0d0d0")); track.setStrokeWidth(16);
        track.setFill(Color.TRANSPARENT); track.setStrokeLineCap(StrokeLineCap.ROUND);

        Arc fill = new Arc(cx, cy, r, r, startDeg, 0);
        fill.setType(ArcType.OPEN);
        fill.setStroke(Color.web("#2d6a4f")); fill.setStrokeWidth(16);
        fill.setFill(Color.TRANSPARENT); fill.setStrokeLineCap(StrokeLineCap.ROUND);

        for (int i = 0; i <= 14; i++) {
            double angRad = Math.toRadians(startDeg + (i / 14.0) * totalDeg);
            Circle tick = new Circle(
                cx + (r - 14) * Math.cos(angRad),
                cy - (r - 14) * Math.sin(angRad), 3.5);
            tick.setFill(Color.web("#b0b0b0"));
            pane.getChildren().add(tick);
        }

        Label tempLabel = new Label("21°C");
        tempLabel.setPrefWidth(size);
        tempLabel.setAlignment(Pos.CENTER);
        tempLabel.setLayoutX(0); tempLabel.setLayoutY(cy - 26);
        tempLabel.setStyle("-fx-font-size:42px; -fx-font-weight:bold; -fx-text-fill:#111;");

        Circle ball = new Circle(11);
        ball.setFill(Color.web("#2d6a4f"));
        ball.setEffect(new DropShadow(8, Color.gray(0, 0.4)));
        
        final boolean[] frozen = { false };
        pane.setOnMousePressed(e  -> frozen[0] = true);
        pane.setOnMouseReleased(e -> frozen[0] = false);

        Runnable update = () -> {
            double temp     = thermostat != null ? thermostat.getTemperature() : 21.0;
            double fraction = Math.max(0, Math.min(1, (temp - minTemp) / (maxTemp - minTemp)));
            double arcLen   = fraction * totalDeg;
            fill.setLength(arcLen);
            double tipRad = Math.toRadians(startDeg + arcLen);
            ball.setCenterX(cx + r * Math.cos(tipRad));
            ball.setCenterY(cy - r * Math.sin(tipRad));
            tempLabel.setText(String.format("%.0f°C", temp));
        };
        update.run();

        if (thermostat != null) {
            thermostat.temperatureProperty().addListener((ob, ov, nv) -> {
                if (!frozen[0]) update.run();
            });
        }

        pane.getChildren().addAll(track, fill, ball, tempLabel);
        return pane;
    }

    // Alarm card
    private Pane alarmCard(double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);
        pane.setStyle("-fx-background-color:WHITE; -fx-background-radius:20;");

        Label title = new Label("Alarm");
        title.setLayoutX(28); title.setLayoutY(10);
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold;");

        alarmSwitch = new ToggleSwitch(false);
        alarmSwitch.setLayoutX(188); alarmSwitch.setLayoutY(12);

        ImageView alarmImg = loadImage("/smarthome/images/alarm.jpg");
        if (alarmImg == null) alarmImg = loadImage("/smarthome/images/alarm.png");
        if (alarmImg != null) {
            alarmImg.setLayoutX(38); alarmImg.setLayoutY(30);
            alarmImg.setFitWidth(160); alarmImg.setFitHeight(140);
            pane.getChildren().add(alarmImg);
        } else {
            Label bell = new Label("🔔");
            bell.setLayoutX(75); bell.setLayoutY(32);
            bell.setStyle("-fx-font-size:80px;");
            pane.getChildren().add(bell);
        }

        Pane bars = new Pane();
        bars.setLayoutX(8); bars.setLayoutY(h - 46);
        bars.setPrefSize(w - 16, 40);
        for (int i = 0; i < 20; i++) {
            Rectangle rect = new Rectangle(i * 11, 0, 7, 8 + Math.random() * 26);
            rect.setFill(Color.web("#e74c3c"));
            rect.setArcWidth(3); rect.setArcHeight(3);
            bars.getChildren().add(rect);
        }

        Timeline barAnim = new Timeline(new KeyFrame(Duration.millis(120), e ->
            bars.getChildren().forEach(node ->
                ((Rectangle) node).setHeight(8 + Math.random() * 26))));
        barAnim.setCycleCount(Timeline.INDEFINITE);

        javafx.scene.Node bellNode = alarmImg != null ? alarmImg
            : pane.getChildren().stream()
                  .filter(n -> n instanceof Label && ((Label) n).getText().equals("🔔"))
                  .findFirst().orElse(null);

        RotateTransition bellAnim = null;
        if (bellNode != null) {
            bellAnim = new RotateTransition(Duration.millis(80), bellNode);
            bellAnim.setFromAngle(-10); bellAnim.setToAngle(10);
            bellAnim.setAutoReverse(true);
            bellAnim.setCycleCount(Animation.INDEFINITE);
        }
        final RotateTransition finalBellAnim = bellAnim;
        final javafx.scene.Node finalBellNode = bellNode;

        alarmSwitch.selectedProperty().addListener((obs, ov, isOn) -> {
            if (isOn) {
                barAnim.play();
                if (finalBellAnim != null) finalBellAnim.play();
            } else {
                barAnim.stop();
                if (finalBellAnim != null) {
                    finalBellAnim.stop();
                    if (finalBellNode != null) finalBellNode.setRotate(0);
                }
            }
        });

        pane.getChildren().addAll(title, alarmSwitch, bars);
        return pane;
    }

// Smart lamp card 
    @SuppressWarnings("null")
    private Pane smartLampCard(double w, double h) {
    Pane pane = new Pane();
    pane.setPrefSize(w, h);
    pane.setStyle("-fx-background-radius:20; -fx-background-color:#B4BBC2;");

    ImageView lampImg = loadImage("/smarthome/images/smart_lamp.jpg");
    if (lampImg == null) lampImg = loadImage("/smarthome/images/smart_lamp.png");
    if (lampImg != null) {
        lampImg.setLayoutX(1); 
        lampImg.setLayoutY(0);
        lampImg.setFitWidth(w - 2); 
        lampImg.setFitHeight(171);
        lampImg.setPreserveRatio(false); 
        lampImg.setSmooth(true);
        Rectangle clip = new Rectangle(w - 2, 171);
        clip.setArcWidth(20); 
        clip.setArcHeight(20);
        lampImg.setClip(clip);
        pane.getChildren().add(lampImg);
    } else {
        Pane fallback = new Pane();
        fallback.setPrefSize(w, 171);
        fallback.setStyle("-fx-background-color:#c8b88a; -fx-background-radius:20 20 0 0;");
        Label ic = new Label("💡"); 
        ic.setLayoutX(100); 
        ic.setLayoutY(60);
        ic.setStyle("-fx-font-size:50px;");
        fallback.getChildren().add(ic);
        pane.getChildren().add(fallback);
    }

    ColorAdjust adjust = new ColorAdjust();
    adjust.setBrightness(-0.7); 
    lampImg.setEffect(adjust);

    Pane overlay = new Pane();
    overlay.setLayoutX(2); 
    overlay.setLayoutY(137);
    overlay.setPrefSize(w - 4, 133);
    overlay.setStyle("-fx-background-radius:20; -fx-background-color:#B4BBC2;");
    overlay.setOpacity(0.42);
    pane.getChildren().add(overlay);

    Label lbl = new Label("Smart Lamp");
    lbl.setLayoutX(16); 
    lbl.setLayoutY(185);
    lbl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#111;");

    ToggleSwitch sw = new ToggleSwitch(bulb != null && bulb.isActive());
    if (bulb != null) sw.selectedProperty().bindBidirectional(bulb.activeProperty());
    sw.setLayoutX(200); 
    sw.setLayoutY(188);

    Label briIcon = new Label("☀");
    briIcon.setLayoutX(16); 
    briIcon.setLayoutY(220);
    briIcon.setStyle("-fx-font-size:13px; -fx-text-fill:#555;");

    Slider slider = new Slider(0, 100, bulb != null ? bulb.getBrightness() : 70);
    slider.setLayoutX(38); 
    slider.setLayoutY(222);
    slider.setPrefWidth(155);
    slider.getStyleClass().add("brightness-slider");

    Label briVal = new Label("70%");
    briVal.setLayoutX(200); 
    briVal.setLayoutY(220);
    briVal.setStyle("-fx-font-size:12px; -fx-text-fill:#555;");

    if (bulb != null) {
        slider.valueProperty().bindBidirectional(bulb.brightnessProperty());
        briVal.textProperty().bind(bulb.brightnessProperty().asString("%.0f%%"));
    }

    if (bulb != null) {
        bulb.brightnessProperty().addListener((ob, ov, nv) -> {
            double pct = nv.doubleValue() / 100.0;
            adjust.setBrightness(-0.7 + pct * 1.1);
        });
    }

    final double[] savedBrightness = { bulb != null ? bulb.getBrightness() : 70 };
    sw.selectedProperty().addListener((obs, ov, isOn) -> {
        if (!isOn) {
            savedBrightness[0] = slider.getValue();
            slider.setValue(0);
        } else {
            slider.setValue(savedBrightness[0]);
        }
    });

    slider.disableProperty().bind(sw.selectedProperty().not());
    pane.opacityProperty().bind(
        javafx.beans.binding.Bindings.when(sw.selectedProperty()).then(1.0).otherwise(0.6));

    pane.getChildren().addAll(lbl, sw, briIcon, slider, briVal);
    return pane;
}

    // Alert history card
    private Pane alertHistoryCard(double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);
        pane.setStyle("-fx-background-color:#C1C1C1; -fx-background-radius:20;");

        Label title = new Label("Alert History");
        title.setLayoutX(20); title.setLayoutY(12);
        title.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#111;");

        ListView<smarthome.model.AlertEntry> list = new ListView<>(controller.getAlertLog());
        list.setLayoutX(10); list.setLayoutY(42);
        list.setPrefSize(w - 20, h - 55);
        list.setStyle("-fx-background-color:transparent; -fx-border-color:transparent;");
        list.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(smarthome.model.AlertEntry item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
                if (!empty) setStyle("-fx-font-size:11px; -fx-text-fill:#8b0000; -fx-background-color:transparent;");
            }
        });

        pane.getChildren().addAll(title, list);
        return pane;
    }

    private Label modeLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-background-color:#d8d8d8; -fx-background-radius:8; -fx-padding:4 12 4 12; -fx-font-size:12px; -fx-text-fill:#555; -fx-font-weight:bold;");
        return lbl;
    }
}