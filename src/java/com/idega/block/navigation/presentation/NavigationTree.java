/*
 * Created on 16.9.2003
 */
package com.idega.block.navigation.presentation;

import java.util.HashMap;
import java.util.Map;
import javax.faces.component.UIComponent;
import com.idega.core.data.ICTreeNode;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;

/**
 * <p>
 * This is the old style standard "NavigationList" component. This class by default renders its layout in an html table.
 * This class is now not recommended to use but is kept because of legacy reasons. The new class replacing this class is this class' superclass,
 * NavigationList which is based on a CSS based layout. 
 * @see NavigationList
 * </p>
 *  Last modified: $Date: 2005/02/22 18:02:17 $ by $Author: tryggvil $
 * 
 * @author <a href="mailto:laddi@idega.com">laddi</a>,<a href="mailto:tryggvil@idega.com">tryggvil</a>
 * @version $Revision: 1.38 $
 */
public class NavigationTree extends NavigationList{
	
	private int _imagePadding = 0;	
	private int _initialIndent = 0;
	private int _indent = 12;
	private int _spaceBetween = 0;
	private int _padding = 0;
	protected String _textAlignment;
	protected String _imageAlignment;
	//protected String _width;
	protected String _backgroundColor;
	protected String _borderColor;
	private boolean _showBorder = false;
	protected boolean useStyleBasedLayout=false;
	
	private Image _iconImage;
	private Image _iconHoverImage;
	private Image _iconSelectedImage;
	private Image _iconCurrentImage;
	private Image _openImage;
	private Image _closedImage;
	private Image _blankImage;
	private Image _paddingImage;
	
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
	
	//Default width of 150 for table layout
	protected static String DEFAULT_WIDTH="150";
	
	public NavigationTree(){
		//Overriding settings from the superclass:
		_textAlignment = Table.HORIZONTAL_ALIGN_LEFT;
		_imageAlignment = Image.ALIGNMENT_ABSOLUTE_MIDDLE;
		setWidth(DEFAULT_WIDTH);
		
		//Temporary debug:
		//setUseStyleBasedLayout(true);
	}

	
	/**
	 * Initialized the tree (by default rendered out as a Table for legacy reasons)
	 * tree.
	 * 
	 * @param iwc
	 * @return
	 */
	protected UIComponent getTree(IWContext iwc) {
		if(isUseStyleBasedLayout()){
			return super.getTree(iwc);
		}
		else{
			return getTreeTable(iwc);
		}
	}
	
	/**
	 * Initialized the surrounding <code>Table</code> and adds all pages to the
	 * tree.
	 * 
	 * @param iwc
	 * @return
	 */
	protected UIComponent getTreeTable(IWContext iwc) {
		
		if (_paddingImage != null) {
			_paddingImage.setPaddingLeft(_imagePadding);
			_paddingImage.setPaddingRight(_imagePadding);
			_paddingImage.setAlignment(Image.ALIGNMENT_ABSOLUTE_MIDDLE);
		}
		
		Table table = new Table();
		String width = getWidth();
		if(width!=null){
			table.setWidth(width);
		}
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
		if (getShowRoot()) {
			addObject(iwc, getRootNode(), table, row, depth);
			setRowAttributes(table, getRootNode(), row, depth, false);
		}

		return table;
	}



	/**
	 * Overrided here because of legacy Table implementation
	 */
	protected UIComponent getSubTreeComponent(UIComponent outerContainer,int row,int depth){
		if(isUseStyleBasedLayout()){
			return super.getSubTreeComponent(outerContainer,row,depth);
		}
		else{
			//in this case it's returning the same table used for the whole tree
			return outerContainer;
		}
	}
	
	/**
	 * Overrided here because of legacy Table implementation.
	 * 
	 */
	protected UIComponent getNodeComponent(UIComponent outerContainer,int row,int depth){
		if(isUseStyleBasedLayout()){
			return super.getSubTreeComponent(outerContainer,row,depth);
		}
		else{
			//in this case it's returning the same table used for the whole tree
			return outerContainer;
		}
	}
	
	protected int setRowAttributes(UIComponent listComponent, ICTreeNode page, int row, int depth, boolean isLastChild) {
		if(listComponent instanceof Table){
			Table table = (Table)listComponent;
			return setTableRowAttributes(table,page,row,depth,isLastChild);
		}
		else{
			return super.setRowAttributes(listComponent,page,row,depth,isLastChild);
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
	protected int setTableRowAttributes(Table table, ICTreeNode page, int row, int depth, boolean isLastChild) {
		table.setCellpadding(1, row, _padding);
		if (table.getColumns() == 2)
			table.setVerticalAlignment(2, row, Table.VERTICAL_ALIGN_BOTTOM);

		String alignment = getDepthAlignment(depth);
		if (alignment == null) {
			alignment = _textAlignment;
		}

		if (alignment.equals(Table.HORIZONTAL_ALIGN_LEFT)) {
			if (_paddingImage != null && depth > 0) {
				for (int a = 0; a < depth; a++) {
					table.add(_paddingImage, 1, row);
				}
			}
			else {
				table.setCellpaddingLeft(1, row, getIndent(depth));
			}
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
	
	protected int addHeaderObject(Table table, int row) {
		return row;
	}
	
	/*
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
				Page populatedPage = getBuilderService(iwc).getPage(String.valueOf(page.getNodeID()));
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
	*/
	
	
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
	protected void addObject(IWContext iwc, ICTreeNode page, UIComponent list, int row, int depth) {
		if(list instanceof Table){
			Table table = (Table)list;
			addObjectToTable(iwc,page,table,row,depth);
		}
		else{
			super.addObject(iwc,page,list,row,depth);
		}
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
	protected void addObjectToTable(IWContext iwc, ICTreeNode page, Table table, int row, int depth) {
		PresentationObject link = (PresentationObject)getLink(page, iwc, depth);

		Image curtainImage = getCurtainImage(depth, isOpen(page));
		if (curtainImage != null && page.getChildCount() > 0) {
			Link imageLink = new Link(curtainImage);
			addParameterToLink(imageLink, page);
			table.add(imageLink, 2, row);
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
			if (getMarkOnlyCurrentPage()) {
				if (getCurrentPageId() == page.getNodeID()) {
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
	
	
	public void setPaddingImage(Image image) {
		_paddingImage = image;
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
	 * Get the indent for the depth specified.
	 * 
	 * @param depth
	 * @return
	 */
	private int getIndent(int depth) {
		return (depth * _indent) + _initialIndent + _padding;
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
	 * Sets the spacing between each row of the tree. Set to 0 by default.
	 * 
	 * @param spaceBetween
	 */
	public void setSpaceBetween(int spaceBetween) {
		_spaceBetween = spaceBetween;
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
		super.setWidth(width);
	}
	

	public void setAlignment(String alignment) {
		_textAlignment = alignment;
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
	 * Sets the indent from parent to child. Set to 12 by default.
	 * 
	 * @param indent
	 */
	public void setIndent(int indent) {
		_indent = indent;
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
	
	/**
	 * Gets if to use the style-class based layout (from the superclass) rather than the default table based one
	 * @return
	 */
	protected boolean isUseStyleBasedLayout() {
		return useStyleBasedLayout;
	}
	/**
	 * Set to use the style-class based layout. By default this is set to false but if set to true it uses the
	 * pure CSS based layout instead of the default Table based one. If this is set to true it disables most of the "layout" set methods in this class.
	 * @param useStyleBasedLayout
	 */
	protected void setUseStyleBasedLayout(boolean useStyleBasedLayout) {
		this.useStyleBasedLayout = useStyleBasedLayout;
	}
}