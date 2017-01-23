package com.idega.block.navigation.bean;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("navigationBean")
@Scope("request")
public class NavigationBean {

	private NavigationItem root;

	private String id;
	private String styleClass;

	private boolean showRoot;
	private boolean openAllNodes;

	private String itemPath;
	private boolean showPageDescription = false;

	private String disabledPages;

	public NavigationItem getRoot() {
		return root;
	}

	public void setRoot(NavigationItem root) {
		this.root = root;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStyleClass() {
		return styleClass;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	public boolean isShowRoot() {
		return showRoot;
	}

	public void setShowRoot(boolean showRoot) {
		this.showRoot = showRoot;
	}

	public boolean isOpenAllNodes() {
		return openAllNodes;
	}

	public void setOpenAllNodes(boolean openAllNodes) {
		this.openAllNodes = openAllNodes;
	}

	public String getItemPath() {
		return itemPath;
	}

	public void setItemPath(String itemPath) {
		this.itemPath = itemPath;
	}

	public boolean isShowPageDescription() {
		return showPageDescription;
	}

	public void setShowPageDescription(boolean showPageDescription) {
		this.showPageDescription = showPageDescription;
	}

	public String getDisabledPages() {
		return disabledPages;
	}

	public void setDisabledPages(String disabledPages) {
		this.disabledPages = disabledPages;
	}

}