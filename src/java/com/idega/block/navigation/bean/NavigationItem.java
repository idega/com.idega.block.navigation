package com.idega.block.navigation.bean;

import java.util.ArrayList;
import java.util.Collection;

import com.idega.builder.business.PageTreeNode;

public class NavigationItem {

	private Collection<NavigationItem> children;

	private PageTreeNode node = null;
	
	private String URI = null;
	private String name = null;
	
	private Collection styles = null;
	private String styleClass = null;

	private boolean hasChildren = false;
	private boolean current = false;
	private boolean currentAncestor = false;
	private boolean open = false;
	private boolean hidden = false;
	private boolean category = false;
	private int index = -1;
	private int depth = -1;

	public Collection<NavigationItem> getChildren() {
		return children;
	}

	public void setChildren(Collection<NavigationItem> children) {
		this.children = children;
		setHasChildren(this.children != null && !this.children.isEmpty());
	}

	public PageTreeNode getNode() {
		return node;
	}

	public void setNode(PageTreeNode node) {
		this.node = node;
	}

	public String getURI() {
		return URI;
	}

	public void setURI(String uRI) {
		URI = uRI;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStyleClass() {
		return styleClass;
	}

	public void setStyleClass(String styleClass) {
		if (styles == null) {
			styles = new ArrayList();
		}
		
		if (!styles.contains(styleClass)) {
			if (this.styleClass != null) {
				this.styleClass += " " + styleClass;
			}
			else {
				this.styleClass = styleClass;
			}
			styles.add(styleClass);
		}
	}

	public boolean isHasChildren() {
		return hasChildren;
	}

	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}

	public boolean isCurrentAncestor() {
		return currentAncestor;
	}

	public void setCurrentAncestor(boolean currentAncestor) {
		this.currentAncestor = currentAncestor;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isCategory() {
		return category;
	}

	public void setCategory(boolean category) {
		this.category = category;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
}