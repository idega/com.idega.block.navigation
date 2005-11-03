/*
 * $Id: NavigationList.java,v 1.17 2005/11/03 11:20:11 laddi Exp $
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
import java.util.Vector;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import com.idega.builder.business.PageTreeNode;
import com.idega.business.IBOLookup;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.data.ICPage;
import com.idega.core.business.ICTreeNodeComparator;
import com.idega.core.data.ICTreeNode;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Parameter;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;


/**
 * <p>
 * This is the new standard "NavigationList" component, for displaying a list of pages (part of the webtree) within an idegaWeb site to link to.
 * It is based purely on CSS style classes for defining its layout.
 * There is a subclass of this called "NavigationTree" that is based on a older "table" based layout which is now discouraged to use
 * because of Web standards compliance.
 * </p>
 *  Last modified: $Date: 2005/11/03 11:20:11 $ by $Author: laddi $
 * 
 * @author <a href="mailto:tryggvil@idega.com">tryggvil</a>
 * @version $Revision: 1.17 $
 */
public class NavigationList extends Block {

	private final static String PARAMETER_SELECTED_PAGE = "nt_selected_page";
	private final static String SESSION_ATTRIBUTE_OPEN_ON_USER_HOMEPAGE = "nt_open_on_user_homepage";
	private final static String IW_BUNDLE_IDENTIFIER = "com.idega.block.navigation";

	private String textStyleName = "text";
	private String linkStyleName = "link";

	private int _currentPageID;
	
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
	private boolean iHideSubPages = false;
	private boolean iUseStyleLinks = true;
	private boolean rootSelected = false;

	private int _rootPageID = -1;
	private String iSelectedID = null;
	private String iListID = null;
	private HashMap _parameters = new HashMap();
	
	private Map _depthOrderPagesAlphabetically;
	private boolean showForbiddenPagesAsDisabled=false;
	private boolean displaySelectedPageAsLink=false;
	private String disabledStyleClass="disabled";
	private String selectedStyleClass="selected";
	private String beforeSelectedStyleClass="beforeSelected";
	private String lastSelectedStyleClass="lastSelected";
	private String afterSelectedStyleClass="afterSelected";
	private String firstSelectedStyleClass="firstSelected";
	private String firstChildStyleClass="firstChild";
	private String lastChildStyleClass="lastChild";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.presentation.PresentationObject#main(com.idega.presentation.IWContext)
	 */
	public void main(IWContext iwc) throws Exception {

		setRootNode(iwc);
		parse(iwc);
		add(getTree(iwc));
	}

	protected void setRootNode(IWContext iwc) throws RemoteException {
		if (_rootPageID == -1) {
			_rootPageID = getBuilderService(iwc).getRootPageId();
		}
		//_rootPage = new PageTreeNode(_rootPageID, iwc);
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
		if (iListID != null) {
			list.setID(iListID);
		}
		String styleClass = getStyleClass();
		if(styleClass!=null){
			list.setStyleClass(styleClass);
		}
		int row = 1;
		int depth = 0;

		//row = addHeaderObject(table, row);
		if (getShowRoot()) {
			ICTreeNode page = getRootNode();
			if (isSelectedPage(page)) {
				rootSelected = true;
			}

			List pageList = new ArrayList();
			pageList.add(page);
			UIComponent nodeComponent = getNodeComponent(list,pageList,page,row,depth,0, false);
			((PresentationObject) nodeComponent).setStyleClass("firstChild");
			addObject(iwc, page, nodeComponent, row, depth, false);
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
		int index = 0;
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

			if (hasPermission||showForbiddenPagesAsDisabled) {
				
				boolean linkIsDisabled = (!hasPermission)&&showForbiddenPagesAsDisabled;
				UIComponent nodeComponent = getNodeComponent(pageList,pagesList,page,row,depth,index, linkIsDisabled);
				addObject(iwc, page, nodeComponent, row, depth, linkIsDisabled);
				row = setRowAttributes(nodeComponent, page, row, depth, (index == 0), !children.hasNext());

				//only recurse down tree if hasPermission==true
				if(hasPermission){
					if (isOpen(page) && page.getChildCount() > 0 && !iHideSubPages) {
						UIComponent newList = getSubTreeComponent(nodeComponent,row,depth);
						row = addToTree(iwc, page.getChildren(), newList, row, depth + 1);
					}
				}
			}
			index++;
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
	 * @param isdisabled TODO
	 * @return
	 */
	protected UIComponent getNodeComponent(UIComponent outerContainer,List pages,ICTreeNode page,int row,int depth,int index, boolean isdisabled){
		ListItem item = new ListItem();
		if (isSelectedPage(page)) {
			item.setStyleClass(getSelectedStyleClass());
			if (iSelectedID != null) {
				item.setID(iSelectedID);
			}
		}
		
		int pageIndex = (getShowRoot() && !page.equals(getRootNode())) ? (index + 2) : (index + 1);
		if (pageIndex % 2 == 0) {
			item.setStyleClass("even");
		}
		else {
			item.setStyleClass("odd");
		}
		
		if (pages != null) {
			int size = pages.size() - 1;
			if (index < size) {
				if (isSelectedPage((ICTreeNode) pages.get(index + 1))) {
					item.setStyleClass(getBeforeSelectedStyleClass());
				}
			}
			if (index == size && isSelectedPage(page) && !rootSelected) {
				item.setStyleClass(getLastSelectedStyleClass());
			}
			if (index > 0) {
				if (isSelectedPage((ICTreeNode) pages.get(index - 1))) {
					item.setStyleClass(getAfterSelectedStyleClass());
				}
			}
			if (index == 0 && isSelectedPage(page) && !getShowRoot()) {
				item.setStyleClass(getFirstSelectedStyleClass());
			}
			if (index == 0 && getShowRoot() && rootSelected && !page.equals(getRootNode())) {
				item.setStyleClass(getAfterSelectedStyleClass());
			}
			if(isdisabled){
				item.setStyleClass(getDisabledStyleClass());
			}
		}
		outerContainer.getChildren().add(item);
		return item;
	}
	
	private boolean isSelectedPage(ICTreeNode page) {
		if (isOpen(page) || page.getNodeID() == getCurrentPageId()) {
			return true;
		}
		return false;
	}
	
	protected int setRowAttributes(UIComponent listComponent, ICTreeNode page, int row, int depth, boolean isFirstChild, boolean isLastChild) {
		if (isFirstChild && !getShowRoot()) {
			((PresentationObject) listComponent).setStyleClass(getFirstChildStyleClass());
		}
		if (isLastChild) {
			((PresentationObject) listComponent).setStyleClass(getLastChildStyleClass());
		}
		return row;
	}
	
	/**
	 * Adds the <code>PresentationObject</code> corresponding to the specified
	 * <code>PageTreeNode</code> and depth.
	 * 
	 * @param iwc
	 * @param page
	 * @param row
	 * @param depth
	 * @param linkIsDisabled TODO
	 * @param table
	 */
	protected void addObject(IWContext iwc, ICTreeNode page, UIComponent list, int row, int depth, boolean linkIsDisabled) {
		UIComponent link = getLink(page, iwc, depth, linkIsDisabled);
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

	protected UIComponent getLink(ICTreeNode page, IWContext iwc, int depth, boolean linkIsDisabled) {
		String name = getLocalizedName(page, iwc);

		if (iUseStyleLinks) {
			if (page.getNodeID() != getCurrentPageId()) {
				String linkStyleClass=linkStyleName;
				Link link = getStyleLink(name, getStyleName(linkStyleClass, depth));
				if(linkIsDisabled){
					link.setURL("#");
				}
				else{
					addParameterToLink(link, page);
				}
				return link;
			}
			else {
				if(displaySelectedPageAsLink){
					Link link = getStyleLink(name, getStyleName(linkStyleName, depth));
					addParameterToLink(link, page);
					return link;
				}
				else{
					Text text = getStyleText(name, getStyleName(textStyleName, depth));
					return text;
				}
			}
		}
		else {
			Link link = new Link(name);
			if(linkIsDisabled){
				link.setURL("#");
			}
			else{
				addParameterToLink(link, page);
			}
			return link;
		}
	}
	
	protected int getCurrentPageId(){
		return _currentPageID;
	}

	protected void addParameterToLink(Link link, ICTreeNode page) {
		boolean isCategory = getIsCategory(page);
		if (!isCategory) {
			link.setPage(page.getNodeID());
		} else {
			link.addParameter(PARAMETER_SELECTED_PAGE, page.getNodeID());
		}
		List parameters = (List) _parameters.get(new Integer(page.getNodeID()));
		if (parameters != null) {
			Iterator iter = parameters.iterator();
			while (iter.hasNext()) {
				link.addParameter((Parameter)iter.next());
			}
		}
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
		BuilderService builderService = null;
		try {
			builderService = getBuilderService(iwc);
			ICTreeNode currentPage = builderService.getPageTree(builderService.getCurrentPageId(iwc));
			_currentPageID = currentPage.getNodeID();
			_currentPages = new ArrayList();
			_currentPages.add(new Integer(_currentPageID));
			debug("Current page is set.");
		
			if (_currentPageID != ((Integer) getRootNodeId()).intValue()) {
				ICTreeNode parent = currentPage.getParentNode();
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
				
				ICTreeNode selectedParent = builderService.getPageTree(Integer.parseInt(iwc.getParameter(PARAMETER_SELECTED_PAGE)));
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
						
						ICTreeNode selectedParent = builderService.getPageTree(homePageID);
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
		//return _rootPage;
		IWContext iwc = IWContext.getInstance();
		if (_rootPageID == -1) {
			try {
				_rootPageID = getBuilderService(iwc).getRootPageId();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		ICTreeNode rootPage = new PageTreeNode(_rootPageID, iwc);
		return rootPage;
		//_rootPage = new PageTreeNode(_rootPageID, iwc);
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

	public void setHideSubPages(boolean hide) {
		iHideSubPages = hide;
	}
	
	public void setListID(String ID) {
		iListID = ID;
	}
	
	public void setSelectedID(String ID) {
		iSelectedID = ID;
	}
	
	public void setUseStyleLinks(boolean useStyleLinks) {
		iUseStyleLinks = useStyleLinks;
	}
	
	public void setParameterForPage(ICPage page, String parameterName, String parameterValue) {
		if (page != null && parameterName != null && !parameterName.trim().equals("")) {
			List list = (List) _parameters.get(page.getPrimaryKey());
			if (list == null) {
				list = new Vector();
				_parameters.put(page.getPrimaryKey(), list);
			}
			Parameter p = new Parameter(parameterName, parameterValue);
			list.add(p);
		}
	}
	
	/**
	 * <p>
	 * Sets the list to show also pages in the list that the current logged in user does not have (view) access to.<br/>
	 * </p>
	 * @param ifShow
	 */
	public void setShowForbiddenPagesAsDisabled(boolean ifShow){
		showForbiddenPagesAsDisabled=ifShow;
	}
	
	/**
	 * <p>
	 * Sets the style class that is rendered out when the property "showForbiddenPagesAsDisabled" is set to true.<br/>
	 * The default value is "disabled".
	 * </p>
	 * @param styleClass
	 */
	public void setDisabledStyleClass(String styleClass){
		this.disabledStyleClass=styleClass;
	}
	
	public String getDisabledStyleClass(){
		return disabledStyleClass;
	}
	
	/**
	 * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
	 */
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[32];
		values[0] = super.saveState(ctx);
		values[1] = this.textStyleName;
		values[2] = this.linkStyleName;
		values[3] = new Integer(_currentPageID);
		//values[4] = _rootPage;
		values[4] = new Integer(_maxDepthForStyles);
		values[5] = _currentPages;
		values[6] = _selectedPages;
		values[7] = Boolean.valueOf(_showRoot);
		values[8] = Boolean.valueOf(_useDifferentStyles);
		values[9] = Boolean.valueOf(_autoCreateHoverStyles);
		values[10] = Boolean.valueOf(_debug);
		values[11] = Boolean.valueOf(_markOnlyCurrentPage);
		values[12] = Boolean.valueOf(iOpenOnUserHomePage);
		values[13] = Boolean.valueOf(iOrderPagesAlphabetically);
		values[14] = Boolean.valueOf(iHideSubPages);
		values[15] = Boolean.valueOf(iUseStyleLinks);
		values[16] = Boolean.valueOf(rootSelected);
		values[17] = new Integer(_rootPageID);
		values[18] = iSelectedID;
		values[19] = iListID;
		values[20] = _parameters;
		values[21] = _depthOrderPagesAlphabetically;
		values[22] = Boolean.valueOf(showForbiddenPagesAsDisabled);
		values[23] = disabledStyleClass;
		values[24] = Boolean.valueOf(displaySelectedPageAsLink);
		values[25] = selectedStyleClass;
		values[26] = beforeSelectedStyleClass;
		values[27] = lastSelectedStyleClass;
		values[28] = afterSelectedStyleClass;
		values[29] = firstSelectedStyleClass;
		values[30] = firstChildStyleClass;
		values[31] = lastChildStyleClass;
		return values;
	}
	
	/**
	 * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
	 */
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[])state;
		super.restoreState(ctx, values[0]);
		textStyleName=(String)values[1];
		linkStyleName=(String)values[2];
		_currentPageID=((Integer)values[3]).intValue();
		//_rootPage=(ICTreeNode)values[4];
		_maxDepthForStyles=((Integer)values[4]).intValue();
		_currentPages=(Collection)values[5];
		_selectedPages=(Collection)values[6];
		_showRoot=((Boolean)values[7]).booleanValue();
		_useDifferentStyles=((Boolean)values[8]).booleanValue();
		_autoCreateHoverStyles=((Boolean)values[9]).booleanValue();
		_debug=((Boolean)values[10]).booleanValue();
		_markOnlyCurrentPage=((Boolean)values[11]).booleanValue();
		iOpenOnUserHomePage=((Boolean)values[12]).booleanValue();
		iOrderPagesAlphabetically=((Boolean)values[13]).booleanValue();
		iHideSubPages=((Boolean)values[14]).booleanValue();
		iUseStyleLinks=((Boolean)values[15]).booleanValue();
		rootSelected=((Boolean)values[16]).booleanValue();
		_rootPageID=((Integer)values[17]).intValue();
		iSelectedID=(String)values[18];
		iListID=(String)values[19];
		_parameters=(HashMap)values[20];
		_depthOrderPagesAlphabetically=(Map)values[21];
		showForbiddenPagesAsDisabled=((Boolean)values[22]).booleanValue();
		disabledStyleClass=(String)values[23];
		displaySelectedPageAsLink=((Boolean)values[24]).booleanValue();
		selectedStyleClass=(String)values[25];
		beforeSelectedStyleClass=(String)values[26];
		lastSelectedStyleClass=(String)values[27];
		afterSelectedStyleClass=(String)values[28];
		firstSelectedStyleClass=(String)values[29];
		firstChildStyleClass=(String)values[30];
		lastChildStyleClass=(String)values[31];
		
	}

	
	/**
	 * @return Returns the displaySelectedPageAsLink.
	 */
	public boolean isDisplaySelectedPageAsLink() {
		return displaySelectedPageAsLink;
	}
	
	/**
	 * @param displaySelectedPageAsLink The displaySelectedPageAsLink to set.
	 */
	public void setDisplaySelectedPageAsLink(boolean displaySelectedPageAsLink) {
		this.displaySelectedPageAsLink = displaySelectedPageAsLink;
	}
	
	
	/**
	 * @return Returns the selectedStyleClass.
	 */
	public String getSelectedStyleClass() {
		return selectedStyleClass;
	}

	
	/**
	 * @param selectedStyleClass The selectedStyleClass to set.
	 */
	public void setSelectedStyleClass(String selectedStyleClass) {
		this.selectedStyleClass = selectedStyleClass;
	}

	
	/**
	 * @return Returns the afterSelectedStyleClass.
	 */
	public String getAfterSelectedStyleClass() {
		return afterSelectedStyleClass;
	}

	
	/**
	 * @param afterSelectedStyleClass The afterSelectedStyleClass to set.
	 */
	public void setAfterSelectedStyleClass(String afterSelectedStyleClass) {
		this.afterSelectedStyleClass = afterSelectedStyleClass;
	}

	
	/**
	 * @return Returns the beforeSelectedStyleClass.
	 */
	public String getBeforeSelectedStyleClass() {
		return beforeSelectedStyleClass;
	}

	
	/**
	 * @param beforeSelectedStyleClass The beforeSelectedStyleClass to set.
	 */
	public void setBeforeSelectedStyleClass(String beforeSelectedStyleClass) {
		this.beforeSelectedStyleClass = beforeSelectedStyleClass;
	}

	
	/**
	 * @return Returns the firstChildStyleClass.
	 */
	public String getFirstChildStyleClass() {
		return firstChildStyleClass;
	}

	
	/**
	 * @param firstChildStyleClass The firstChildStyleClass to set.
	 */
	public void setFirstChildStyleClass(String firstChildStyleClass) {
		this.firstChildStyleClass = firstChildStyleClass;
	}

	
	/**
	 * @return Returns the firstSelectedStyleClass.
	 */
	public String getFirstSelectedStyleClass() {
		return firstSelectedStyleClass;
	}

	
	/**
	 * @param firstSelectedStyleClass The firstSelectedStyleClass to set.
	 */
	public void setFirstSelectedStyleClass(String firstSelectedStyleClass) {
		this.firstSelectedStyleClass = firstSelectedStyleClass;
	}

	
	/**
	 * @return Returns the lastChildStyleClass.
	 */
	public String getLastChildStyleClass() {
		return lastChildStyleClass;
	}

	
	/**
	 * @param lastChildStyleClass The lastChildStyleClass to set.
	 */
	public void setLastChildStyleClass(String lastChildStyleClass) {
		this.lastChildStyleClass = lastChildStyleClass;
	}

	
	/**
	 * @return Returns the lastSelectedStyleClass.
	 */
	public String getLastSelectedStyleClass() {
		return lastSelectedStyleClass;
	}

	
	/**
	 * @param lastSelectedStyleClass The lastSelectedStyleClass to set.
	 */
	public void setLastSelectedStyleClass(String lastSelectedStyleClass) {
		this.lastSelectedStyleClass = lastSelectedStyleClass;
	}
	
}