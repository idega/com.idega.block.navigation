package com.idega.block.navigation.uicomponent;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.httpclient.HttpException;
import com.idega.block.navigation.presentation.NavigationTree;
import com.idega.business.IBOLookup;
import com.idega.content.data.WebDAVBean;
import com.idega.core.data.ICTreeNode;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.WebdavExtendedResource;

/**
 * @author gimmi
 */
public class FolderNavigationTree extends NavigationTree {

	private String rootFolder = "";
	private WebDAVBean rootNode;
	private int thePageId = -1;
	
	private static final String PARAMETER_FOLDER_PATH = "cv_prt";//For now, should be ContentViewer.PARAMETER_ROOT_FOLDER;
	
	protected void parse(IWContext iwc) {
		if (iwc.isParameterSet(PARAMETER_FOLDER_PATH)) {
			System.out.println("Selected page parameter is set.");
			try {
				this._currentPages = new ArrayList();
				this._selectedPages = new ArrayList();
				String url = iwc.getParameter(PARAMETER_FOLDER_PATH);
				
				IWSlideSession ss = (IWSlideSession) IBOLookup.getSessionInstance(iwc, IWSlideSession.class);
				url = url.replaceFirst(ss.getWebdavServerURI(), "");
				
				
				WebdavExtendedResource selectedNode = ss.getWebdavResource(url);
				WebDAVBean selectedParent = new WebDAVBean(selectedNode);
				while (selectedParent != null && !selectedParent.getWebDavUrl().equals( getRootNodeId() )) {
					this._currentPages.add(selectedParent.getWebDavUrl());
					this._selectedPages.add(selectedParent.getWebDavUrl());
					selectedParent = (WebDAVBean) selectedParent.getParentNode();
				}
			} catch (HttpException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("No selected page parameter in request.");
		}
	}
	
	protected void addParameterToLink(Link link, ICTreeNode node) {
		link.addParameter(PARAMETER_FOLDER_PATH, ((WebDAVBean)node).getWebDavUrl());
		if (this.thePageId > 0) {
			link.setPage(this.thePageId);
		}
	}
	
	
	protected ICTreeNode getRootNode() {
		return this.rootNode;
	}
	
	protected Object getRootNodeId() {
		return this.rootNode.getWebDavUrl();
	}
	
	protected void setRootNode(IWContext iwc) throws RemoteException {
		IWSlideSession ss = (IWSlideSession) IBOLookup.getSessionInstance(iwc, IWSlideSession.class);
		try {
			WebdavExtendedResource root = ss.getWebdavResource(this.rootFolder);
			this.rootNode = new WebDAVBean(root);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	} 

	protected String getLocalizedName(ICTreeNode node, IWContext iwc) {
		return node.getNodeName();
	}
	
	protected boolean getIsCategory(ICTreeNode node) {
		return !node.isLeaf();
	}
	
	public void setRootFolder(String rootFolder) {
		this.rootFolder = rootFolder;
	}

	protected int addToTree(IWContext iwc, Iterator children, Table table, int row, int depth) {
		int index = 0;
		while (children.hasNext()) {
			Object obj = children.next();
			try {
			WebDAVBean node = (WebDAVBean) obj;
			

				addObject(iwc, node, table, row, depth, false);
				row = setRowAttributes(table, node, row, depth, (index == 0), !children.hasNext());
				
				if (isOpen(node) && node.getChildCount() > 0) {
					row = addToTree(iwc, node.getChildrenIterator(), table, row, depth + 1);
				}
			} catch (ClassCastException c) {
				c.printStackTrace();
			}
			index++;
		}
		
		return row;
	}

	protected boolean isSelected(ICTreeNode page) {
		boolean returner = false;
		if (this._selectedPages != null && this._selectedPages.contains(((WebDAVBean) page).getWebDavUrl())) {
			returner =  true;
		}
		return returner;
	}
	
	protected boolean isCurrent(ICTreeNode page) {
		boolean returner = false;
		if (this._currentPages != null && this._currentPages.contains(((WebDAVBean) page).getWebDavUrl())) {
			returner = true;
		}
		return returner;
	}

	public void setPage(int pageId) {
		this.thePageId = pageId;
	}
	
	
}
