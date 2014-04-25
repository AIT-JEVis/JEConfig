/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.classes;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.jevis.jeapi.JEVisClass;
import org.jevis.jeapi.JEVisException;
import org.jevis.jeapi.JEVisType;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class TypeTable {

    public TypeTable() {
    }

    public GridPane buildTree(JEVisClass jclass) {
        GridPane gridPane = new GridPane();
//        gridPane.setPadding(new Insets(20, 0, 20, 20));
        gridPane.setPadding(new Insets(0, 0, 0, 0));
        gridPane.setHgap(7);
        gridPane.setVgap(7);


        TableColumn otherClassCol = new TableColumn("Attribute");
        otherClassCol.setCellValueFactory(new PropertyValueFactory<RelationshipColum, String>("otherClass"));
        TableColumn typeCol = new TableColumn("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<RelationshipColum, String>("type"));
        TableColumn directionCol = new TableColumn("GUI Type");
        directionCol.setCellValueFactory(new PropertyValueFactory<RelationshipColum, String>("direction"));

        TableView table = new TableView();
        table.setMinWidth(555d);//TODo: replace Dirty workaround
        table.setPrefHeight(200d);//TODo: replace Dirty workaround
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(otherClassCol, typeCol, directionCol);




        try {
//            final ObservableList<JEVisClassRelationship> data = FXCollections.observableArrayList(jclass.getRelationships());
            List<TypeColum> tjc = new LinkedList<>();
            for (JEVisType rel : jclass.getTypes()) {
                System.out.println("add Types: " + rel);
                tjc.add(new TypeColum(rel));
            }

            final ObservableList<TypeColum> data = FXCollections.observableArrayList(tjc);
            table.setItems(data);


        } catch (JEVisException ex) {
            Logger.getLogger(ClassRelationshipTable.class.getName()).log(Level.SEVERE, null, ex);
        }

        gridPane.add(table, 0, 0);
        gridPane.setHgrow(table, Priority.ALWAYS);

        return gridPane;
    }
}