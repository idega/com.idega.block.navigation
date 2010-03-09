package com.idega.block.navigation.bean;

import javax.faces.component.UIComponent;

public class NavigationListBean {
	
	private String pageKey = null;
	private UIComponent object = null;
	private boolean hiddenInMenu = false;
	
	public NavigationListBean() {}
	
	public NavigationListBean(String pageKey, UIComponent object, boolean hiddenInMenu) {
		this();
		
		this.pageKey = pageKey;
		this.object = object;
		this.hiddenInMenu = hiddenInMenu;
	}

	public String getPageKey() {
		return pageKey;
	}

	public void setPageKey(String pageKey) {
		this.pageKey = pageKey;
	}

	public UIComponent getObject() {
		return object;
	}

	public void setObject(UIComponent object) {
		this.object = object;
	}

	public boolean isHiddenInMenu() {
		return hiddenInMenu;
	}

	public void setHiddenInMenu(boolean hiddenInMenu) {
		this.hiddenInMenu = hiddenInMenu;
	}
}