package com.idega.block.navigation.presentation;
import java.util.Iterator;
import java.util.Vector;

import com.idega.builder.business.PageTreeNode;
import com.idega.builder.handler.HorizontalAlignmentHandler;
import com.idega.builder.handler.HorizontalVerticalViewHandler;
import com.idega.builder.handler.VerticalAlignmentHandler;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.data.ICPage;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWStyleManager;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.util.text.StyleConstants;
import com.idega.util.text.TextStyler;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000-2001 idega.is All Rights Reserved
 * Company:      idega
  *@author <a href="mailto:aron@idega.is">Aron Birkir</a> & <a href="mailto:laddi@idega.is">Thorhallur Helgason</a>
 * @version 1.0
 */
public class NavigationMenu extends Block {
	private final static int VERTICAL = HorizontalVerticalViewHandler.VERTICAL, HORIZONTAL = HorizontalVerticalViewHandler.HORIZONTAL;
	private int viewType = 1;
	private int rootNode = -1;
	private IWBundle iwb;
	private IWResourceBundle iwrb;
	private final static String IW_BUNDLE_IDENTIFIER = "com.idega.block.navigation";
	private int fontSize = 2;
	private String fontColor = "#000000";
	private String bgrColor = "#FFFFFF";
	private String highlightFontColor = "#999999";
	private String subHighlightFontColor = "#999999";
	private String higlightBgrColor = "#FFFFFF";
	private String tableBackGroundColor = null;
	private String width = null;
	private String height = null;
	private boolean _styles = true;
	private boolean _subStyles = true;
	private String _name;
	private String _hoverName;
	private String _subName;
	private String _subHoverName;
	private String fontStyle;
	private String subFontStyle;
	private String fontHoverColor;
	private boolean fontHoverUnderline = false;
	private String subFontHoverColor;
	private boolean subFontHoverUnderline = false;
	private Image _iconImage;
	private Image _iconOverImage;
	private Image _subIconImage;
	private Image _subIconOverImage;
	private Image _spacer;
	private Image spacer;
	private String spacerText;
	private String spacerTextStyle;
	private int spacerTextPadding = 0;
	private Image subNodeImage;
	private int _widthFromIcon = 5;
	private int _subWidthFromParent = 10;
	private int cellPadding = 0;
	private int cellSpacing = 0;
	private int _spaceBetween = 0;
	private int currentPageId = -1;
	private int parentPageId = -1;
	private boolean _addParentID = false;
	private boolean asTab = false;
	private boolean asButton = false;
	private boolean asFlipped = false;
	private boolean withRootAsHome = true;
	private boolean _showSubPages = false;
	private boolean _showAllSubPages = false;
	private String HomeVerticalAlignment = VerticalAlignmentHandler.BOTTOM;
	private String HomeHorizontalAlignment = HorizontalAlignmentHandler.RIGHT;
	public NavigationMenu() {
		this.setSpacing(2);
	}
	public void main(IWContext iwc) throws Exception{
		setStyles();
		BuilderService bs = getBuilderService(iwc);
		if (rootNode == -1) {
			rootNode = bs.getRootPageId();
		}
		//String sCurrentPageId = iwc.getParameter(com.idega.builder.business.BuilderLogic.IB_PAGE_PARAMETER);
		currentPageId = bs.getCurrentPageId(iwc);
		try {
			parentPageId = Integer.parseInt(iwc.getParameter("parent_id"));
		}
		catch (NumberFormatException e) {
			parentPageId = -1;
		}
		
		if (parentPageId == -1 && _addParentID) {
			try {
				parentPageId = ((Integer) iwc.getSessionAttribute("parent_id")).intValue();
			}
			catch (Exception e) {
				parentPageId = -1;
				parentPageId = rootNode;
			}
		}
		
		if (parentPageId != -1) {
			iwc.setSessionAttribute("parent_id", new Integer(parentPageId));
		}
		
		PageTreeNode node = new PageTreeNode(rootNode, iwc);
		boolean bottom = !HomeVerticalAlignment.equals(VerticalAlignmentHandler.TOP);
		boolean left = !HomeHorizontalAlignment.equals(HorizontalAlignmentHandler.RIGHT);
		boolean vertical = viewType == VERTICAL;
		Vector nodeVector = new Vector();
		if (withRootAsHome && ((!bottom && vertical) || (!vertical && left))) {
			nodeVector.add(node);
			withRootAsHome = false;
		}
		Iterator iter = node.getChildren();
		while (iter.hasNext())
			nodeVector.add((PageTreeNode) iter.next());
		if (withRootAsHome && (bottom || !left))
			nodeVector.add(node);
		int row = 1, col = 1;
		Table T = new Table();
		T.setCellpadding(cellPadding);
		T.setCellspacing(cellSpacing);
		if (tableBackGroundColor != null)
			T.setColor(tableBackGroundColor);
		if (width != null)
			T.setWidth(width);
		if (height != null)
			T.setHeight(height);
		Link L = null;
		spacer = Table.getTransparentCell(iwc);
		spacer.setWidth(_widthFromIcon);
		subNodeImage = (Image) spacer.clone();
		subNodeImage.setWidth(_subWidthFromParent);
		subNodeImage.setHeight(2);
		Image spaceBetween = (Image) spacer.clone();
		spaceBetween.setHeight(_spaceBetween);
		Iterator iterator = nodeVector.iterator();
		while (iterator.hasNext()) {
			PageTreeNode n = (PageTreeNode) iterator.next();
			L = getLink(n.getLocalizedNodeName(iwc), n.getNodeID(), rootNode, _addParentID, false);
			if (_iconImage != null) {
				Image image = new Image(_iconImage.getMediaURL(iwc));
				if (_iconOverImage != null)
					L.setOnMouseOverImage(image, _iconOverImage);
				T.add(image, col, row);
				T.add(spacer, col, row);
				if (!vertical)
					col++;
			}
			if (!vertical)
				T.add(L, col++, row);
			else {
				//T.mergeCells(col, row, col + 1, row);  //merging the cells causes wrong behaviour on the vertical alignment of the link
				T.add(L, col+1, row++);					 
				if (_showAllSubPages) {
					if (n.getNodeID() != rootNode)
						row = addSubLinks(iwc, T, col, row, L, n);
				}
				else {
					if (_showSubPages && (n.getNodeID() == currentPageId || n.getNodeID() == parentPageId) && n.getNodeID() != rootNode) {
						row = addSubLinks(iwc, T, col, row, L, n);
					}
				}
			}
			if (_spacer != null && iterator.hasNext()) {
				if (!vertical)
					T.add(_spacer, col++, row);
				else
					T.add(_spacer, col, row++);
			}
			if (spacerText != null && iterator.hasNext()) {
				Text text = new Text(spacerText);
				if (spacerTextStyle != null) {
					text.setFontStyle(spacerTextStyle);
				}
				if (!vertical) {
					T.setCellpaddingLeft(col, row, spacerTextPadding);
					T.setCellpaddingRight(col, row, spacerTextPadding);
					T.add(text, col++, row);
				}
				else {
					T.setCellpaddingTop(col, row, spacerTextPadding);
					T.setCellpaddingBottom(col, row, spacerTextPadding);
					T.add(text, col, row++);
				}
			}
			if (_spaceBetween > 0 && vertical) {
				T.add(spaceBetween, col, row++);
			}
		}
		add(T);
	}
	private int addSubLinks(IWContext iwc, Table table, int column, int row, Link link, PageTreeNode node) {
		Table subTable = new Table();
		subTable.setColumns(2);
		subTable.setCellpadding(0);
		subTable.setCellspacing(0);
		int subRow = 1;
		
		Iterator i = node.getChildren();
		while (i.hasNext()) {
			PageTreeNode subNode = (PageTreeNode) i.next();
			link = getLink(subNode.getLocalizedNodeName(iwc), subNode.getNodeID(), node.getNodeID(), true, true);
			if (_subWidthFromParent > 0) {
				subTable.add(subNodeImage, 1, subRow);
			}
			if (_subIconImage != null) {
				Image image = new Image(_subIconImage.getMediaURL(iwc));
				if (_subIconOverImage != null)
					link.setOnMouseOverImage(image, _subIconOverImage);
				subTable.add(image, 2, subRow);
				subTable.add(spacer, 2, subRow);
			}
			subTable.add(link, 2, subRow++);
		}
		table.add(subTable, column, row++);
		return row;
	}
	private Link getLink(String text, int PageId, int parentPageID, boolean addParentID, boolean isSubPage) {
		Link L = new Link(text);
		if (_styles) {
			if (isSubPage && _subStyles) {
				if (PageId == currentPageId)
					L.setStyle(_subHoverName);
				else
					L.setStyle(_subName);
			}
			else {
				if (PageId == currentPageId)
					L.setStyle(_hoverName);
				else
					L.setStyle(_name);
			}
		}
		else {
			if (PageId == currentPageId)
				L.setFontColor(highlightFontColor);
			else
				L.setFontColor(fontColor);
			L.setFontSize(fontSize);
		}
		L.setPage(PageId);
		if (addParentID)
			L.addParameter("parent_id", parentPageID);
		if (asButton) {
			L.setAsImageButton(asButton, true);
		}
		else if (asTab) {
			L.setAsImageTab(asTab, true, asFlipped);
		}
		return L;
	}
	private void setStyles() {
		if (_name == null)
			_name = this.getName();
		if (_name == null) {
			if (getICObjectInstanceID() != -1)
				_name = "nav_" + Integer.toString(getICObjectInstanceID());
			else
				_name = "nav_" + Double.toString(Math.random());
		}
		_hoverName = "hover_" + _name;
		_subName = "sub_" + _name;
		_subHoverName = "subHover_" + _name;
		if (fontStyle == null) {
			fontStyle = IWStyleManager.getInstance().getStyle("A");
		}
		if (getParentPage() != null && fontStyle != null) {
			TextStyler styler = new TextStyler(fontStyle);
			if (fontHoverUnderline)
				styler.setStyleValue(StyleConstants.ATTRIBUTE_TEXT_DECORATION, StyleConstants.TEXT_DECORATION_UNDERLINE);
			if (fontHoverColor != null)
				styler.setStyleValue(StyleConstants.ATTRIBUTE_COLOR, fontHoverColor);
			getParentPage().setStyleDefinition("A." + _name, fontStyle);
			getParentPage().setStyleDefinition("A." + _name + ":hover", styler.getStyleString());
			TextStyler styler2 = new TextStyler(fontStyle);
			if (highlightFontColor != null)
				styler2.setStyleValue(StyleConstants.ATTRIBUTE_COLOR, highlightFontColor);
			String style = styler2.getStyleString();
			getParentPage().setStyleDefinition("A." + _hoverName, style);
			if (fontHoverUnderline)
				styler2.setStyleValue(StyleConstants.ATTRIBUTE_TEXT_DECORATION, StyleConstants.TEXT_DECORATION_UNDERLINE);
			if (fontHoverColor != null)
				styler2.setStyleValue(StyleConstants.ATTRIBUTE_COLOR, fontHoverColor);
			getParentPage().setStyleDefinition("A." + _hoverName + ":hover", styler2.getStyleString());
		}
		else {
			_styles = false;
		}
		if (getParentPage() != null && subFontStyle != null) {
			TextStyler styler = new TextStyler(subFontStyle);
			if (subFontHoverUnderline)
				styler.setStyleValue(StyleConstants.ATTRIBUTE_TEXT_DECORATION, StyleConstants.TEXT_DECORATION_UNDERLINE);
			if (subFontHoverColor != null)
				styler.setStyleValue(StyleConstants.ATTRIBUTE_COLOR, subFontHoverColor);
			getParentPage().setStyleDefinition("A." + _subName, subFontStyle);
			getParentPage().setStyleDefinition("A." + _subName + ":hover", styler.getStyleString());
			TextStyler styler2 = new TextStyler(subFontStyle);
			if (subHighlightFontColor != null)
				styler2.setStyleValue(StyleConstants.ATTRIBUTE_COLOR, subHighlightFontColor);
			String style = styler2.getStyleString();
			getParentPage().setStyleDefinition("A." + _subHoverName, style);
			if (subFontHoverUnderline)
				styler2.setStyleValue(StyleConstants.ATTRIBUTE_TEXT_DECORATION, StyleConstants.TEXT_DECORATION_UNDERLINE);
			if (subFontHoverColor != null)
				styler2.setStyleValue(StyleConstants.ATTRIBUTE_COLOR, subFontHoverColor);
			getParentPage().setStyleDefinition("A." + _subHoverName + ":hover", styler2.getStyleString());
		}
		else {
			_subStyles = false;
		}
	}
	public void setViewType(int type) {
		viewType = type;
	}
	public void setHorizontal(boolean horizontal) {
		if (horizontal)
			viewType = HORIZONTAL;
	}
	public void setVertical(boolean vertical) {
		if (vertical)
			viewType = VERTICAL;
	}
	public void setRootNode(ICPage page) {
		rootNode = page.getID();
	}
	public void setRootNode(int rootId) {
		rootNode = rootId;
	}
	public void setFontColor(String color) {
		fontColor = color;
	}
	public void setFontSize(int size) {
		fontSize = size;
	}
	public void setFontStyle(String style) {
		fontStyle = style;
	}
	public void setSubFontStyle(String style) {
		subFontStyle = style;
	}
	public void setFontHoverColor(String color) {
		fontHoverColor = color;
	}
	public void setSubpagesFontHoverColor(String color) {
		subFontHoverColor = color;
	}
	public void setFontHoverUnderline(boolean underline) {
		fontHoverUnderline = underline;
	}
	public void setSubpagesFontHoverUnderline(boolean underline) {
		subFontHoverUnderline = underline;
	}
	public void setBackgroundColor(String color) {
		bgrColor = color;
	}
	public void setTableBackgroundColor(String color) {
		tableBackGroundColor = color;
	}
	public void setHighlightFontColor(String color) {
		highlightFontColor = color;
	}
	public void setSubpagesHighlightFontColor(String color) {
		subHighlightFontColor = color;
	}
	public void setHighligtBackgroundColor(String color) {
		highlightFontColor = color;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public void setUseRootAsHome(boolean useRootAsHome) {
		withRootAsHome = useRootAsHome;
	}
	public void setPadding(int padding) {
		cellPadding = padding;
	}
	public void setSpacing(int spacing) {
		cellSpacing = spacing;
	}
	public void setSpaceBetween(int spaceBetween) {
		_spaceBetween = spaceBetween;
	}
	public void setHomeHorizontalAlignment(String align) {
		if (align.equals(HorizontalAlignmentHandler.LEFT) || align.equals(HorizontalAlignmentHandler.RIGHT))
			HomeHorizontalAlignment = align;
	}
	public void setHomeVerticalAlignment(String align) {
		if (align.equals(VerticalAlignmentHandler.BOTTOM) || align.equals(VerticalAlignmentHandler.TOP))
			HomeVerticalAlignment = align;
	}
	public void setAsButtons(boolean asButtons) {
		asButton = asButtons;
	}
	public void setAsTabs(boolean asTabs, boolean Flipped) {
		asTab = asTabs;
		asFlipped = Flipped;
	}
	public void setIconImage(Image iconImage) {
		_iconImage = iconImage;
	}
	public void setSubpageIconImage(Image iconImage) {
		_subIconImage = iconImage;
	}
	public void setIconOverImage(Image iconOverImage) {
		_iconOverImage = iconOverImage;
	}
	public void setSubpageIconOverImage(Image iconOverImage) {
		_subIconOverImage = iconOverImage;
	}
	public void setWidthFromIcon(int widthFromIcon) {
		_widthFromIcon = widthFromIcon;
	}
	public void setSubPageWidthFromParent(int subWidthFromParent) {
		_subWidthFromParent = subWidthFromParent;
	}
	public void setSpacerImage(Image spacerImage) {
		_spacer = spacerImage;
	}
	public void setAddParentID(boolean addID) {
		if (addID && (!_showSubPages || !_showAllSubPages))
			_addParentID = addID;
	}
	public void setShowSubPages(boolean showSubPages) {
		_showSubPages = showSubPages;
		setAddParentID(true);
	}
	public void setShowAllSubPages(boolean showAllSubPages) {
		_showAllSubPages = showAllSubPages;
		setAddParentID(true);
	}
	public Object clone() {
		NavigationMenu obj = null;
		try {
			obj = (NavigationMenu) super.clone();
			if (this._iconImage != null) {
				obj._iconImage = (Image) this._iconImage.clone();
			}
			if (this._iconOverImage != null) {
				obj._iconOverImage = (Image) this._iconOverImage.clone();
			}
			if (this._subIconImage != null) {
				obj._subIconImage = (Image) this._subIconImage.clone();
			}
			if (this._subIconOverImage != null) {
				obj._subIconOverImage = (Image) this._subIconOverImage.clone();
			}
			if (this._spacer != null) {
				obj._spacer = (Image) this._spacer.clone();
			}
			if (this.spacer != null) {
				obj.spacer = (Image) this.spacer.clone();
			}
			if (this.subNodeImage != null) {
				obj.subNodeImage = (Image) this.subNodeImage.clone();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		return obj;
	}
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
	/**
	 * @param spacerText The spacerText to set.
	 */
	public void setSpacerText(String spacerText) {
		this.spacerText = spacerText;
	}
	/**
	 * @param spacerTextStyle The spacerTextStyle to set.
	 */
	public void setSpacerTextStyle(String spacerTextStyle) {
		this.spacerTextStyle = spacerTextStyle;
	}
	
	public void setSpacerTextPadding(int padding) {
		this.spacerTextPadding = padding;
	}
}
