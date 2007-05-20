package com.idega.block.navigation.presentation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.faces.component.UIComponent;

import com.idega.builder.business.PageTreeNode;
import com.idega.builder.dynamicpagetrigger.presentation.DPTCrawlableLink;
import com.idega.builder.dynamicpagetrigger.util.DPTCrawlableContainer;
import com.idega.core.data.ICTreeNode;
import com.idega.presentation.Page;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Span;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;

public class NavigationListDPTCrawlableContainer extends NavigationList implements DPTCrawlableContainer {

	private HashMap map = new HashMap();
	
	public String getId() {
		return super.getId();
	}
		
	protected Link constructLink(PresentationObject po) {
		String key = null;
		if (po instanceof Span) {
			Iterator its = po.getChildren().iterator();
			Text text = null;
			while (its.hasNext() && text == null) {
				try {
					text = (Text) its.next();
				} catch (Exception e) {
				}
			}
			if (text != null) {
				key = text.getText();
			}
		}

		DPTCrawlableLink link = null;
		if (key != null && map.containsKey(key)) {
			link = (DPTCrawlableLink) map.get(key);
		} else {
			link = new DPTCrawlableLink();
		}

		link.setPresentationObject(po);

		return link;
	}

	public Collection getDPTCrawlables() {
		UIComponent comp = this.getParent();
		while (comp != null && !(comp instanceof Page)) {
			comp = comp.getParent();
		}
		
		Collection children = getRootNode().getChildren();
		
		if (children != null && !children.isEmpty()) {
			Iterator chiter = children.iterator();
			Vector v = new Vector();
			while (chiter.hasNext()) {
				Object o = chiter.next();
				if (o instanceof PageTreeNode) {
					PageTreeNode l = (PageTreeNode) o;
					int dptTempateID = l.getNodeID();
					DPTCrawlableLink dpt = new DPTCrawlableLink();
					dpt.setDPTTemplateId(dptTempateID);
					dpt.setText(l.getNodeName());
					
					map.put(l.getNodeName(), dpt);
					
					v.add(dpt);
				}
			}
		


			return v;
		}
		return null;
	}
	
	protected void addParameterToLink(Link link, ICTreeNode page, boolean parametersOnly) {
		super.addParameterToLink(link, page, true);
	}


}
