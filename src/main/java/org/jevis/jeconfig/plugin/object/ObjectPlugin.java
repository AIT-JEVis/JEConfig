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

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.tool.LoadingPane;

/**
 * Theis JEConfig plugin allowes the user con work with the Objects in the JEVis
 * System.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ObjectPlugin implements Plugin {

    private StringProperty name = new SimpleStringProperty("*NO_NAME*");
    private StringProperty id = new SimpleStringProperty("*NO_ID*");
    private JEVisDataSource ds;
    private BorderPane border;
//    private ObjectTree tf;
    private ObjectTree tree;
    private LoadingPane editorLodingPane = new LoadingPane();
    private LoadingPane treeLodingPane = new LoadingPane();
    private ToolBar toolBar;

    public ObjectPlugin(JEVisDataSource ds, String newname) {
        this.ds = ds;
        name.set(newname);
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public void setName(String value) {
        name.set(value);
    }

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public String getUUID() {
        return id.get();
    }

    @Override
    public void setUUID(String newid) {
        id.set(newid);
    }

    @Override
    public StringProperty uuidProperty() {
        return id;
    }

    @Override
    public Node getConntentNode() {
        if (border == null) {

//            VBox editorPane = new VBox();
//            editorPane.setId("objecteditorpane");
            tree = new ObjectTree(ds);

            VBox left = new VBox();
            left.setStyle("-fx-background-color: #E2E2E2;");
//            SearchBox search = new SearchBox();
            left.getChildren().addAll(tree);
            VBox.setVgrow(tree, Priority.ALWAYS);
//            VBox.setVgrow(search, Priority.NEVER);

            treeLodingPane.setContent(tree);
            editorLodingPane.setContent(tree.getEditor().getView());

            SplitPane sp = new SplitPane();
            sp.setDividerPositions(.3d);
            sp.setOrientation(Orientation.HORIZONTAL);
            sp.setId("mainsplitpane");
            sp.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
//            sp.getItems().setAll(left, tree.getEditor().getView());
            sp.getItems().setAll(treeLodingPane, editorLodingPane);

            treeLodingPane.endLoading();
            editorLodingPane.endLoading();

            tree.getLoadingObjectProperty().addListener(new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        editorLodingPane.startLoading();
                    } else {
                        editorLodingPane.endLoading();
                    }
                }
            });
            tree.getLoadingProperty().addListener(new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        treeLodingPane.startLoading();
                    } else {
                        treeLodingPane.endLoading();
                    }
                }
            });

//            SplitPane sp = SplitPaneBuilder.create()
//                    .items(left, tree.getEditor().getView())
//                    .dividerPositions(new double[]{.2d, 0.8d}) // why does this not work!?
//                    .orientation(Orientation.HORIZONTAL)
//                    .build();
//            sp.setId("mainsplitpane");
//            sp.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
            border = new BorderPane();
            border.setCenter(sp);
            border.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
        }

        return border;
    }

    @Override
    public Node getMenu() {
        return null;
    }

    @Override
    public Node getToolbar() {

        if (toolBar == null) {
            toolBar = new ToolBar();
            toolBar.setId("ObjectPlugin.Toolbar");
            double iconSize = 20;
            ToggleButton newB = new ToggleButton("", JEConfig.getImage("list-add.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, newB, Constants.Plugin.Command.NEW);

            ToggleButton save = new ToggleButton("", JEConfig.getImage("save.gif", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, save, Constants.Plugin.Command.SAVE);

            ToggleButton delete = new ToggleButton("", JEConfig.getImage("list-remove.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, delete, Constants.Plugin.Command.DELTE);

            Separator sep1 = new Separator();

            ToggleButton reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, reload, Constants.Plugin.Command.RELOAD);

            ToggleButton addTable = new ToggleButton("", JEConfig.getImage("add_table.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(addTable);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, addTable, Constants.Plugin.Command.ADD_TABLE);

            ToggleButton editTable = new ToggleButton("", JEConfig.getImage("edit_table.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(editTable);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, editTable, Constants.Plugin.Command.EDIT_TABLE);

            ToggleButton createWizard = new ToggleButton("", JEConfig.getImage("create_wizard.png", iconSize, iconSize));
            GlobalToolBar.changeBackgroundOnHoverUsingBinding(createWizard);
            GlobalToolBar.BuildEventhandler(ObjectPlugin.this, createWizard, Constants.Plugin.Command.CREATE_WIZARD);

            toolBar.getItems().addAll(save, newB, delete, sep1, addTable, editTable,createWizard);
        }

        return toolBar;
    }

    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    @Override
    public void setDataSource(JEVisDataSource ds) {
        this.ds = ds;
    }

    @Override
    public void handelRequest(int cmdType) {
        try {
            switch (cmdType) {
                case Constants.Plugin.Command.SAVE:
                    tree.fireSaveAttributes(false);
                    break;
                case Constants.Plugin.Command.DELTE:
                    tree.fireDelete(tree.getSelectedObject());
                    break;
                case Constants.Plugin.Command.EXPAND:
                    break;
                case Constants.Plugin.Command.NEW:
                    tree.fireEventNew(tree.getSelectedObject());
                    break;
                case Constants.Plugin.Command.RELOAD:
                    tree.reload();
                    break;
                case Constants.Plugin.Command.ADD_TABLE:
                    tree.fireEventCreateTable(tree.getSelectedObject());
                    break;
                case Constants.Plugin.Command.EDIT_TABLE:
                    tree.fireEventEditTable(tree.getSelectedObject());
                    break;
                case Constants.Plugin.Command.CREATE_WIZARD:
                    tree.fireEventCreateWizard(tree.getSelectedObject());
                    break;
                default:
                    System.out.println("Unknows command ignore...");
            }
        } catch (Exception ex) {
        }

    }

    @Override
    public void fireCloseEvent() {
        try {
            tree.fireSaveAttributes(true);
        } catch (JEVisException ex) {
            Logger.getLogger(ObjectPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Save() {
        try {
            tree.fireSaveAttributes(false);
        } catch (JEVisException ex) {
            Logger.getLogger(ObjectPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("1394482640_package_settings.png", 20, 20);
    }
}
