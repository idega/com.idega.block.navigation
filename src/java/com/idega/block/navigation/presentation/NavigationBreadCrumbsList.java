/*
 * $Id: NavigationBreadCrumbsList.java,v 1.2 2005/12/28 12:43:43 gimmi Exp $
 * Created on Dec 28, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.navigation.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.faces.context.FacesContext;
import com.idega.builder.business.PageTreeNode;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.data.ICPage;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Text;


public class NavigationBreadCrumbsList extends Block {
	
	private final static String IW_BUNDLE_IDENTIFIER = "com.idega.block.navigation";

	private ICPage rootPage = null;
	private boolean showRoot = true;
	private boolean ignoreCategoryPages = false;
	
	public void main(IWContext iwc) throws Exception {
		BuilderService iBuilderService = getBuilderService(iwc);

		if (rootPage == null) {
			rootPage = iBuilderService.getRootPage();
		}
		ICPage currentPage = iBuilderService.getCurrentPage(iwc);

		
		List pages = new ArrayList();
		PageTreeNode page = new PageTreeNode(((Integer) currentPage.getPrimaryKey()).intValue(), iwc);
		boolean showPage = true;
		boolean isCategoryPage = false;

		while (showPage) {
			if (page.getNodeID() == ((Integer) rootPage.getPrimaryKey()).intValue()) {
				showPage = false;
			}
			
			if (ignoreCategoryPages && page.isCategory()) {
				isCategoryPage = true;
			}
			else {
				isCategoryPage = false;
			}
			
			if (!isCategoryPage) {
				if (page.getNodeID() == ((Integer) currentPage.getPrimaryKey()).intValue()) {
					Text pageText = new Text(page.getLocalizedNodeName(iwc));
					pages.add(pageText);
				}
				else {
					Link pageLink = new Link(page.getLocalizedNodeName(iwc));
					pageLink.setPage(page.getNodeID());
					pages.add(pageLink);
				}
			}
			
			page = (PageTreeNode) page.getParentNode();
			if (page == null) {
				showPage = false;
			}
		}
		
		Collections.reverse(pages);
		
		Lists list = new Lists();
		list.setId(super.getId());
		Iterator iter = pages.iterator();
		boolean first = true;
		while (iter.hasNext()) {
			ListItem li = new ListItem();
			li.getChildren().add(iter.next());
			if (first) {
				first = false;
				if (!iter.hasNext()) {
					li.setStyleClass("firstPage lastPage");
				} else {
					li.setStyleClass("firstPage");
				}
			}
			if (!iter.hasNext()) {
				li.setStyleClass("lastPage");
			}
			list.getChildren().add(li);
		}
		
		add(list);
	}
	
	public void setRootPage(ICPage page) {
		rootPage = page;
	}
	
	public void setShowRootPage(boolean show) {
		showRoot = show;
	}
	
	public void setIgnoreCategoryPages(boolean hide) {
		ignoreCategoryPages = hide;
	}
	
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
	
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[4];
		values[0] = super.saveState(ctx);
		values[1] = new Boolean(ignoreCategoryPages);
		values[2] = rootPage;
		values[3] = new Boolean(showRoot);
		
		return values;
	}
	
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[])state;
		super.restoreState(ctx, values[0]);
		ignoreCategoryPages = ((Boolean) values[1]).booleanValue();
		rootPage = (ICPage) values[2];
		showRoot = ((Boolean) values[3]).booleanValue();
	}
}