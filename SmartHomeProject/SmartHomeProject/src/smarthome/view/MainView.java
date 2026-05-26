package smarthome.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import smarthome.controller.HomeController;
import smarthome.model.Room;

public class MainView {

    private BorderPane     root;
    private final HomeController controller;
    private StackPane      contentArea;
    private String         currentRoom = "Living room";

    // Cached room views
    private LivingRoomView livingView;
    private KitchenView    kitchenView;
    private BedroomView    bedroomView;

    public MainView(HomeController controller) {
        this.controller = controller;
        build();
    }

    private void build() {
        root = new BorderPane();
        root.getStyleClass().add("main-root");

        root.setLeft(buildSidebar());

        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        livingView  = new LivingRoomView(controller, this);
        kitchenView = new KitchenView(controller, this);
        bedroomView = new BedroomView(controller, this);

        switchRoom("Living room");

        root.setCenter(contentArea);
    }

    // Switches the visible room view
    public void switchRoom(String name) {
    currentRoom = name;
    contentArea.getChildren().clear();

    switch (name) {
        case "Living room" -> contentArea.getChildren().add(livingView.getRoot());
        case "Kitchen" -> contentArea.getChildren().add(kitchenView.getRoot());
        case "Bed room" -> contentArea.getChildren().add(bedroomView.getRoot());
    }

    for (Room r : controller.getRooms()) {
        if (r.getName().equals(name)) {
            controller.setSelectedRoom(r);
            break;
        }
    }

    livingView.refreshRoomsPanel();
    kitchenView.refreshRoomsPanel();
    bedroomView.refreshRoomsPanel();
}

    private VBox buildSidebar() {
        VBox bar = new VBox(0);
        bar.getStyleClass().add("sidebar");
        bar.setAlignment(Pos.TOP_CENTER);
        bar.setPrefWidth(72);

        VBox logo = new VBox(3);
        logo.setAlignment(Pos.CENTER);
        logo.setPrefHeight(56);
        logo.setMinHeight(56);
        logo.setMaxHeight(56);
        logo.setPadding(new Insets(14, 0, 14, 0));
        logo.setStyle("-fx-background-color: #1a3d2e; -fx-min-width: 72;");

        Label iconLbl = new Label("⚙");
        iconLbl.setStyle("-fx-font-size: 22px; -fx-text-fill: white;");
        Label esoLbl  = new Label("ESO");
        esoLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;");
        logo.getChildren().addAll(iconLbl, esoLbl);

        // Navigation icons
        String[] navIcons = {"❄", "💡", "🌡", "📷", "🛡", "⚡"};
        VBox nav = new VBox(6);
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(18, 0, 0, 0));
        for (String ic : navIcons) {
            Label btn = new Label(ic);
            btn.getStyleClass().add("nav-icon");
            btn.setPrefWidth(72);
            btn.setAlignment(Pos.CENTER);
            nav.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Bottom icons
        VBox bottom = new VBox(6);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(0, 0, 16, 0));
        bottom.getChildren().addAll(makeNavIcon("⚙"), makeNavIcon("🚪"));

        bar.getChildren().addAll(logo, nav, spacer, bottom);
        return bar;
    }

    private Label makeNavIcon(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("nav-icon");
        l.setPrefWidth(72);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    public BorderPane getRoot() {
        return root;
    }
}