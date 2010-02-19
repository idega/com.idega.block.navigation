package com.idega.block.navigation.bean;

import java.util.Collection;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("breadCrumbsBean")
@Scope("request")
public class BreadCrumbsBean {

	private Collection<NavigationItem> children;

	private String id;
	private String styleClass;
	private String divider;

	public Collection<NavigationItem> getChildren() {
		return children;
	}

	public void setChildren(Collection<NavigationItem> children) {
		this.children = children;
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

	public String getDivider() {
		return divider;
	}

	public void setDivider(String divider) {
		this.divider = divider;
	}
}