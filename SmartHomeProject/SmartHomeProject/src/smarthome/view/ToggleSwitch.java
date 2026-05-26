package smarthome.view;

import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public final class ToggleSwitch extends StackPane {

    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    private final Rectangle track  = new Rectangle(40, 20);
    private final Circle    thumb  = new Circle(8);
    private final TranslateTransition anim = new TranslateTransition(Duration.millis(180), thumb);

    private static final Color ON_COLOR  = Color.web("#2d6a4f");
    private static final Color OFF_COLOR = Color.web("#cccccc");

    public ToggleSwitch(boolean initialValue) {
        track.setArcWidth(20);
        track.setArcHeight(20);
        thumb.setFill(Color.WHITE);
        thumb.setEffect(new javafx.scene.effect.DropShadow(3, Color.gray(0, 0.3)));

        getChildren().addAll(track, thumb);
        setPadding(new Insets(0, 2, 0, 2));
        setMinSize(44, 24);
        setMaxSize(44, 24);

        selected.addListener((obs, ov, nv) -> animateSwitch(nv));
        setSelected(initialValue);

        setOnMouseClicked(e -> setSelected(!isSelected()));
    }

    private void animateSwitch(boolean on) {
        anim.stop();
        anim.setToX(on ? 10 : -10);
        anim.play();
        track.setFill(on ? ON_COLOR : OFF_COLOR);
    }

    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean v) { selected.set(v); }
    public BooleanProperty selectedProperty() { return selected; }
}