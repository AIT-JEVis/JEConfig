/**
 * Copyright (C) 2015 WernerLamprecht <werner.lamprecht@ymail.com>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * This wizard is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.structurewizard;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.object.ObjectTree;
import org.jevis.jeconfig.tool.ImageConverter;

/**
 *
 * Checks if the building with the same name exists, otherwise it creates a new
 * one
 *
 * @author Werner Lamprecht, Zeyd Bilal Calis
 */
public class AutomatedWizardStep1 extends WizardPane {

    private JEVisObject parentObject;
    private TextField newBuildingTxtf;
    // JEVis tree for structure updates
    private ObjectTree tree;
    // global Object variable
    private WizardSelectedObject wizardSelectedObject;
    Button test;

    /**
     *
     * @param parentObject Has to be 'Monitored Object Directory' node
     * @param tree JEVis structure tree
     * @param wizardSelectedObject Selected Object in JECionfig
     */
    public AutomatedWizardStep1(JEVisObject parentObject, ObjectTree tree, WizardSelectedObject wizardSelectedObject) {
        this.wizardSelectedObject = wizardSelectedObject;
        this.tree = tree;
        setParentObject(parentObject);
        setMinSize(500, 500);
        setContent(getInit());
        setGraphic(JEConfig.getImage("create_wizard.png", 100, 100));
    }

    /**
     * Sets the needed graphicl interface objects
     *
     * @return JAVA FX GUI Objects
     */
    private BorderPane getInit() {

        BorderPane root = new BorderPane();
        GridPane gridpane = new GridPane();

        Label descriptionLbl = new Label("Select the JEVis Building Node to create the Structure");

        Label newBuildingLbl = new Label("New Building Name");

        ObservableList<JEVisObject> options = FXCollections.observableArrayList();

        try {
            List<JEVisObject> buildingList = getParentObject().getChildren();
            for (JEVisObject buildingObject : buildingList) {
                if (buildingObject.getClass().equals("Building")) {
                    //options = FXCollections.observableArrayList(buildingObject);
                }
            }
            options = FXCollections.observableArrayList(getParentObject().getChildren());
        } catch (JEVisException ex) {
            Logger.getLogger(AutomatedWizardStep1.class.getName()).log(Level.SEVERE, null, ex);
        }

        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                final ListCell<JEVisObject> cell = new ListCell<JEVisObject>() {
                    {
                        super.setPrefWidth(260);
                    }

                    @Override
                    public void updateItem(JEVisObject item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            HBox box = new HBox(5);
                            box.setAlignment(Pos.CENTER_LEFT);
                            try {
                                ImageView icon = ImageConverter.convertToImageView(item.getJEVisClass().getIcon(), 15, 15);
                                Label cName = new Label(item.getName());
                                cName.setTextFill(Color.BLACK);
                                box.getChildren().setAll(icon, cName);

                            } catch (JEVisException ex) {
                                Logger.getLogger(AutomatedWizardStep1.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            setGraphic(box);
                        }
                    }
                };
                return cell;
            }
        };

        ComboBox<JEVisObject> classComboBox = new ComboBox<JEVisObject>();

        for (JEVisObject option : options) {
            classComboBox.getItems().add(option);
        }

        classComboBox.setCellFactory(cellFactory);
        classComboBox.setButtonCell(cellFactory.call(null));
        classComboBox.setMinWidth(250);
        classComboBox.getSelectionModel().selectFirst();

        classComboBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                newBuildingTxtf.setText(classComboBox.getSelectionModel().getSelectedItem().getName());
            }
        });

        classComboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                newBuildingTxtf.setText(classComboBox.getSelectionModel().getSelectedItem().getName());
            }
        });

        gridpane.setHgap(10);//horizontal gap in pixels 
        gridpane.setVgap(10);//vertical gap in pixels
        gridpane.setPadding(new Insets(50, 10, 10, 10));////margins around the whole grid

        newBuildingTxtf = new TextField();
        newBuildingTxtf.setPrefWidth(200);

        gridpane.addRow(0, descriptionLbl);
        gridpane.addRow(1, newBuildingLbl, newBuildingTxtf);
        gridpane.addRow(2, new Label("Or select existing:"), classComboBox);

        root.setTop(gridpane);
        return root;
    }

    @Override
    public void onEnteringPage(Wizard wizard) {

        //Hides the 'Back' button
        ObservableList<ButtonType> list = getButtonTypes();

        for (ButtonType type : list) {
            if (type.getButtonData().equals(ButtonBar.ButtonData.BACK_PREVIOUS)) {
                Node prev = lookupButton(type);
                prev.visibleProperty().setValue(Boolean.FALSE);

            }
        }
    }

    /**
     * Create the new buiilding or get ID if it exists
     *
     * @param wizard
     */
    @Override
    public void onExitingPage(Wizard wizard) {

        JEVisClass buildingClass = null;
        List<JEVisClass> listClasses = null;

        try {
            // Get the all allowed classes from selected parent
            listClasses = getParentObject().getAllowedChildrenClasses();
            // If 'Building' is allowed, assign to value
            for (JEVisClass element : listClasses) {
                if (element.getName().equals("Building")) {
                    buildingClass = element;
                }
            }

            JEVisObject buildingObject = null;

            List<JEVisObject> existingChildList = getParentObject().getChildren();
            // Check if the building name already exists, if not, create a new one
            for (JEVisObject jeO : existingChildList) {
                Logger.getLogger(ManualWizardStep1.class.getName()).log(Level.SEVERE, jeO.getName());
                if (jeO.getName().equals(newBuildingTxtf.getText())) {
                    buildingObject = jeO;
                    break;
                }
            }
            // If buildingObject still null, create a new one
            if (buildingObject == null) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Create new Building");
                alert.setHeaderText(null);
                alert.setContentText("You are creating a new Building");

                Optional<ButtonType> result = alert.showAndWait();

                Logger.getLogger(ManualWizardStep1.class.getName()).log(Level.SEVERE, "new created");
                buildingObject = getParentObject().buildObject(newBuildingTxtf.getText(), buildingClass);
                //buildingObject.commit();
            }
            // Set the buildingobject for the next step
            wizardSelectedObject.setCurrentSelectedBuildingObject(buildingObject);

        } catch (JEVisException ex) {
            Logger.getLogger(ManualWizardStep1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JEVisObject getParentObject() {
        return this.parentObject;
    }

    public void setParentObject(JEVisObject parentObject) {
        this.parentObject = parentObject;
    }
}
