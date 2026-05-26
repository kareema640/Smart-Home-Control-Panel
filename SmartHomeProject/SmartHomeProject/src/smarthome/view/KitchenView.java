package smarthome.view;

import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import smarthome.controller.HomeController;
import smarthome.model.devices.*;

public class KitchenView extends BaseRoomView {

    private AirPurifier  purifier;
    private Thermostat   thermostat;
    private SmartBulb    bulb;

    // Field so attachDeviceListeners() can reach the alarm switch
    private ToggleSwitch alarmSwitch;

    public KitchenView(HomeController ctrl, MainView mv) {
        super(ctrl, mv);
        resolveDevices();
        build();
    }

    private void resolveDevices() {
        for (var d : controller.getRooms().get(1).getDevices()) {
            if (d instanceof AirPurifier airPurifier) purifier   = airPurifier;
            if (d instanceof Thermostat thermostat1)  thermostat = thermostat1;
            if (d instanceof SmartBulb smartBulb)   bulb       = smartBulb;
        }
    }

    private void build() {
        buildShell("Kitchen Temp",
            thermostat != null ? thermostat.getTemperature() : 24,
            thermostat != null ? thermostat.getHumidity()    : 75,
            bulb       != null ? bulb.getBrightness()        : 36,
            controller.getEnergyUsed());

        AnchorPane canvas = new AnchorPane();
        canvas.setStyle("-fx-background-color: #F2F2F2;");
        canvas.setPrefSize(1334, 870);

        // Stat cards 
        canvas.getChildren().addAll(
            statPane(45,  15, "Kitchen Temp",
                thermostat != null ? String.format("%.0f °C", thermostat.getTemperature()) : "24 °C"),
            statPane(296, 15, "Humidity",
                thermostat != null ? String.format("%.0f %%", thermostat.getHumidity()) : "75 %"),
            statPane(554, 15, "Brightness",
                bulb != null ? String.format("%.0f %%", bulb.getBrightness()) : "36 %"),
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

        // Temperature card 
        Pane tempPane = tempCard(274, 386);
        AnchorPane.setLeftAnchor(tempPane, 45.0);
        AnchorPane.setTopAnchor(tempPane,  466.0);
        canvas.getChildren().add(tempPane);

        // Standards card 
        Pane stdPane = standardsCard(235, 152);
        AnchorPane.setLeftAnchor(stdPane, 343.0);
        AnchorPane.setTopAnchor(stdPane,  463.0);
        canvas.getChildren().add(stdPane);

        // Air purifier 
        Pane purifierPane = airPurifierCard(415, 463);
        AnchorPane.setLeftAnchor(purifierPane, 610.0);
        AnchorPane.setTopAnchor(purifierPane,  144.0);
        canvas.getChildren().add(purifierPane);

        // Alarm card
        Pane alarmPane = alarmCard(242, 228);
        AnchorPane.setLeftAnchor(alarmPane, 343.0);
        AnchorPane.setTopAnchor(alarmPane,  627.0);
        canvas.getChildren().add(alarmPane);

        // ES Light 
        Pane esPane = esLightCard(251, 270);
        AnchorPane.setLeftAnchor(esPane, 1052.0);
        AnchorPane.setTopAnchor(esPane,  329.0);
        canvas.getChildren().add(esPane);

        // Alert history 
        Pane alertPane = alertHistoryCard(730, 243);
        AnchorPane.setLeftAnchor(alertPane, 604.0);
        AnchorPane.setTopAnchor(alertPane,  626.0);
        canvas.getChildren().add(alertPane);

        // Called AFTER alarmCard()
        attachDeviceListeners();

        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setFitToWidth(false);
        scroll.setFitToHeight(false);
        scroll.setStyle("-fx-background-color:#F2F2F2; -fx-background:#F2F2F2;");
        root.setCenter(scroll);
        root.setRight(null);
    }

    // Registers listeners on all devices
    private void attachDeviceListeners() {
        if (thermostat != null) {
            thermostat.temperatureProperty().addListener((ob, ov, nv) -> {
                logAlert("Kitchen: Temperature changed to " + String.format("%.0f°C", nv.doubleValue()));
                if (nv.doubleValue() > 35 && alarmSwitch != null && !alarmSwitch.isSelected()) {
                    Platform.runLater(() -> {
                        alarmSwitch.setSelected(true);
                        logAlert("Kitchen: Alarm triggered — temperature exceeded 35°C");
                    });
                }
            });
            thermostat.humidityProperty().addListener((ob, ov, nv) -> {
    logAlert("Kitchen: Humidity changed to " + String.format("%.0f%%", nv.doubleValue()));

    // Trigger alarm
    if (nv.doubleValue() > 85 && alarmSwitch != null && !alarmSwitch.isSelected()) {
        Platform.runLater(() -> {
            alarmSwitch.setSelected(true);
            logAlert("Kitchen: Alarm triggered — humidity exceeded 85%");
        });
    }

    if (nv.doubleValue() > 85 && purifier != null && !purifier.isActive()) {
        Platform.runLater(() -> {
            purifier.setActive(true);
            logAlert("Kitchen: Air Purifier auto-activated — humidity exceeded 85%");
            });
        }
    });
            thermostat.activeProperty().addListener((ob, ov, nv) -> {
                logAlert("Kitchen: Thermostat " + (nv ? "turned ON" : "turned OFF"));
                if (nv && alarmSwitch != null && !alarmSwitch.isSelected()) {
                    Platform.runLater(() -> {
                        alarmSwitch.setSelected(true);
                        logAlert("Kitchen: Alarm triggered — thermostat turned on");
                    });
                }
            });
        }
        if (bulb != null) {
            bulb.activeProperty().addListener((ob, ov, nv) -> {
                logAlert("Kitchen: ES Light " + (nv ? "turned ON" : "turned OFF"));
                if (nv && alarmSwitch != null && !alarmSwitch.isSelected()) {
                    Platform.runLater(() -> {
                        alarmSwitch.setSelected(true);
                        logAlert("Kitchen: Alarm triggered — ES Light turned on");
                    });
                }
            });
            bulb.brightnessProperty().addListener((ob, ov, nv) ->
                logAlert("Kitchen: ES Light brightness changed to " + String.format("%.0f%%", nv.doubleValue())));
        }
        if (purifier != null) {
            purifier.activeProperty().addListener((ob, ov, nv) -> {
                logAlert("Kitchen: Air Purifier " + (nv ? "turned ON" : "turned OFF"));
                if (nv && alarmSwitch != null && !alarmSwitch.isSelected()) {
                    Platform.runLater(() -> {
                        alarmSwitch.setSelected(true);
                        logAlert("Kitchen: Alarm triggered — Air Purifier turned on");
                    });
                }
            });
            purifier.purityProperty().addListener((ob, ov, nv) ->
                logAlert("Kitchen: Air purity changed to " + String.format("%.0f%%", nv.doubleValue())));
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

        ImageView img = loadImage("/smarthome/images/kitchen.jpg");
        if (img == null) img = loadImage("/smarthome/images/kitchen.png");
        if (img != null) {
            img.setFitWidth(w); img.setFitHeight(h);
            img.setPreserveRatio(false); img.setSmooth(true);
            Rectangle clip = new Rectangle(w, h);
            clip.setArcWidth(20); clip.setArcHeight(20);
            img.setClip(clip);
            pane.getChildren().add(img);
        } else {
            pane.setStyle("-fx-background-color:#d0b080; -fx-background-radius:20;");
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

    // Temperature card
    private Pane tempCard(double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);
        pane.setStyle("-fx-background-color:#CDCBCB; -fx-background-radius:20;");

        Label title = new Label("Temperature");
        title.setLayoutX(19); title.setLayoutY(14);
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#333333;");

        StackPane dial = buildTempDial();
        dial.setLayoutX((w - 160) / 2.0);
        dial.setLayoutY(44);

        String activeStyle = "-fx-background-color:#ffffff; -fx-background-radius:10; -fx-text-fill:#27ae60; -fx-font-weight:bold;";
        String normalStyle = "-fx-background-color:#e0e0e0; -fx-background-radius:10; -fx-text-fill:#555555;";

        Button heatBtn = new Button("♨\nHeating");
        heatBtn.setPrefSize(90, 52);
        heatBtn.setStyle(thermostat != null && thermostat.isHeating() ? activeStyle : normalStyle);

        Button coolBtn = new Button("❄\nCooling");
        coolBtn.setPrefSize(90, 52);
        coolBtn.setStyle(thermostat != null && !thermostat.isHeating() ? activeStyle : normalStyle);

        if (thermostat != null) {
            heatBtn.setOnAction(e -> {
                thermostat.setHeating(true);
                heatBtn.setStyle(activeStyle); coolBtn.setStyle(normalStyle);
            });
            coolBtn.setOnAction(e -> {
                thermostat.setHeating(false);
                coolBtn.setStyle(activeStyle); heatBtn.setStyle(normalStyle);
            });
        }

        double modeY = 44 + 160 + 12;
        heatBtn.setLayoutX(w / 2.0 - 95); heatBtn.setLayoutY(modeY);
        coolBtn.setLayoutX(w / 2.0 + 5);  coolBtn.setLayoutY(modeY);

        double humY = modeY + 64;
        Label humLbl = new Label("Humidity");
        humLbl.setLayoutX(19); humLbl.setLayoutY(humY);
        humLbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#333333;");

        Label humVal = new Label("75 %");
        humVal.setLayoutX(w - 55); humVal.setLayoutY(humY);
        humVal.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#333333;");

        Slider humSlider = new Slider(0, 100, 75);
        humSlider.getStyleClass().add("brightness-slider");
        humSlider.setLayoutX(19); humSlider.setLayoutY(humY + 22);
        humSlider.setPrefWidth(w - 38);

        if (thermostat != null) {
            humSlider.valueProperty().bindBidirectional(thermostat.humidityProperty());
            humVal.textProperty().bind(thermostat.humidityProperty().asString("%.0f %%"));
        }

        double aqY = humY + 56;
        Label aqLbl = new Label("Air Quality");
        aqLbl.setLayoutX(19); aqLbl.setLayoutY(aqY);
        aqLbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#333333;");

        Label aqVal = new Label("Good");
        aqVal.setLayoutX(w - 60); aqVal.setLayoutY(aqY);
        aqVal.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#27ae60;");

        if (thermostat != null) {
            thermostat.airQualityProperty().addListener((ob, ov, nv) ->
                Platform.runLater(() -> {
                    aqVal.setText(nv);
                    String color = "Good".equalsIgnoreCase(nv) ? "#27ae60"
                                 : "Moderate".equalsIgnoreCase(nv) ? "#f39c12" : "#e74c3c";
                    aqVal.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:" + color + ";");
                }));
        }

        pane.getChildren().addAll(title, dial, heatBtn, coolBtn, humLbl, humVal, humSlider, aqLbl, aqVal);
        return pane;
    }

    private StackPane buildTempDial() {
        StackPane stack = new StackPane();
        stack.setPrefSize(160, 160);

        Circle outer = new Circle(70);
        outer.setFill(Color.web("#e8e8e8"));
        outer.setStroke(Color.web("#bbbbbb")); outer.setStrokeWidth(2);

        Circle inner = new Circle(52);
        inner.setFill(Color.web("#f5f5f5"));

        VBox innerBox = new VBox(2); innerBox.setAlignment(Pos.CENTER);
        Label modeLbl = new Label("HEATING");
        modeLbl.setStyle("-fx-font-size:9px; -fx-text-fill:#888;");
        Label tempVal = new Label("22");
        tempVal.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:#222;");

        if (thermostat != null) {
            tempVal.setText(String.format("%.0f", thermostat.getTemperature()));
            thermostat.temperatureProperty().addListener((ob, ov, nv) ->
                tempVal.setText(String.format("%.0f", nv.doubleValue())));
            modeLbl.textProperty().bind(
                javafx.beans.binding.Bindings.when(thermostat.heatingProperty())
                    .then("HEATING").otherwise("COOLING"));
        }
        innerBox.getChildren().addAll(modeLbl, tempVal);
        stack.getChildren().addAll(outer, inner, innerBox);

        final double[] lastY = { 0 };
        stack.setOnMousePressed(e -> lastY[0] = e.getSceneY());
        stack.setOnMouseDragged(e -> {
            if (thermostat != null) {
                double delta = lastY[0] - e.getSceneY();
                if (Math.abs(delta) > 5) {
                    double t = thermostat.getTemperature();
                    if (delta > 0 && t < 30) thermostat.setTemperature(t + 1);
                    else if (delta < 0 && t > 16) thermostat.setTemperature(t - 1);
                    lastY[0] = e.getSceneY();
                }
            }
        });
        stack.setOnScroll(e -> {
            if (thermostat != null) {
                double t = thermostat.getTemperature();
                if (e.getDeltaY() > 0 && t < 30) thermostat.setTemperature(t + 1);
                else if (e.getDeltaY() < 0 && t > 16) thermostat.setTemperature(t - 1);
            }
        });
        return stack;
    }

    // Standards card
    private Pane standardsCard(double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);
        pane.setStyle("-fx-background-color:#CDCBCB; -fx-background-radius:20;");

        Label title = new Label("STANDARDS");
        title.setLayoutX(23); title.setLayoutY(9);
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#333333;");

        Label warn = new Label("⚠️");
        warn.setLayoutX(w - 34); warn.setLayoutY(9);
        warn.setStyle("-fx-font-size:14px;");

        Label r1 = new Label("Warning if temp > 35 °C");
        r1.setLayoutX(24); r1.setLayoutY(46);
        r1.setStyle("-fx-font-size:12px; -fx-text-fill:#777777;");

        Label r2 = new Label("Warning if Humidity > 85%");
        r2.setLayoutX(24); r2.setLayoutY(74);
        r2.setStyle("-fx-font-size:12px; -fx-text-fill:#777777;");

        pane.getChildren().addAll(title, warn, r1, r2);
        return pane;
    }

    // Air purifier card
    private Pane airPurifierCard(double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);
        pane.setStyle("-fx-background-color:#B4BBC2; -fx-background-radius:20;");

        Label title = new Label("Air Purifier");
        title.setLayoutX(22); title.setLayoutY(18);
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#111111;");

        ToggleSwitch sw = new ToggleSwitch(purifier != null && purifier.isActive());
        if (purifier != null) sw.selectedProperty().bindBidirectional(purifier.activeProperty());
        sw.setLayoutX(w - 58); sw.setLayoutY(20);

        double dialSize = 300;
        Pane dialPane = buildPurityDialArc(dialSize);
        dialPane.setLayoutX((w - dialSize) / 2.0);
        dialPane.setLayoutY(60);

        double btnY = 60 + dialSize + 12;
        Button minus = roundBtn("−");
        minus.setLayoutX(w / 2.0 - 72); minus.setLayoutY(btnY);
        Button plus  = roundBtn("+");
        plus.setLayoutX(w / 2.0 + 26);  plus.setLayoutY(btnY);

        if (purifier != null) {
            plus.setOnAction(e -> {
                double cur = purifier.getPurity();
                if (cur < 100) purifier.setPurity(Math.min(100, cur + 5));
            });
            minus.setOnAction(e -> {
                double cur = purifier.getPurity();
                if (cur > 0) purifier.setPurity(Math.max(0, cur - 5));
            });
        }

        double modeY = btnY + 58;
        Label autoLbl = modeLabel("☀  Auto");
        autoLbl.setLayoutX(w / 2.0 - 125); autoLbl.setLayoutY(modeY);
        Label speedVal = modeLabel("medium");
        if (purifier != null) speedVal.textProperty().bind(purifier.speedProperty());
        speedVal.setLayoutX(w / 2.0 + 18); speedVal.setLayoutY(modeY);

        dialPane.disableProperty().bind(sw.selectedProperty().not());
        minus.disableProperty().bind(sw.selectedProperty().not());
        plus.disableProperty().bind(sw.selectedProperty().not());
        dialPane.opacityProperty().bind(javafx.beans.binding.Bindings.when(sw.selectedProperty()).then(1.0).otherwise(0.5));
        minus.opacityProperty().bind(javafx.beans.binding.Bindings.when(sw.selectedProperty()).then(1.0).otherwise(0.5));
        plus.opacityProperty().bind(javafx.beans.binding.Bindings.when(sw.selectedProperty()).then(1.0).otherwise(0.5));

        pane.getChildren().addAll(title, sw, dialPane, minus, plus, autoLbl, speedVal);
        return pane;
    }

    // Arc purity dial
    private Pane buildPurityDialArc(double size) {
        Pane pane = new Pane();
        pane.setPrefSize(size, size);

        double cx = size / 2.0, cy = size / 2.0;
        double r  = size / 2.0 - 20;
        double startDeg = -220.0, totalDeg = 260.0;

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
            double tx = cx + (r - 14) * Math.cos(angRad);
            double ty = cy - (r - 14) * Math.sin(angRad);
            Circle tick = new Circle(tx, ty, 3.5);
            tick.setFill(Color.web("#b0b0b0"));
            pane.getChildren().add(tick);
        }

        Label valLbl = new Label("64%");
        valLbl.setPrefWidth(size);
        valLbl.setAlignment(Pos.CENTER);
        valLbl.setLayoutX(0); valLbl.setLayoutY(cy - 26);
        valLbl.setStyle("-fx-font-size:42px; -fx-font-weight:bold; -fx-text-fill:#111;");

        Circle ball = new Circle(11);
        ball.setFill(Color.web("#2d6a4f"));
        ball.setEffect(new DropShadow(8, Color.gray(0, 0.4)));

        final boolean[] frozen = { false };

        Runnable update = () -> {
            double purity   = purifier != null ? purifier.getPurity() : 64.0;
            double fraction = Math.max(0, Math.min(1, purity / 100.0));
            double arcLen   = fraction * totalDeg;
            fill.setLength(arcLen);
            double tipRad = Math.toRadians(startDeg + arcLen);
            ball.setCenterX(cx + r * Math.cos(tipRad));
            ball.setCenterY(cy - r * Math.sin(tipRad));
            valLbl.setText(String.format("%.0f%%", purity));
        };
        update.run();

        if (purifier != null) {
            purifier.purityProperty().addListener((ob, ov, nv) -> {
                if (!frozen[0]) update.run();
            });
        }

      
        if (purifier != null) {
            purifier.activeProperty().addListener((ob, ov, isActive) -> {
                if (isActive) {
                    fill.setStroke(Color.web("#2d6a4f"));
                    ball.setFill(Color.web("#2d6a4f"));
                } else {
                   
                    fill.setStroke(Color.web("#2d6a4f"));
                    ball.setFill(Color.web("#2d6a4f"));
                }
            });
        }
       

        pane.getChildren().addAll(track, fill, ball, valLbl);
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
        ToggleSwitch sw = alarmSwitch;
        sw.setLayoutX(188); sw.setLayoutY(12);

        Label bell = new Label("🔔");
        bell.setLayoutX(75); bell.setLayoutY(32);
        bell.setStyle("-fx-font-size:80px;");

        HBox bars = new HBox(2);
        bars.setAlignment(Pos.CENTER);
        java.util.List<javafx.animation.ScaleTransition> scaleAnimations = new java.util.ArrayList<>();

        for (int i = 0; i < 16; i++) {
            Rectangle r = new Rectangle(3, 18);
            r.setFill(Color.web("#e74c3c"));
            r.setArcWidth(2); r.setArcHeight(2);
            bars.getChildren().add(r);

            javafx.animation.ScaleTransition scale =
                new javafx.animation.ScaleTransition(
                    javafx.util.Duration.millis(200 + Math.random() * 200), r);
            scale.setToY(0.3 + Math.random() * 0.7);
            scale.setAutoReverse(true);
            scale.setCycleCount(javafx.animation.Animation.INDEFINITE);
            scaleAnimations.add(scale);
        }

        bars.setLayoutX(8);
        bars.setLayoutY(h - 46);
        bars.setPrefWidth(w - 16);

        javafx.animation.RotateTransition bellAnim =
            new javafx.animation.RotateTransition(javafx.util.Duration.millis(80), bell);
        bellAnim.setFromAngle(-10); bellAnim.setToAngle(10);
        bellAnim.setAutoReverse(true);
        bellAnim.setCycleCount(javafx.animation.Animation.INDEFINITE);

        sw.selectedProperty().addListener((obs, ov, isOn) -> {
            if (isOn) {
                bellAnim.play();
                for (var anim : scaleAnimations) anim.play();
            } else {
                bellAnim.stop();
                bell.setRotate(0);
                for (var anim : scaleAnimations) {
                    anim.stop();
                    anim.getNode().setScaleY(1.0);
                }
            }
        });

        pane.getChildren().addAll(title, sw, bell, bars);
        return pane;
    }

    // ES Light card
    private Pane esLightCard(double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);
        pane.setStyle("-fx-background-radius:20; -fx-background-color:#B4BBC2;");

        ImageView lampImg = loadImage("/smarthome/images/ES_LIGHT.jpg");
        if (lampImg == null) lampImg = loadImage("/smarthome/images/smart_lamp.jpg");
        if (lampImg == null) lampImg = loadImage("/smarthome/images/smart_lamp.png");

        if (lampImg != null) {
            lampImg.setLayoutX(1); lampImg.setLayoutY(0);
            lampImg.setFitWidth(w - 2); lampImg.setFitHeight(171);
            lampImg.setPreserveRatio(false); lampImg.setSmooth(true);
            Rectangle clip = new Rectangle(w - 2, 171);
            clip.setArcWidth(20); clip.setArcHeight(20);
            lampImg.setClip(clip);

            javafx.scene.effect.ColorAdjust colorAdjust = new javafx.scene.effect.ColorAdjust();
            lampImg.setEffect(colorAdjust);
            if (bulb != null) {
                bulb.brightnessProperty().addListener((ob, ov, nv) -> {
                    double pct = nv.doubleValue() / 100.0;
                    colorAdjust.setBrightness(-0.7 + (pct * 1.1));
                });
            }
            pane.getChildren().add(lampImg);
        } else {
            Pane fallback = new Pane();
            fallback.setPrefSize(w, 171);
            fallback.setStyle("-fx-background-color:#c8b88a; -fx-background-radius:20 20 0 0;");
            Label ic = new Label("💡"); ic.setLayoutX(100); ic.setLayoutY(60);
            ic.setStyle("-fx-font-size:50px;");
            fallback.getChildren().add(ic);
            pane.getChildren().add(fallback);
        }

        Pane overlay = new Pane();
        overlay.setLayoutX(2); overlay.setLayoutY(137);
        overlay.setPrefSize(w - 4, 133);
        overlay.setStyle("-fx-background-radius:20; -fx-background-color:#B4BBC2;");
        overlay.setOpacity(0.42);
        pane.getChildren().add(overlay);

        Label lbl = new Label("ES Light");
        lbl.setLayoutX(16); lbl.setLayoutY(185);
        lbl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#111;");

        ToggleSwitch sw = new ToggleSwitch(bulb != null && bulb.isActive());
        if (bulb != null) sw.selectedProperty().bindBidirectional(bulb.activeProperty());
        sw.setLayoutX(200); sw.setLayoutY(188);

        Label briIcon = new Label("☀");
        briIcon.setLayoutX(16); briIcon.setLayoutY(222);
        briIcon.setStyle("-fx-font-size:13px; -fx-text-fill:#555;");

        Slider slider = new Slider(0, 100, bulb != null ? bulb.getBrightness() : 36);
        slider.setLayoutX(38); slider.setLayoutY(224);
        slider.setPrefWidth(155);
        slider.getStyleClass().add("brightness-slider");

        Label briVal = new Label("36%");
        briVal.setLayoutX(200); briVal.setLayoutY(222);
        briVal.setStyle("-fx-font-size:12px; -fx-text-fill:#555;");

        if (bulb != null) {
            slider.valueProperty().bindBidirectional(bulb.brightnessProperty());
            briVal.textProperty().bind(bulb.brightnessProperty().asString("%.0f%%"));
        }

        final double[] savedBrightness = { bulb != null ? bulb.getBrightness() : 36 };
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

    private Button roundBtn(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(46, 46);
        btn.setStyle("-fx-background-color:#dcdcdc; -fx-background-radius:23; -fx-font-size:22px; -fx-font-weight:bold; -fx-cursor:hand;");
        return btn;
    }

    private Label modeLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-background-color:#d8d8d8; -fx-background-radius:8; -fx-padding:4 12 4 12; -fx-font-size:12px; -fx-text-fill:#555; -fx-font-weight:bold;");
        return lbl;
    }
}