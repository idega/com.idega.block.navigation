package com.idega.block.navigation.presentation;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.httpclient.HttpException;

import com.idega.business.IBOLookup;
import com.idega.content.data.WebDAVBean;
//import com.idega.content.presentation.ContentViewer;
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
				_currentPages = new ArrayList();
				_selectedPages = new ArrayList();
				String url = iwc.getParameter(PARAMETER_FOLDER_PATH);
				
				IWSlideSession ss = (IWSlideSession) IBOLookup.getSessionInstance(iwc, IWSlideSession.class);
				url = url.replaceFirst(ss.getWebdavServerURI(), "");
				
				
				WebdavExtendedResource selectedNode = ss.getWebdavResource(url);
				WebDAVBean selectedParent = new WebDAVBean(selectedNode);
				while (selectedParent != null && !selectedParent.getWebDavUrl().equals( getRootNodeId() )) {
					_currentPages.add(selectedParent.getWebDavUrl());
					_selectedPages.add(selectedParent.getWebDavUrl());
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
		else
			System.out.println("No selected page parameter in request.");
	}
	
	protected void addParameterToLink(Link link, ICTreeNode node) {
		link.addParameter(PARAMETER_FOLDER_PATH, ((WebDAVBean)node).getWebDavUrl());
		if (thePageId > 0) {
			link.setPage(thePageId);
		}
		link.setPage(24);
	}
	
	
	protected ICTreeNode getRootNode() {
		return rootNode;
	}
	
	protected Object getRootNodeId() {
		return rootNode.getWebDavUrl();
	}
	
	protected void setRootNode(IWContext iwc) throws RemoteException {
		IWSlideSession ss = (IWSlideSession) IBOLookup.getSessionInstance(iwc, IWSlideSession.class);
		try {
			WebdavExtendedResource root = ss.getWebdavResource("/files/contribute-web");
//			WebdavExtendedResource root = ss.getWebdavResource(rootFolder);
			rootNode = new WebDAVBean(root);
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
		while (children.hasNext()) {
			Object obj = children.next();
			try {
			WebDAVBean node = (WebDAVBean) obj;
			

				addObject(iwc, node, table, row, depth);
				row = setRowAttributes(table, node, row, depth, !children.hasNext());
				
				if (isOpen(node) && node.getChildCount() > 0) {
					row = addToTree(iwc, node.getChildrenIterator(), table, row, depth + 1);
				}
			} catch (ClassCastException c) {
				c.printStackTrace();
			}
		}
		
		return row;
	}

	protected boolean isSelected(ICTreeNode page) {
		boolean returner = false;
		if (_selectedPages != null && _selectedPages.contains(((WebDAVBean) page).getWebDavUrl()))
			returner =  true;
		return returner;
	}
	
	protected boolean isCurrent(ICTreeNode page) {
		boolean returner = false;
		if (_currentPages != null && _currentPages.contains(((WebDAVBean) page).getWebDavUrl()))
			returner = true;
		return returner;
	}

	public void setPage(int pageId) {
		this.thePageId = pageId;
	}
	
	
}
