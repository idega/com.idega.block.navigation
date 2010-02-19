package com.idega.block.navigation.business;

import javax.faces.component.UIComponent;

public class NavigationListBean {
	
	private String pageKey = null;
	private UIComponent object = null;
	
	public NavigationListBean() {}
	
	public NavigationListBean(String pageKey, UIComponent object) {
		this();
		
		this.pageKey = pageKey;
		this.object = object;
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

}
