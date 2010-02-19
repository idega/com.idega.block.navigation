/*
 * $Id: NavigationList.java,v 1.40 2009/01/14 09:29:49 valdas Exp $
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

import com.idega.block.navigation.utils.NavigationConstants;
import com.idega.builder.business.PageTreeNode;
import com.idega.business.IBOLookup;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.data.ICPage;
import com.idega.core.business.ICTreeNodeComparator;
import com.idega.core.data.ICTreeNode;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Span;
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
 *  Last modified: $Date: 2009/01/14 09:29:49 $ by $Author: valdas $
 * 
 * @author <a href="mailto:tryggvil@idega.com">tryggvil</a>
 * @version $Revision: 1.40 $
 * @deprecated Use <code>Navigation</code> instead
 */
@Deprecated
public class NavigationList extends NavigationBlock {

	private final static String PARAMETER_SELECTED_PAGE = "nt_selected_page";
	private final static String SESSION_ATTRIBUTE_OPEN_ON_USER_HOMEPAGE = "nt_open_on_user_homepage";

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
	private boolean addStyleClassOnSelectedItem = true;
	private boolean openAllNodes = false;

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
	private String extraLastItemStyleClass="extraAfterLastChild";
	private boolean addExtraLastItem=false;
	
	private String current = "current";
	private String currentAncestor = "currentAncestor";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.presentation.PresentationObject#main(com.idega.presentation.IWContext)
	 */
	@Override
	public void main(IWContext iwc) throws Exception {
		setRootNode(iwc);
		parse(iwc);
		add(getTree(iwc));
	}

	protected void setRootNode(IWContext iwc) throws RemoteException {
		if (this._rootPageID == -1) {
			if(getIsRootCurrentPage()){
				int currentPageId = getBuilderService(iwc).getCurrentPageId(iwc);
				this._rootPageID = currentPageId;
			}
			else if(getIsRootCurrentPageParent()){
				ICPage currentPage = getBuilderService(iwc).getCurrentPage(iwc);
				ICTreeNode currentParent = currentPage.getParentNode();
				if(currentParent!=null){
					this._rootPageID = currentParent.getIndex(currentParent);
				}
			}
			else{
				this._rootPageID = getBuilderService(iwc).getRootPageId();
			}
		}
	}

	/**
	 * Initialized the main <code>ul</code> tag and adds all page links to the
	 * tree.
	 * 
	 * @param iwc
	 * @return
	 */
	protected UIComponent getTree(IWContext iwc) {
		Lists list = new Lists();
		if (this.iListID != null) {
			list.setID(this.iListID);
		}
		String styleClass = getStyleClass();
		if(styleClass!=null){
			list.setStyleClass(styleClass);
		}
		int row = 1;
		int depth = 0;

		if (getShowRoot()) {
			ICTreeNode page = getRootNode();
			if (isSelectedPage(page)) {
				this.rootSelected = true;
			}

			List pageList = new ArrayList();
			pageList.add(page);
			UIComponent nodeComponent = getNodeComponent(list,pageList,page,row,depth,0, false, iwc);
			((PresentationObject) nodeComponent).setStyleClass(getFirstChildStyleClass());
			addObject(iwc, page, nodeComponent, row, depth, false);
		}

		row = addToTree(iwc, getRootNode().getChildren(), list, row, depth);

		if(getAddExtraLastItem()){
			
			ListItem nodeComponent = new ListItem();
			nodeComponent.setStyleClass(getExtraLastItemStyleClass());
			list.getChildren().add(nodeComponent);
		}
		
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
				String pageKey = page.getId();
				//Page populatedPage = getBuilderService(iwc).getPage(pageKey);
				//hasPermission = iwc.hasViewPermission(populatedPage);
				hasPermission = iwc.getAccessController().hasViewPermissionForPageKey(pageKey,iwc);
			}
			catch (Exception re) {
				log(re);
			}

			if (hasPermission||this.showForbiddenPagesAsDisabled) {
				boolean linkIsDisabled = (!hasPermission)&&this.showForbiddenPagesAsDisabled;
				UIComponent nodeComponent = getNodeComponent(pageList,pagesList,page,row,depth,index, linkIsDisabled, iwc);
				addObject(iwc, page, nodeComponent, row, depth, linkIsDisabled);
				row = setRowAttributes(nodeComponent, page, row, depth, (index == 0), !children.hasNext());

				//only recurse down tree if hasPermission==true
				if(hasPermission){
					if (isOpen(page) && page.getChildCount() > 0 && !this.iHideSubPages) {
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
	protected ListItem getNodeComponent(UIComponent outerContainer,List pages,ICTreeNode page,int row,int depth,int index, boolean isdisabled, IWContext iwc) {
		ListItem item = new ListItem();
		if (isSelectedPage(page)) {
			if (isAddStyleClassOnSelectedItem()) {
				item.setStyleClass(getSelectedStyleClass());
			}
			if (this.iSelectedID != null) {
				item.setID(this.iSelectedID);
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
				if (isSelectedPage((ICTreeNode) pages.get(index + 1)) && isAddStyleClassOnSelectedItem()) {
					item.setStyleClass(getBeforeSelectedStyleClass());
				}
			}
			
			if (index == size && isSelectedPage(page) && !this.rootSelected && isAddStyleClassOnSelectedItem()) {
				item.setStyleClass(getLastSelectedStyleClass());
			}
			
			if (index > 0) {
				if (isSelectedPage((ICTreeNode) pages.get(index - 1)) && isAddStyleClassOnSelectedItem()) {
					item.setStyleClass(getAfterSelectedStyleClass());
				}
			}
			
			if (index == 0 && isSelectedPage(page) && !getShowRoot() && isAddStyleClassOnSelectedItem()) {
				item.setStyleClass(getFirstSelectedStyleClass());
			}
			else if (getShowRoot() && this.rootSelected && page.equals(getRootNode())) {
				item.setStyleClass(getFirstSelectedStyleClass());
			}
			
			if (index == 0 && getShowRoot() && this.rootSelected && !page.equals(getRootNode())) {
				item.setStyleClass(getAfterSelectedStyleClass());
			}
			
			if(isdisabled){
				item.setStyleClass(getDisabledStyleClass());
			}
		}
		outerContainer.getChildren().add(item);
		
		setPageInvisibleInNavigation(iwc, page.getId(), item);
		
		return item;
	}
	
	private boolean isSelectedPage(ICTreeNode page) {
		if (isOpen(page) || Integer.parseInt(page.getId()) == getCurrentPageId()) {
			return true;
		}
		return false;
	}
	
	protected int setRowAttributes(UIComponent listComponent, ICTreeNode page, int row, int depth, boolean isFirstChild, boolean isLastChild) {
		if (isFirstChild && !getShowRoot()) {
			if (isAddStyleClassOnSelectedItem()) {
				((PresentationObject) listComponent).setStyleClass(getFirstChildStyleClass());
			}
		}
		if (isLastChild) {
			if (isAddStyleClassOnSelectedItem()) {
				((PresentationObject) listComponent).setStyleClass(getLastChildStyleClass());
			}
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
		UIComponent link = getLink(page, list, iwc, depth, linkIsDisabled);
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

	protected UIComponent getLink(ICTreeNode page, UIComponent parent, IWContext iwc, int depth, boolean linkIsDisabled) {
		String name = getLocalizedName(page, iwc);

		String disabledLink = "javascript:void(0)";
		if (this.iUseStyleLinks) {
			Link link = null;
			if (Integer.parseInt(page.getId()) != getCurrentPageId()) {
				if (isOpen(page)) {
					if (isAddStyleClassOnSelectedItem()) {
						link = getDefaultLink(name, this.linkStyleName, depth);
					}
					else {
						link = constructLink(new Span(new Text(name)));
						link.setStyleClass(currentAncestor);
						if (parent != null && parent instanceof ListItem) {
							((ListItem) parent).setStyleClass(currentAncestor);
						}
					}
				}
				else {
					link = getDefaultLink(name, this.linkStyleName, depth);
				}
				if (linkIsDisabled) {
					link.setURL(disabledLink);
				}
				else {
					addParameterToLink(link, page, false);
				}
				
				setAsCategoryPage(page, link);
				
				return link;
			}
			else {
				if (this.displaySelectedPageAsLink) {
					if (isAddStyleClassOnSelectedItem()) {
						link = getDefaultLink(name, this.linkStyleName, depth);
					}
					else {
						link = constructLink(new Span(new Text(name)));
						if (iwc.getViewRoot().findComponent(current) == null) {
							link.setId(current);
						}
						link.setStyleClass(current);
						if (parent != null && parent instanceof ListItem) {
							((ListItem) parent).setStyleClass(current);
						}
					}
					addParameterToLink(link, page, false);
					setAsCategoryPage(page, link);
					
					return link;
				}
				else {
					Text text = new Text(name);
					if (isAddStyleClassOnSelectedItem()) {
						text.setStyleClass(getStyleName(this.textStyleName, depth));
					}
					return text;
				}
			}
		}
		else {
			Link link = constructLink(new Span(new Text(name)));
			if (linkIsDisabled) {
				link.setURL(disabledLink);
			}
			else {
				addParameterToLink(link, page, false);
			}
			setAsCategoryPage(page, link);
			
			return link;
		}
	}
	
	protected Link constructLink(PresentationObject po) {
		return new Link(po);
	}
	
	private Link getDefaultLink(String name, String styleClass, int depth) {
		Link link = constructLink(new Span(new Text(name)));
		link.setStyleClass(getStyleName(styleClass, depth));
		return link;
	}
	
	protected int getCurrentPageId(){
		return this._currentPageID;
	}

	protected void addParameterToLink(Link link, ICTreeNode page, boolean parametersOnly) {
		if (!parametersOnly) {
			boolean isCategory = getIsCategory(page);
			if (!isCategory) {
				link.setPage(Integer.parseInt(page.getId()));
			} else {
				link.addParameter(PARAMETER_SELECTED_PAGE, page.getId());
			}
		}
		List parameters = (List) this._parameters.get(new Integer(page.getId()));
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
		if (this._depthOrderPagesAlphabetically != null) {
			Boolean order = (Boolean) this._depthOrderPagesAlphabetically.get(new Integer(depth));
			if (order != null) {
				return order.booleanValue();
			}
		}
		return this.iOrderPagesAlphabetically;
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
		if (this._useDifferentStyles) {
			if (this._maxDepthForStyles != -1 && depth >= this._maxDepthForStyles) {
				styleName = styleName + "_" + this._maxDepthForStyles;
			}
			else {
				styleName = styleName + "_" + depth;
			}
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
		if (!openAllNodes) {
			boolean isOpen = isCurrent(page);
			if (!isOpen) {
				isOpen = isSelected(page);
			}
			return isOpen;
		} else {
			return true;
		}
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
		if (this._currentPages != null && this._currentPages.contains(new Integer(page.getId()))) {
			return true;
		}
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
		if (this._selectedPages != null && this._selectedPages.contains(new Integer(page.getId()))) {
			return true;
		}
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
			this._currentPageID = Integer.parseInt(currentPage.getId());
			this._currentPages = new ArrayList();
			this._currentPages.add(new Integer(this._currentPageID));
			debug("Current page is set.");
		
			if (this._currentPageID != ((Integer) getRootNodeId()).intValue()) {
				ICTreeNode parent = currentPage.getParentNode();
				if (parent != null) {
					while (parent != null && Integer.parseInt(parent.getId()) != ((Integer) getRootNodeId()).intValue()) {
						debug("Adding page with ID = " + parent.getId() + " to currentMap");
						this._currentPages.add(new Integer(parent.getId()));
						parent = parent.getParentNode();
						if (parent == null) {
							break;
						}
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
				this._selectedPages = new ArrayList();
				
				ICTreeNode selectedParent = builderService.getPageTree(Integer.parseInt(iwc.getParameter(PARAMETER_SELECTED_PAGE)));
				while (selectedParent != null && Integer.parseInt(selectedParent.getId()) != ((Integer) getRootNodeId()).intValue()) {
					debug("Adding page with ID = " + selectedParent.getId() + " to selectedMap");
					this._selectedPages.add(new Integer(selectedParent.getId()));
					selectedParent = selectedParent.getParentNode();
				}
			}
			catch (RemoteException re) {
				re.printStackTrace();
			}
		}
		else {
			debug("No selected page parameter in request.");
			
			if (this.iOpenOnUserHomePage && iwc.isLoggedOn()) {
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
						this._selectedPages = new ArrayList();
						
						ICTreeNode selectedParent = builderService.getPageTree(homePageID);
						while (selectedParent != null && Integer.parseInt(selectedParent.getId()) != ((Integer) getRootNodeId()).intValue()) {
							debug("Adding page with ID = " + selectedParent.getId() + " to selectedMap");
							this._selectedPages.add(new Integer(selectedParent.getId()));
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
	@Override
	public String getBundleIdentifier() {
		return NavigationConstants.IW_BUNDLE_IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.presentation.Block#autoCreateGlobalHoverStyles()
	 */
	@Override
	protected boolean autoCreateGlobalHoverStyles() {
		return this._autoCreateHoverStyles;
	}

	/**
	 * Sets the page to use as the root for the tree. If nothing is selected, the
	 * root page of the <code>IBDomain</code> is used.
	 * 
	 * @param rootPageID
	 */
	public void setRootPage(ICPage rootPage) {
		this._rootPageID = ((Integer) rootPage.getPrimaryKey()).intValue();
	}


	/**
	 * Sets to show the root page in the tree. Set to FALSE by default.
	 * 
	 * @param showRoot
	 */
	public void setShowRoot(boolean showRoot) {
		this._showRoot = showRoot;
	}

	
	/**
	 * Gets if the root should be showed
	 * @return
	 */
	public boolean getShowRoot(){
		return this._showRoot;
	}

	/**
	 * Sets to use different styles for each level of the tree. Set to FALSE by
	 * default.
	 * 
	 * @param useDifferentStyles
	 */
	public void setUseDifferentStyles(boolean useDifferentStyles) {
		this._useDifferentStyles = useDifferentStyles;
	}

	/**
	 * Sets to auto create hovers styles in the style sheet for link styles. Set
	 * to FALSE by default.
	 * 
	 * @param autoCreateHoverStyles
	 */
	public void setAutoCreateHoverStyles(boolean autoCreateHoverStyles) {
		this._autoCreateHoverStyles = autoCreateHoverStyles;
	}

	/**
	 * Sets to order pages alphabetically for a specific depth level.
	 * 
	 * @param depth
	 * @param orderAlphabetically
	 */
	public void setDepthOrderPagesAlphabetically(int depth, boolean orderAlphabetically) {
		if (this._depthOrderPagesAlphabetically == null) {
			this._depthOrderPagesAlphabetically = new HashMap();
		}
		this._depthOrderPagesAlphabetically.put(new Integer(depth - 1), new Boolean(orderAlphabetically));
	}


	/**
	 * Sets the maximum depth for styles in the <code>NavigationTree</code>.
	 * Used to restrict how far down the tree new styles are specified. Set to -1
	 * by default, meaning no restrictions.
	 * 
	 * @param maxDepthForStyles
	 */
	public void setMaxDepthForStyles(int maxDepthForStyles) {
		this._maxDepthForStyles = maxDepthForStyles;
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
		this._debug = debug;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.presentation.PresentationObject#debug(java.lang.String)
	 */
	@Override
	public void debug(String outputString) {
		if (this._debug) {
			System.out.println("[NavigationTree]: " + outputString);
		}
	}

	protected ICTreeNode getRootNode() {
		//return _rootPage;
		IWContext iwc = IWContext.getInstance();
		if (this._rootPageID == -1) {
			try {
				this._rootPageID = getBuilderService(iwc).getRootPageId();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		ICTreeNode rootPage = new PageTreeNode(this._rootPageID, iwc);
		return rootPage;
		//_rootPage = new PageTreeNode(_rootPageID, iwc);
	}

	protected Object getRootNodeId() {
		return new Integer(this._rootPageID);
	}

	/**
	 * @param onlyCurrentPage
	 *          The _markOnlyCurrentPage to set.
	 */
	public void setToMarkOnlyCurrentPage(boolean onlyCurrentPage) {
		this._markOnlyCurrentPage = onlyCurrentPage;
	}
	
	/**
	 * Gets the value of the _markOnlyCurrentPage property
	 */
	public boolean getMarkOnlyCurrentPage() {
		return this._markOnlyCurrentPage;
	}

	public void setOpenOnUserHomepage(boolean openOnUserHomepage) {
		this.iOpenOnUserHomePage = openOnUserHomepage;
	}
	
	public void setToOrderPagesAlphabetically(boolean orderAlphabetically) {
		this.iOrderPagesAlphabetically = orderAlphabetically;
	}

	public void setHideSubPages(boolean hide) {
		this.iHideSubPages = hide;
	}
	
	public void setListID(String ID) {
		this.iListID = ID;
	}
	
	public void setSelectedID(String ID) {
		this.iSelectedID = ID;
	}
	
	public void setUseStyleLinks(boolean useStyleLinks) {
		this.iUseStyleLinks = useStyleLinks;
	}
	
	public void setParameterForPage(ICPage page, String parameterName, String parameterValue) {
		if (page != null && parameterName != null && !parameterName.trim().equals("")) {
			List list = (List) this._parameters.get(page.getPrimaryKey());
			if (list == null) {
				list = new Vector();
				this._parameters.put(page.getPrimaryKey(), list);
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
		this.showForbiddenPagesAsDisabled=ifShow;
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
		return this.disabledStyleClass;
	}
	
	/**
	 * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
	 */
	@Override
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[36];
		values[0] = super.saveState(ctx);
		values[1] = this.textStyleName;
		values[2] = this.linkStyleName;
		values[3] = new Integer(this._currentPageID);
		//values[4] = _rootPage;
		values[4] = new Integer(this._maxDepthForStyles);
		values[5] = this._currentPages;
		values[6] = this._selectedPages;
		values[7] = Boolean.valueOf(this._showRoot);
		values[8] = Boolean.valueOf(this._useDifferentStyles);
		values[9] = Boolean.valueOf(this._autoCreateHoverStyles);
		values[10] = Boolean.valueOf(this._debug);
		values[11] = Boolean.valueOf(this._markOnlyCurrentPage);
		values[12] = Boolean.valueOf(this.iOpenOnUserHomePage);
		values[13] = Boolean.valueOf(this.iOrderPagesAlphabetically);
		values[14] = Boolean.valueOf(this.iHideSubPages);
		values[15] = Boolean.valueOf(this.iUseStyleLinks);
		values[16] = Boolean.valueOf(this.rootSelected);
		values[17] = new Integer(this._rootPageID);
		values[18] = this.iSelectedID;
		values[19] = this.iListID;
		values[20] = this._parameters;
		values[21] = this._depthOrderPagesAlphabetically;
		values[22] = Boolean.valueOf(this.showForbiddenPagesAsDisabled);
		values[23] = this.disabledStyleClass;
		values[24] = Boolean.valueOf(this.displaySelectedPageAsLink);
		values[25] = this.selectedStyleClass;
		values[26] = this.beforeSelectedStyleClass;
		values[27] = this.lastSelectedStyleClass;
		values[28] = this.afterSelectedStyleClass;
		values[29] = this.firstSelectedStyleClass;
		values[30] = this.firstChildStyleClass;
		values[31] = this.lastChildStyleClass;
		values[32] = this.extraLastItemStyleClass;
		values[33] = new Boolean(this.addExtraLastItem);
		values[34] = new Boolean(this.addStyleClassOnSelectedItem);
		values[35] = new Boolean(openAllNodes);
		return values;
	}
	
	/**
	 * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
	 */
	@Override
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[])state;
		super.restoreState(ctx, values[0]);
		this.textStyleName=(String)values[1];
		this.linkStyleName=(String)values[2];
		this._currentPageID=((Integer)values[3]).intValue();
		//_rootPage=(ICTreeNode)values[4];
		this._maxDepthForStyles=((Integer)values[4]).intValue();
		this._currentPages=(Collection)values[5];
		this._selectedPages=(Collection)values[6];
		this._showRoot=((Boolean)values[7]).booleanValue();
		this._useDifferentStyles=((Boolean)values[8]).booleanValue();
		this._autoCreateHoverStyles=((Boolean)values[9]).booleanValue();
		this._debug=((Boolean)values[10]).booleanValue();
		this._markOnlyCurrentPage=((Boolean)values[11]).booleanValue();
		this.iOpenOnUserHomePage=((Boolean)values[12]).booleanValue();
		this.iOrderPagesAlphabetically=((Boolean)values[13]).booleanValue();
		this.iHideSubPages=((Boolean)values[14]).booleanValue();
		this.iUseStyleLinks=((Boolean)values[15]).booleanValue();
		this.rootSelected=((Boolean)values[16]).booleanValue();
		this._rootPageID=((Integer)values[17]).intValue();
		this.iSelectedID=(String)values[18];
		this.iListID=(String)values[19];
		this._parameters=(HashMap)values[20];
		this._depthOrderPagesAlphabetically=(Map)values[21];
		this.showForbiddenPagesAsDisabled=((Boolean)values[22]).booleanValue();
		this.disabledStyleClass=(String)values[23];
		this.displaySelectedPageAsLink=((Boolean)values[24]).booleanValue();
		this.selectedStyleClass=(String)values[25];
		this.beforeSelectedStyleClass=(String)values[26];
		this.lastSelectedStyleClass=(String)values[27];
		this.afterSelectedStyleClass=(String)values[28];
		this.firstSelectedStyleClass=(String)values[29];
		this.firstChildStyleClass=(String)values[30];
		this.lastChildStyleClass=(String)values[31];
		this.extraLastItemStyleClass=(String)values[32];
		this.addExtraLastItem=((Boolean)values[33]).booleanValue();
		this.addStyleClassOnSelectedItem=((Boolean)values[34]).booleanValue();
		this.openAllNodes= ((Boolean) values[35]).booleanValue();
	}

	
	/**
	 * @return Returns the displaySelectedPageAsLink.
	 */
	public boolean isDisplaySelectedPageAsLink() {
		return this.displaySelectedPageAsLink;
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
		return this.selectedStyleClass;
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
		return this.afterSelectedStyleClass;
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
		return this.beforeSelectedStyleClass;
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
		return this.firstChildStyleClass;
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
		return this.firstSelectedStyleClass;
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
		return this.lastChildStyleClass;
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
		return this.lastSelectedStyleClass;
	}

	
	/**
	 * @param lastSelectedStyleClass The lastSelectedStyleClass to set.
	 */
	public void setLastSelectedStyleClass(String lastSelectedStyleClass) {
		this.lastSelectedStyleClass = lastSelectedStyleClass;
	}
	
	/**
	 * <p>
	 * Sets the "root" of this navigation list as the parent of the current (i.e. currently requested) page.
	 * </p>
	 * @param rootIsParentOfCurrentPage
	 */
	public void setRootAsCurrentPageParent(boolean rootIsParentOfCurrentPage){
		getAttributes().put("rootAsCurrentPageParent",new Boolean(rootIsParentOfCurrentPage));
	}
	
	public boolean getIsRootCurrentPageParent(){
		Boolean b = (Boolean) getAttributes().get("rootAsCurrentPageParent");
		if(b!=null){
			return b.booleanValue();
		}
		return false;
	}
	
	/**
	 * <p>
	 * Sets the "root" of this navigation list as the current (i.e. currently requested) page.
	 * </p>
	 * @param rootIsParentOfCurrentPage
	 */
	public void setRootAsCurrentPage(boolean rootIsParentOfCurrentPage){
		getAttributes().put("rootAsCurrentPage",new Boolean(rootIsParentOfCurrentPage));
	}
	
	public boolean getIsRootCurrentPage(){
		Boolean b = (Boolean) getAttributes().get("rootAsCurrentPage");
		if(b!=null){
			return b.booleanValue();
		}
		return false;
	}
	
	/**
	 * <p>
	 * Sets if an extra (empty) &lt;li&gt; item should be rendered at the end of the list.<br/>
	 * This list-item gets by default the style class extraAfterLastChild.<br/>
	 * </p>
	 * @param addExtraLastItem
	 */
	public void setAddExtraLastItem(boolean addExtraLastItem){
		this.addExtraLastItem=addExtraLastItem;
	}
	
	public boolean getAddExtraLastItem(){
		return this.addExtraLastItem;
	}
	
	/**
	 * @return Returns the extraAfterLastChildStyleClass.
	 */
	public String getExtraLastItemStyleClass() {
		return this.extraLastItemStyleClass;
	}
	
	/**
	 * @param extraAfterLastChildStyleClass The extraAfterLastChildStyleClass to set.
	 */
	public void setExtraLastItemStyleClass(String extraAfterLastChildStyleClass) {
		this.extraLastItemStyleClass = extraAfterLastChildStyleClass;
	}

	public boolean isAddStyleClassOnSelectedItem() {
		return addStyleClassOnSelectedItem;
	}

	public void setAddStyleClassOnSelectedItem(boolean addStyleClassOnSelectedItem) {
		this.addStyleClassOnSelectedItem = addStyleClassOnSelectedItem;
	}
	
	public void setOpenAllNodes(boolean openAllNodes) {
		this.openAllNodes = openAllNodes;
	}
}