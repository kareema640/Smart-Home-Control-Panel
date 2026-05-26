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

public class BedroomView extends BaseRoomView {

    private Thermostat   thermostat;
    private RGBStrip     rgbStrip;
    private MotionSensor motionSensor;

    // Reference to the alarm switch
    private ToggleSwitch alarmSwitch;

    private static final double CW  = 1334;
    private static final double CH  = 805;
    private static final double PAD = 45;

    public BedroomView(HomeController ctrl, MainView mv) {
        super(ctrl, mv);
        resolveDevices();
        build();
    }

    private void resolveDevices() {
        for (var d : controller.getRooms().get(2).getDevices()) {
            if (d instanceof Thermostat thermostat1)   thermostat   = thermostat1;
            if (d instanceof RGBStrip rGBStrip)     rgbStrip     = rGBStrip;
            if (d instanceof MotionSensor motionSensor1) motionSensor = motionSensor1;
        }
    }

    private void build() {
        buildShell("BedRoom Temp",
            thermostat != null ? thermostat.getTemperature() : 24,
            thermostat != null ? thermostat.getHumidity()    : 75,
            rgbStrip   != null ? rgbStrip.getBrightness()    : 70,
            controller.getEnergyUsed());

        AnchorPane canvas = new AnchorPane();
        canvas.setStyle("-fx-background-color: #F2F2F2;");
        canvas.setPrefSize(CW, CH);

        // Stat cards
        canvas.getChildren().addAll(
            statPane(45,  15, "BedRoom Temp",
                thermostat != null ? String.format("%.0f °C", thermostat.getTemperature()) : "24 °C"),
            statPane(296, 15, "Humidity",
                thermostat != null ? String.format("%.0f %%", thermostat.getHumidity()) : "75 %"),
            statPane(554, 15, "Brightness",
                rgbStrip != null ? String.format("%.0f %%", rgbStrip.getBrightness()) : "70 %"),
            statPane(804, 15, "Energy Used",
                String.format("%.0f Kwh", controller.getEnergyUsed()))
        );

        // Rooms panel 
        VBox roomsPanel = buildRoomsPanel();
        roomsPanel.setPrefSize(259, 298);
        AnchorPane.setLeftAnchor(roomsPanel, 1047.0);
        AnchorPane.setTopAnchor(roomsPanel,  15.0);
        canvas.getChildren().add(roomsPanel);

        // Camera card 
        double camW = 539, camH = 298;
        double camX = PAD, camY = 140;
        Pane cameraPane = cameraCard(camW, camH);
        AnchorPane.setLeftAnchor(cameraPane, camX);
        AnchorPane.setTopAnchor(cameraPane,  camY);
        canvas.getChildren().add(cameraPane);

        // RGB light card 
        double rgbW = camW, rgbH = 390;
        double rgbX = PAD, rgbY = camY + camH + 20;
        Pane rgbPane = rgbLightCard(rgbW, rgbH);
        AnchorPane.setLeftAnchor(rgbPane, rgbX);
        AnchorPane.setTopAnchor(rgbPane,  rgbY);
        canvas.getChildren().add(rgbPane);

        // AC dial card 
        double acW = 430, acH = 415;
        double acX = camX + camW + 20, acY = camY;
        Pane acPane = acDialCard(acW, acH);
        AnchorPane.setLeftAnchor(acPane, acX);
        AnchorPane.setTopAnchor(acPane,  acY);
        canvas.getChildren().add(acPane);

        // Alarm card 
        Pane alarmPane = alarmCard();
        AnchorPane.setLeftAnchor(alarmPane, 1050.0);
        AnchorPane.setTopAnchor(alarmPane,  320.0);
        canvas.getChildren().add(alarmPane);

        // Alert history 
        double alertX = acX;
        double alertY = acY + acH + 20;
        double alertW = CW - PAD - alertX + 20;
        double alertH = CH - alertY - 8 + 50;
        Pane alertPane = alertHistoryCard(alertW, alertH);
        AnchorPane.setLeftAnchor(alertPane, alertX);
        AnchorPane.setTopAnchor(alertPane,  alertY);
        canvas.getChildren().add(alertPane);

        // Attach listeners for alert logging
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
                logAlert("Bedroom: Temperature changed to " + String.format("%.0f°C", nv.doubleValue()));
                if (nv.doubleValue() > 35 && alarmSwitch != null && !alarmSwitch.isSelected()) {
                    Platform.runLater(() -> {
                        alarmSwitch.setSelected(true);
                        logAlert("Bedroom: Alarm triggered — temperature exceeded 35°C");
                    });
                }
            });
            thermostat.humidityProperty().addListener((ob, ov, nv) ->
                logAlert("Bedroom: Humidity changed to " + String.format("%.0f%%", nv.doubleValue())));
            thermostat.activeProperty().addListener((ob, ov, nv) -> {
                logAlert("Bedroom: AC " + (nv ? "turned ON" : "turned OFF"));
                if (nv && alarmSwitch != null && !alarmSwitch.isSelected()) {
                    Platform.runLater(() -> {
                        alarmSwitch.setSelected(true);
                        logAlert("Bedroom: Alarm triggered — AC turned on");
                    });
                }
            });
        }
        if (rgbStrip != null) {
            rgbStrip.activeProperty().addListener((ob, ov, nv) -> {
                logAlert("Bedroom: RGB Light " + (nv ? "turned ON" : "turned OFF"));
                if (nv && alarmSwitch != null && !alarmSwitch.isSelected()) {
                    Platform.runLater(() -> {
                        alarmSwitch.setSelected(true);
                        logAlert("Bedroom: Alarm triggered — RGB light turned on");
                    });
                }
            });
            rgbStrip.brightnessProperty().addListener((ob, ov, nv) ->
                logAlert("Bedroom: RGB brightness changed to " + String.format("%.0f%%", nv.doubleValue())));
        }
        if (motionSensor != null) {
            motionSensor.activeProperty().addListener((ob, ov, nv) -> {
                logAlert("Bedroom: Motion Sensor " + (nv ? "activated" : "deactivated"));
                if (nv && alarmSwitch != null && !alarmSwitch.isSelected()) {
                    Platform.runLater(() -> {
                        alarmSwitch.setSelected(true);
                        logAlert("Bedroom: Alarm triggered — motion detected");
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

    //stat card
    private Pane statPane(double x, double y, String label, String value) {
        Pane pane = new Pane();
        pane.setPrefSize(226, 99);
        pane.setStyle("-fx-background-color:WHITE; -fx-background-radius:20;");
        AnchorPane.setLeftAnchor(pane, x);
        AnchorPane.setTopAnchor(pane,  y);

        Label lbl = new Label(label);
        lbl.setLayoutX(28); lbl.setLayoutY(14);
        lbl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#888;");

        Label val = new Label(value);
        val.setLayoutX(28); val.setLayoutY(46);
        val.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#111;");

        pane.getChildren().addAll(lbl, val);
        return pane;
    }

    // Camera card
    private Pane cameraCard(double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);

        ImageView img = loadImage("/smarthome/images/bedroom.jpg");
        if (img == null) img = loadImage("/smarthome/images/bedroom.png");
        if (img != null) {
            img.setFitWidth(w); img.setFitHeight(h);
            img.setPreserveRatio(false); img.setSmooth(true);
            Rectangle clip = new Rectangle(w, h);
            clip.setArcWidth(20); clip.setArcHeight(20);
            img.setClip(clip);
            pane.getChildren().add(img);
        } else {
            pane.setStyle("-fx-background-color:#c4b09a; -fx-background-radius:20;");
        }

        Pane badge = new Pane();
        badge.setLayoutX(18); badge.setLayoutY(12);
        badge.setPrefSize(65, 22);
        badge.setStyle("-fx-background-color:WHITE; -fx-background-radius:20;");
        Pane dot = new Pane();
        dot.setLayoutX(7); dot.setLayoutY(6);
        dot.setPrefSize(9, 9);
        dot.setStyle("-fx-background-color:RED; -fx-background-radius:10;");
        Label liveLbl = new Label("Live");
        liveLbl.setLayoutX(20); liveLbl.setLayoutY(2);
        liveLbl.setStyle("-fx-font-size:11px; -fx-font-weight:bold;");
        badge.getChildren().addAll(dot, liveLbl);
        pane.getChildren().add(badge);
        return pane;
    }

    // RGB light card 
    private Pane rgbLightCard(double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);
        pane.setStyle("-fx-background-color:#CDB4DB; -fx-background-radius:20; -fx-border-color:white; -fx-border-width:3;");

        Label title = new Label("RGB Light");
        title.setLayoutX(16); title.setLayoutY(14);
        title.setStyle("-fx-font-weight:bold; -fx-font-size:15px;");

        ToggleSwitch mainSw = new ToggleSwitch(rgbStrip != null && rgbStrip.isActive());
        if (rgbStrip != null) mainSw.selectedProperty().bindBidirectional(rgbStrip.activeProperty());
        mainSw.setLayoutX(w - 56); mainSw.setLayoutY(16);

        double imgH = h * 0.44;
        Pane imgPane = new Pane();
        imgPane.setLayoutX(8); imgPane.setLayoutY(44);
        imgPane.setPrefSize(w - 16, imgH);
        imgPane.setStyle("-fx-background-color:linear-gradient(to bottom right,#4a0080,#9b3dc8); -fx-background-radius:12;");

        javafx.scene.effect.ColorAdjust colorFx = new javafx.scene.effect.ColorAdjust();
        colorFx.setBrightness(-0.8);

        ImageView rgbImg = loadImage("/smarthome/images/white.jpeg");
        if (rgbImg == null) rgbImg = loadImage("/smarthome/images/white.png");
        if (rgbImg != null) {
            rgbImg.setFitWidth(w - 16); rgbImg.setFitHeight(imgH);
            rgbImg.setPreserveRatio(false); rgbImg.setSmooth(true);
            Rectangle clip = new Rectangle(w - 16, imgH);
            clip.setArcWidth(12); clip.setArcHeight(12);
            rgbImg.setClip(clip);
            rgbImg.setEffect(colorFx);
            imgPane.getChildren().add(rgbImg);
        } else {
            Label pl = new Label("🔮"); pl.setStyle("-fx-font-size:32px;");
            pl.setLayoutX((w - 16) / 2 - 20); pl.setLayoutY(imgH / 2 - 20);
            imgPane.getChildren().add(pl);
        }

        final ImageView finalImg = rgbImg;
        boolean[] redOn   = { rgbStrip == null || rgbStrip.isRedOn() };
        boolean[] greenOn = { rgbStrip != null && rgbStrip.isGreenOn() };
        boolean[] blueOn  = { rgbStrip == null || rgbStrip.isBlueOn() };

        // Color mixing
        Runnable updateFx = () -> {
            boolean mainOn = mainSw.isSelected();
            if (!mainOn) {
                colorFx.setBrightness(-0.85); colorFx.setHue(0); colorFx.setSaturation(0);
            } else {
                double bri = (finalImg != null)
                    ? (rgbStrip != null ? rgbStrip.getBrightness() : 55) / 100.0 : 0.5;
                colorFx.setBrightness(bri * 0.8 - 0.4);
                double r = redOn[0] ? 1.0 : 0.0;
                double g = greenOn[0] ? 1.0 : 0.0;
                double b = blueOn[0] ? 1.0 : 0.0;
                if (r == 0 && g == 0 && b == 0) {
                    colorFx.setHue(0); colorFx.setSaturation(0);
                } else {
                    Color mixed = Color.color(r, g, b);
                    double hueNorm = mixed.getHue() / 180.0;
                    if (hueNorm > 1.0) hueNorm -= 2.0;
                    colorFx.setHue(hueNorm);
                    colorFx.setSaturation(0.9);
                }
            }
        };

        double rowY = 44 + imgH + 12;
        HBox redRow   = rgbRow("Red Light",   "#e74c3c", rgbStrip != null ? rgbStrip.redOnProperty()   : null, redOn,   updateFx, redOn[0]);
        HBox greenRow = rgbRow("Green Light", "#27ae60", rgbStrip != null ? rgbStrip.greenOnProperty() : null, greenOn, updateFx, greenOn[0]);
        HBox blueRow  = rgbRow("Blue Light",  "#2980b9", rgbStrip != null ? rgbStrip.blueOnProperty()  : null, blueOn,  updateFx, blueOn[0]);
        redRow.setLayoutX(8);   redRow.setLayoutY(rowY);       redRow.setPrefWidth(w - 16);
        greenRow.setLayoutX(8); greenRow.setLayoutY(rowY + 34); greenRow.setPrefWidth(w - 16);
        blueRow.setLayoutX(8);  blueRow.setLayoutY(rowY + 68); blueRow.setPrefWidth(w - 16);

       Slider slider = new Slider(0, 100, rgbStrip != null ? rgbStrip.getBrightness() : 55);
        if (rgbStrip != null) slider.valueProperty().bindBidirectional(rgbStrip.brightnessProperty());
        slider.valueProperty().addListener((ob, ov, nv) -> updateFx.run());

        Label briVal = new Label(String.format("%.0f%%", slider.getValue()));
        if (rgbStrip != null) briVal.textProperty().bind(rgbStrip.brightnessProperty().asString("%.0f%%"));
        briVal.setStyle("-fx-font-size:12px;");

        
        if (rgbStrip != null) {
            
            slider.setDisable(!rgbStrip.isActive());
        }

        final double[] savedRGBBrightness = { rgbStrip != null ? rgbStrip.getBrightness() : 55 };
        
        mainSw.selectedProperty().addListener((obs, ov, isOn) -> {
            if (!isOn) {
                
                savedRGBBrightness[0] = slider.getValue();
                slider.setDisable(true);
                if (rgbStrip != null) {
                    rgbStrip.setBrightness(0.0);
                }
            } else {
                
                slider.setDisable(false);
                if (rgbStrip != null) {
                    rgbStrip.setBrightness(savedRGBBrightness[0] > 0 ? savedRGBBrightness[0] : 55);
                }
            }
        });


        HBox briRow = new HBox(8);
        briRow.setAlignment(Pos.CENTER_LEFT);
        briRow.setStyle("-fx-background-color:#CDB4DB;-fx-background-radius:10;-fx-border-color:black;-fx-border-radius:10;-fx-padding:4 8 4 8;");
        Label briIcon = new Label("☀");
        briIcon.setStyle("-fx-background-color:#CDB4DB;-fx-background-radius:15;-fx-border-color:black;-fx-border-radius:15;-fx-padding:4 8 4 8;");
        HBox.setHgrow(slider, Priority.ALWAYS);
        briRow.getChildren().addAll(briIcon, slider, briVal);
        briRow.setLayoutX(8); briRow.setLayoutY(rowY + 108);
        briRow.setPrefWidth(w - 16);

        mainSw.selectedProperty().addListener((ob, ov, nv) -> updateFx.run());
        updateFx.run();

        pane.getChildren().addAll(title, mainSw, imgPane, redRow, greenRow, blueRow, briRow);
        return pane;
    }

    private HBox rgbRow(String label, String colorHex,
                        javafx.beans.property.BooleanProperty prop,
                        boolean[] stateHolder, Runnable updateFx, boolean initVal) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(8); dot.setFill(Color.web(colorHex));
        Label lbl = new Label(label); lbl.setStyle("-fx-font-size:13px;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        ToggleSwitch tsw = new ToggleSwitch(initVal);
        stateHolder[0] = initVal;
        if (prop != null) tsw.selectedProperty().bindBidirectional(prop);
        tsw.selectedProperty().addListener((ob, ov, nv) -> { stateHolder[0] = nv; updateFx.run(); });
        row.getChildren().addAll(dot, lbl, sp, tsw);
        return row;
    }

    // AC dial card
    private Pane acDialCard(double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);
        pane.setStyle("-fx-background-color:#B4BBC2; -fx-background-radius:20;");

        Label title = new Label("Air Conditioner");
        title.setLayoutX(20); title.setLayoutY(16);
        title.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#111111;");

        ToggleSwitch sw = new ToggleSwitch(thermostat != null && thermostat.isActive());
        if (thermostat != null) sw.selectedProperty().bindBidirectional(thermostat.activeProperty());
        sw.setLayoutX(w - 58); sw.setLayoutY(18);

        double dialSize = 262;
        Pane dialPane = buildArcDial(dialSize);
        dialPane.setLayoutX((w - dialSize) / 2.0);
        dialPane.setLayoutY(52);

        double btnY = 52 + dialSize + 10;
        Button minus = roundBtn("−");
        minus.setLayoutX(w / 2.0 - 60); minus.setLayoutY(btnY);
        Button plus  = roundBtn("+");
        plus.setLayoutX(w / 2.0 + 20);  plus.setLayoutY(btnY);

        if (thermostat != null) {
            minus.setOnAction(e -> thermostat.sendCommand("TEMP_DN"));
            plus.setOnAction(e  -> thermostat.sendCommand("TEMP_UP"));
        }

        double modeY = btnY + 50;
        Label autoLbl  = modeLabel("☀  Auto");
        autoLbl.setLayoutX(w / 2.0 - 100); autoLbl.setLayoutY(modeY);
        Label timerLbl = modeLabel("⏱  1:37 min");
        timerLbl.setLayoutX(w / 2.0 + 12); timerLbl.setLayoutY(modeY);

        // Disable and fade all controls when AC is off
        dialPane.disableProperty().bind(sw.selectedProperty().not());
        minus.disableProperty().bind(sw.selectedProperty().not());
        plus.disableProperty().bind(sw.selectedProperty().not());
        dialPane.opacityProperty().bind(javafx.beans.binding.Bindings.when(sw.selectedProperty()).then(1.0).otherwise(0.5));
        minus.opacityProperty().bind(javafx.beans.binding.Bindings.when(sw.selectedProperty()).then(1.0).otherwise(0.5));
        plus.opacityProperty().bind(javafx.beans.binding.Bindings.when(sw.selectedProperty()).then(1.0).otherwise(0.5));

        pane.getChildren().addAll(title, sw, dialPane, minus, plus, autoLbl, timerLbl);
        return pane;
    }

    // Arc temperature dial
    private Pane buildArcDial(double size) {
        Pane pane = new Pane();
        pane.setPrefSize(size, size);

        double cx = size / 2.0, cy = size / 2.0;
        double r  = size / 2.0 - 16;
        double startDeg = -220.0, totalDeg = 260.0;
        double minTemp = 16.0, maxTemp = 30.0;

        Arc track = new Arc(cx, cy, r, r, startDeg, totalDeg);
        track.setType(ArcType.OPEN);
        track.setStroke(Color.web("#d0d0d0")); track.setStrokeWidth(14);
        track.setFill(Color.TRANSPARENT); track.setStrokeLineCap(StrokeLineCap.ROUND);

        Arc fill = new Arc(cx, cy, r, r, startDeg, 0);
        fill.setType(ArcType.OPEN);
        fill.setStroke(Color.web("#2d6a4f")); fill.setStrokeWidth(14);
        fill.setFill(Color.TRANSPARENT); fill.setStrokeLineCap(StrokeLineCap.ROUND);

        for (int i = 0; i <= 12; i++) {
            double angle = Math.toRadians(startDeg + i * (totalDeg / 12.0));
            Circle tick = new Circle(cx + (r - 12) * Math.cos(angle), cy - (r - 12) * Math.sin(angle), 2.5);
            tick.setFill(Color.web("#aaaaaa"));
            pane.getChildren().add(tick);
        }

        Label tempLabel = new Label("21°C");
        tempLabel.setPrefWidth(size);
        tempLabel.setAlignment(Pos.CENTER);
        tempLabel.setLayoutX(0); tempLabel.setLayoutY(cy - 20);
        tempLabel.setStyle("-fx-font-weight:bold; -fx-font-size:36px; -fx-text-fill:#111;");

        Circle ball = new Circle(10);
        ball.setFill(Color.web("#2d6a4f"));
        ball.setEffect(new DropShadow(7, Color.gray(0, 0.4)));

        final boolean[] frozen = { false };

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

        // Arc color cycles automatically
      
        fill.setStroke(Color.web("#2d6a4f")); 
        ball.setFill(Color.web("#2d6a4f"));

        if (thermostat != null) {
            thermostat.activeProperty().addListener((ob, ov, isActive) -> {
               
                fill.setStroke(Color.web("#2d6a4f")); 
                ball.setFill(Color.web("#2d6a4f"));
            });
        }
     

        pane.getChildren().addAll(track, fill, ball, tempLabel);
        return pane;
    }

    // Alarm card
    private VBox alarmCard() {
        VBox card = new VBox(8);
        card.setPrefWidth(259);
        card.setPrefHeight(230);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color:white; -fx-background-radius:20;");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Alarm");
        title.setStyle("-fx-font-weight:bold; -fx-font-size:22px;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        alarmSwitch = new ToggleSwitch(false);
        header.getChildren().addAll(title, sp, alarmSwitch);

        Label bell = new Label("🔔");
        bell.setStyle("-fx-font-size:60px;");
        bell.setMaxWidth(Double.MAX_VALUE);
        bell.setAlignment(Pos.CENTER);

        // Bell swings 
        javafx.animation.RotateTransition swing =
            new javafx.animation.RotateTransition(javafx.util.Duration.millis(150), bell);
        swing.setFromAngle(-18); swing.setToAngle(18);
        swing.setCycleCount(javafx.animation.Animation.INDEFINITE);
        swing.setAutoReverse(true);

        HBox waves = new HBox(8);
        waves.setAlignment(Pos.CENTER);
        java.util.List<HBox> waveList = new java.util.ArrayList<>();
        java.util.List<javafx.animation.FadeTransition> fadeList = new java.util.ArrayList<>();

        for (int i = 0; i < 3; i++) {
            HBox w = soundWave();
            waveList.add(w);
            waves.getChildren().add(w);

            javafx.animation.FadeTransition fade =
                new javafx.animation.FadeTransition(javafx.util.Duration.millis(400 + i * 120), w);
            fade.setFromValue(0.3); fade.setToValue(1.0);
            fade.setCycleCount(javafx.animation.Animation.INDEFINITE);
            fade.setAutoReverse(true);
            fadeList.add(fade);
        }

        alarmSwitch.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                swing.play();
                fadeList.forEach(javafx.animation.FadeTransition::play);
                waveList.forEach(w -> w.setOpacity(1.0));
            } else {
                swing.stop();
                bell.setRotate(0);
                fadeList.forEach(javafx.animation.FadeTransition::stop);
                waveList.forEach(w -> w.setOpacity(0.25));
            }
        });

        waveList.forEach(w -> w.setOpacity(0.25));

        card.getChildren().addAll(header, bell, waves);
        return card;
    }

    // Sound wave graphic
    private HBox soundWave() {
        HBox wave = new HBox(2);
        wave.setAlignment(Pos.CENTER);
        double[] heights = { 8, 14, 20, 14, 8 };
        for (double h : heights) {
            Rectangle r = new Rectangle(3, h);
            r.setFill(Color.web("#e74c3c"));
            r.setArcWidth(2); r.setArcHeight(2);
            wave.getChildren().add(r);
        }
        return wave;
    }

    // Alert history card
    private Pane alertHistoryCard(double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);
        pane.setStyle("-fx-background-color:#C1C1C1; -fx-background-radius:20;");

        Label title = new Label("Alert History");
        title.setLayoutX(18); title.setLayoutY(10);
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#111;");

        ListView<smarthome.model.AlertEntry> list = new ListView<>(controller.getAlertLog());
        list.setLayoutX(8); list.setLayoutY(36);
        list.setPrefSize(w - 16, h - 46);
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
        btn.setPrefSize(40, 40);
        btn.setStyle("-fx-background-color:#dcdcdc; -fx-background-radius:20; -fx-font-size:20px; -fx-font-weight:bold; -fx-cursor:hand;");
        return btn;
    }

    private Label modeLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-background-color:#d8d8d8; -fx-background-radius:8; -fx-padding:4 10 4 10; -fx-font-weight:bold; -fx-font-size:11px; -fx-text-fill:#555555;");
        return lbl;
    }

    // Rooms panel override
    @Override
    protected VBox buildRoomsPanel() {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("rooms-panel");
        panel.setPadding(new Insets(14));

        Label title = new Label("Rooms");
        title.getStyleClass().add("section-title");

        VBox list = new VBox(4);
        java.util.List<HBox>  rows       = new java.util.ArrayList<>();
        java.util.List<Label> indicators = new java.util.ArrayList<>();

        String currentRoom = mainView != null ? mainView.getCurrentRoom() : "";

        for (smarthome.model.Room r : controller.getRooms()) {
            boolean isSelected = r.getName().equals(currentRoom);

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 10, 8, 10));
            row.getStyleClass().add(isSelected ? "room-item-selected" : "room-item");

            Label ic  = new Label(r.getIcon()); ic.setStyle("-fx-font-size:14px;");
            VBox info = new VBox(1);
            Label nm  = new Label(r.getName()); nm.getStyleClass().add("room-name");
            Label cnt = new Label(r.getDeviceCount() + " devices"); cnt.getStyleClass().add("room-count");
            info.getChildren().addAll(nm, cnt);

            Region sp    = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Label check  = new Label(isSelected ? "✓" : "›");
            check.setStyle(isSelected
                ? "-fx-text-fill:#2d6a4f; -fx-font-weight:bold;"
                : "-fx-text-fill:#aaaaaa;");

            indicators.add(check);
            row.getChildren().addAll(ic, info, sp, check);
            rows.add(row);

            final String roomName = r.getName();
            final int    idx      = rows.size() - 1;

            row.setOnMouseClicked(e -> {
                for (int i = 0; i < rows.size(); i++) {
                    boolean sel = (i == idx);
                    rows.get(i).getStyleClass().setAll(sel ? "room-item-selected" : "room-item");
                    indicators.get(i).setText(sel ? "✓" : "›");
                    indicators.get(i).setStyle(sel
                        ? "-fx-text-fill:#2d6a4f; -fx-font-weight:bold;"
                        : "-fx-text-fill:#aaaaaa;");
                }
                if (mainView != null) mainView.switchRoom(roomName);
            });
            row.setStyle("-fx-cursor:hand;");
            list.getChildren().add(row);
        }

        Button addRoom = new Button("+ Add room");
        addRoom.getStyleClass().add("add-room-btn");
        addRoom.setMaxWidth(Double.MAX_VALUE);

        panel.getChildren().addAll(title, list, addRoom);
        return panel;
    }
}