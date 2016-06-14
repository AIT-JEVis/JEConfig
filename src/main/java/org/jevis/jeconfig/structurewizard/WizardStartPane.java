package org.jevis.jeconfig.structurewizard;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.jevis.jeconfig.JEConfig;

/**
 *
 * @author Zeyd Bilal Calis
 */
// Entscheidungsklasse fuer das Manual oder Automatedauswahl.
public class WizardStartPane extends WizardPane {

    private String control = "Manual";
    private RadioButton manual;
    private RadioButton automated;

    public WizardStartPane() {
        setMinSize(500, 500);

        setContent(getInit());
        setGraphic(JEConfig.getImage("create_wizard.png", 100, 100));
    }

    private VBox getInit() {
        VBox vbox = new VBox();

        Label index = new Label();
        index.setText("Select JEVis Structure Mode\n\n"
                + "Manual: Guided Structure Creation Mode\n\n"
                + "Automated Wiotech Structure Creation:\n"
                + "Creates the needed JEVis structure for a \nWiotech WSN Setup");

        ToggleGroup group = new ToggleGroup();
        manual = new RadioButton("Manual");
        manual.setToggleGroup(group);
        manual.setSelected(true);

        automated = new RadioButton("Automated Wiotech Structure Creation");
        automated.setToggleGroup(group);

        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                if (manual.isSelected()) {
                    setControl(manual.getText());
                } else if (automated.isSelected()) {
                    setControl(automated.getText());
                }
            }
        });

        vbox.setSpacing(30);

        vbox.getChildren().addAll(index, manual , automated);
        vbox.setPadding(new Insets(200, 10, 10, 20));

        return vbox;
    }

    @Override
    public void onEnteringPage(Wizard wizard) {

        ObservableList<ButtonType> list = getButtonTypes();

        for (ButtonType type : list) {
            if (type.getButtonData().equals(ButtonBar.ButtonData.BACK_PREVIOUS)) {
                Node prev = lookupButton(type);
                prev.visibleProperty().setValue(Boolean.FALSE);
            }
        }
    }

    public void setControl(String control) {
        this.control = control;
    }

    public String getControl() {
        return control;
    }
}
