/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.plugin.graph;

import java.util.Date;
import java.util.GregorianCalendar;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class GraphPlugin implements Plugin {

    private StringProperty name = new SimpleStringProperty("Graph");
    private StringProperty id = new SimpleStringProperty("*NO_ID*");
    private JEVisDataSource ds;
    private BorderPane border;
//    private ObjectTree tf;

    public GraphPlugin(JEVisDataSource ds, String newname) {
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
            border = new BorderPane();

            final SwingNode swingNode = new SwingNode();
            border.setCenter(swingNode);

            createSwingContent(swingNode);

//            border.setCenter(lineChart);
            border.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
        }

        return border;
    }

    private void createSwingContent(final SwingNode swingNode) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                swingNode.setContent(new JButton("Click me!"));
            }
        });
    }

    @Override
    public Node getMenu() {
        return null;
    }

    @Override
    public Node getToolbar() {
        return null;
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
            System.out.println("Command to ClassPlugin: " + cmdType);
            switch (cmdType) {
                case Constants.Plugin.Command.SAVE:
                    System.out.println("save");
                    break;
                case Constants.Plugin.Command.DELTE:
                    break;
                case Constants.Plugin.Command.EXPAND:
                    System.out.println("Expand");
                    break;
                case Constants.Plugin.Command.NEW:
                    break;
                case Constants.Plugin.Command.RELOAD:
                    System.out.println("reload");
                    break;
                default:
                    System.out.println("Unknows command ignore...");
            }
        } catch (Exception ex) {
        }

    }

    @Override
    public void fireCloseEvent() {
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("1415314386_Graph.png", 20, 20);
    }
}
