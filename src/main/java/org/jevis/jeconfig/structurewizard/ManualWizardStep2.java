/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.structurewizard;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.util.Pair;
import javax.measure.unit.Unit;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.GridChange;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetColumn;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.jeconfig.bulkedit.SpreadsheetViewTable;
import org.jevis.jeconfig.structurewizard.WizardSelectedObject;
import org.jevis.jeconfig.plugin.object.ObjectTree;
import org.joda.time.DateTime;

/**
 *
 * @author Bilal
 */
public class ManualWizardStep2 extends WizardPane {

    private final ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
    private ObservableList<SpreadsheetCell> cells;
    private GridBase grid;
    private SpreadsheetView spv;
    private WizardSelectedObject wizardSelectedObject;
    private ObjectTree tree;
    private Button finish;
    private int rowCount;
    private int columnCount;
    private ObservableList<String> columnHeaderNamesDataTable = FXCollections.observableArrayList();
    private ObservableList<Pair<String, ArrayList<String>>> pairList = FXCollections.observableArrayList();
    private ObservableList<String> listUnits = FXCollections.observableArrayList();
    private ObservableList<String> listUnitSymbols = FXCollections.observableArrayList();

    public ManualWizardStep2(ObjectTree tree, WizardSelectedObject wizardSelectedObject) {
        this.wizardSelectedObject = wizardSelectedObject;
        this.tree = tree;
        setMinSize(700, 830);

        setGraphic(null);
    }

    @Override
    public void onEnteringPage(Wizard wizard) {
        ObservableList<ButtonType> list = getButtonTypes();

        for (ButtonType type : list) {
            if (type.getButtonData().equals(ButtonBar.ButtonData.BACK_PREVIOUS)) {
                Node prev = lookupButton(type);
                prev.visibleProperty().setValue(Boolean.FALSE);
            }

            if (type.getButtonData().equals(ButtonBar.ButtonData.FINISH)) {
                finish = (Button) lookupButton(type);
                finish.setDisable(true);
                finish.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        //Lies die Daten von der Tabelle und Speichere sie in die pairList ab.
                        for (int i = 0; i < grid.getRowCount(); i++) {
                            String spcObjectName = rows.get(i).get(0).getText();

                            if (!spcObjectName.equals("")) {
                                ArrayList<String> attributs = new ArrayList<>();
                                for (int j = 1; j < grid.getColumnCount(); j++) {
                                    SpreadsheetCell spcAttribut = rows.get(i).get(j);
                                    attributs.add(spcAttribut.getText());
                                }
                                pairList.add(new Pair(spcObjectName, attributs));
                            }
                        }

                        //Erzeuge Data Object
                        //Data Class
                        JEVisClass dataClass = null;
                        List<JEVisClass> listDataDirectoryClasses = null;
                        // Data Point Class
                        JEVisClass dataPointClass = null;
                        List<JEVisClass> listDataPointClasses = null;

                        try {
                            //Get the all children from created DataDirectory und DataPointDirectory.
                            listDataDirectoryClasses = wizardSelectedObject.getCurrentDataDirectory().getAllowedChildrenClasses();
                            listDataPointClasses = wizardSelectedObject.getCurrentDataPointDirectory().getAllowedChildrenClasses();

                            for (JEVisClass classElement : listDataDirectoryClasses) {
                                if (classElement.getName().endsWith("Data")) {
                                    dataClass = classElement;
                                }
                            }

                            for (JEVisClass classElement : listDataPointClasses) {
                                if (classElement.getName().endsWith("Data Point")) {
                                    dataPointClass = classElement;
                                }
                            }

                            //Lies alles von der pairList ab und erzeuge die Objekte.
                            for (Pair<String, ArrayList<String>> pair : pairList) {
                                //Parent ist DataDirectory.
                                //Commit Data Object
                                JEVisObject newDataObject = wizardSelectedObject.getCurrentDataDirectory().buildObject(pair.getKey(), dataClass);
                                newDataObject.commit();
                                final TreeItem<JEVisObject> newDataObjectTreeItem = tree.buildItem(newDataObject);

                                TreeItem<JEVisObject> parentDataDirectory = tree.getObjectTreeItem(wizardSelectedObject.getCurrentDataDirectory());

                                parentDataDirectory.getChildren().add(newDataObjectTreeItem);

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        tree.getSelectionModel().select(newDataObjectTreeItem);
                                    }
                                });
                                // Set attribute for DisplayUnit and InputUnit
                                JEVisAttribute attributeValue = newDataObject.getAttribute("Value");

                                if (pair.getValue().get(0).isEmpty() && pair.getValue().get(1).isEmpty()) {
                                    attributeValue.setDisplayUnit(new JEVisUnitImp("", "", JEVisUnit.Prefix.NONE));
                                } else {
                                    String displaySymbol = pair.getValue().get(1);
                                    if (pair.getValue().get(0).isEmpty() && !pair.getValue().get(1).isEmpty()) {
                                        attributeValue.setDisplayUnit(new JEVisUnitImp(Unit.valueOf(displaySymbol), "", JEVisUnit.Prefix.NONE));
                                    } else {
                                        JEVisUnit.Prefix prefixDisplayUnit = JEVisUnit.Prefix.valueOf(pair.getValue().get(0));
                                        attributeValue.setDisplayUnit(new JEVisUnitImp(Unit.valueOf(displaySymbol), "", prefixDisplayUnit));
                                    }
                                }

                                if (pair.getValue().get(0).isEmpty() && pair.getValue().get(1).isEmpty()) {
                                    attributeValue.setInputUnit(new JEVisUnitImp("", "", JEVisUnit.Prefix.NONE));
                                } else {
                                    String displaySymbol = pair.getValue().get(1);
                                    if (pair.getValue().get(0).isEmpty() && !pair.getValue().get(1).isEmpty()) {
                                        attributeValue.setInputUnit(new JEVisUnitImp(Unit.valueOf(displaySymbol), "", JEVisUnit.Prefix.NONE));
                                    } else {
                                        JEVisUnit.Prefix prefixDisplayUnit = JEVisUnit.Prefix.valueOf(pair.getValue().get(0));
                                        attributeValue.setInputUnit(new JEVisUnitImp(Unit.valueOf(displaySymbol), "", prefixDisplayUnit));
                                    }
                                }

                                attributeValue.commit();
                                //Parent ist DataPointDirectory
                                //Commit Data Point Object
                                JEVisObject newDataPointObject = wizardSelectedObject.getCurrentDataPointDirectory().buildObject(pair.getKey(), dataPointClass);
                                newDataPointObject.commit();

                                final TreeItem<JEVisObject> newDataPointTreeItem = tree.buildItem(newDataPointObject);
                                
                                TreeItem<JEVisObject> parentDataPointDirectory = tree.getObjectTreeItem(wizardSelectedObject.getCurrentDataPointDirectory());

                                parentDataPointDirectory.getChildren().add(newDataPointTreeItem);

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        tree.getSelectionModel().select(newDataPointTreeItem);
                                    }
                                });
                                // Create the samples  for the Target and Value Identifier attribute.
                                JEVisAttribute attributeTarget = newDataPointObject.getAttribute("Target");
                                attributeTarget.buildSample(new DateTime(), newDataObject.getID()).commit();

                                //Die neue Version hat 'Value Identifier' attribut nicht! Value Identifier--> Objectname zb. Data001
//                              JEVisAttribute attributeValueIdentifier = newDataPointObject.getAttribute("Value Identifier");
//                              attributeValueIdentifier.buildSample(new DateTime(), pair.getValue().get(2)).commit();
                            }
                        } catch (JEVisException ex) {
                            Logger.getLogger(ManualWizardStep1.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        //Expand the building Object.
                        tree.expandSelected(true);
                    }
                }
                );
            }
        }
        setContent(getInit());
    }

    private BorderPane getInit() {
        new CreateNewWizardTable(finish);

        BorderPane root = new BorderPane();

        root.setCenter(spv);
        return root;

    }

    private void addUnits() {
        JEVisUnit.Prefix[] prefixes = JEVisUnit.Prefix.values();

        for (int i = 0; i < prefixes.length; i++) {
            String strPrefix = prefixes[i].toString();
            listUnits.add(strPrefix);
        }
    }

    private void addSymbols() {
        listUnitSymbols.addAll("m/s\u00B2",
                "g", "mol", "atom", "rad", "bit", "\u0025", "centiradian", "dB", "\u00b0", "\u0027", "byte", "rev", "\u00A8", "sphere", "sr", "rad/s\u00B2", "rad/s", "Bq", "Ci", "Hz",
                "m\u00B2", "a", "ha", "cm\u00B2", "km\u00B2", "kat", "\u20AC", "\u20A6", "\u20B9", "\u0024", "*\u003F*", "\u00A5", "Hits/cm\u00B2", "Hits/m\u00B2", "\u03A9/cm\u00B2", "bit/s", "\u002D", "s", "m", "h", "day", "day_sidereal",
                "week", "month", "year", "year_calendar", "year_sidereal", "g/\u0028cms\u0029", "F", "C", "e", "Fd", "Fr", "S", "A", "Gi", "H", "V", "\u03A9", "J",
                "eV", "erg", "N", "dyn", "kgf", "lbf", "lx", "La", "W/m\u00B2", "m\u00B2/s", "cm\u00B2/s", "\u00C5", "ua", "cm", "foot_survey_us", "ft", "in", "km", "ly",
                "mi", "mm", "nmi", "pc", "pixel", "pt", "yd", "W", "Wb", "Mx", "T", "G", "kg", "u", "me", "t", "oz", "lb", "ton_uk", "ton_us", "kg/s",
                "cd", "hp", "lm", "var", "Pa", "atm", "bar", "in Hg", "mmHg", "Gy", "rem", "Sv", "rd", "Rd", "rev/s", "grade", "K", "\u00b0C", "\u00b0F", "\u00b0R",
                "Nm", "Wh", "Ws", "m/s", "c", "km/h", "kn", "Mach", "mph", "m\u00B3", "in\u00B3", "gallon_dry_us", "gal", "gallon_uk", "l", "oz_uk", "kg/m\u00B3", "m\u00B3/s");
    }

    // Erstelle eine neue Tabelle
    class CreateNewWizardTable {

        public CreateNewWizardTable(Button finish) {
            String[] colNames = {"Object Name", "Prefix", "Symbol", "Channel"};
            rowCount = 1000;
            columnCount = colNames.length;

            grid = new GridBase(rowCount, columnCount);

            for (int row = 0; row < grid.getRowCount(); ++row) {
                cells = FXCollections.observableArrayList();
                for (int column = 0; column < grid.getColumnCount(); ++column) {
                    cells.add(SpreadsheetCellType.STRING.createCell(row, column, 1, 1, ""));
                }
                rows.add(cells);
            }
            grid.setRows(rows);
            spv = new SpreadsheetViewTable(rows, grid);
            spv.setGrid(grid);
            spv.getStylesheets().add("styles/Table.css");
            ObservableList<SpreadsheetColumn> colList = spv.getColumns();

            for (SpreadsheetColumn colListElement : colList) {
                colListElement.setPrefWidth(150);
            }

            spv.setEditable(true);
            spv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            columnHeaderNamesDataTable.addAll(colNames);

            spv.getGrid().getColumnHeaders().addAll(columnHeaderNamesDataTable);

            addUnits();
            addSymbols();
            //GridChange Event for Prefix and Symbol Input Control
            spv.getGrid().addEventHandler(GridChange.GRID_CHANGE_EVENT, new EventHandler<GridChange>() {

                @Override
                public void handle(GridChange event) {
                    inputControl(finish);
                }
            });
        }
    }

    public void inputControl(Button finish) {
        ObservableList<String> listPrefix = FXCollections.observableArrayList();
        ObservableList<String> listSymbols = FXCollections.observableArrayList();

        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcPrefix = rows.get(i).get(1);
            if (!spcPrefix.getText().equals("")) {
                listPrefix.add(spcPrefix.getText());
            }
        }
        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcSymbol = rows.get(i).get(2);
            if (!spcSymbol.getText().equals("")) {
                listSymbols.add(spcSymbol.getText());
            }
        }

        if (listUnits.containsAll(listPrefix) && listUnitSymbols.containsAll(listSymbols)) {
            finish.setDisable(false);
        } else {
            finish.setDisable(true);
        }

        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcPrefix = rows.get(i).get(1);
            if (!spcPrefix.getText().equals("")) {
                if (!listUnits.contains(spcPrefix.getText())) {
                    spcPrefix.getStyleClass().add("spreadsheet-cell-error");
                } else {
                    spcPrefix.getStyleClass().remove("spreadsheet-cell-error");
                }
            } else {
                spcPrefix.getStyleClass().remove("spreadsheet-cell-error");
            }
        }

        for (int i = 0; i < grid.getRowCount(); i++) {
            SpreadsheetCell spcSymbol = rows.get(i).get(2);
            if (!spcSymbol.getText().equals("")) {
                if (!listUnitSymbols.contains(spcSymbol.getText())) {
                    spcSymbol.getStyleClass().add("spreadsheet-cell-error");
                } else {
                    spcSymbol.getStyleClass().remove("spreadsheet-cell-error");
                }
            } else {
                spcSymbol.getStyleClass().remove("spreadsheet-cell-error");
            }
        }

        listSymbols.clear();
        listPrefix.clear();
    }
}
