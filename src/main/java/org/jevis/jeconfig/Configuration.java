/**
 * Copyright (C) 2014-2015 Envidatec GmbH <info@envidatec.com>
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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javafx.application.Application;
import org.jevis.api.JEVisOption;
import org.jevis.application.ParameterHelper;
import org.jevis.commons.config.CommonOptions;
import static org.jevis.commons.config.CommonOptions.FXLogin.FXLogin;
import org.jevis.commons.config.OptionFactory;

/**
 * This class holds the configutraion for the JEConfig.
 *
 * TODO: replace this with the JEVisOption
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class Configuration {

    public static final JEVisOption JECONFIG = OptionFactory.BuildOption(null, "jeconfig", "", "JEConfig option group");
    public static final JEVisOption URL_WELCOME = OptionFactory.BuildOption(JECONFIG, "welcomeurl", "", "URL of the welcome screen");

    private JEVisOption options;
    private List<JEVisOption> configuration;

    private String _loginIcon = "/icons/logo_coffee_klein.png";//"/icons/kaust.jpg";//"/icons/openjevislogo_simple2.png";
    private String _welcomeURL = "http://coffee-project.eu/";//"http://www.kaust.edu.sa/research-technology-park.html";//http://openjevis.org/projects/openjevis/wiki/JEConfig3#JEConfig-Version-3";
    private String _watermark = "/icons/logo_JEVis_OPEN_Ohne_Schatten_long_v0_10.png";
    private boolean _ssl = false;
    private boolean _showServer = false;
    private String _defaultServerURL = "jevis:jevistest@coffee-project.eu:13306/jevis";//"jevis:Taexu3Eesesieth3eid1@lthneo.kaust.edu.sa:3306/jevis";//user:password@server:3306/jevis";
//    private final String _defaultServerURL = "user:password@server:3306/jevis";

    public static enum COLORS {

        BACKGROUND
    }

    /**
     * Returns if the Sever URL should be visible in the LoginDialog
     *
     * @return
     */
    public boolean getShowServer() {
        return _showServer;
    }

    /**
     * Returns the default Server URL for the Login dialog
     *
     * @return
     */
    public String getDefaultServer() {
        return _defaultServerURL;
    }

    /**
     * returns if SSL is enabled for the DS connection
     *
     * @return
     */
    public boolean getEnabledSSL() {
        return _ssl;
    }

    public String getLoginIcon() {
        return _loginIcon;
    }

    public URI getWelcomeURL() throws MalformedURLException, URISyntaxException {
        return new URI(_welcomeURL);
    }

    public String getWatermark() {
        return _watermark;
    }

    public void parseParameters(Application.Parameters args) {

//        System.out.println("Pars config");
        configuration = ParameterHelper.ParseJEVisConfiguration(args);
        for (JEVisOption opt : configuration) {
            if (opt.equals(JECONFIG)) {
                System.out.println("Found " + JECONFIG.getKey());
                options = opt;
            }
        }

        if (options != null) {
            if (options.hasOption(URL_WELCOME.getKey())) {
                JEVisOption opt = options.getOption(URL_WELCOME.getKey());
                if (opt.getValue().equals("off")) {
                    _welcomeURL = "";
                } else {
                    _welcomeURL = opt.getValue();
                }
            } else {
                _welcomeURL = "http://openjevis.org/";
            }
        } else {
            _welcomeURL = "http://openjevis.org/";

        }
//        System.out.println("Welcome URL: " + _welcomeURL);

//        for (Map.Entry<String, String> entry : args.getNamed().entrySet()) {
//            System.out.println(entry.getKey() + " : " + entry.getValue());
//        }
        if (args.getNamed().containsKey("loginbanner")) {
//            System.out.println("LoginIcon: " + args.getNamed().get("loginbanner"));
            _loginIcon = args.getNamed().get("loginbanner");
        }

//        if (args.getNamed().containsKey("welcomeurl")) {
////            System.out.println("welcomeurl: " + args.getNamed().get("welcomeurl"));
//            _welcomeURL = args.getNamed().get("welcomeurl");
//        }
        if (args.getNamed().containsKey("watermark")) {
//            System.out.println("watermark: " + args.getNamed().get("watermark"));
            _watermark = args.getNamed().get("watermark");
        }

    }

}
