package com.idega.block.navigation.presentation;
import java.util.Iterator;
import java.util.Vector;

import com.idega.block.navigation.utils.NavigationConstants;
import com.idega.builder.business.PageTreeNode;
import com.idega.builder.handler.HorizontalAlignmentHandler;
import com.idega.builder.handler.HorizontalVerticalViewHandler;
import com.idega.builder.handler.VerticalAlignmentHandler;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.data.ICPage;
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
	private int fontSize = 2;
	private String fontColor = "#000000";
	private String highlightFontColor = "#999999";
	private String subHighlightFontColor = "#999999";
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
		if (this.rootNode == -1) {
			this.rootNode = bs.getRootPageId();
		}
		//String sCurrentPageId = iwc.getParameter(com.idega.builder.business.BuilderLogic.IB_PAGE_PARAMETER);
		this.currentPageId = bs.getCurrentPageId(iwc);
		try {
			this.parentPageId = Integer.parseInt(iwc.getParameter("parent_id"));
		}
		catch (NumberFormatException e) {
			this.parentPageId = -1;
		}
		
		if (this.parentPageId == -1 && this._addParentID) {
			try {
				this.parentPageId = ((Integer) iwc.getSessionAttribute("parent_id")).intValue();
			}
			catch (Exception e) {
				this.parentPageId = -1;
				this.parentPageId = this.rootNode;
			}
		}
		
		if (this.parentPageId != -1) {
			iwc.setSessionAttribute("parent_id", new Integer(this.parentPageId));
		}
		
		PageTreeNode node = new PageTreeNode(this.rootNode, iwc);
		boolean bottom = !this.HomeVerticalAlignment.equals(VerticalAlignmentHandler.TOP);
		boolean left = !this.HomeHorizontalAlignment.equals(HorizontalAlignmentHandler.RIGHT);
		boolean vertical = this.viewType == VERTICAL;
		Vector nodeVector = new Vector();
		if (this.withRootAsHome && ((!bottom && vertical) || (!vertical && left))) {
			nodeVector.add(node);
			this.withRootAsHome = false;
		}
		Iterator iter = node.getChildrenIterator();
		while (iter.hasNext()) {
			nodeVector.add(iter.next());
		}
		if (this.withRootAsHome && (bottom || !left)) {
			nodeVector.add(node);
		}
		int row = 1, col = 1;
		Table T = new Table();
		T.setCellpadding(this.cellPadding);
		T.setCellspacing(this.cellSpacing);
		if (this.tableBackGroundColor != null) {
			T.setColor(this.tableBackGroundColor);
		}
		if (this.width != null) {
			T.setWidth(this.width);
		}
		if (this.height != null) {
			T.setHeight(this.height);
		}
		Link L = null;
		this.spacer = Table.getTransparentCell(iwc);
		this.spacer.setWidth(this._widthFromIcon);
		this.subNodeImage = (Image) this.spacer.clone();
		this.subNodeImage.setWidth(this._subWidthFromParent);
		this.subNodeImage.setHeight(2);
		Image spaceBetween = (Image) this.spacer.clone();
		spaceBetween.setHeight(this._spaceBetween);
		Iterator iterator = nodeVector.iterator();
		while (iterator.hasNext()) {
			PageTreeNode n = (PageTreeNode) iterator.next();
			L = getLink(n.getLocalizedNodeName(iwc), n.getNodeID(), this.rootNode, this._addParentID, false);
			if (this._iconImage != null) {
				Image image = new Image(this._iconImage.getMediaURL(iwc));
				if (this._iconOverImage != null) {
					L.setOnMouseOverImage(image, this._iconOverImage);
				}
				T.add(image, col, row);
				T.add(this.spacer, col, row);
				if (!vertical) {
					col++;
				}
			}
			if (!vertical) {
				T.add(L, col++, row);
			}
			else {
				//T.mergeCells(col, row, col + 1, row);  //merging the cells causes wrong behaviour on the vertical alignment of the link
				T.add(L, col+1, row++);					 
				if (this._showAllSubPages) {
					if (n.getNodeID() != this.rootNode) {
						row = addSubLinks(iwc, T, col, row, L, n);
					}
				}
				else {
					if (this._showSubPages && (n.getNodeID() == this.currentPageId || n.getNodeID() == this.parentPageId) && n.getNodeID() != this.rootNode) {
						row = addSubLinks(iwc, T, col, row, L, n);
					}
				}
			}
			if (this._spacer != null && iterator.hasNext()) {
				if (!vertical) {
					T.add(this._spacer, col++, row);
				}
				else {
					T.add(this._spacer, col, row++);
				}
			}
			if (this.spacerText != null && iterator.hasNext()) {
				Text text = new Text(this.spacerText);
				if (this.spacerTextStyle != null) {
					text.setFontStyle(this.spacerTextStyle);
				}
				if (!vertical) {
					T.setCellpaddingLeft(col, row, this.spacerTextPadding);
					T.setCellpaddingRight(col, row, this.spacerTextPadding);
					T.add(text, col++, row);
				}
				else {
					T.setCellpaddingTop(col, row, this.spacerTextPadding);
					T.setCellpaddingBottom(col, row, this.spacerTextPadding);
					T.add(text, col, row++);
				}
			}
			if (this._spaceBetween > 0 && vertical) {
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
		
		Iterator i = node.getChildrenIterator();
		while (i.hasNext()) {
			PageTreeNode subNode = (PageTreeNode) i.next();
			link = getLink(subNode.getLocalizedNodeName(iwc), subNode.getNodeID(), node.getNodeID(), true, true);
			if (this._subWidthFromParent > 0) {
				subTable.add(this.subNodeImage, 1, subRow);
			}
			if (this._subIconImage != null) {
				Image image = new Image(this._subIconImage.getMediaURL(iwc));
				if (this._subIconOverImage != null) {
					link.setOnMouseOverImage(image, this._subIconOverImage);
				}
				subTable.add(image, 2, subRow);
				subTable.add(this.spacer, 2, subRow);
			}
			subTable.add(link, 2, subRow++);
		}
		table.add(subTable, column, row++);
		return row;
	}
	private Link getLink(String text, int PageId, int parentPageID, boolean addParentID, boolean isSubPage) {
		Link L = new Link(text);
		if (this._styles) {
			if (isSubPage && this._subStyles) {
				if (PageId == this.currentPageId) {
					L.setStyle(this._subHoverName);
				}
				else {
					L.setStyle(this._subName);
				}
			}
			else {
				if (PageId == this.currentPageId) {
					L.setStyle(this._hoverName);
				}
				else {
					L.setStyle(this._name);
				}
			}
		}
		else {
			if (PageId == this.currentPageId) {
				L.setFontColor(this.highlightFontColor);
			}
			else {
				L.setFontColor(this.fontColor);
			}
			L.setFontSize(this.fontSize);
		}
		L.setPage(PageId);
		if (addParentID) {
			L.addParameter("parent_id", parentPageID);
		}
		if (this.asButton) {
			L.setAsImageButton(this.asButton, true);
		}
		else if (this.asTab) {
			L.setAsImageTab(this.asTab, true, this.asFlipped);
		}
		return L;
	}
	private void setStyles() {
		if (this._name == null) {
			this._name = this.getName();
		}
		if (this._name == null) {
			if (getICObjectInstanceID() != -1) {
				this._name = "nav_" + Integer.toString(getICObjectInstanceID());
			}
			else {
				this._name = "nav_" + Double.toString(Math.random());
			}
		}
		this._hoverName = "hover_" + this._name;
		this._subName = "sub_" + this._name;
		this._subHoverName = "subHover_" + this._name;
		if (this.fontStyle == null) {
			this.fontStyle = IWStyleManager.getInstance().getStyle("A");
		}
		if (getParentPage() != null && this.fontStyle != null) {
			TextStyler styler = new TextStyler(this.fontStyle);
			if (this.fontHoverUnderline) {
				styler.setStyleValue(StyleConstants.ATTRIBUTE_TEXT_DECORATION, StyleConstants.TEXT_DECORATION_UNDERLINE);
			}
			if (this.fontHoverColor != null) {
				styler.setStyleValue(StyleConstants.ATTRIBUTE_COLOR, this.fontHoverColor);
			}
			getParentPage().setStyleDefinition("A." + this._name, this.fontStyle);
			getParentPage().setStyleDefinition("A." + this._name + ":hover", styler.getStyleString());
			TextStyler styler2 = new TextStyler(this.fontStyle);
			if (this.highlightFontColor != null) {
				styler2.setStyleValue(StyleConstants.ATTRIBUTE_COLOR, this.highlightFontColor);
			}
			String style = styler2.getStyleString();
			getParentPage().setStyleDefinition("A." + this._hoverName, style);
			if (this.fontHoverUnderline) {
				styler2.setStyleValue(StyleConstants.ATTRIBUTE_TEXT_DECORATION, StyleConstants.TEXT_DECORATION_UNDERLINE);
			}
			if (this.fontHoverColor != null) {
				styler2.setStyleValue(StyleConstants.ATTRIBUTE_COLOR, this.fontHoverColor);
			}
			getParentPage().setStyleDefinition("A." + this._hoverName + ":hover", styler2.getStyleString());
		}
		else {
			this._styles = false;
		}
		if (getParentPage() != null && this.subFontStyle != null) {
			TextStyler styler = new TextStyler(this.subFontStyle);
			if (this.subFontHoverUnderline) {
				styler.setStyleValue(StyleConstants.ATTRIBUTE_TEXT_DECORATION, StyleConstants.TEXT_DECORATION_UNDERLINE);
			}
			if (this.subFontHoverColor != null) {
				styler.setStyleValue(StyleConstants.ATTRIBUTE_COLOR, this.subFontHoverColor);
			}
			getParentPage().setStyleDefinition("A." + this._subName, this.subFontStyle);
			getParentPage().setStyleDefinition("A." + this._subName + ":hover", styler.getStyleString());
			TextStyler styler2 = new TextStyler(this.subFontStyle);
			if (this.subHighlightFontColor != null) {
				styler2.setStyleValue(StyleConstants.ATTRIBUTE_COLOR, this.subHighlightFontColor);
			}
			String style = styler2.getStyleString();
			getParentPage().setStyleDefinition("A." + this._subHoverName, style);
			if (this.subFontHoverUnderline) {
				styler2.setStyleValue(StyleConstants.ATTRIBUTE_TEXT_DECORATION, StyleConstants.TEXT_DECORATION_UNDERLINE);
			}
			if (this.subFontHoverColor != null) {
				styler2.setStyleValue(StyleConstants.ATTRIBUTE_COLOR, this.subFontHoverColor);
			}
			getParentPage().setStyleDefinition("A." + this._subHoverName + ":hover", styler2.getStyleString());
		}
		else {
			this._subStyles = false;
		}
	}
	public void setViewType(int type) {
		this.viewType = type;
	}
	public void setHorizontal(boolean horizontal) {
		if (horizontal) {
			this.viewType = HORIZONTAL;
		}
	}
	public void setVertical(boolean vertical) {
		if (vertical) {
			this.viewType = VERTICAL;
		}
	}
	public void setRootNode(ICPage page) {
		this.rootNode = page.getID();
	}
	public void setRootNode(int rootId) {
		this.rootNode = rootId;
	}
	public void setFontColor(String color) {
		this.fontColor = color;
	}
	public void setFontSize(int size) {
		this.fontSize = size;
	}
	public void setFontStyle(String style) {
		this.fontStyle = style;
	}
	public void setSubFontStyle(String style) {
		this.subFontStyle = style;
	}
	public void setFontHoverColor(String color) {
		this.fontHoverColor = color;
	}
	public void setSubpagesFontHoverColor(String color) {
		this.subFontHoverColor = color;
	}
	public void setFontHoverUnderline(boolean underline) {
		this.fontHoverUnderline = underline;
	}
	public void setSubpagesFontHoverUnderline(boolean underline) {
		this.subFontHoverUnderline = underline;
	}
	public void setBackgroundColor(String color) {
	}
	public void setTableBackgroundColor(String color) {
		this.tableBackGroundColor = color;
	}
	public void setHighlightFontColor(String color) {
		this.highlightFontColor = color;
	}
	public void setSubpagesHighlightFontColor(String color) {
		this.subHighlightFontColor = color;
	}
	public void setHighligtBackgroundColor(String color) {
		this.highlightFontColor = color;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public void setUseRootAsHome(boolean useRootAsHome) {
		this.withRootAsHome = useRootAsHome;
	}
	public void setPadding(int padding) {
		this.cellPadding = padding;
	}
	public void setSpacing(int spacing) {
		this.cellSpacing = spacing;
	}
	public void setSpaceBetween(int spaceBetween) {
		this._spaceBetween = spaceBetween;
	}
	public void setHomeHorizontalAlignment(String align) {
		if (align.equals(HorizontalAlignmentHandler.LEFT) || align.equals(HorizontalAlignmentHandler.RIGHT)) {
			this.HomeHorizontalAlignment = align;
		}
	}
	public void setHomeVerticalAlignment(String align) {
		if (align.equals(VerticalAlignmentHandler.BOTTOM) || align.equals(VerticalAlignmentHandler.TOP)) {
			this.HomeVerticalAlignment = align;
		}
	}
	public void setAsButtons(boolean asButtons) {
		this.asButton = asButtons;
	}
	public void setAsTabs(boolean asTabs, boolean Flipped) {
		this.asTab = asTabs;
		this.asFlipped = Flipped;
	}
	public void setIconImage(Image iconImage) {
		this._iconImage = iconImage;
	}
	public void setSubpageIconImage(Image iconImage) {
		this._subIconImage = iconImage;
	}
	public void setIconOverImage(Image iconOverImage) {
		this._iconOverImage = iconOverImage;
	}
	public void setSubpageIconOverImage(Image iconOverImage) {
		this._subIconOverImage = iconOverImage;
	}
	public void setWidthFromIcon(int widthFromIcon) {
		this._widthFromIcon = widthFromIcon;
	}
	public void setSubPageWidthFromParent(int subWidthFromParent) {
		this._subWidthFromParent = subWidthFromParent;
	}
	public void setSpacerImage(Image spacerImage) {
		this._spacer = spacerImage;
	}
	public void setAddParentID(boolean addID) {
		if (addID && (!this._showSubPages || !this._showAllSubPages)) {
			this._addParentID = addID;
		}
	}
	public void setShowSubPages(boolean showSubPages) {
		this._showSubPages = showSubPages;
		setAddParentID(true);
	}
	public void setShowAllSubPages(boolean showAllSubPages) {
		this._showAllSubPages = showAllSubPages;
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
		return NavigationConstants.IW_BUNDLE_IDENTIFIER;
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
