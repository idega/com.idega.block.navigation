/*
 * $Id: NavigationList.java,v 1.2 2005/03/01 23:22:25 tryggvil Exp $
 * Created on 16.2.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.navigation.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import com.idega.builder.business.PageTreeNode;
import com.idega.business.IBOLookup;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.data.ICPage;
import com.idega.core.business.ICTreeNodeComparator;
import com.idega.core.data.ICTreeNode;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Text;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;


/**
 * <p>
 * This is the new standard "NavigationList" component, for displaying a list of pages (part of the webtree) within an idegaWeb site to link to.
 * It is based purely on CSS style classes for defining its layout.
 * There is a subclass of this called "NavigationTree" that is based on a older "table" based layout which is now discouraged to use
 * because of Web standards compliance.
 * </p>
 *  Last modified: $Date: 2005/03/01 23:22:25 $ by $Author: tryggvil $
 * 
 * @author <a href="mailto:tryggvil@idega.com">tryggvil</a>
 * @version $Revision: 1.2 $
 */
public class NavigationList extends Block {

	private final static String PARAMETER_SELECTED_PAGE = "nt_selected_page";
	private final static String SESSION_ATTRIBUTE_OPEN_ON_USER_HOMEPAGE = "nt_open_on_user_homepage";
	private final static String IW_BUNDLE_IDENTIFIER = "com.idega.block.navigation";

	private String textStyleName = "text";
	private String linkStyleName = "link";

	private transient IWResourceBundle _iwrb;
	private transient IWBundle _iwb;
	private transient BuilderService _builderService;
	private transient ICTreeNode _currentPage;

	private int _currentPageID;
	private ICTreeNode _rootPage;
	private int _maxDepthForStyles = -1;

	protected Collection _currentPages;
	protected Collection _selectedPages;

	private boolean _showRoot = false;
	private boolean _useDifferentStyles = false;
	private boolean _autoCreateHoverStyles = false;
	private boolean _debug = false;
	private boolean _markOnlyCurrentPage = false;
	private boolean iOpenOnUserHomePage = false;
	private boolean iOrderPagesAlphabetically = false;

	private int _rootPageID = -1;
	
	private Map _depthOrderPagesAlphabetically;


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.presentation.PresentationObject#main(com.idega.presentation.IWContext)
	 */
	public void main(IWContext iwc) throws Exception {
		_iwb = getBundle(iwc);
		_iwrb = getResourceBundle(iwc);
		_builderService = getBuilderService(iwc);

		setRootNode(iwc);

		parse(iwc);
		add(getTree(iwc));
	}

	protected void setRootNode(IWContext iwc) throws RemoteException {
		if (_rootPageID == -1) {
			_rootPageID = _builderService.getRootPageId();
		}
		_rootPage = new PageTreeNode(_rootPageID, iwc);
	}

	/**
	 * Initialized the main <code>ul</code> tag and adds all page links to the
	 * tree.
	 * 
	 * @param iwc
	 * @return
	 */
	protected UIComponent getTree(IWContext iwc) {
		//TODO: implement a CSS version here
		Lists list = new Lists();
		String styleClass = getStyleClass();
		if(styleClass!=null){
			list.setStyleClass(styleClass);
		}
		int row = 1;
		int depth = 0;

		//row = addHeaderObject(table, row);
		if (getShowRoot()) {
			addObject(iwc, getRootNode(), list, row, depth);
			//setRowAttributes(list, getRootNode(), row, depth, false);
		}

		row = addToTree(iwc, getRootNode().getChildren(), list, row, depth);

		return list;
	}


	/**
	 * Adds the given <code>Collection</code> of child pages to the UIComponent holding the list
	 * @param iwc
	 * @param childrenCollection
	 * @param pageList
	 * @param row
	 * @param depth
	 * @return
	 */
	protected int addToTree(IWContext iwc, Collection childrenCollection, UIComponent pageList, int row, int depth) {
		
		List pagesList = new ArrayList(childrenCollection);
		if (getDepthOrderPagesAlphabetically(depth)) {
			Collections.sort(pagesList, new ICTreeNodeComparator(iwc.getCurrentLocale()));
		}
		
		Iterator children = pagesList.iterator();
		while (children.hasNext()) {
			ICTreeNode page = (ICTreeNode) children.next();

			boolean hasPermission = true;
			try {
				String pageKey = String.valueOf(page.getNodeID());
				//Page populatedPage = getBuilderService(iwc).getPage(pageKey);
				//hasPermission = iwc.hasViewPermission(populatedPage);
				hasPermission = iwc.getAccessController().hasViewPermissionForPageKey(pageKey,iwc);
			}
			catch (Exception re) {
				log(re);
			}

			if (hasPermission) {
				UIComponent nodeComponent = getNodeComponent(pageList,row,depth);
				
				addObject(iwc, page, nodeComponent, row, depth);
				row = setRowAttributes(nodeComponent, page, row, depth, !children.hasNext());

				if (isOpen(page) && page.getChildCount() > 0) {
					UIComponent newList = getSubTreeComponent(nodeComponent,row,depth);
					row = addToTree(iwc, page.getChildren(), newList, row, depth + 1);
				}
			}
		}

		return row;
	}
	
	/**
	 * Gets the UIComponent used for a new iteration for a new subpages node.<br>
	 * The default implementation is returning a new Lists component.
	 * @param outerContainer
	 * @param row
	 * @param depth
	 * @return
	 */
	protected UIComponent getSubTreeComponent(UIComponent outerContainer,int row,int depth){
		Lists newList = new Lists();
		outerContainer.getChildren().add(newList);
		return newList;
	}
	
	/**
	 * Gets the UIComponent used for a each node in the list.<br>
	 * The default implementation is returning a new ListItem component and adding to the list.
	 * @param outerContainer
	 * @param row
	 * @param depth
	 * @return
	 */
	protected UIComponent getNodeComponent(UIComponent outerContainer,int row,int depth){
		ListItem item = new ListItem();
		outerContainer.getChildren().add(item);
		return item;
	}
	
	protected int setRowAttributes(UIComponent listComponent, ICTreeNode page, int row, int depth, boolean isLastChild) {
		//Does nothing here but is used by the NavigationTree component
		return row;
	}
	
	/**
	 * Adds the <code>PresentationObject</code> corresponding to the specified
	 * <code>PageTreeNode</code> and depth.
	 * 
	 * @param iwc
	 * @param page
	 * @param table
	 * @param row
	 * @param depth
	 */
	protected void addObject(IWContext iwc, ICTreeNode page, UIComponent list, int row, int depth) {
		UIComponent link = getLink(page, iwc, depth);
		list.getChildren().add(link);
	}
	
	/**
	 * Gets the <code>PresentationObject</code> corresponding to the specified
	 * <code>PageTreeNode</code> and depth.
	 * 
	 * @param page
	 * @param iwc
	 * @param depth
	 * @return
	 */
	protected String getLocalizedName(ICTreeNode node, IWContext iwc) {
		return ((PageTreeNode) node).getLocalizedNodeName(iwc);
	}

	protected boolean getIsCategory(ICTreeNode node) {
		return ((PageTreeNode) node).isCategory();
	}

	protected UIComponent getLink(ICTreeNode page, IWContext iwc, int depth) {
		String name = getLocalizedName(page, iwc);

		if (page.getNodeID() != getCurrentPageId()) {
			Link link = getStyleLink(name, getStyleName(linkStyleName, depth));
			addParameterToLink(link, page);
			return link;
		}
		else {
			Text text = getStyleText(name, getStyleName(textStyleName, depth));
			return text;
		}
	}
	
	protected int getCurrentPageId(){
		return _currentPageID;
	}

	protected void addParameterToLink(Link link, ICTreeNode page) {
		boolean isCategory = getIsCategory(page);
		if (!isCategory)
			link.setPage(page.getNodeID());
		else
			link.addParameter(PARAMETER_SELECTED_PAGE, page.getNodeID());
	}




	/**
	 * Gets whether to order pages for specified depth level alphabetically.  Will
	 * return the default value if nothing is set for the specified depth.
	 * 
	 * @param depth
	 *          The depth to get whether to order alphabetically or not.
	 * @return
	 */
	protected boolean getDepthOrderPagesAlphabetically(int depth) {
		if (_depthOrderPagesAlphabetically != null) {
			Boolean order = (Boolean) _depthOrderPagesAlphabetically.get(new Integer(depth));
			if (order != null) {
				return order.booleanValue();
			}
		}
		return iOrderPagesAlphabetically;
	}

	/**
	 * Gets the stylename for the specified depth.
	 * 
	 * @param styleName
	 *          The stylename to use.
	 * @param depth
	 *          The depth to get the stylename for.
	 * @return
	 */
	private String getStyleName(String styleName, int depth) {
		if (_useDifferentStyles) {
			if (_maxDepthForStyles != -1 && depth >= _maxDepthForStyles)
				styleName = styleName + "_" + _maxDepthForStyles;
			else
				styleName = styleName + "_" + depth;
		}
		return styleName;
	}

	/**
	 * Checks to see if the specified <code>PageTreeNode</code> is open or
	 * closed.
	 * 
	 * @param page
	 *          The <code>PageTreeNode</code> to check.
	 * @return
	 */
	protected boolean isOpen(ICTreeNode page) {
		boolean isOpen = isCurrent(page);
		if (!isOpen)
			isOpen = isSelected(page);
		return isOpen;
	}

	/**
	 * Checks to see if the specified <code>PageTreeNode</code> is the current
	 * page or one of its parent pages.
	 * 
	 * @param page
	 *          The <code>PageTreeNode</code> to check.
	 * @return
	 */
	protected boolean isCurrent(ICTreeNode page) {
		if (_currentPages != null && _currentPages.contains(new Integer(page.getNodeID())))
			return true;
		return false;
	}

	/**
	 * Checks to see if the specified <code>PageTreeNode</code> is the selected
	 * page of one of its parent pages.
	 * 
	 * @param page
	 *          The <code>PageTreeNode</code> to check.
	 * @return
	 */
	protected boolean isSelected(ICTreeNode page) {
		if (_selectedPages != null && _selectedPages.contains(new Integer(page.getNodeID())))
			return true;
		return false;
	}

	/**
	 * Retrieves the current page as well as the selected page and puts all
	 * parents into a <code>Collection</code> to draw the tree with the correct
	 * branches open/closed.
	 * 
	 * @param iwc
	 */
	protected void parse(IWContext iwc) {
		try {
			_currentPage = _builderService.getPageTree(_builderService.getCurrentPageId(iwc));
			_currentPageID = _currentPage.getNodeID();
			_currentPages = new ArrayList();
			_currentPages.add(new Integer(_currentPageID));
			debug("Current page is set.");
		
			if (_currentPageID != ((Integer) getRootNodeId()).intValue()) {
				ICTreeNode parent = _currentPage.getParentNode();
				if (parent != null) {
					while (parent != null && parent.getNodeID() != ((Integer) getRootNodeId()).intValue()) {
						debug("Adding page with ID = " + parent.getNodeID() + " to currentMap");
						_currentPages.add(new Integer(parent.getNodeID()));
						parent = parent.getParentNode();
						if (parent == null)
							break;
					}
				}
			}
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		
		if (iwc.isParameterSet(PARAMETER_SELECTED_PAGE)) {
			debug("Selected page parameter is set.");
			try {
				_selectedPages = new ArrayList();
				
				ICTreeNode selectedParent = _builderService.getPageTree(Integer.parseInt(iwc.getParameter(PARAMETER_SELECTED_PAGE)));
				while (selectedParent != null && selectedParent.getNodeID() != ((Integer) getRootNodeId()).intValue()) {
					debug("Adding page with ID = " + selectedParent.getNodeID() + " to selectedMap");
					_selectedPages.add(new Integer(selectedParent.getNodeID()));
					selectedParent = selectedParent.getParentNode();
				}
			}
			catch (RemoteException re) {
				re.printStackTrace();
			}
		}
		else {
			debug("No selected page parameter in request.");
			
			if (iOpenOnUserHomePage && iwc.isLoggedOn()) {
				boolean openOnUserHomePage = true;
				try {
					openOnUserHomePage = ((Boolean) iwc.getSessionAttribute(SESSION_ATTRIBUTE_OPEN_ON_USER_HOMEPAGE)).booleanValue();
				}
				catch (NullPointerException npe) {
					openOnUserHomePage = true;
				}
				
				try {
					User newUser = iwc.getCurrentUser();
					int homePageID = getUserBusiness(iwc).getHomePageIDForUser(newUser);
					if (openOnUserHomePage && homePageID != -1) {
						_selectedPages = new ArrayList();
						
						ICTreeNode selectedParent = _builderService.getPageTree(homePageID);
						while (selectedParent != null && selectedParent.getNodeID() != ((Integer) getRootNodeId()).intValue()) {
							debug("Adding page with ID = " + selectedParent.getNodeID() + " to selectedMap");
							_selectedPages.add(new Integer(selectedParent.getNodeID()));
							selectedParent = selectedParent.getParentNode();
						}
						iwc.setSessionAttribute(SESSION_ATTRIBUTE_OPEN_ON_USER_HOMEPAGE, Boolean.FALSE);
					}
				}
				catch (RemoteException re) {
					re.printStackTrace();
				}
			}
		}
	}
	protected UserBusiness getUserBusiness(IWContext iwc) throws java.rmi.RemoteException {
		return (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.presentation.PresentationObject#getBundleIdentifier()
	 */
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.presentation.Block#autoCreateGlobalHoverStyles()
	 */
	protected boolean autoCreateGlobalHoverStyles() {
		return _autoCreateHoverStyles;
	}

	/**
	 * Sets the page to use as the root for the tree. If nothing is selected, the
	 * root page of the <code>IBDomain</code> is used.
	 * 
	 * @param rootPageID
	 */
	public void setRootPage(ICPage rootPage) {
		_rootPageID = ((Integer) rootPage.getPrimaryKey()).intValue();
	}


	/**
	 * Sets to show the root page in the tree. Set to FALSE by default.
	 * 
	 * @param showRoot
	 */
	public void setShowRoot(boolean showRoot) {
		_showRoot = showRoot;
	}

	
	/**
	 * Gets if the root should be showed
	 * @return
	 */
	public boolean getShowRoot(){
		return _showRoot;
	}

	/**
	 * Sets to use different styles for each level of the tree. Set to FALSE by
	 * default.
	 * 
	 * @param useDifferentStyles
	 */
	public void setUseDifferentStyles(boolean useDifferentStyles) {
		_useDifferentStyles = useDifferentStyles;
	}

	/**
	 * Sets to auto create hovers styles in the style sheet for link styles. Set
	 * to FALSE by default.
	 * 
	 * @param autoCreateHoverStyles
	 */
	public void setAutoCreateHoverStyles(boolean autoCreateHoverStyles) {
		_autoCreateHoverStyles = autoCreateHoverStyles;
	}

	/**
	 * Sets to order pages alphabetically for a specific depth level.
	 * 
	 * @param depth
	 * @param orderAlphabetically
	 */
	public void setDepthOrderPagesAlphabetically(int depth, boolean orderAlphabetically) {
		if (_depthOrderPagesAlphabetically == null)
			_depthOrderPagesAlphabetically = new HashMap();
		_depthOrderPagesAlphabetically.put(new Integer(depth - 1), new Boolean(orderAlphabetically));
	}


	/**
	 * Sets the maximum depth for styles in the <code>NavigationTree</code>.
	 * Used to restrict how far down the tree new styles are specified. Set to -1
	 * by default, meaning no restrictions.
	 * 
	 * @param maxDepthForStyles
	 */
	public void setMaxDepthForStyles(int maxDepthForStyles) {
		_maxDepthForStyles = maxDepthForStyles;
	}


	/**
	 * Sets the name of the style to use for links in the stylesheet. Is set by
	 * default to a global name, can be altered to allow for individual settings.
	 * 
	 * @param linkStyleName
	 */
	public void setLinkStyleName(String linkStyleName) {
		this.linkStyleName = linkStyleName;
	}

	/**
	 * Sets the name of the style to use for texts in the stylesheet. Is set by
	 * default to a global name, can be altered to allow for individual settings.
	 * 
	 * @param textStyleName
	 */
	public void setTextStyleName(String textStyleName) {
		this.textStyleName = textStyleName;
	}



	/**
	 * Sets to debug actions.
	 * 
	 * @param debug
	 */
	public void setDebug(boolean debug) {
		_debug = debug;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.presentation.PresentationObject#debug(java.lang.String)
	 */
	public void debug(String outputString) {
		if (_debug)
			System.out.println("[NavigationTree]: " + outputString);
	}

	protected ICTreeNode getRootNode() {
		return _rootPage;
	}

	protected Object getRootNodeId() {
		return new Integer(_rootPageID);
	}

	/**
	 * @param onlyCurrentPage
	 *          The _markOnlyCurrentPage to set.
	 */
	public void setToMarkOnlyCurrentPage(boolean onlyCurrentPage) {
		_markOnlyCurrentPage = onlyCurrentPage;
	}
	
	/**
	 * Gets the value of the _markOnlyCurrentPage property
	 */
	public boolean getMarkOnlyCurrentPage() {
		return _markOnlyCurrentPage;
	}

	public void setOpenOnUserHomepage(boolean openOnUserHomepage) {
		iOpenOnUserHomePage = openOnUserHomepage;
	}
	
	public void setToOrderPagesAlphabetically(boolean orderAlphabetically) {
		iOrderPagesAlphabetically = orderAlphabetically;
	}

}