/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.structurewizard;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.bulkedit.CreateTable;
import org.jevis.jeconfig.plugin.object.ObjectTree;
import org.jevis.jeconfig.tool.ImageConverter;

/**
 *
 * @author Zeyd Bilal Calis
 */
//In dieser Klasse wird ausgewaehlte Objekt vom ComboBox erzeugt.
public class ManualWizardStep6 extends WizardPane {

    private JEVisClass createClass;
    private TextField fileNameTextField;
    private ObjectTree tree;
    private WizardSelectedObject wizardSelectedObject;

    public ManualWizardStep6(ObjectTree tree, WizardSelectedObject wizardSelectedObject) {
        this.wizardSelectedObject = wizardSelectedObject;
        this.tree = tree;
        setMinSize(500, 500);
        setGraphic(null);

    }

    @Override
    public void onEnteringPage(Wizard wizard) {
        setContent(getInit());
        ObservableList<ButtonType> list = getButtonTypes();

        for (ButtonType type : list) {
            if (type.getButtonData().equals(ButtonBar.ButtonData.BACK_PREVIOUS)) {
                Node prev = lookupButton(type);
                prev.visibleProperty().setValue(Boolean.FALSE);
            }
        }
    }

    @Override
    public void onExitingPage(Wizard wizard) {
        //Erzeuge das Objekt
        //Set new Data Point Directory!
        commitObject();
        for (int i = 0; i < wizardSelectedObject.getCurrentTemplateObjects().size(); i++) {
            try {
                //System.out.println(wizardSelectedObject.getCurrentTemplateObjects().get(i).getJEVisClass().getName());
                if (wizardSelectedObject.getCurrentTemplateObjects().get(i).getJEVisClass().getName().endsWith("Data Point Directory")) {
                    wizardSelectedObject.setCurrentDataPointDirectory(wizardSelectedObject.getCurrentTemplateObjects().get(i));
                }
            } catch (JEVisException ex) {
                Logger.getLogger(ManualWizardStep6.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void commitObject() {
        try {
            //Create Object.
            JEVisObject newObject = wizardSelectedObject.getCurrentSelectedObject().buildObject(fileNameTextField.getText(), createClass);
            newObject.commit();

            //Wähle das neue Objekt als CurrentSelectedObject aus!
            wizardSelectedObject.setCurrentSelectedObject(newObject);
            //Speichere das Objekt in die Liste ab.
            wizardSelectedObject.setCurrentTemplateObjects(newObject);

            //Check ob das neue Objekt Kind hat oder nicht.
//            if (newObject.getAllowedChildrenClasses().isEmpty()) {
//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setTitle("Information Dialog");
//                alert.setHeaderText(null);
//                alert.setContentText(createClass.getName() + " has no children! \n"
//                        + ""
//                        + "Please check your structure!");
//                alert.showAndWait();
//            }
        } catch (JEVisException ex) {
            Logger.getLogger(ManualWizardStep6.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // GUI Elemente
    private HBox getInit() {

        ObservableList<JEVisClass> options = FXCollections.observableArrayList();

        try {
            options = FXCollections.observableArrayList(wizardSelectedObject.getCurrentSelectedObject().getAllowedChildrenClasses());
        } catch (JEVisException ex) {
            Logger.getLogger(ManualWizardStep6.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Set the cell properties for ComboBox
        Callback<ListView<JEVisClass>, ListCell<JEVisClass>> cellFactory = new Callback<ListView<JEVisClass>, ListCell<JEVisClass>>() {
            @Override
            public ListCell<JEVisClass> call(ListView<JEVisClass> param) {
                final ListCell<JEVisClass> cell = new ListCell<JEVisClass>() {
                    {
                        super.setPrefWidth(260);
                    }

                    @Override
                    public void updateItem(JEVisClass item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            HBox box = new HBox(5);
                            box.setAlignment(Pos.CENTER_LEFT);
                            try {
                                ImageView icon = ImageConverter.convertToImageView(item.getIcon(), 15, 15);
                                Label cName = new Label(item.getName());
                                cName.setTextFill(Color.BLACK);
                                box.getChildren().setAll(icon, cName);

                            } catch (JEVisException ex) {
                                Logger.getLogger(CreateTable.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            setGraphic(box);
                        }
                    }
                };
                return cell;
            }
        };

        Label fileNamelbl = new Label();
        fileNameTextField = new TextField();
        fileNameTextField.setPrefWidth(200);

        try {
            fileNameTextField.setPromptText(wizardSelectedObject.getCurrentSelectedObject().getAllowedChildrenClasses().get(0).getName());
            fileNameTextField.setText(fileNameTextField.getPromptText());
        } catch (JEVisException ex) {
            Logger.getLogger(ManualWizardStep3.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Add the children in to the ComboBox
        ComboBox<JEVisClass> classComboBox = new ComboBox<JEVisClass>(options);
        classComboBox.setCellFactory(cellFactory);
        classComboBox.setButtonCell(cellFactory.call(null));
        classComboBox.setMinWidth(250);
        classComboBox.getSelectionModel().selectFirst();
        createClass = classComboBox.getSelectionModel().getSelectedItem();

        classComboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createClass = classComboBox.getSelectionModel().getSelectedItem();
                try {
                    fileNameTextField.setText(classComboBox.getSelectionModel().getSelectedItem().getName());
                } catch (JEVisException ex) {
                    Logger.getLogger(ManualWizardStep3.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        fileNamelbl.setText("Name : ");

        //filename and ComboBox
        HBox hBox = new HBox();
        hBox.setSpacing(30);
        hBox.getChildren().addAll(fileNamelbl, fileNameTextField, classComboBox);
        hBox.setPadding(new Insets(200, 10, 10, 10));

        return hBox;
    }
}
