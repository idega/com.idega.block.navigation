package com.idega.block.navigation.presentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.ejb.FinderException;

import com.idega.builder.business.PageTreeNode;
import com.idega.builder.dynamicpagetrigger.presentation.DPTCrawlableLink;
import com.idega.builder.dynamicpagetrigger.util.DPTCrawlableContainer;
import com.idega.core.builder.data.ICPage;
import com.idega.core.builder.data.ICPageHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;

public class NavigationListDPTCrawlableContainer extends NavigationList implements DPTCrawlableContainer {


	public Collection getDPTCrawlables() {
		Collection children = new ArrayList();
		
		Vector v = new Vector();
		if (getShowRoot()) {
			children.add(getRootNode());
		}
		
		children.addAll(getRootNode().getChildren());
		
		if (children != null && !children.isEmpty()) {
			Iterator chiter = children.iterator();
			while (chiter.hasNext()) {
				Object o = chiter.next();
				if (o instanceof PageTreeNode) {
					PageTreeNode l = (PageTreeNode) o;
					String dptTempateID = l.getId();
					DPTCrawlableLink dpt = new DPTCrawlableLink();
					dpt.setDPTTemplateId(Integer.parseInt(dptTempateID));
					dpt.setText(l.getNodeName());
					
					v.add(dpt);
				}
			}
		}
		return v;
	}

	public void setRootId(int rootId) {
		if (rootId > 0) {
			try {
				ICPageHome home = (ICPageHome) IDOLookup.getHome(ICPage.class);
				setRootPage(home.findByPrimaryKey(rootId));
			} catch (IDOLookupException e) {
				e.printStackTrace();
			} catch (FinderException e) {
				e.printStackTrace();
			}
		}
	}
	
	public int getRootId() {
		return Integer.parseInt(super.getRootNodeId().toString());
	}

}
