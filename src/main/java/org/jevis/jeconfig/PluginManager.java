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
package org.jevis.jeconfig;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.plugin.graph.view.GraphPluginView;
import org.jevis.jeconfig.plugin.object.ObjectPlugin;

/**
 * The PluginManger controls the view of the different Plugins
 *
 * i* @author Florian Simon <florian.simon@envidatec.com>
 */
public class PluginManager {

    private List<Plugin> _plugins = new ArrayList<>();
    private JEVisDataSource _ds;
    private boolean _watermark = true;
    private Plugin _selectedPlugin = null;
    private Number _tabPos = 0;
    private Number _tabPosOld = 0;
    private TabPane tabPane;
    private AnchorPane toolbar = new AnchorPane();
    private ObjectProperty<Plugin> selectedPluginProperty = new SimpleObjectProperty();

    public PluginManager(JEVisDataSource _ds) {
        this._ds = _ds;
    }

    public void addPlugin(Plugin plugin) {
        _plugins.add(plugin);
    }

    public void removePlugin(Plugin plugin) {
        _plugins.remove(plugin);
    }

    public List<Plugin> getPlugins() {
        return _plugins;
    }

    /**
     * Add all plugins based on the JEVis usersettings
     *
     * @param user
     */
    public void addPluginsByUserSetting(JEVisObject user) {
        //TODO: load the user an add only the allowed plugins
        _plugins.add(new ObjectPlugin(_ds, "Resources"));
        _plugins.add(new GraphPluginView(_ds, "Graph"));
        _plugins.add(new org.jevis.jeconfig.plugin.classes.ClassPlugin(_ds, "Classes"));
        _plugins.add(new org.jevis.jeconfig.plugin.unit.UnitPlugin(_ds, "Units"));
//        _plugins.add(new MapViewPlugin(_ds, "GIS"));

    }

    public void setWatermark(boolean water) {
        _watermark = water;
    }

    public Node getView() {
        StackPane box = new StackPane();

        tabPane = new TabPane();
        tabPane.setSide(Side.LEFT);

        //TMP soution to hide the development if Graph
        final KeyCombination openGraph = KeyCodeCombination.keyCombination("Ctrl+Alt+g");
        JEConfig.getStage().getScene().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {

                if (openGraph.match(event)) {
                    System.out.println("JEGraph hotkey");
                    Plugin plugin = new GraphPluginView(_ds, "Graph");
                    _plugins.add(plugin);
                    Tab pluginTab = new Tab(plugin.getName());
                    pluginTab.setContent(plugin.getConntentNode());
                    tabPane.getTabs().add(pluginTab);
                    pluginTab.setGraphic(plugin.getIcon());
                }

            }
        }
        );

        toolbar.setStyle("-fx-background-color: #CCFF99;");
//        AnchorPane.setTopAnchor(toolbar, 0.0);
//        AnchorPane.setLeftAnchor(toolbar, 0.0);
//        AnchorPane.setRightAnchor(toolbar, 0.0);
//        AnchorPane.setBottomAnchor(toolbar, 0.0);

        for (Plugin plugin : _plugins) {
            Tab pluginTab = new Tab(plugin.getName());
            pluginTab.setClosable(false);
            pluginTab.setTooltip(new Tooltip(plugin.getUUID()));
//            pluginTab.setContent(plugin.getView().getNode());
            pluginTab.setContent(plugin.getConntentNode());
            tabPane.getTabs().add(pluginTab);

            pluginTab.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event t) {
                    Plugin plugin = _plugins.get(_tabPosOld.intValue());
                    _plugins.remove(plugin);
                }
            });

            pluginTab.setGraphic(plugin.getIcon());

        }

        selectedPluginProperty.addListener(new ChangeListener<Plugin>() {

            @Override
            public void changed(ObservableValue<? extends Plugin> observable, Plugin oldValue, Plugin newValue) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
//                        toolbar.getChildren().removeAll();
                        Node pluginToolbar = newValue.getToolbar();

                        toolbar.getChildren().setAll(pluginToolbar);
                        AnchorPane.setTopAnchor(pluginToolbar, 0.0);
                        AnchorPane.setLeftAnchor(pluginToolbar, 0.0);
                        AnchorPane.setRightAnchor(pluginToolbar, 0.0);
                        AnchorPane.setBottomAnchor(pluginToolbar, 0.0);

                    }
                });
            }
        });
        selectedPluginProperty.setValue(_plugins.get(0));

        tabPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
                selectedPluginProperty.setValue(_plugins.get(newValue.intValue()));

            }
        });

        //Watermark is disabled
        //Todo: configure via start parameter
//        if (_watermark) {
//            VBox waterBox = new VBox(); //TODO better load the
//            waterBox.setId("watermark");
//            waterBox.setStyle(null);
//            waterBox.setDisable(true);
//            box.getChildren().addAll(tabPane,
//                    waterBox);
//        } else {
//            box.getChildren().addAll(tabPane);
//        }
        box.getChildren().addAll(tabPane);

        return box;
    }

    Plugin getSelectedPlugin() {
        return selectedPluginProperty.getValue();
//        return _plugins.get(_tabPos.intValue());
    }

    public Node getToolbar() {
        return toolbar;
//        return getSelectedPlugin().getToolbar();
    }
}
