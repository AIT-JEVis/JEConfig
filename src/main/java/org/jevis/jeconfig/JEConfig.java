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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.application.application.JavaVersionCheck;
import org.jevis.application.login.FXLogin;
import org.jevis.application.statusbar.Statusbar;
import org.jevis.commons.application.ApplicationInfo;
import org.jevis.jeconfig.tool.WelcomePage;

/**
 * This is the main class of the JEConfig. The JEConfig is an JAVAFX programm,
 * the early version will need the MAVEN javafx 2.0 plugin to be build for java
 * 1.7
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEConfig extends Application {

    /**
     * Defines the version information in the about dialog
     */
    public static ApplicationInfo PROGRAMM_INFO = new ApplicationInfo("JEConfig", "3.0.16 2015-12-03");
    private static Preferences pref = Preferences.userRoot().node("JEVis.JEConfig");

    final Configuration _config = new Configuration();
    private static Stage _primaryStage;
    private static File _lastFile;
    private static JEVisDataSource _mainDS;
    private JEVisDataSource ds = null;
    //Workaround to load classes and roots while login
    private static List<JEVisClass> preLodedClasses = new ArrayList<>();
    private static List<JEVisObject> preLodedRootObjects = new ArrayList<>();
    private static String _lastpath = "";
    private static User _currentUser;

    @Override
    public void init() throws Exception {
        super.init();
        Parameters parameters = getParameters();
        _config.parseParameters(parameters);
    }

    @Override
    public void start(Stage primaryStage) {

        JavaVersionCheck checkVersion = new JavaVersionCheck();
        if (!checkVersion.isVersionOK()) {
            System.exit(1);
        }

        _primaryStage = primaryStage;
        initGUI(primaryStage);
    }

    /**
     * Build an new JEConfig Login and main frame/stage
     *
     * @param primaryStage
     */
    private void initGUI(Stage primaryStage) {
        AnchorPane jeconfigRoot = new AnchorPane();

        Scene scene = new Scene(jeconfigRoot);
        primaryStage.setScene(scene);

        FXLogin login = new FXLogin(primaryStage, getParameters());

        AnchorPane.setTopAnchor(jeconfigRoot, 0.0);
        AnchorPane.setRightAnchor(jeconfigRoot, 0.0);
        AnchorPane.setLeftAnchor(jeconfigRoot, 0.0);
        AnchorPane.setBottomAnchor(jeconfigRoot, 0.0);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        login.getLoginStatus().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    _mainDS = login.getDataSource();
                    ds = _mainDS;
                    _currentUser = new User(ds);

//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            FadeTransition ft = new FadeTransition(Duration.millis(1500), login);
//                            ft.setFromValue(1.0);
//                            ft.setToValue(0);
//                            ft.setCycleCount(1);
//                            ft.play();
//                        }
//                    });
                    JEConfig.PROGRAMM_INFO.setJEVisAPI(ds.getInfo());
                    JEConfig.PROGRAMM_INFO.addLibrary(org.jevis.commons.application.Info.INFO);
                    JEConfig.PROGRAMM_INFO.addLibrary(org.jevis.application.Info.INFO);

                    preLodedClasses = login.getAllClasses();
                    preLodedRootObjects = login.getRootObjects();

                    PluginManager pMan = new PluginManager(ds);
                    GlobalToolBar toolbar = new GlobalToolBar(pMan);
                    pMan.addPluginsByUserSetting(null);

                    BorderPane border = new BorderPane();
                    VBox vbox = new VBox();
                    vbox.setStyle("-fx-background-color: black;");
//                    vbox.getChildren().addAll(new TopMenu(), toolbar.ToolBarFactory());
                    vbox.getChildren().addAll(new TopMenu(), pMan.getToolbar());
                    border.setTop(vbox);
                    border.setCenter(pMan.getView());

                    Statusbar statusBar = new Statusbar(ds);

                    border.setBottom(statusBar);

                    //Disable GUI is StatusBar note an disconnect
                    border.disableProperty().bind(statusBar.connectedProperty.not());

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            AnchorPane.setTopAnchor(border, 0.0);
                            AnchorPane.setRightAnchor(border, 0.0);
                            AnchorPane.setLeftAnchor(border, 0.0);
                            AnchorPane.setBottomAnchor(border, 0.0);

                            jeconfigRoot.getChildren().setAll(border);
                            try {
                                WelcomePage welcome = new WelcomePage(primaryStage, _config.getWelcomeURL());
                            } catch (URISyntaxException ex) {
                                Logger.getLogger(JEConfig.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (MalformedURLException ex) {
                                Logger.getLogger(JEConfig.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                } else {
                    System.exit(0);
                }

            }
        });

        AnchorPane.setTopAnchor(login, 0.0);
        AnchorPane.setRightAnchor(login, 0.0);
        AnchorPane.setLeftAnchor(login, 0.0);
        AnchorPane.setBottomAnchor(login, 0.0);

        scene.getStylesheets().add("/styles/Styles.css");
        primaryStage.getIcons().add(getImage("JEVisIconBlue.png"));

        primaryStage.setTitle("JEConfig");

        primaryStage.setMaximized(true);
        primaryStage.show();

        jeconfigRoot.getChildren().setAll(login);

        primaryStage.onCloseRequestProperty().addListener(new ChangeListener<EventHandler<WindowEvent>>() {

            @Override
            public void changed(ObservableValue<? extends EventHandler<WindowEvent>> ov, EventHandler<WindowEvent> t, EventHandler<WindowEvent> t1) {
                try {
                    System.out.println("Disconnect");
                    ds.disconnect();
                } catch (JEVisException ex) {
                    Logger.getLogger(JEConfig.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Returns the main JEVis Datasource of this JEConfig Try not to use this
     * because it may disapear
     *
     * @return
     * @deprecated
     */
    public static JEVisDataSource getDataSource() {
        return _mainDS;
    }

    public static Stage getStage() {
        return _primaryStage;
    }

    /**
     * Returns the last path the local user selected
     *
     * @return
     */
    public static File getLastPath() {
        if (_lastpath.equals("")) {
            _lastpath = pref.get("lastPath", System.getProperty("user.home"));
        }
        File file = new File(_lastpath);
        if (file.exists()) {
            if (file.isDirectory()) {
                return file;
            } else {
                return file.getParentFile();
            }

        } else {
            return new File(pref.get("lastPath", System.getProperty("user.home")));
        }
    }

    /**
     * Set the last path the user selected for an file opration
     *
     * @param file
     */
    public static void setLastPath(File file) {
        _lastFile = file;
        _lastpath = file.getPath();
        pref.put("lastPath", file.getPath());

    }

    /**
     * maximized the given stage
     *
     * @deprecated
     * @param primaryStage
     */
    public static void maximize(Stage primaryStage) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
    }

    /**
     * Return an common resource
     *
     * @param file
     * @return
     */
    public static String getResource(String file) {
        //        scene.getStylesheets().addAll(this.getClass().getResource("/org/jevis/jeconfig/css/main.css").toExternalForm());

//        System.out.println("get Resouce: " + file);
        return JEConfig.class.getResource("/styles/" + file).toExternalForm();
//        return JEConfig.class.getResource("/org/jevis/jeconfig/css/" + file).toExternalForm();

    }

    /**
     * Fet an image out of the common resources
     *
     * @param icon
     * @return
     */
    public static Image getImage(String icon) {
        try {
//            System.out.println("getIcon: " + icon);
            return new Image(JEConfig.class.getResourceAsStream("/icons/" + icon));
//            return new Image(JEConfig.class.getResourceAsStream("/org/jevis/jeconfig/image/" + icon));
        } catch (Exception ex) {
            System.out.println("Could not load icon: " + "/icons/   " + icon);
            return new Image(JEConfig.class.getResourceAsStream("/icons/1393355905_image-missing.png"));
        }
    }

    /**
     * Get an imge in the given size from the common
     *
     * @param icon
     * @param height
     * @param width
     * @return
     */
    public static ImageView getImage(String icon, double height, double width) {
        ImageView image = new ImageView(JEConfig.getImage(icon));
        image.fitHeightProperty().set(height);
        image.fitWidthProperty().set(width);
        return image;
    }

    /**
     * Inform the user the some precess is working
     *
     * @param working
     */
    public static void loadNotification(final boolean working) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (working) {
                    getStage().getScene().setCursor(Cursor.WAIT);
                } else {
                    getStage().getScene().setCursor(Cursor.DEFAULT);
                }
            }
        });

    }

    /**
     * Get the JEVisObkject of the current
     *
     * @return
     */
    static public User getCurrentUser() {
        return _currentUser;
    }

    /**
     * Get the static list of preload JEVisClasses
     *
     * @return
     */
    static public List<JEVisClass> getPreLodedClasses() {
        return preLodedClasses;
    }

    /**
     * Get the static list of all root objects for this user
     *
     * @return
     */
    static public List<JEVisObject> getPreLodedRootObjects() {
        return preLodedRootObjects;
    }

}
