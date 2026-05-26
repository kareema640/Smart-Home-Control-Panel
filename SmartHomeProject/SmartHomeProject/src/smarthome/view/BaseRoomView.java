package smarthome.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import smarthome.controller.HomeController;
import smarthome.model.Room;

public abstract class BaseRoomView {

    protected HomeController controller;
    protected MainView       mainView;
    protected BorderPane     root;

    private final double roomsPanelX = 1047.0;
    private final double roomsPanelY = 15.0;

    public BaseRoomView(HomeController ctrl, MainView mv) {
        this.controller = ctrl;
        this.mainView   = mv;
    }

    // Builds the root shell with the top bar
    protected BorderPane buildShell(String statLabel,
                                    double temp, double hum,
                                    double bri,  double energy) {
        root = new BorderPane();
        root.setPadding(new Insets(0));
        root.getStyleClass().add("room-shell");
        root.setTop(buildTopBar());
        return root;
    }

    // Top bar
    private HBox buildTopBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.getStyleClass().add("top-bar");

        Label title = new Label("Welcome Smart Home!");
        title.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("Search...");
        search.getStyleClass().add("search-field");
        search.setPrefWidth(190);

        Label bell   = new Label("🔔");
        bell.getStyleClass().add("top-icon");
        Label avatar = new Label("👤");
        avatar.getStyleClass().add("top-icon");

        bar.getChildren().addAll(title, spacer, search, bell, avatar);
        return bar;
    }

    // Small stat card 
    protected HBox statCard(String label, String value) {
        HBox card = new HBox();
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(150);
        card.setPadding(new Insets(10, 14, 10, 14));

        VBox inner = new VBox(3);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        Label val = new Label(value);
        val.getStyleClass().add("stat-value");
        inner.getChildren().addAll(lbl, val);
        card.getChildren().add(inner);
        return card;
    }

    private AnchorPane findCanvas() {
        if (root == null) return null;
        javafx.scene.Node center = root.getCenter();
        if (center instanceof ScrollPane sp) {
            javafx.scene.Node content = sp.getContent();
            if (content instanceof AnchorPane ap) return ap;
        }
        return null;
    }

    public void refreshRoomsPanel() {
        AnchorPane canvas = findCanvas();
        if (canvas == null) return;

        VBox oldPanel = null;
        for (javafx.scene.Node node : canvas.getChildren()) {
            if (node instanceof VBox vb && vb.getStyleClass().contains("rooms-panel")) {
                oldPanel = vb;
                break;
            }
        }
        if (oldPanel != null) canvas.getChildren().remove(oldPanel);

        // Build a fresh panel 
        VBox newPanel = buildRoomsPanel();
        newPanel.setPrefSize(259, 298);
        AnchorPane.setLeftAnchor(newPanel, roomsPanelX);
        AnchorPane.setTopAnchor(newPanel,  roomsPanelY);
        canvas.getChildren().add(newPanel);
    }

    // Rooms navigation panel 
    protected VBox buildRoomsPanel() {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("rooms-panel");
        panel.setPadding(new Insets(14));
        panel.setPrefWidth(200);

        Label title = new Label("Rooms");
        title.getStyleClass().add("section-title");

        VBox list = new VBox(4);

        java.util.List<HBox>  rows       = new java.util.ArrayList<>();
        java.util.List<Label> indicators = new java.util.ArrayList<>();

        // Read current room 
        String currentRoom = mainView != null ? mainView.getCurrentRoom() : "";

        for (Room r : controller.getRooms()) {
            boolean isSelected = r.getName().equals(currentRoom);

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 10, 8, 10));
            row.getStyleClass().add(isSelected ? "room-item-selected" : "room-item");

            Label ic = new Label(r.getIcon());
            ic.setStyle("-fx-font-size: 14px;");

            VBox info = new VBox(1);
            Label nm  = new Label(r.getName());
            nm.getStyleClass().add("room-name");
            Label cnt = new Label(r.getDeviceCount() + " devices");
            cnt.getStyleClass().add("room-count");
            info.getChildren().addAll(nm, cnt);

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Label check = new Label(isSelected ? "✓" : "›");
            check.setStyle(isSelected
                ? "-fx-text-fill: #2d6a4f; -fx-font-weight: bold;"
                : "-fx-text-fill: #aaaaaa;");

            row.getChildren().addAll(ic, info, sp, check);
            rows.add(row);
            indicators.add(check);

            final String roomName = r.getName();
            final int    idx      = rows.size() - 1;

            row.setOnMouseClicked(e -> {
                for (int i = 0; i < rows.size(); i++) {
                    boolean sel = (i == idx);
                    rows.get(i).getStyleClass().setAll(sel ? "room-item-selected" : "room-item");
                    indicators.get(i).setText(sel ? "✓" : "›");
                    indicators.get(i).setStyle(sel
                        ? "-fx-text-fill: #2d6a4f; -fx-font-weight: bold;"
                        : "-fx-text-fill: #aaaaaa;");
                }
                if (mainView != null) mainView.switchRoom(roomName);
            });

            row.setStyle("-fx-cursor: hand;");
            list.getChildren().add(row);
        }

        Button addRoom = new Button("+ Add room");
        addRoom.getStyleClass().add("add-room-btn");
        addRoom.setMaxWidth(Double.MAX_VALUE);

        panel.getChildren().addAll(title, list, addRoom);
        return panel;
    }

    // Alert panel 
    protected VBox buildAlertPanel() {
        VBox card = new VBox(8);
        card.getStyleClass().add("alert-panel");
        card.setPadding(new Insets(12));
        card.setPrefWidth(200);

        Label title = new Label("Alert History");
        title.getStyleClass().add("card-title");

        ListView<smarthome.model.AlertEntry> list =
                new ListView<>(controller.getAlertLog());
        list.getStyleClass().add("alert-list");
        VBox.setVgrow(list, Priority.ALWAYS);
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(smarthome.model.AlertEntry item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
                if (!empty) setStyle("-fx-font-size:10px; -fx-text-fill:#c0392b;");
            }
        });

        card.getChildren().addAll(title, list);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    // Logs a device event
    protected void logAlert(String message) {
        String time = java.time.LocalTime.now().toString().substring(0, 8);
        controller.getAlertLog().add(new smarthome.model.AlertEntry(time + " " + message));
    }

    public String getCurrentRoom() {
        return mainView != null ? mainView.getCurrentRoom() : "";
    }

    public BorderPane getRoot() {
        return root;
    }
}