package com.idega.block.navigation.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;

import com.idega.block.navigation.bean.BreadCrumbsBean;
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

public class BreadCrumbs extends IWBaseComponent {

	public static final String COMPONENT_TYPE = "com.idega.BreadCrumbs";

	public static final String IW_BUNDLE_IDENTIFIER = "com.idega.block.navigation";

	private String faceletPath = null;

	private int rootPageID = -1;

	private String id = null;
	private String styleClass = null;
	private String divider = ">";

	private boolean hideCategoryPages = false;

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	@Override
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);

		this.faceletPath = (String) values[1];
		this.rootPageID = ((Integer) values[2]).intValue();
		this.id = (String) values[3];
		this.styleClass = (String) values[4];
		this.hideCategoryPages = ((Boolean) values[5]).booleanValue();
		this.divider = (String) values[6];
	}

	@Override
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[7];
		values[0] = super.saveState(ctx);
		values[1] = this.faceletPath;
		values[2] = new Integer(this.rootPageID);
		values[3] = this.id;
		values[4] = this.styleClass;
		values[5] = Boolean.valueOf(this.hideCategoryPages);
		values[6] = this.divider;

		return values;
	}

	@Override
	public void initializeComponent(FacesContext context) {
		IWContext iwc = IWContext.getIWContext(context);
		if (getFaceletPath() == null) {
			setFaceletPath(getBundle(context, getBundleIdentifier()).getFaceletURI("breadCrumbs.xhtml"));
		}

		BreadCrumbsBean bean = getBeanInstance("breadCrumbsBean");
		bean.setChildren(getChildren(iwc));
		bean.setId(getID());
		bean.setStyleClass(getStyleClass());
		if (getDivider() != null && getDivider().length() > 0) {
			bean.setDivider(getDivider());
		}

		FaceletComponent facelet = (FaceletComponent) iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(getFaceletPath());
		add(facelet);
	}

	private Collection<NavigationItem> getChildren(IWContext iwc) {
		try {
			List<NavigationItem> pages = new ArrayList<NavigationItem>();

			BuilderService service = BuilderServiceFactory.getBuilderService(iwc);

			if (this.rootPageID == -1) {
				rootPageID = service.getRootPageId();
			}
			int currentPageID = service.getCurrentPageId(iwc);

			PageTreeNode page = new PageTreeNode(currentPageID, iwc);
			boolean showPage = true;
			while (showPage) {
				if (page.getNodeID() == rootPageID) {
					showPage = false;
				}

				if (!(isHideCategoryPages() && page.isCategory())) {
					NavigationItem item = new NavigationItem();
					item.setNode(page);
					item.setCurrent(page.getNodeID() == currentPageID);
					item.setURI(service.getPageURI(page.getNodeID()));
					item.setName(page.getNodeName(iwc.getCurrentLocale()));

					if (page.isCategory()) {
						Collection<PageTreeNode> nodes = page.getChildren();
						if (ListUtil.isEmpty(nodes)) {
							item.setURI(CoreConstants.HASH);
						}
						else {
							ICTreeNode firstChild = nodes.iterator().next();
							item.setURI(service.getPageURI(firstChild.getId()));
						}
						item.setCategory(true);
					}

					pages.add(item);
				}

				page = (PageTreeNode) page.getParentNode();
				if (page == null) {
					showPage = false;
				}
			}
			Collections.reverse(pages);

			setStyles(pages);

			return pages;
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	private void setStyles(Collection<NavigationItem> childItems) {
		int index = 0;
		Iterator<NavigationItem> it = childItems.iterator();
		while (it.hasNext()) {
			NavigationItem item = it.next();
			item.setIndex(index++);

			item.setStyleClass((item.getIndex() + 1) % 2 == 0 ? "even" : "odd");
			if (item.getIndex() == 0) {
				item.setStyleClass("firstPage");
			}
			if (!it.hasNext()) {
				item.setStyleClass("lastPage");
			}
			if (item.isCategory()) {
				item.setStyleClass("categoryPage");
			}
		}
	}

	/**
	 * Sets the page to use as the root for the list. If nothing is selected, the
	 * root page of the <code>IBDomain</code> is used.
	 *
	 * @param rootPageID
	 */
	public void setRootPage(ICPage rootPage) {
		this.rootPageID = ((Integer) rootPage.getPrimaryKey()).intValue();
	}

	/**
	 * Sets the page to use as the root for the list. If nothing is selected, the
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
			return "breadcrumbs";
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

	public boolean isHideCategoryPages() {
		return hideCategoryPages;
	}

	/**
	 * Sets to hide category pages in the list.
	 *
	 * @param hideCategoryPages
	 */
	public void setHideCategoryPages(boolean hideCategoryPages) {
		this.hideCategoryPages = hideCategoryPages;
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

	public String getDivider() {
		return divider;
	}

	/**
	 * Sets the divider between items in the list
	 *
	 * @param divider
	 */
	public void setDivider(String divider) {
		this.divider = divider;
	}
}