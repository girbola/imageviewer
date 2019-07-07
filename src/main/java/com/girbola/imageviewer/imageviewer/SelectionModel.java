/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.imageviewer.imageviewer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/**
 *
 * @author Marko Lokka
 */
public class SelectionModel {

	final private String style_deselected = "-fx-border-color: white;" + "-fx-border-radius: 1 1 <1 1;" + "-fx-border-style: none;"
			+ "-fx-border-width: 2px;";
	final private String style_removed = "-fx-border-color: red;" + "-fx-border-width: 2px;";
	final private String style_selected = "-fx-border-color: red;" + "-fx-border-width: 2px;";

	private SimpleIntegerProperty selectedIndicator_property = new SimpleIntegerProperty();

	private ObservableList<Node> selectionList = FXCollections.observableArrayList();

	public SelectionModel() {
		this.selectionList.addListener(new ListChangeListener<Node>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Node> c) {
				selectedIndicator_property.set(selectionList.size());
			}
		});
	}

	public String getStyle_removed() {
		return style_removed;
	}

	public synchronized void addAll(Node node) {
		// Dialogs.sprintf("addAll: " + node);
		if (!contains(node)) {
			node.setStyle(style_selected);
			this.selectionList.add(node);
		}
	}

	public synchronized void add(Node node) {
		// Dialogs.sprintf("add: " + node);

		if (!contains(node)) {
			node.setStyle(style_selected);
			this.selectionList.add(node);
			// }
		} else {
			Dialogs.sprintf("remove: " + node);
			remove(node);
		}

	}

	public synchronized void addOnly(Node node) {
		if (!contains(node)) {
			node.setStyle(style_selected);
			this.selectionList.add(node);
		}

	}

	public synchronized void clearAll() {
		Dialogs.sprintf("clearing all");

		while (!this.selectionList.isEmpty()) {
			remove(this.selectionList.iterator().next());
		}

	}

	public synchronized boolean contains(Node node) {
		return this.selectionList.contains(node);
	}

	public void log() {
		Dialogs.sprintf("Items in model: " + Arrays.asList(selectionList.toArray()));
	}

	public synchronized void remove(Node node) {

		Dialogs.sprintf("removing effect: " + node.getId());
		if (contains(node)) {
			node.getStyleClass().remove(style_selected);
			node.setStyle(style_deselected);
			this.selectionList.remove(node);

		}
	}

	public synchronized void invertSelection(Pane pane) {
		if (pane == null || !pane.isVisible()) {
			return;
		}
		List<Node> list = new ArrayList<>();

		if (pane instanceof GridPane) {
			for (Node grid : pane.getChildren()) {
				// if (grid instanceof VBox) {
				if (!contains(grid)) {
					list.add(grid);
					// }
				}
			}
		}

		if (list.isEmpty()) {
			Dialogs.sprintf("list was empty at selectionModel lineb " + selectionList.size());
			return;
		}
		clearAll();
		list.forEach((n) -> {
			add(n);
		});
		list.clear();

	}

	public synchronized void isAllSelected(CheckBox checkBox, List<Path> list) {
		List<Path> sel = new ArrayList<>();
		if (list == null) {
			Dialogs.sprintf("isAllSelected getUserData were null");
			return;
		}
		for (Path path : list) {
			for (Node node : selectionList) {
				Path node_fl = (Path) node.getUserData();
				if (node_fl.equals(path)) {
					sel.add(node_fl);
				}
			}
		}
		if (sel.size() == 0) {
			checkBox.setSelected(false);
		} else if (sel.size() == list.size()) {
			checkBox.setSelected(true);
		} else if (sel.size() != list.size()) {
			checkBox.setIndeterminate(true);
		}
	}

	public synchronized void setSelectionList(ObservableList<Node> selectionList) {
		this.selectionList = selectionList;
	}

	public ObservableList<Node> getSelectionList() {
		return this.selectionList;
	}

	public SimpleIntegerProperty getSelectedIndicator_property() {
		return this.selectedIndicator_property;
	}

	public synchronized void setSelected(SimpleIntegerProperty selected) {
		this.selectedIndicator_property = selected;
	}

}
