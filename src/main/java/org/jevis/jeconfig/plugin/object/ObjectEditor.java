/**
 * Copyright (C) 2009 - 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.plugin.object;

import org.jevis.jeconfig.plugin.object.extension.GenericAttributeExtension;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
//import javafx.scene.control.Dialogs;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.jevis.api.JEVisObject;
import org.jevis.application.dialog.ConfirmDialog;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.object.extension.BasicMathExtension;
import org.jevis.jeconfig.plugin.object.extension.LinkExtension;
import org.jevis.jeconfig.plugin.object.extension.MemberExtension;
import org.jevis.jeconfig.plugin.object.extension.RootExtension;
import org.jevis.jeconfig.plugin.object.extension.PermissionExtension;
import org.jevis.jeconfig.plugin.object.extension.ProcessChainExtension;
import org.jevis.jeconfig.tool.ImageConverter;

/**
 * This Edior is used to configure the Attributes of an Objects. Its used in the
 * right side next to the Objects Tree.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ObjectEditor {

    private JEVisObject _currentObject = null;
    private List<ObjectEditorExtension> extensions = new LinkedList<>();
    private boolean _hasChanged = true;
    private String _lastOpenEditor = "";

    private AnchorPane _view;

    public ObjectEditor() {
        _view = new AnchorPane();
        _view.setId("objecteditorpane");
        _view.getStylesheets().add("/styles/Styles.css");
        _view.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);

    }

    public void commitAll() {
        for (ObjectEditorExtension extension : extensions) {
            if (extension.needSave()) {
                extension.save();
            }
        }
    }

    public void checkIfSaved(JEVisObject obj) {
//        System.out.println("checkIfSaved: " + obj);
        if (_currentObject != null && !Objects.equals(obj.getID(), _currentObject.getID())) {

            List<ObjectEditorExtension> needSave = new ArrayList<>();

//            _hasChanged = true;
            for (ObjectEditorExtension extension : extensions) {
                if (extension.needSave()) {
                    System.out.println("extension need save: " + extension.getTitel());
                    needSave.add(extension);
                }
            }
//            System.out.println("needSave.size: " + needSave.size());

            if (!needSave.isEmpty()) {
                //workaround for fast saving without requesting
//                commitAll();
//                System.out.println("need save");
                ConfirmDialog dia = new ConfirmDialog();
                ConfirmDialog.Response re = dia.show(JEConfig.getStage(), "Save", "Save Attribute Changes", "Changes will be lost if not saved, do you want to save now?");
                if (re == ConfirmDialog.Response.YES) {
                    commitAll();
                } else {
                    _hasChanged = false;
                }
            }

        }
    }

    public Node getView() {
        return _view;
    }

    public void setObject(final JEVisObject obj) {
        Task<Void> load = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadObject(obj);
                return null;
            }
        };

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                JEConfig.getStage().getScene().setCursor(Cursor.WAIT);

            }
        });

        load.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent event) {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        JEConfig.getStage().getScene().setCursor(Cursor.DEFAULT);

                    }
                });
            }
        });

        new Thread(load).start();

    }

    public void loadObject(final JEVisObject obj) {
        checkIfSaved(obj);
        _currentObject = obj;
        Platform.runLater(new Runnable() {

            @Override
            public void run() {

//                AnchorPane content = new AnchorPane();
                Accordion accordion = new Accordion();
                accordion.getStylesheets().add("/styles/objecteditor.css");
                accordion.setStyle("-fx-box-border: transparent;");

                List<TitledPane> taps = new ArrayList<>();
                extensions = new ArrayList<>();

                extensions.add(new GenericAttributeExtension(obj));
//                extensions.add(new BasicMathExtension(obj));
                extensions.add(new MemberExtension(obj));
                extensions.add(new PermissionExtension(obj));
                extensions.add(new RootExtension(obj));
                extensions.add(new LinkExtension(obj));
                extensions.add(new ProcessChainExtension(obj));

                for (final ObjectEditorExtension ex : extensions) {
                    if (ex.isForObject(obj)) {
                        TitledPane newTab = new TitledPane(ex.getTitel(), ex.getView());
                        newTab.getStylesheets().add("/styles/objecteditor.css");
//                        newTab.setStyle("-fx-background-color: transparent;");

                        newTab.setAnimated(false);
                        taps.add(newTab);
                        ex.getValueChangedProperty().addListener(new ChangeListener<Boolean>() {

                            @Override
                            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                                if (t1) {
                                    _hasChanged = t1;//TODO: enable/disbale the save button
                                }
                            }
                        });

                        newTab.expandedProperty().addListener(new ChangeListener<Boolean>() {

                            @Override
                            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                                if (t1) {
                                    try {
                                        JEConfig.loadNotification(true);
                                        System.out.println("Expansion is visible: " + ex.getTitel());
//                                        loaderP.setProgress(1);
                                        ex.setVisible();
//                                        loaderP.setContent(content);

//                                        updateView(content, ex);
//                                        loaderP.setProgress(100);
                                        _lastOpenEditor = ex.getTitel();
                                        JEConfig.loadNotification(false);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });

                    }
                }

                accordion.getPanes().addAll(taps);
                AnchorPane.setTopAnchor(accordion, 0.0);
                AnchorPane.setRightAnchor(accordion, 0.0);
                AnchorPane.setLeftAnchor(accordion, 0.0);
                AnchorPane.setBottomAnchor(accordion, 0.0);
//                content.getChildren().add(accordion);

                //load the last Extensions for the new object
                boolean foundTab = false;
                if (!taps.isEmpty()) {

                    for (ObjectEditorExtension ex : extensions) {
                        if (ex.getTitel().equals(_lastOpenEditor)) {
                            ex.setVisible();
//                            updateView(content, ex);
                        }
                    }
                    for (TitledPane tap : taps) {
                        if (tap.getText().equals(_lastOpenEditor)) {
                            accordion.setExpandedPane(tap);
                            foundTab = true;
                        }
                    }

                }
//                FXProgressPanel loaderP = new FXProgressPanel(true, accordion);
//                _view.getChildren().setAll(loaderP);

                if (!foundTab) {
//                    updateView(content, extensions.get(0));
                    extensions.get(0).setVisible();
                    accordion.setExpandedPane(taps.get(0));
                    taps.get(0).requestFocus();
                    _lastOpenEditor = extensions.get(0).getTitel();
                }

                //Header
                ImageView classIcon;
                try {
                    classIcon = ImageConverter.convertToImageView(obj.getJEVisClass().getIcon(), 60, 60);
                } catch (Exception ex) {
                    classIcon = JEConfig.getImage("1390343812_folder-open.png", 20, 20);
                }

//                FlowPane header = new FlowPane();
                GridPane header = new GridPane();
                try {
//                header.setStyle("-fx-background-color: linear-gradient(from 25% 25% to 100% 100%, #1a719c, #f4f4f4)");
                    Label nameLabel = new Label("Name:");
                    Label objectName = new Label(obj.getName());
                    objectName.setStyle("-fx-font-weight: bold;");
                    Label classlabel = new Label("Type:");
                    Label className = new Label(obj.getJEVisClass().getName());
                    Label idlabel = new Label("ID:");
                    Label idField = new Label(obj.getID() + "");

                    //TODO: would be nice if the user can copy the ID and name but the layout is broken if i use this textfield code
//                    idField.setEditable(false);
//                    idField.setStyle("-fx-background-color: transparent ;-fx-background-insets: 0px ;-fx-text-box-border: transparent;");
                    Region spacer = new Region();
                    GridPane.setVgrow(spacer, Priority.ALWAYS);
                    GridPane.setVgrow(nameLabel, Priority.NEVER);
                    GridPane.setVgrow(classlabel, Priority.NEVER);
                    GridPane.setVgrow(idlabel, Priority.NEVER);
                    GridPane.setVgrow(objectName, Priority.NEVER);
                    GridPane.setVgrow(className, Priority.NEVER);
                    GridPane.setVgrow(idField, Priority.NEVER);

                    header.add(classIcon, 0, 0, 1, 4);

                    header.add(nameLabel, 1, 0, 1, 1);
                    header.add(classlabel, 1, 1, 1, 1);
                    header.add(idlabel, 1, 2, 1, 1);

                    header.add(objectName, 2, 0, 1, 1);
                    header.add(className, 2, 1, 1, 1);
                    header.add(idField, 2, 2, 1, 1);

                    header.add(spacer, 1, 3, 2, 1);

                    Separator sep = new Separator(Orientation.HORIZONTAL);
                    GridPane.setVgrow(sep, Priority.ALWAYS);

//                    header.add(sep, 0, 4, 4, 1);
                    header.setPadding(new Insets(10));
                    header.setVgap(5);
                    header.setHgap(12);
                    header.setPadding(new Insets(10, 0, 20, 10));
//                    header.getChildren().setAll(classIcon, objectName);
                } catch (Exception ex) {

                }

                BorderPane pane = new BorderPane();
                pane.setTop(header);
                pane.setCenter(accordion);
                AnchorPane.setRightAnchor(pane, 1.0);
                AnchorPane.setLeftAnchor(pane, 1.0);
                AnchorPane.setTopAnchor(pane, 1.0);
                AnchorPane.setBottomAnchor(pane, 1.0);

                _view.getChildren().setAll(pane);
//                _view.getChildren().setAll(accordion);
            }
        });

    }

}
