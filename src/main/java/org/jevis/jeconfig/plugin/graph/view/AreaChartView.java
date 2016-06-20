/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph.view;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.application.jevistree.plugin.BarchartPlugin;
import org.jevis.application.jevistree.plugin.TableEntry;
import org.jevis.jeconfig.plugin.graph.data.GraphDataModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author broder
 */
public class AreaChartView implements Observer {

    private final GraphDataModel dataModel;
    private AreaChart<Number, Number> areaChart;
    private VBox vbox;
    private Region areaChartRegion;
    private final TableView table;
    private final ObservableList<TableEntry> tableData = FXCollections.observableArrayList();

    public AreaChartView(GraphDataModel dataModel) {
        this.dataModel = dataModel;
        dataModel.addObserver(this);

        table = new TableView();
        TableColumn name = new TableColumn("Name");
        name.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("name"));
        TableColumn value = new TableColumn("Value");
        value.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("value"));
        TableColumn dateCol = new TableColumn("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("date"));

        final ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
        TableEntry tableEntry = new TableEntry("testeintrag");
        tableData.add(tableEntry);
        table.setItems(tableData);

        table.getColumns().addAll(name, value, dateCol);
    }

    public XYChart getAreaChart() {
        return areaChart;
    }

    public void drawDefaultAreaChart() {
        ObservableList<XYChart.Series<Number, Number>> series = FXCollections.observableArrayList();

        ObservableList<XYChart.Data<Number, Number>> series1Data = FXCollections.observableArrayList();
        XYChart.Data<Number, Number> data1 = new XYChart.Data<Number, Number>(new GregorianCalendar(2012, 11, 15).getTime().getTime(), 2);
        Rectangle rect = new Rectangle(0, 0);
        rect.setVisible(false);
        data1.setNode(rect);
        series1Data.add(data1);
        series1Data.add(new XYChart.Data<Number, Number>(new GregorianCalendar(2014, 5, 3).getTime().getTime(), 4));

        ObservableList<XYChart.Data<Number, Number>> series2Data = FXCollections.observableArrayList();
        series2Data.add(new XYChart.Data<Number, Number>(new GregorianCalendar(2014, 0, 13).getTime().getTime(), 8));
        series2Data.add(new XYChart.Data<Number, Number>(new GregorianCalendar(2014, 7, 27).getTime().getTime(), 4));

        series.add(new XYChart.Series<>("Series1", series1Data));
        series.add(new XYChart.Series<>("Series2", series2Data));

        NumberAxis numberAxis = new NumberAxis();
//        DateAxis dateAxis = new DateAxis();
        Axis dateAxis = new DateValueAxis();
        areaChart = new AreaChart<>(dateAxis, numberAxis, series);
        areaChart.setTitle("default");
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            System.out.println("update chart view");
            this.drawAreaChart();
        } catch (JEVisException ex) {
            Logger.getLogger(AreaChartView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void drawAreaChart() throws JEVisException {
        Set<BarchartPlugin.DataModel> selectedData = dataModel.getSelectedData();

        ObservableList<XYChart.Series<Number, Number>> series = FXCollections.observableArrayList();

        String title = "Chart 1";
        for (BarchartPlugin.DataModel singleRow : selectedData) {
            System.out.println("curTitle:" + singleRow.getTitle());
            title = singleRow.getTitle();
            List<JEVisSample> samples = singleRow.getSamples();
            ObservableList<XYChart.Data<Number, Number>> series1Data = FXCollections.observableArrayList();
            TreeMap<Double, JEVisSample> sampleMap = new TreeMap();

            TableEntry tableEntry = new TableEntry(singleRow.getObject().getName());
            singleRow.setTableEntry(tableEntry);
            tableData.add(tableEntry);

            for (JEVisSample sample : samples) {
                sampleMap.put((double) sample.getTimestamp().getMillis(), sample);
                DateTime dateTime = sample.getTimestamp();
                Double value = sample.getValueAsDouble();
//                Date date = dateTime.toGregorianCalendar().getTime();
                Long timestamp = dateTime.getMillis();
                XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>(timestamp, value);

                Rectangle rect = new Rectangle(0, 0);
                rect.setVisible(false);
                data.setNode(rect);
                series1Data.add(data);
            }
            XYChart.Series<Number, Number> currentSerie = new XYChart.Series<>(singleRow.getObject().getName(), series1Data);
            singleRow.setSampleMap(sampleMap);
            series.add(currentSerie);
        }

        table.setItems(tableData);
        NumberAxis numberAxis = new NumberAxis();
        Axis dateAxis = new DateValueAxis();

        areaChart = new AreaChart<>(dateAxis, numberAxis, series);
        System.out.println("Title:" + title);
        areaChart.setTitle(title);
        areaChart.setCreateSymbols(false);
        areaChart.layout();

        areaChart.getXAxis().setAutoRanging(true);
        areaChart.getYAxis().setAutoRanging(true);

        areaChart.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Double valueForDisplay = (Double) areaChart.getXAxis().getValueForDisplay(mouseEvent.getX());
//                List<Double> values = new ArrayList<>();
                for (BarchartPlugin.DataModel singleRow : selectedData) {
                    Double higherKey = singleRow.getSampleMap().higherKey(valueForDisplay);
                    Double lowerKey = singleRow.getSampleMap().lowerKey(valueForDisplay);
                    Double nearest = higherKey;
                    if (lowerKey - valueForDisplay < higherKey - valueForDisplay) {
                        nearest = lowerKey;
                    }

                    try {
                        System.out.println("here");
                        Double valueAsDouble = singleRow.getSampleMap().get(nearest).getValueAsDouble();
                        TableEntry tableEntry = singleRow.getTableEntry();
                        DateTime dateTime = new DateTime(Math.round(nearest));
                        tableEntry.setDate(dateTime.toString(DateTimeFormat.forPattern("yyyy.MM.dd HH:mm:ss")));
                        tableEntry.setValue(valueAsDouble.toString());
                        table.layout();
                    } catch (JEVisException ex) {
                        Logger.getLogger(AreaChartView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
//                System.out.println(valueForDisplay);
//                for (Double d : values) {
//                    System.out.println(d);
//                }

            }
        });

        ChartPanManager panner = new ChartPanManager(areaChart);
        panner.setMouseFilter(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                System.out.println("mouse event");
                if (mouseEvent.getButton() == MouseButton.SECONDARY
                        || (mouseEvent.getButton() == MouseButton.PRIMARY
                        && mouseEvent.isShortcutDown())) {
                    //let it through
                } else {
                    mouseEvent.consume();
                }
            }
        });
        panner.start();
        areaChartRegion = JFXChartUtil.setupZooming(areaChart, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                System.out.println("zooming");
                if (mouseEvent.getButton() != MouseButton.PRIMARY
                        || mouseEvent.isShortcutDown()) {
                    mouseEvent.consume();
                }
            }
        });

        JFXChartUtil.addDoublePrimaryClickAutoRangeHandler(areaChart);
    }

    public Region getAreaChartRegion() {
        return areaChartRegion;
    }

    public VBox getVbox() {
        return vbox;
    }

    public TableView getLegend() {
        return table;
    }

}
