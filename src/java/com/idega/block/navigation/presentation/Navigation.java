package com.idega.block.navigation.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

import com.idega.block.navigation.bean.NavigationBean;
import com.idega.block.navigation.bean.NavigationItem;
import com.idega.builder.business.PageTreeNode;
import com.idega.business.IBORuntimeException;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.builder.data.ICPage;
import com.idega.core.data.ICTreeNode;
import com.idega.facelets.ui.FaceletComponent;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;

public class Navigation extends IWBaseComponent {

	public static final String COMPONENT_TYPE = "com.idega.Navigation";

	public static final String IW_BUNDLE_IDENTIFIER = "com.idega.block.navigation";

	public static final String FACELET_PATH_PROPERTY = "faceletPath";
	public static final String FACELET_ITEM_PATH_PROPERTY = "faceletItemPath";
	public static final String ROOT_PAGE_PROPERTY = "rootPageID";
	public static final String ID_PROPERTY = "id";
	public static final String STYLE_CLASS_PROPERTY = "styleClass";
	public static final String SHOW_ROOT_PROPERTY = "showRoot";
	public static final String OPEN_ALL_NODES_PROPERTY = "openAllNodes";
	public static final String HIDE_SUB_PAGES_PROPERTY = "hideSubPages";

	private String faceletPath = null;
	private String faceletItemPath = null;

	private int rootPageID = -1;
	private int currentPageID;
	protected Collection<Integer> currentPages;

	private String id = null;
	private String styleClass = null;

	private boolean showRoot = true;
	private boolean openAllNodes = false;
	private boolean hideSubPages = false;
	
	private boolean showPageDescription = false;

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	@Override
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);

		this.faceletPath = (String) values[1];
		this.faceletItemPath = (String) values[2];
		this.rootPageID = ((Integer) values[3]).intValue();
		this.id = (String) values[4];
		this.styleClass = (String) values[5];
		this.showRoot = ((Boolean) values[6]).booleanValue();
		this.openAllNodes = ((Boolean) values[7]).booleanValue();
		this.hideSubPages = ((Boolean) values[8]).booleanValue();
		this.showPageDescription = ((Boolean) values[9]).booleanValue();
		
		IWContext iwc = IWContext.getIWContext(ctx);
		prepareBeans(iwc);
	}

	private void prepareBeans(IWContext iwc){
		NavigationBean bean = getBeanInstance("navigationBean");
		bean.setRoot(getRoot(iwc));
		bean.setShowRoot(isShowRoot());
		bean.setOpenAllNodes(isOpenAllNodes());
		bean.setId(getID());
		bean.setStyleClass(getStyleClass());
		bean.setItemPath(getFaceletItemPath());
		bean.setShowPageDescription(isShowPageDescription());
	}
	@Override
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[10];
		values[0] = super.saveState(ctx);
		values[1] = this.faceletPath;
		values[2] = this.faceletItemPath;
		values[3] = new Integer(this.rootPageID);
		values[4] = this.id;
		values[5] = this.styleClass;
		values[6] = Boolean.valueOf(this.showRoot);
		values[7] = Boolean.valueOf(this.openAllNodes);
		values[8] = Boolean.valueOf(this.hideSubPages);
		values[9] = Boolean.valueOf(this.showPageDescription);

		return values;
	}

	@Override
	public void initializeComponent(FacesContext context) {
		handleExpressions(context);

		IWContext iwc = IWContext.getIWContext(context);
		if (getFaceletPath() == null) {
			setFaceletPath(getBundle(context, getBundleIdentifier()).getFaceletURI("navigation.xhtml"));
		}
		if (getFaceletItemPath() == null) {
			setFaceletItemPath(getBundle(context, getBundleIdentifier()).getFaceletURI("navigationItem.xhtml"));
		}

		prepareBeans(iwc);

		FaceletComponent facelet = (FaceletComponent) iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(getFaceletPath());
		add(facelet);
	}

	private void handleExpressions(FacesContext context) {
		ValueExpression ve = getValueExpression(FACELET_PATH_PROPERTY);
    	if (ve != null) {
	    	String path = (String) ve.getValue(context.getELContext());
	    	setFaceletPath(path);
    	}

		ve = getValueExpression(FACELET_ITEM_PATH_PROPERTY);
    	if (ve != null) {
	    	String path = (String) ve.getValue(context.getELContext());
	    	setFaceletItemPath(path);
    	}

		ve = getValueExpression(ROOT_PAGE_PROPERTY);
    	if (ve != null) {
	    	int rootPageID = ((Integer) ve.getValue(context.getELContext())).intValue();
	    	setRootPage(rootPageID);
    	}

		ve = getValueExpression(ID_PROPERTY);
    	if (ve != null) {
	    	String id = (String) ve.getValue(context.getELContext());
	    	setID(id);
    	}

		ve = getValueExpression(STYLE_CLASS_PROPERTY);
    	if (ve != null) {
	    	String styleClass = (String) ve.getValue(context.getELContext());
	    	setStyleClass(styleClass);
    	}

		ve = getValueExpression(SHOW_ROOT_PROPERTY);
    	if (ve != null) {
	    	boolean showRoot = ((Boolean) ve.getValue(context.getELContext())).booleanValue();
	    	setShowRoot(showRoot);
    	}

		ve = getValueExpression(OPEN_ALL_NODES_PROPERTY);
    	if (ve != null) {
	    	boolean openAllNodes = ((Boolean) ve.getValue(context.getELContext())).booleanValue();
	    	setOpenAllNodes(openAllNodes);
    	}

		ve = getValueExpression(HIDE_SUB_PAGES_PROPERTY);
    	if (ve != null) {
	    	boolean hideSubPages = ((Boolean) ve.getValue(context.getELContext())).booleanValue();
	    	setHideSubPages(hideSubPages);
    	}
	}

	private NavigationItem getRoot(IWContext iwc) {
		try {
			BuilderService service = BuilderServiceFactory.getBuilderService(iwc);

			PageTreeNode node = null;
			if (this.rootPageID == -1) {
				this.rootPageID = service.getRootPageId();
			}
			node = new PageTreeNode(this.rootPageID, iwc);

			ICTreeNode<?> currentPage = service.getPageTree(service.getCurrentPageId(iwc));
			this.currentPageID = Integer.parseInt(currentPage.getId());
			this.currentPages = new ArrayList<Integer>();
			this.currentPages.add(new Integer(this.currentPageID));

			if (this.currentPageID != node.getNodeID()) {
				ICTreeNode<?> parent = (ICTreeNode<?>) currentPage.getParentNode();
				if (parent != null) {
					while (parent != null && Integer.parseInt(parent.getId()) != node.getNodeID()) {
						this.currentPages.add(new Integer(parent.getId()));
						parent = (ICTreeNode<?>) parent.getParentNode();
						if (parent == null) {
							break;
						}
					}
				}
			}

			NavigationItem item = new NavigationItem();
			item.setName(node.getNodeName(iwc.getCurrentLocale()));
			item.setNode(node);
			item.setURI(service.getPageURI(node.getNodeID()));
			item.setDepth(0);
			item.setIndex(-1);
			item.setOpen(true);
			item.setHidden(false);
			item.setCurrent(isCurrent(node));
			item.setCurrentAncestor(false);

			getTree(iwc, item);

			return item;
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	private void getTree(IWContext iwc, NavigationItem item) {
		try {
			BuilderService service = BuilderServiceFactory.getBuilderService(iwc);
			PageTreeNode node = item.getNode();
			Collection<NavigationItem> childItems = new ArrayList<NavigationItem>();

			Collection<PageTreeNode> children = node.getChildren();
			Locale locale = iwc.getCurrentLocale();
			if (children != null && !children.isEmpty()) {
				int index = 0;
				for (Iterator<PageTreeNode> it = children.iterator(); it.hasNext();) {
					PageTreeNode childNode = it.next();

					boolean hasPermission = true;
					try {
						hasPermission = iwc.getAccessController().hasViewPermissionForPageKey(childNode.getId(),iwc);
					}
					catch (Exception re) {
						Logger logger = Logger.getLogger(this.getClass().getName());
						logger.log(Level.SEVERE, "Error while getting permissions", re);
					}

					if (hasPermission) {
						NavigationItem childItem = new NavigationItem();
						childItem.setName(childNode.getNodeName(iwc.getCurrentLocale()));
						if(isShowPageDescription()){
							item.setDescription(node.getLocalizedNodeDescription(locale));
						}
						childItem.setNode(childNode);
						childItem.setURI(service.getPageURI(childNode.getNodeID()));
						childItem.setDepth(item.getDepth() + 1);
						childItem.setIndex(index++);
						childItem.setHidden(false);
						childItem.setCurrent(isCurrent(childNode));
						childItem.setCurrentAncestor(isCurrentAncestor(childNode));

						if (!childNode.isLeaf() && isOpen(childNode)) {
							childItem.setOpen(true);
						}
						else {
							childItem.setOpen(false);
						}

						if (childNode.isCategory()) {
							Collection<PageTreeNode> nodes = childNode.getChildren();
							if (ListUtil.isEmpty(nodes)) {
								childItem.setURI(CoreConstants.HASH);
							}
							else {
								ICTreeNode<?> firstChild = nodes.iterator().next();
								childItem.setURI(service.getPageURI(firstChild.getId()));
							}
							childItem.setCategory(true);
						}

						if (childNode.isHiddenInMenu()) {
							childItem.setHidden(true);
						}


						childItems.add(childItem);
						if (!isHideSubPages()) {
							getTree(iwc, childItem);
						}
					}
				}

				setStyles(item, childItems);
				item.setChildren(childItems);
			}
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	private boolean isOpen(PageTreeNode node) {
		if (!isOpenAllNodes()) {
			return isCurrentAncestor(node) || isCurrent(node);
		}
		else {
			return true;
		}
	}

	private boolean isCurrentAncestor(PageTreeNode node) {
		if (this.currentPages != null && this.currentPages.contains(new Integer(node.getId())) && !isCurrent(node)) {
			return true;
		}
		return false;
	}

	private boolean isCurrent(PageTreeNode node) {
		if (this.currentPageID == node.getNodeID()) {
			return true;
		}
		return false;
	}

	private void setStyles(NavigationItem item, Collection<NavigationItem> childItems) {
		NavigationItem previousItem = null;

		for (Iterator<NavigationItem> it = childItems.iterator(); it.hasNext();) {
			NavigationItem childItem = it.next();
			if (childItem.isCurrent()) {
				childItem.setStyleClass("current active");
				if (previousItem != null) {
					previousItem.setStyleClass("beforeSelected");
				}
				else {
					childItem.setStyleClass("firstSelected");
				}
			}
			if (childItem.isCurrentAncestor()) {
				childItem.setStyleClass("currentAncestor");
			}
			if (previousItem != null && previousItem.isCurrent()) {
				childItem.setStyleClass("afterSelected");
			}

			if (isShowRoot() && item.getDepth() == 0 && item.isCurrent() && childItem.getIndex() == 0) {
				childItem.setStyleClass("afterSelected");
			}
			if (isShowRoot() && childItem.getIndex() == 0 && childItem.isCurrent() && item.getDepth() == 0) {
				item.setStyleClass("beforeSelected");
			}
			if (isShowRoot() && item.isCurrent() && item.getDepth() == 0) {
				item.setStyleClass("current");
			}

			if (isShowRoot() && item.getDepth() == 0) {
				item.setStyleClass("odd");
				childItem.setStyleClass((childItem.getIndex() + 1) % 2 == 0 ? "odd" : "even");
				if (item.isCurrent()) {
					item.setStyleClass("firstSelected");
				}
			}
			else if (!isShowRoot() || item.getDepth() != 0) {
				childItem.setStyleClass((childItem.getIndex() + 1) % 2 == 0 ? "even" : "odd");
			}

			if (isShowRoot() && item.getDepth() == 0) {
				item.setStyleClass("first");
			}
			else if (!isShowRoot() || item.getDepth() != 0) {
				if (childItem.getIndex() == 0) {
					childItem.setStyleClass("first");
				}
			}
			if (!it.hasNext()) {
				childItem.setStyleClass("last");
			}

			if (childItem.isHidden()) {
				childItem.setStyleClass(CoreConstants.HIDDEN_PAGE_IN_MENU_STYLE_CLASS);
			}

			previousItem = childItem;
		}
	}

	/**
	 * Sets the page to use as the root for the tree. If nothing is selected, the
	 * root page of the <code>IBDomain</code> is used.
	 *
	 * @param rootPageID
	 */
	public void setRootPage(ICPage rootPage) {
		this.rootPageID = ((Integer) rootPage.getPrimaryKey()).intValue();
	}

	/**
	 * Sets the page to use as the root for the tree. If nothing is selected, the
	 * root page of the <code>IBDomain</code> is used.
	 *
	 * @param rootPageID
	 */
	public void setRootPage(int rootPageID) {
		this.rootPageID = rootPageID;
	}

	private String getID() {
		if (this.id == null) {
			return super.getId();
		}
		else {
			return this.id;
		}
	}

	/**
	 * Sets the ID for this <code>Navigation</code>.
	 *
	 * @param id
	 */
	public void setID(String id) {
		this.id = id;
	}

	private String getStyleClass() {
		if (this.styleClass == null) {
			return "navigation";
		}
		else {
			return this.styleClass;
		}
	}

	/**
	 * Sets the class value for this <code>Navigation</code>.
	 *
	 * @param id
	 */
	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	private boolean isShowRoot() {
		return showRoot;
	}

	/**
	 * Sets to show the root page in the list.
	 *
	 * @param showRoot
	 */
	public void setShowRoot(boolean showRoot) {
		this.showRoot = showRoot;
	}

	private boolean isOpenAllNodes() {
		return openAllNodes;
	}

	/**
	 * Sets to show all pages in the navigation list.
	 *
	 * @param showRoot
	 */
	public void setOpenAllNodes(boolean openAllNodes) {
		this.openAllNodes = openAllNodes;
	}

	private boolean isHideSubPages() {
		return hideSubPages;
	}

	/**
	 * Sets to hide all sub pages below the first level.
	 *
	 * @param hideSubpages
	 */
	public void setHideSubPages(boolean hideSubPages) {
		this.hideSubPages = hideSubPages;
	}

	private String getFaceletPath() {
		return faceletPath;
	}

	/**
	 * Sets the URI for the main facelet file.
	 *
	 * @param faceletPath	An absolute path to the facelet file
	 */
	public void setFaceletPath(String faceletPath) {
		this.faceletPath = faceletPath;
	}

	private String getFaceletItemPath() {
		return faceletItemPath;
	}

	/**
	 * Sets the URI for the item facelet file.
	 *
	 * @param faceletItemPath	An absolute path to the facelet file for each item in the list
	 */
	public void setFaceletItemPath(String faceletItemPath) {
		this.faceletItemPath = faceletItemPath;
	}

	public boolean isShowPageDescription() {
		return showPageDescription;
	}

	public void setShowPageDescription(boolean showPageDescription) {
		this.showPageDescription = showPageDescription;
	}
}