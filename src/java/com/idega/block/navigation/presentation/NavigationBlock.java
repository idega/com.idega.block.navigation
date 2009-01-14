package com.idega.block.navigation.presentation;

import java.util.Collection;

import com.idega.builder.business.PageTreeNode;
import com.idega.core.data.ICTreeNode;
import com.idega.presentation.Block;
import com.idega.presentation.text.Link;
import com.idega.util.ListUtil;

public class NavigationBlock extends Block {
	
	protected boolean getIsCategory(ICTreeNode node) {
		return node instanceof PageTreeNode ? ((PageTreeNode) node).isCategory() : false;
	}
	
	@SuppressWarnings("unchecked")
	protected void setAsCategoryPage(ICTreeNode page, Link link) {
		if (!getIsCategory(page)) {
			return;
		}
		
		Collection<ICTreeNode> children = page.getChildren();
		if (ListUtil.isEmpty(children)) {
			link.setURL("javascript:void(0)");
			return;
		}
		
		ICTreeNode firstChild = children.iterator().next();
		try {
			link.setPage(Integer.valueOf(firstChild.getId()));
		} catch(NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
}
