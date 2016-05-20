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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;
import org.jevis.api.JEVisSample;
import org.jevis.application.dialog.ConfirmDialog;
import org.jevis.application.dialog.ExceptionDialog;
import org.jevis.application.dialog.InfoDialog;
import org.jevis.commons.CommonClasses;
import org.jevis.commons.CommonObjectTasks;
import org.jevis.jeconfig.JEConfig;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ObjectTree extends TreeView<JEVisObject> {

    private ObjectEditor _editor = new ObjectEditor();
    private JEVisDataSource _ds;

    private HashMap<Long, TreeItem<JEVisObject>> _itemCache;
    private HashMap<Long, ObjectGraphic> _graphicCache;
    private HashMap<TreeItem<JEVisObject>, ObservableList<TreeItem<JEVisObject>>> _itemChildren;
    private ObservableList<TreeItem<JEVisObject>> _emtyList = FXCollections.emptyObservableList();

    private JEVisObject _dragObj;
    private boolean _isDrag = false;
    private double treeHieght;
    private SimpleBooleanProperty loadingProperty = new SimpleBooleanProperty();
    private SimpleBooleanProperty loadingObjectProperty = new SimpleBooleanProperty();

    private TreeItem<JEVisObject> _lastItemToCopy = null;

    public ObjectTree() {

    }

    public ObjectTree(JEVisDataSource ds) {
        super();
        try {
            _ds = ds;
            _itemCache = new HashMap<>();
            _graphicCache = new HashMap<>();
            _itemChildren = new HashMap<>();

            getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<JEVisObject>>() {

                @Override
                public void changed(ObservableValue<? extends TreeItem<JEVisObject>> ov, TreeItem<JEVisObject> t, TreeItem<JEVisObject> t1) {
                    try {
                        if (t1.getValue() != null && t1.getValue().getJEVisClass() != null) {
                            loadingObjectProperty.setValue(true);
                            if (t1.getValue().getJEVisClass().getName().equals(CommonClasses.LINK.NAME)) {
                                System.out.println("changed: oh object is a link so im loading the linked object");
                                _editor.setObject(t1.getValue().getLinkedObject());
                            } else {
                                _editor.setObject(t1.getValue());
                            }
                            loadingObjectProperty.setValue(false);
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            treeHieght = getHeight();

            setCellFactory(new Callback<TreeView<JEVisObject>, TreeCell<JEVisObject>>() {
                @Override
                public TreeCell<JEVisObject> call(TreeView<JEVisObject> p) {
                    return new ObjectCell();
                }
            });

            final KeyCombination copyID = new KeyCodeCombination(KeyCode.F1);
//            final KeyCombination copyObj = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);
            final KeyCombination copyObj = KeyCodeCombination.keyCombination("Ctrl+C");
            final KeyCombination pasteObj = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);
            final KeyCombination add = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
            final KeyCombination rename = new KeyCodeCombination(KeyCode.F2);
            final KeyCombination delete = new KeyCodeCombination(KeyCode.DELETE);
            final KeyCombination pageDown = new KeyCodeCombination(KeyCode.PAGE_DOWN);

            //Keycombinations need Key Pressed
            addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

                @Override
                public void handle(KeyEvent t) {
                    if (copyObj.match(t)) {
                        copyCurrentItem();
                        t.consume();
                    } else if (pasteObj.match(t)) {
                        pasteCopyItem();
                    }
                }
            });

            //Singel key events anre released
            addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {

                @Override
                public void handle(KeyEvent t) {

                    try {
//                        System.out.println("Keyevent: " + t);

                        JEVisObject selectedObject = getSelectionModel().getSelectedItem().getValue();

                        if (copyID.match(t)) {
                            System.out.println("Copy ID");
                            Clipboard clip = Clipboard.getSystemClipboard();
                            ClipboardContent content = new ClipboardContent();

                            content.putString(getSelectionModel().getSelectedItem().getValue().getID().toString());
                            System.out.println("CopyID: " + getSelectionModel().getSelectedItem().getValue().getID().toString());
                            clip.setContent(content);
                            t.consume();
                        } else if (rename.match(t)) {
                            System.out.println("F2 rename event");
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    fireEventRename();
                                }
                            });
                            t.consume();

                        } else if (delete.match(t)) {
                            fireDelete(getSelectedObject());
                            t.consume();
                        } else if (add.match(t)) {
                            fireEventNew(selectedObject);
                            t.consume();
                        }

                    } catch (Exception ex) {
                        System.out.println("execption while tree key event: " + ex);
                    }
                }

            }
            );

            setId("objecttree");

            getStylesheets().add("/styles/Styles.css");
            setPrefWidth(JEConfig.getStage().getWidth() / 1.5);//500

            setShowRoot(false);
//
//            List<JEVisObject> roots = ds.getRootObjects();
//            TreeItem<JEVisObject> rootItem = buildItem(roots.get(0));

            JEVisObject root = new JEVisRootObject(ds);
            TreeItem<JEVisObject> rootItem = buildItem(root);
            setRoot(rootItem);

            getSelectionModel().select(rootItem);
            setEditable(true);

        } catch (Exception ex) {
//            Logger.getLogger(ObjectTree.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }

    }

    /**
     * Pased the object copied
     */
    public void pasteCopyItem() {
        if (_lastItemToCopy != null && getSelectionModel().getSelectedItem() != null) {
            showMoveDialog(_lastItemToCopy.getValue(), getSelectionModel().getSelectedItem().getValue());
        } else {
            System.out.println("Error 6");
        }

    }

    /**
     * Cupy the current Item into the clpbord & also remeber it for the past
     * function
     */
    public void copyCurrentItem() {
        Clipboard clip = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        JEVisObject obj = getSelectionModel().getSelectedItem().getValue();
        _lastItemToCopy = getSelectionModel().getSelectedItem();
        String text = "";
        try {
            text = String.format("ID: %s Name: %s Class: %s", obj.getID().toString(), obj.getName(), obj.getJEVisClass().getName());
        } catch (JEVisException ex) {
            Logger.getLogger(ObjectTree.class.getName()).log(Level.SEVERE, null, ex);
        }
        content.putString(text);
        clip.setContent(content);
    }

    public SimpleBooleanProperty getLoadingProperty() {
        return loadingProperty;
    }

    public void setLoadingProperty(SimpleBooleanProperty loadingProperty) {
        this.loadingProperty = loadingProperty;
    }

    public SimpleBooleanProperty getLoadingObjectProperty() {
        return loadingObjectProperty;
    }

    public void setLoadingObjectProperty(SimpleBooleanProperty loadingObjectProperty) {
        this.loadingObjectProperty = loadingObjectProperty;
    }

    public JEVisObject getDragItem() {
        return _dragObj;
    }

    public void setDragItem(JEVisObject obj) {
        _dragObj = obj;
    }

    public ObjectGraphic getObjectGraphic(JEVisObject object) {

        if (_graphicCache.containsKey(object.getID())) {
            return _graphicCache.get(object.getID());
        }

//        System.out.println("grahic does not exist create for: " + object);
        ObjectGraphic graph = new ObjectGraphic(object, this);
        _graphicCache.put(object.getID(), graph);

        return graph;
    }

    public TreeItem<JEVisObject> getObjectTreeItem(JEVisObject object) {
        return buildItem(object);
    }

    public TreeItem<JEVisObject> buildItem(JEVisObject object) {
        if (_itemCache.containsKey(object.getID())) {
            return _itemCache.get(object.getID());
        }

//        System.out.println("buildItem: " + object);
        final TreeItem<JEVisObject> newItem = new ObjectItem(object, this);
        _itemCache.put(object.getID(), newItem);

        return _itemCache.get(object.getID());
    }

    public void addChildrenList(TreeItem<JEVisObject> item, ObservableList<TreeItem<JEVisObject>> list) {

        loadingProperty.setValue(true);
        _itemChildren.put(item, list);
        try {
            for (JEVisObject child : item.getValue().getChildren()) {
                TreeItem<JEVisObject> newItem = buildItem(child);
                list.add(newItem);
            }
        } catch (JEVisException ex) {
            Logger.getLogger(ObjectTree.class.getName()).log(Level.SEVERE, null, ex);
        }
        sortList(list);
        loadingProperty.setValue(false);

    }

    public ObservableList<TreeItem<JEVisObject>> getChildrenList(TreeItem<JEVisObject> item) {
        if (item == null || item.getValue() == null) {
            return _emtyList;
        }

        if (_itemChildren.containsKey(item)) {
            return _itemChildren.get(item);
        }

        Task<ObservableList<TreeItem<JEVisObject>>> load = new Task<ObservableList<TreeItem<JEVisObject>>>() {
            @Override
            protected ObservableList<TreeItem<JEVisObject>> call() throws Exception {
                ObservableList<TreeItem<JEVisObject>> list = FXCollections.observableArrayList();
                try {
                    for (JEVisObject child : item.getValue().getChildren()) {
                        TreeItem<JEVisObject> newItem = buildItem(child);
                        list.add(newItem);
                    }
                } catch (JEVisException ex) {
                    Logger.getLogger(ObjectTree.class.getName()).log(Level.SEVERE, null, ex);
                }
                sortList(list);
                _itemChildren.put(item, list);

                return list;
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
                        System.out.println("success");
                        JEConfig.getStage().getScene().setCursor(Cursor.DEFAULT);

                    }
                });
            }
        });

        new Thread(load).start();

        try {
            return load.get();
        } catch (Exception ex) {
            Logger.getLogger(ObjectTree.class.getName()).log(Level.SEVERE, null, ex);
            return FXCollections.observableArrayList();
        }

    }

    private void getAllExpanded(List<TreeItem<JEVisObject>> list, TreeItem<JEVisObject> item) {
        if (item.isExpanded()) {
            list.add(item);
            for (TreeItem<JEVisObject> i : item.getChildren()) {
                getAllExpanded(list, i);
            }
        }
    }

    private void expandAll(List<TreeItem<JEVisObject>> list, TreeItem<JEVisObject> root) {
//        System.out.println("expand all");
        for (final TreeItem<JEVisObject> item : root.getChildren()) {
            for (final TreeItem<JEVisObject> child : list) {
                if (item.getValue().getID().equals(child.getValue().getID())) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            item.setExpanded(true);
                        }
                    });

                }
            }
            expandAll(list, item);
        }
    }

    public void reload() {

    }

    public void expandSelected(boolean expand) {
        TreeItem<JEVisObject> item = getSelectionModel().getSelectedItem();
        expand(item, expand);
    }

    private void expand(TreeItem<JEVisObject> item, boolean expand) {
        if (!item.isLeaf()) {
            if (item.isExpanded() && !expand) {
                item.setExpanded(expand);
            } else if (!item.isExpanded() && expand) {
                item.setExpanded(expand);
            }

            for (TreeItem<JEVisObject> child : item.getChildren()) {
                expand(child, expand);
            }
        }
    }

    public void fireEventRename() {
        System.out.println("fireRename");
//        setEditFix(true);
//        edit(_cl.getCurrentItem());

        JEVisObject currentObject = getSelectionModel().getSelectedItem().getValue();
        final TreeItem<JEVisObject> currentItem = getSelectionModel().getSelectedItem();

        NewObjectDialog dia = new NewObjectDialog();
        if (currentObject != null) {
            try {
                if (dia.show(JEConfig.getStage(),
                        currentObject.getJEVisClass(),
                        currentObject,
                        true,
                        NewObjectDialog.Type.RENAME,
                        currentItem.getValue().getName()
                ) == NewObjectDialog.Response.YES) {
                    try {
                        if (!dia.getCreateName().isEmpty()) {
                            currentItem.getValue().setName(dia.getCreateName());
                            currentItem.getValue().commit();
                            getObjectGraphic(currentObject).update();

//                            Platform.runLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    currentItem.getParent().setExpanded(false);
//                                    currentItem.getParent().setExpanded(true);
//                                }
//                            });
                        }

                    } catch (JEVisException ex) {
                        Logger.getLogger(ObjectTree.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (JEVisException ex) {
                Logger.getLogger(ObjectTree.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void fireSaveAttributes(boolean ask) throws JEVisException {

        if (ask) {
            _editor.checkIfSaved(null);
        } else {
            _editor.commitAll();
        }

    }

    public void fireDelete(JEVisObject obj) {
        ConfirmDialog dia = new ConfirmDialog();
        String question = "Do you want to delete \"" + obj.getName() + "\" ?";

        if (dia.show(JEConfig.getStage(), "Delete Object", "Delete Object?", question) == ConfirmDialog.Response.YES) {
            try {
                System.out.println("User want to delete: " + obj.getName());

                obj.delete();
                getObjectTreeItem(obj).getParent().getChildren().remove(getObjectTreeItem(obj));
                getSelectionModel().select(getObjectTreeItem(obj).getParent());

            } catch (Exception ex) {
                ex.printStackTrace();
//                    Dialogs.showErrorDialog(JEConfig.getStage(), ex.getMessage(), "Error", "Error", ex);

            }
        }
    }

    public JEVisObject getSelectedObject() {
        return getSelectionModel().getSelectedItem().getValue();
    }

    public void fireEventNew(final JEVisObject parent) {

        NewObjectDialog dia = new NewObjectDialog();
//        JEVisObject currentObject = _cl.getCurrentItem().getValue().getObject();
//        final TreeItem currentItem = _cl.getCurrentItem();

        if (parent != null) {
            if (dia.show(JEConfig.getStage(), null, parent, false, NewObjectDialog.Type.NEW, null) == NewObjectDialog.Response.YES) {
                System.out.println("create new: " + dia.getCreateName() + " class: " + dia.getCreateClass() + " " + dia.getCreateCount() + " times");

                for (int i = 0; i < dia.getCreateCount(); i++) {
                    try {
                        //TODo check for uniq
//                if(!dia.getCreateClass().isUnique()){
//
//                }
                        String name = dia.getCreateName();
                        if (dia.getCreateCount() > 1) {
                            name += " " + (i + 1);
                        }

                        JEVisObject newObject = parent.buildObject(name, dia.getCreateClass());
                        newObject.commit();
                        final TreeItem<JEVisObject> newTreeItem = buildItem(newObject);
                        TreeItem<JEVisObject> parentItem = getObjectTreeItem(parent);

                        parentItem.getChildren().add(newTreeItem);

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                getSelectionModel().select(newTreeItem);
                            }
                        });

                    } catch (JEVisException ex) {
                        //TODO: Cancel all if one faild befor he has to see the exeption dia.getCreateCount() times
                        Logger.getLogger(ObjectTree.class.getName()).log(Level.SEVERE, null, ex);
                        //TODO: the need an unique way to identify erors

                        if (ex.getMessage().equals("Can not create User with this name. The User has to be unique on the System")) {
                            InfoDialog info = new InfoDialog();
                            info.show(JEConfig.getStage(), "Waring", "Could not create user", "Could not create new user because this user exists already.");

                        } else {
                            ExceptionDialog errorDia = new ExceptionDialog();
                            errorDia.show(JEConfig.getStage(), "Error", "Could not create user", "Could not create new user.", ex, JEConfig.PROGRAMM_INFO);

                        }

                    }
                }

            }
        }

    }

    //TODO i dont like this way
    public ObjectEditor getEditor() {
        return _editor;
    }

    public static void sortList(ObservableList<TreeItem<JEVisObject>> list) {
        Comparator<TreeItem<JEVisObject>> sort = new Comparator<TreeItem<JEVisObject>>() {

            @Override
            public int compare(TreeItem<JEVisObject> o1, TreeItem<JEVisObject> o2) {
//                System.out.println("Compare: \n " + o1 + " with\n " + o2);
                try {
                    if (o2.getValue().getJEVisClass() != null && o1.getValue().getJEVisClass() != null) {
                        int classCom = o1.getValue().getJEVisClass().compareTo(o2.getValue().getJEVisClass());

                        if (classCom == 0) {//Class is the same now use Name
                            return o2.getValue().getJEVisClass().compareTo(o2.getValue().getJEVisClass());
                        } else {
                            return classCom;
                        }
                    } else {
//                        System.out.println("null Sort: o1:" + o1.getValue() + " o2:" + o2.getValue());
                        if (o1.getValue().getJEVisClass() != null && o2.getValue().getJEVisClass() != null) {
                            return o1.getValue().getJEVisClass().compareTo(o2.getValue().getJEVisClass());
                        } else {
                            return 0;
                        }

                    }

                } catch (Exception ex) {
//                    Logger.getLogger(ObjectItem.class.getName()).log(Level.SEVERE, null, ex);
//                    throw new NullPointerException();
                    ex.printStackTrace();
                    return 0;
                }
            }
        };

        FXCollections.sort(list, sort);
    }

    /**
     *
     * @param moveObj
     * @param targetObj
     */
    public void moveObject(final JEVisObject moveObj, final JEVisObject targetObj) {
        try {
            System.out.println("add new Relationship: " + moveObj.getName() + "-> " + targetObj.getName());
            JEVisRelationship newRel = moveObj.buildRelationship(targetObj, JEVisConstants.ObjectRelationship.PARENT, JEVisConstants.Direction.FORWARD);

            // remove other parent relationships
            for (JEVisRelationship rel : moveObj.getRelationships(JEVisConstants.ObjectRelationship.PARENT)) {
                if (!rel.equals(newRel) && rel.getStartObject().equals(moveObj)) {
                    System.out.println("remove relationship " + moveObj.getName() + " -> " + rel.getOtherObject(moveObj).getName());
                    moveObj.deleteRelationship(rel);

                    TreeItem<JEVisObject> dragParentItem = getObjectTreeItem(rel.getOtherObject(moveObj));
                    getChildrenList(dragParentItem).remove(getObjectTreeItem(moveObj));

                }
            }

            //move gui
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    TreeItem<JEVisObject> dragItem = getObjectTreeItem(moveObj);
                    TreeItem<JEVisObject> targetItem = getObjectTreeItem(targetObj);
                    getChildrenList(targetItem).add(dragItem);
                }
            });

        } catch (JEVisException ex) {
            Logger.getLogger(ObjectCell.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    public void copyObject(final JEVisObject toCopyObj, final JEVisObject newParent, String newName, boolean includeContent, boolean recursive) {
        try {

            System.out.println("-> Copy (" + toCopyObj + ") under (" + newParent + ")");
            JEVisObject newObject = newParent.buildObject(newName, toCopyObj.getJEVisClass());

            for (JEVisAttribute originalAtt : toCopyObj.getAttributes()) {
                JEVisAttribute newAtt = newObject.getAttribute(originalAtt.getType());
                //Copy the basic attribute config
                newAtt.setDisplaySampleRate(originalAtt.getDisplaySampleRate());
                newAtt.setDisplayUnit(originalAtt.getDisplayUnit());
                newAtt.setInputSampleRate(originalAtt.getInputSampleRate());
                newAtt.setInputUnit(originalAtt.getInputUnit());

                //if chosen copy the samples
                if (includeContent) {
                    if (originalAtt.hasSample()) {

                        List<JEVisSample> newSamples = new ArrayList<>();
                        for (JEVisSample sample : originalAtt.getAllSamples()) {
                            newSamples.add(newAtt.buildSample(sample.getTimestamp(), sample.getValue(), sample.getNote()));
                        }
                        newAtt.addSamples(newSamples);
                    }
                }
            }

            //TODO: we need an recursive check to avoid an endless loop
            //Also copy the children if chosen
            if (recursive) {
                for (JEVisObject otherChild : toCopyObj.getChildren()) {
                    System.out.println("---> Copy Child: " + otherChild);
                    copyObject(otherChild, newObject, otherChild.getName(), includeContent, recursive);
                }
            }

            TreeItem<JEVisObject> dragTargetItem = getObjectTreeItem(newParent);
            TreeItem<JEVisObject> newItem = getObjectTreeItem(newObject);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    getChildrenList(dragTargetItem).add(newItem);

                }
            });

        } catch (JEVisException ex) {
            showError("Error", ex.getMessage(), ex.getCause().getMessage());
            Logger.getLogger(ObjectCell.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    private void showError(String title, String titleLong, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(titleLong);
        alert.setContentText(message);
        alert.show();
    }

    /**
     *
     * @param linkSrcObj
     * @param targetParent
     * @param linkName
     */
    public void buildLinkObject(JEVisObject linkSrcObj, final JEVisObject targetParent, String linkName) {
        try {
            System.out.println("build new link(" + linkName + "): " + linkSrcObj.getName() + " -> " + targetParent.getName());

            JEVisObject newLinkObj = targetParent.buildObject(linkName, _ds.getJEVisClass(CommonClasses.LINK.NAME));

            try {
                CommonObjectTasks.createLink(newLinkObj, linkSrcObj);
                final TreeItem<JEVisObject> treeItem = buildItem(newLinkObj);

                //move gui
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        TreeItem<JEVisObject> targetItem = getObjectTreeItem(targetParent);
                        targetItem.setExpanded(false);
                        getChildrenList(targetItem).add(treeItem);
                        targetItem.setExpanded(true);
                        getSelectionModel().select(treeItem);

                    }
                });

            } catch (Exception ex) {
                Logger.getLogger(ObjectTree.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (JEVisException ex) {
            Logger.getLogger(ObjectTree.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param dragObj
     * @param targetParent
     */
    private void showMoveDialog(JEVisObject dragObj, JEVisObject targetParent) {
//        System.out.println("showMoveDialog: romove: " + dragObj + "  targetParent: " + targetParent);
        CopyObjectDialog dia = new CopyObjectDialog();
        CopyObjectDialog.Response re = dia.show(JEConfig.getStage(), dragObj, targetParent);

        if (re == CopyObjectDialog.Response.MOVE) {
            moveObject(dragObj, targetParent);
        } else if (re == CopyObjectDialog.Response.LINK) {
            buildLinkObject(dragObj, targetParent, dia.getCreateName());
        } else if (re == CopyObjectDialog.Response.COPY) {
//            System.out.println("Copy count: " + dia.getCreateCount());
            for (int i = 0; i < dia.getCreateCount(); ++i) {
//                System.out.println("Copy #" + i);
                copyObject(dragObj, targetParent, dia.getCreateName(), dia.isIncludeData(), dia.isRecursion());
            }

        }
    }

    /**
     *
     */
    public class ObjectCell extends TreeCell<JEVisObject> {

        @Override
        protected void updateItem(final JEVisObject obj, boolean emty) {

            super.updateItem(obj, emty);
            if (!emty) {
//                System.out.print("->");
                ObjectGraphic grph = getObjectGraphic(obj);
                setText(grph.getText());
                setGraphic(grph.getGraphic());
                setTooltip(grph.getToolTip());
                setContextMenu(grph.getContexMenu());

                //---------------------- Drag & Drop part --------------
                setOnDragDetected(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent e) {
//                        System.out.println("Drag deteced");
                        _isDrag = true;
//                        System.out.println("Drag Source: " + obj.getName());
                        ClipboardContent content = new ClipboardContent();
//                        content.putString(obj.getName());
                        Dragboard dragBoard = startDragAndDrop(TransferMode.ANY);
                        content.put(DataFormat.PLAIN_TEXT, obj.getName());
                        dragBoard.setContent(content);

                        setDragItem(obj);
                        e.consume();

                    }
                });

                setOnDragDone(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent dragEvent) {
//                        System.out.println("Drag done on " + obj.getName());
                        dragEvent.consume();
                        _isDrag = false;
                    }
                });

                //TODO: ceh if its ok to move the Object here
                setOnDragOver(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent dragEvent) {
//                        System.out.println("Drag Over: " + obj.getName());

                        try {
                            if (getDragItem().isAllowedUnder(obj)) {
                                dragEvent.acceptTransferModes(TransferMode.ANY);
                            }

                            if (obj.getJEVisClass().getName().equals("View Directory") || obj.getJEVisClass().getName().equals(CommonClasses.LINK.NAME)) {
                                dragEvent.acceptTransferModes(TransferMode.ANY);
                            }

                        } catch (JEVisException ex) {
                            Logger.getLogger(ObjectTree.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        dragEvent.consume();

                    }
                });

                setOnDragDropped(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(final DragEvent dragEvent) {
//                        System.out.println("\nDrag dropped on " + obj.getName());
//                        System.out.println("To Drag: " + getDragItem().getName());
                        dragEvent.consume();//to disable the drag cursor

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (dragEvent.isAccepted()) {
//                                    JEConfig.getStage().getScene().setCursor(Cursor.DEFAULT);
                                    showMoveDialog(getDragItem(), obj);
                                }

                            }
                        });

                    }
                });

            } else {
                setText(null);
                setGraphic(null);
            }

        }
    }

}
