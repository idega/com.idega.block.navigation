/*
 * Created on 16.9.2003
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
import com.idega.presentation.Image;
import com.idega.presentation.Page;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;

/**
 * @author laddi
 */
public class NavigationTree extends Block {

	private final static String PARAMETER_SELECTED_PAGE = "nt_selected_page";
	private final static String SESSION_ATTRIBUTE_OPEN_ON_USER_HOMEPAGE = "nt_open_on_user_homepage";
	private final static String IW_BUNDLE_IDENTIFIER = "com.idega.block.navigation";

	private String textStyleName = "text";
	private String linkStyleName = "link";

	private IWResourceBundle _iwrb;
	private IWBundle _iwb;
	private BuilderService _builderService;
	private ICTreeNode _currentPage;

	private int _currentPageID;
	private ICTreeNode _rootPage;

	protected Collection _currentPages;
	protected Collection _selectedPages;

	private boolean _showRoot = false;
	private boolean _useDifferentStyles = false;
	private boolean _autoCreateHoverStyles = false;
	private boolean _showBorder = false;
	private boolean _debug = false;
	private boolean _markOnlyCurrentPage = false;
	private boolean iOpenOnUserHomePage = false;
	private boolean iOrderPagesAlphabetically = false;

	private int _rootPageID = -1;
	private int _initialIndent = 0;
	private int _indent = 12;
	private int _spaceBetween = 0;
	private int _maxDepthForStyles = -1;
	private int _padding = 0;
	private int _imagePadding = 0;

	private String _textAlignment = Table.HORIZONTAL_ALIGN_LEFT;
	private String _imageAlignment = Image.ALIGNMENT_ABSOLUTE_MIDDLE;
	private String _width = String.valueOf(150);
	private String _backgroundColor;
	private String _borderColor;

	private Map _depthColor;
	private Map _depthHoverColor;
	private Map _depthImage;
	private Map _depthHoverImage;
	private Map _depthSelectedColor;
	private Map _depthCurrentColor;
	private Map _depthSelectedImage;
	private Map _depthCurrentImage;
	private Map _depthHeight;
	private Map _depthPaddingTop;
	private Map _depthSpacingColor;
	private Map _depthSpacingImage;
	private Map _depthAlignment;
	private Map _depthOpenImage;
	private Map _depthClosedImage;
	private Map _depthOrderPagesAlphabetically;

	private Image _iconImage;
	private Image _iconHoverImage;
	private Image _iconSelectedImage;
	private Image _iconCurrentImage;
	private Image _openImage;
	private Image _closedImage;
	private Image _blankImage;

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
	 * Initialized the surrounding <code>Table</code> and adds all pages to the
	 * tree.
	 * 
	 * @param iwc
	 * @return
	 */
	private PresentationObject getTree(IWContext iwc) {
		Table table = new Table();
		table.setWidth(_width);
		table.setCellpaddingAndCellspacing(0);
		if (_openImage != null || _closedImage != null) {
			table.setColumns(2);
			table.setWidth(1, Table.HUNDRED_PERCENT);
		}
		if (_showBorder) {
			table.setLineFrame(true);
			table.setLinesBetween(true);
			if (_borderColor != null)
				table.setLineColor(_borderColor);
		}

		int row = 1;
		int depth = 0;

		row = addHeaderObject(table, row);
		row = addToTree(iwc, getRootNode().getChildren(), table, row, depth);
		if (_showRoot) {
			addObject(iwc, getRootNode(), table, row, depth);
			setRowAttributes(table, getRootNode(), row, depth, false);
		}

		return table;
	}

	protected int addHeaderObject(Table table, int row) {
		return row;
	}

	/**
	 * Adds the given <code>Iterator</code> of child pages to the tree.
	 * 
	 * @param iwc
	 * @param children
	 * @param table
	 * @param row
	 * @param depth
	 * @return
	 */
	protected int addToTree(IWContext iwc, Collection childrenCollection, Table table, int row, int depth) {
		List list = new ArrayList(childrenCollection);
		if (getDepthOrderPagesAlphabetically(depth)) {
			Collections.sort(list, new ICTreeNodeComparator(iwc.getCurrentLocale()));
		}
		
		Iterator children = list.iterator();
		while (children.hasNext()) {
			ICTreeNode page = (ICTreeNode) children.next();

			boolean hasPermission = true;
			try {
				Page populatedPage = _builderService.getPage(String.valueOf(page.getNodeID()));
				hasPermission = iwc.hasViewPermission(populatedPage);
			}
			catch (Exception re) {
				log(re);
			}

			if (hasPermission) {
				addObject(iwc, page, table, row, depth);
				row = setRowAttributes(table, page, row, depth, !children.hasNext());

				if (isOpen(page) && page.getChildCount() > 0) {
					row = addToTree(iwc, page.getChildren(), table, row, depth + 1);
				}
			}
		}

		return row;
	}

	/**
	 * Adds the <code>PresentationObject</code> corresponding to the specified
	 * <code>PageTreeNode</code> and depth as well as the icon image (if it
	 * exists).
	 * 
	 * @param iwc
	 * @param page
	 * @param table
	 * @param row
	 * @param depth
	 */
	protected void addObject(IWContext iwc, ICTreeNode page, Table table, int row, int depth) {
		PresentationObject link = getLink(page, iwc, depth);

		Image curtainImage = getCurtainImage(depth, isOpen(page));
		if (curtainImage != null && page.getChildCount() > 0) {
			table.add(curtainImage, 2, row);
		}
		else if (_blankImage != null) {
			table.add(_blankImage, 2, row);
		}

		boolean isSelected = isSelected(page);
		boolean isCurrent = isCurrent(page);

		Image linkImage = null;
		if (isSelected) {
			linkImage = getDepthSelectedImage(depth);
		}
		else if (isCurrent) {
			linkImage = getDepthCurrentImage(depth);
		}

		if (linkImage == null) {
			linkImage = getDepthImage(depth);
		}

		if (linkImage != null) {
			if (_imagePadding > 0)
				linkImage.setPaddingRight(_imagePadding);
			table.add(linkImage, 1, row);

			if (!isSelected && !isCurrent) {
				Image linkHoverImage = getDepthHoverImage(depth);
				if (linkHoverImage != null) {
					linkImage.setOverImage(linkHoverImage);
					linkHoverImage.setVerticalSpacing(3);
					link.setMarkupAttributeMultivalued("onmouseover", "swapImage('" + linkImage.getName() + "','','" + linkHoverImage.getMediaURL(iwc) + "',1)");
					link.setMarkupAttributeMultivalued("onmouseout", "swapImgRestore()");
				}
			}
		}

		String hoverColor = getDepthHoverColor(page, depth);
		if (hoverColor != null) {
			link.setMarkupAttributeMultivalued("onmouseover", "getElementById('row" + row + "').style.background='" + hoverColor + "'");
			String color = getDepthColor(page, depth);
			if (color != null) {
				link.setMarkupAttributeMultivalued("onmouseout", "getElementById('row" + row + "').style.background='" + color + "'");
			}
		}

		table.add(link, 1, row);
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

	private PresentationObject getLink(ICTreeNode page, IWContext iwc, int depth) {
		String name = getLocalizedName(page, iwc);

		if (page.getNodeID() != _currentPageID) {
			Link link = getStyleLink(name, getStyleName(linkStyleName, depth));
			addParameterToLink(link, page);
			return link;
		}
		else {
			Text text = getStyleText(name, getStyleName(textStyleName, depth));
			return text;
		}
	}

	protected void addParameterToLink(Link link, ICTreeNode page) {
		boolean isCategory = getIsCategory(page);
		if (!isCategory)
			link.setPage(page.getNodeID());
		else
			link.addParameter(PARAMETER_SELECTED_PAGE, page.getNodeID());
	}

	/**
	 * Returns the open/closed image to display to the far right of the page in
	 * the tree.
	 * 
	 * @param page
	 * @return
	 */
	private Image getCurtainImage(ICTreeNode page) {
		if (isOpen(page))
			return _openImage;
		return _closedImage;
	}

	private Image getCurtainImage(int depth, boolean isOpen) {
		if (isOpen) {
			if (_depthOpenImage != null) {
				return (Image) _depthOpenImage.get(new Integer(depth));
			}
			return _openImage;
		}
		else {
			if (_depthClosedImage != null) {
				return (Image) _depthClosedImage.get(new Integer(depth));
			}
			return _closedImage;
		}
	}

	/**
	 * Sets the attributes for the specified row and depth.
	 * 
	 * @param table
	 * @param row
	 * @param depth
	 * @return
	 */
	protected int setRowAttributes(Table table, ICTreeNode page, int row, int depth, boolean isLastChild) {
		table.setCellpadding(1, row, _padding);
		if (table.getColumns() == 2)
			table.setVerticalAlignment(2, row, Table.VERTICAL_ALIGN_BOTTOM);

		String alignment = getDepthAlignment(depth);
		if (alignment == null) {
			alignment = _textAlignment;
		}

		if (alignment.equals(Table.HORIZONTAL_ALIGN_LEFT)) {
			table.setCellpaddingLeft(1, row, getIndent(depth));
		}
		else if (alignment.equals(Table.HORIZONTAL_ALIGN_RIGHT)) {
			table.setCellpaddingRight(1, row, getIndent(depth));
		}

		String color = getDepthColor(page, depth);
		if (color != null)
			table.setRowColor(row, color);

		String height = getDepthHeight(depth);
		if (height != null) {
			table.setHeight(row, height);
		}

		int topPadding = getDepthPaddingTop(depth);
		if (topPadding != -1) {
			table.setVerticalAlignment(1, row, Table.VERTICAL_ALIGN_TOP);
			table.setCellpaddingTop(1, row, topPadding);
		}

		table.getCellAt(1, row).setID("row" + row);
		table.setAlignment(1, row, alignment);
		table.setNoWrap(1, row++);

		boolean addBreak = false;
		if (_spaceBetween > 0) {
			String spacingColor = null;
			if (isLastChild && depth != 0) {
				spacingColor = getDepthSpacingColor(depth - 1);
			}
			else {
				spacingColor = getDepthSpacingColor(depth);
			}
			if (spacingColor != null) {
				table.setRowColor(row, spacingColor);
			}
			table.setHeight(row, _spaceBetween);

			addBreak = true;
		}

		Image depthSpacingImage = getDepthSpacingImage(depth);
		if (isLastChild && depth != 0) {
			depthSpacingImage = getDepthSpacingImage(depth - 1);
		}
		else if (!isOpen(page)) {
			depthSpacingImage = getDepthSpacingImage(depth);
		}
		if (depthSpacingImage != null) {
			table.mergeCells(1, row, table.getColumns(), row);
			table.add(depthSpacingImage, 1, row);
			addBreak = true;
		}

		if (addBreak) {
			row++;
		}

		return row;
	}

	/**
	 * Get the indent for the depth specified.
	 * 
	 * @param depth
	 * @return
	 */
	private int getIndent(int depth) {
		return (depth * _indent) + _initialIndent + _padding;
	}

	/**
	 * Gets the background color for the depth specified. If no color is specified
	 * for the depth, the general color is returned. If the general color is non
	 * existing, NULL is returned.
	 * 
	 * @param depth
	 *          The depth to get the background color for.
	 * @return
	 */
	private String getDepthColor(ICTreeNode page, int depth) {
		if (!page.equals(this.getRootNode())) {
			if (_markOnlyCurrentPage) {
				if (isCurrent(page)) {
					if (_depthCurrentColor != null) {
						String color = (String) _depthCurrentColor.get(new Integer(depth));
						if (color != null) {
							return color;
						}
						else {
							return (String) _depthCurrentColor.get(new Integer(0));
						}
					}
				}
			}
			else {
				if (isCurrent(page)) {
					if (_depthCurrentColor != null) {
						String color = (String) _depthCurrentColor.get(new Integer(depth));
						if (color != null) {
							return color;
						}
						else {
							return (String) _depthCurrentColor.get(new Integer(0));
						}
					}
				}
				if (isSelected(page)) {
					if (_depthSelectedColor != null) {
						String color = (String) _depthSelectedColor.get(new Integer(depth));
						if (color != null) {
							return color;
						}
						else {
							return (String) _depthSelectedColor.get(new Integer(0));
						}
					}
				}
			}
		}

		if (_depthColor != null) {
			String color = (String) _depthColor.get(new Integer(depth));
			if (color != null)
				return color;
		}
		if (_backgroundColor != null)
			return _backgroundColor;

		return null;
	}

	/**
	 * Gets the background color for the depth specified. If no color is specified
	 * for the depth, the general color is returned. If the general color is non
	 * existing, NULL is returned.
	 * 
	 * @param depth
	 *          The depth to get the background color for.
	 * @return
	 */
	private String getDepthHoverColor(ICTreeNode page, int depth) {
		if (isCurrent(page)) {
			return null;
		}
		else if (isSelected(page)) {
			return null;
		}
		else {
			if (_depthHoverColor != null) {
				String color = (String) _depthHoverColor.get(new Integer(depth));
				if (color != null) {
					return color;
				}
			}
			return null;
		}
	}

	/**
	 * Gets the row height for the depth specified.
	 * 
	 * @param depth
	 *          The depth to get the row height for.
	 * @return
	 */
	private String getDepthHeight(int depth) {
		if (_depthHeight != null) {
			String height = (String) _depthHeight.get(new Integer(depth));
			if (height != null) {
				return height;
			}
		}
		return null;
	}

	/**
	 * Gets the spacing color for the depth specified.
	 * 
	 * @param depth
	 *          The depth to get the row height for.
	 * @return
	 */
	private String getDepthSpacingColor(int depth) {
		if (_depthSpacingColor != null) {
			String color = (String) _depthSpacingColor.get(new Integer(depth));
			if (color != null) {
				return color;
			}
		}
		return null;
	}

	/**
	 * Gets the spacing image for the depth specified.
	 * 
	 * @param depth
	 *          The depth to get the row height for.
	 * @return
	 */
	private Image getDepthSpacingImage(int depth) {
		if (_depthSpacingImage != null) {
			Image image = (Image) _depthSpacingImage.get(new Integer(depth));
			if (image != null) {
				return image;
			}
		}
		return null;
	}

	/**
	 * Gets the alignment for the depth specified.
	 * 
	 * @param depth
	 *          The depth to get the row height for.
	 * @return
	 */
	private String getDepthAlignment(int depth) {
		if (_depthAlignment != null) {
			String alignment = (String) _depthAlignment.get(new Integer(depth));
			if (alignment != null) {
				return alignment;
			}
		}
		return null;
	}

	/**
	 * Gets the top padding for the depth specified.
	 * 
	 * @param depth
	 *          The depth to get the top padding for.
	 * @return
	 */
	private int getDepthPaddingTop(int depth) {
		if (_depthPaddingTop != null) {
			Integer padding = (Integer) _depthPaddingTop.get(new Integer(depth));
			if (padding != null) {
				return padding.intValue();
			}
		}
		return -1;
	}

	/**
	 * Gets the icon image for the depth specified. If no image is specified for
	 * the depth, the general icon image is returned. If the general icon image is
	 * non existing, NULL is returned.
	 * 
	 * @param depth
	 *          The depth to get the icon image for.
	 * @return
	 */
	private Image getDepthImage(int depth) {
		if (_depthImage != null) {
			Image image = (Image) _depthImage.get(new Integer(depth));
			if (image != null) {
				image.setAlignment(_imageAlignment);
				return image;
			}
		}
		if (_iconImage != null) {
			_iconImage.setAlignment(_imageAlignment);
			return _iconImage;
		}

		return null;
	}

	/**
	 * Gets the icon image for the depth specified. If no image is specified for
	 * the depth, the general icon image is returned. If the general icon image is
	 * non existing, NULL is returned.
	 * 
	 * @param depth
	 *          The depth to get the icon image for.
	 * @return
	 */
	private Image getDepthHoverImage(int depth) {
		if (_depthHoverImage != null) {
			Image image = (Image) _depthHoverImage.get(new Integer(depth));
			if (image != null) {
				image.setAlignment(_imageAlignment);
				return image;
			}
		}
		if (_iconHoverImage != null) {
			_iconHoverImage.setAlignment(_imageAlignment);
			return _iconHoverImage;
		}

		return null;
	}

	/**
	 * Gets the icon image for the depth specified. If no image is specified for
	 * the depth, the general icon image is returned. If the general icon image is
	 * non existing, NULL is returned.
	 * 
	 * @param depth
	 *          The depth to get the icon image for.
	 * @return
	 */
	private Image getDepthSelectedImage(int depth) {
		if (_depthSelectedImage != null) {
			Image image = (Image) _depthSelectedImage.get(new Integer(depth));
			if (image != null) {
				image.setAlignment(_imageAlignment);
				return image;
			}
		}
		if (_iconSelectedImage != null) {
			_iconSelectedImage.setAlignment(_imageAlignment);
			return _iconSelectedImage;
		}

		return null;
	}

	/**
	 * Gets the icon image for the depth specified. If no image is specified for
	 * the depth, the general icon image is returned. If the general icon image is
	 * non existing, NULL is returned.
	 * 
	 * @param depth
	 *          The depth to get the icon image for.
	 * @return
	 */
	private Image getDepthCurrentImage(int depth) {
		if (_depthCurrentImage != null) {
			Image image = (Image) _depthCurrentImage.get(new Integer(depth));
			if (image != null) {
				image.setAlignment(_imageAlignment);
				return image;
			}
		}
		if (_iconCurrentImage != null) {
			_iconCurrentImage.setAlignment(_imageAlignment);
			return _iconCurrentImage;
		}

		return null;
	}

	/**
	 * Gets whether to order pages for specified depth level alphabetically.  Will
	 * return the default value if nothing is set for the specified depth.
	 * 
	 * @param depth
	 *          The depth to get whether to order alphabetically or not.
	 * @return
	 */
	private boolean getDepthOrderPagesAlphabetically(int depth) {
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
					while (parent.getNodeID() != ((Integer) getRootNodeId()).intValue()) {
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
				while (selectedParent.getNodeID() != ((Integer) getRootNodeId()).intValue()) {
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
						while (selectedParent.getNodeID() != ((Integer) getRootNodeId()).intValue()) {
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
	 * Sets the indent from parent to child. Set to 12 by default.
	 * 
	 * @param indent
	 */
	public void setIndent(int indent) {
		_indent = indent;
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
	 * Sets the spacing between each row of the tree. Set to 0 by default.
	 * 
	 * @param spaceBetween
	 */
	public void setSpaceBetween(int spaceBetween) {
		_spaceBetween = spaceBetween;
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
	 * Sets the background color for a specific depth level.
	 * 
	 * @param depth
	 * @param color
	 */
	public void setDepthColor(int depth, String color) {
		if (_depthColor == null)
			_depthColor = new HashMap();
		_depthColor.put(new Integer(depth - 1), color);
	}

	/**
	 * Sets the background hover color for a specific depth level.
	 * 
	 * @param depth
	 * @param color
	 */
	public void setDepthHoverColor(int depth, String color) {
		if (_depthHoverColor == null)
			_depthHoverColor = new HashMap();
		_depthHoverColor.put(new Integer(depth - 1), color);
	}

	/**
	 * Sets the background color for all depth levels.
	 * 
	 * @param color
	 */
	public void setBackgroundColor(String color) {
		_backgroundColor = color;
	}

	/**
	 * Sets the icon image to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthImage(int depth, Image image) {
		if (_depthImage == null)
			_depthImage = new HashMap();
		_depthImage.put(new Integer(depth - 1), image);
	}

	/**
	 * Sets the current icon image to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthCurrentImage(int depth, Image image) {
		if (_depthCurrentImage == null)
			_depthCurrentImage = new HashMap();
		_depthCurrentImage.put(new Integer(depth - 1), image);
	}

	/**
	 * Sets the selected icon image to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthSelectedImage(int depth, Image image) {
		if (_depthSelectedImage == null)
			_depthSelectedImage = new HashMap();
		_depthSelectedImage.put(new Integer(depth - 1), image);
	}

	/**
	 * Sets the icon image to display for a specific depth level on hover.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthHoverImage(int depth, Image image) {
		if (_depthHoverImage == null)
			_depthHoverImage = new HashMap();
		_depthHoverImage.put(new Integer(depth - 1), image);
	}

	/**
	 * Sets the icon image to display for a specific depth level on hover.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthHeight(int depth, String height) {
		if (_depthHeight == null)
			_depthHeight = new HashMap();
		_depthHeight.put(new Integer(depth - 1), height);
	}

	/**
	 * Sets the spacing color to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthSpacingColor(int depth, String color) {
		if (_depthSpacingColor == null)
			_depthSpacingColor = new HashMap();
		_depthSpacingColor.put(new Integer(depth - 1), color);
	}

	/**
	 * Sets the spacing image to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthSpacingImage(int depth, Image image) {
		if (_depthSpacingImage == null)
			_depthSpacingImage = new HashMap();
		_depthSpacingImage.put(new Integer(depth - 1), image);
	}

	/**
	 * Sets the icon image to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthPaddingTop(int depth, int padding) {
		if (_depthPaddingTop == null)
			_depthPaddingTop = new HashMap();
		_depthPaddingTop.put(new Integer(depth - 1), new Integer(padding));
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
	 * Sets the icon image to display for all depth levels.
	 * 
	 * @param image
	 */
	public void setIconImage(Image image) {
		_iconImage = image;
	}

	/**
	 * Sets the hover icon image to display for all depth levels.
	 * 
	 * @param image
	 */
	public void setIconHoverImage(Image image) {
		_iconHoverImage = image;
	}

	/**
	 * Sets the current icon image to display for all depth levels.
	 * 
	 * @param image
	 */
	public void setIconCurrentImage(Image image) {
		_iconCurrentImage = image;
	}

	/**
	 * Sets the selected icon image to display for all depth levels.
	 * 
	 * @param image
	 */
	public void setIconSelectedImage(Image image) {
		_iconSelectedImage = image;
	}

	/**
	 * Sets the alignment of the text/link relative to the icon image specified.
	 * 
	 * @param imageAlignment
	 * @see com.idega.block.navigation.presentation.NavigationTree#setIconImage(com.idega.block.presentation.Image)
	 * @see com.idega.block.navigation.presentation.NavigationTree#setDepthImage(int,com.idega.block.presentation.Image)
	 */
	public void setImageAlignment(String imageAlignment) {
		_imageAlignment = imageAlignment;
	}

	/**
	 * Sets the distance from the icon image to the link. Set to 0 by default.
	 * 
	 * @param imagePadding
	 * @see com.idega.block.navigation.presentation.NavigationTree#setIconImage(com.idega.block.presentation.Image)
	 * @see com.idega.block.navigation.presentation.NavigationTree#setDepthImage(int,com.idega.block.presentation.Image)
	 */
	public void setImagePadding(int imagePadding) {
		_imagePadding = imagePadding;
	}

	/**
	 * Sets the indent for the first level in the tree. Set to 0 by default.
	 * 
	 * @param initialIndent
	 */
	public void setInitialIndent(int initialIndent) {
		_initialIndent = initialIndent;
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
	 * Sets the padding for individual cells in the <code>NavigationTree</code>.
	 * Set to 0 by default.
	 * 
	 * @param padding
	 */
	public void setPadding(int padding) {
		_padding = padding;
	}

	/**
	 * Sets the width of the <code>NavigationTree</code>. Set to 150px by
	 * default.
	 * 
	 * @param width
	 */
	public void setWidth(String width) {
		_width = width;
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
	 * Sets the color for the border around the <code>NavigationTree</code>
	 * 
	 * @param borderColor
	 * @see com.idega.block.navigation.presentation.NavigationTree#setShowBorder(boolean)
	 */
	public void setBorderColor(String borderColor) {
		_borderColor = borderColor;
	}

	/**
	 * Sets whether to set a border around the entire <code>NavigationTree</code>
	 * 
	 * @param showBorder
	 */
	public void setShowBorder(boolean showBorder) {
		_showBorder = showBorder;
	}

	/**
	 * Sets the closed <code>Image</code> to display in the tree.
	 * 
	 * @param closedImage
	 */
	public void setClosedImage(Image closedImage) {
		_closedImage = closedImage;
	}

	/**
	 * Sets the closed image to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthClosedImage(int depth, Image image) {
		if (_depthClosedImage == null)
			_depthClosedImage = new HashMap();
		_depthClosedImage.put(new Integer(depth - 1), image);
	}

	/**
	 * Sets the open <code>Image</code> to display in the tree.
	 * 
	 * @param openImage
	 */
	public void setOpenImage(Image openImage) {
		_openImage = openImage;
	}

	/**
	 * Sets the open image to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthOpenImage(int depth, Image image) {
		if (_depthOpenImage == null)
			_depthOpenImage = new HashMap();
		_depthOpenImage.put(new Integer(depth - 1), image);
	}

	/**
	 * Sets the blank <code>Image</code> to display in the tree when page has no
	 * children.
	 * 
	 * @param blankImage
	 */
	public void setBlankImage(Image blankImage) {
		_blankImage = blankImage;
	}

	/**
	 * Sets to debug actions.
	 * 
	 * @param debug
	 */
	public void setDebug(boolean debug) {
		_debug = debug;
	}

	/**
	 * Returns the padding used for the tree.
	 * 
	 * @return
	 */
	protected int getPadding() {
		return _padding;
	}

	/**
	 * Returns the background color used for the tree. Returns NULL if no color is
	 * set.
	 * 
	 * @return
	 */
	protected String getBackgroundColor() {
		return _backgroundColor;
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

	/**
	 * Sets the background color for the pages that are 'current', i.e. the
	 * current page or its parent pages.
	 * 
	 * @param currentColor
	 *          The currentColor to set.
	 */
	public void setCurrentColor(String currentColor) {
		setDepthCurrentColor(1, currentColor);
	}

	/**
	 * Sets the background color for a specific depth level.
	 * 
	 * @param depth
	 * @param color
	 */
	public void setDepthCurrentColor(int depth, String color) {
		if (_depthCurrentColor == null)
			_depthCurrentColor = new HashMap();
		_depthCurrentColor.put(new Integer(depth - 1), color);
	}

	/**
	 * Sets the background color for the pages that are 'selected', i.e. the
	 * selected page or its parent pages.
	 * 
	 * @param selectedColor
	 *          The selectedColor to set.
	 */
	public void setSelectedColor(String selectedColor) {
		setDepthSelectedColor(1, selectedColor);
	}

	/**
	 * Sets the background color for a specific depth level.
	 * 
	 * @param depth
	 * @param color
	 */
	public void setDepthSelectedColor(int depth, String color) {
		if (_depthSelectedColor == null)
			_depthSelectedColor = new HashMap();
		_depthSelectedColor.put(new Integer(depth - 1), color);
	}

	/**
	 * Sets the alignment for a specific depth level.
	 * 
	 * @param depth
	 * @param alignment
	 */
	public void setDepthAlignment(int depth, String alignment) {
		if (_depthAlignment == null)
			_depthAlignment = new HashMap();
		_depthAlignment.put(new Integer(depth - 1), alignment);
	}

	public void setAlignment(String alignment) {
		_textAlignment = alignment;
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

	public void setOpenOnUserHomepage(boolean openOnUserHomepage) {
		iOpenOnUserHomePage = openOnUserHomepage;
	}
	
	public void setToOrderPagesAlphabetically(boolean orderAlphabetically) {
		iOrderPagesAlphabetically = orderAlphabetically;
	}
}