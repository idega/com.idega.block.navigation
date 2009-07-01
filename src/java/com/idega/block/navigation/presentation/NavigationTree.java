/*
 * Created on 16.9.2003
 */
package com.idega.block.navigation.presentation;

import java.util.HashMap;
import java.util.List;
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
 *  Last modified: $Date: 2008/10/20 13:51:54 $ by $Author: laddi $
 * 
 * @author <a href="mailto:laddi@idega.com">laddi</a>,<a href="mailto:tryggvil@idega.com">tryggvil</a>
 * @version $Revision: 1.46 $
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
		this._textAlignment = Table.HORIZONTAL_ALIGN_LEFT;
		this._imageAlignment = Image.ALIGNMENT_ABSOLUTE_MIDDLE;
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
	@Override
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
		
		if (this._paddingImage != null) {
			this._paddingImage.setPaddingLeft(this._imagePadding);
			this._paddingImage.setPaddingRight(this._imagePadding);
			this._paddingImage.setAlignment(Image.ALIGNMENT_ABSOLUTE_MIDDLE);
		}
		
		Table table = new Table();
		String width = getWidth();
		if(width!=null){
			table.setWidth(width);
		}
		table.setCellpaddingAndCellspacing(0);
		if (this._openImage != null || this._closedImage != null) {
			table.setColumns(2);
			table.setWidth(1, Table.HUNDRED_PERCENT);
		}
		if (this._showBorder) {
			table.setLineFrame(true);
			table.setLinesBetween(true);
			if (this._borderColor != null) {
				table.setLineColor(this._borderColor);
			}
		}

		int row = 1;
		int depth = 0;

		row = addHeaderObject(table, row);
		row = addToTree(iwc, getRootNode().getChildren(), table, row, depth);
		if (getShowRoot()) {
			addObject(iwc, getRootNode(), table, row, depth, false);
			setRowAttributes(table, getRootNode(), row, depth, false, true);
		}

		return table;
	}



	/**
	 * Overrided here because of legacy Table implementation
	 */
	@Override
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
	protected UIComponent getNodeComponent(UIComponent outerContainer,List pages,ICTreeNode page,int row,int depth, int index, boolean isdisabled){
		if(isUseStyleBasedLayout()){
			return super.getSubTreeComponent(outerContainer,row,depth);
		}
		else{
			//in this case it's returning the same table used for the whole tree
			return outerContainer;
		}
	}
	
	@Override
	protected int setRowAttributes(UIComponent listComponent, ICTreeNode page, int row, int depth, boolean isFirstChild, boolean isLastChild) {
		if(listComponent instanceof Table){
			Table table = (Table)listComponent;
			return setTableRowAttributes(table,page,row,depth,isLastChild);
		}
		else{
			return super.setRowAttributes(listComponent,page,row,depth,isFirstChild,isLastChild);
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
		table.setCellpadding(1, row, this._padding);
		if (table.getColumns() == 2) {
			table.setVerticalAlignment(2, row, Table.VERTICAL_ALIGN_BOTTOM);
		}

		String alignment = getDepthAlignment(depth);
		if (alignment == null) {
			alignment = this._textAlignment;
		}

		if (alignment.equals(Table.HORIZONTAL_ALIGN_LEFT)) {
			if (this._paddingImage != null && depth > 0) {
				for (int a = 0; a < depth; a++) {
					table.add(this._paddingImage, 1, row);
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
		if (color != null) {
			table.setRowColor(row, color);
		}

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
		if (this._spaceBetween > 0) {
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
			table.setHeight(row, this._spaceBetween);

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
	 * @param row
	 * @param depth
	 * @param table
	 */
	@Override
	protected void addObject(IWContext iwc, ICTreeNode page, UIComponent list, int row, int depth, boolean linkIsDisabled) {
		if(list instanceof Table){
			Table table = (Table)list;
			addObjectToTable(iwc,page,table,row,depth);
		}
		else{
			super.addObject(iwc,page,list,row,depth, linkIsDisabled);
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
		PresentationObject link = (PresentationObject)getLink(page, null, iwc, depth, false);

		Image curtainImage = getCurtainImage(depth, isOpen(page));
		if (curtainImage != null && page.getChildCount() > 0) {
			Link imageLink = new Link(curtainImage);
			addParameterToLink(imageLink, page, false);
			table.add(imageLink, 2, row);
		}
		else if (this._blankImage != null) {
			table.add(this._blankImage, 2, row);
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
			if (this._imagePadding > 0) {
				linkImage.setPaddingRight(this._imagePadding);
			}
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

	
	private Image getCurtainImage(int depth, boolean isOpen) {
		if (isOpen) {
			if (this._depthOpenImage != null) {
				return (Image) this._depthOpenImage.get(new Integer(depth));
			}
			return this._openImage;
		}
		else {
			if (this._depthClosedImage != null) {
				return (Image) this._depthClosedImage.get(new Integer(depth));
			}
			return this._closedImage;
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
		if (this._depthHoverImage != null) {
			Image image = (Image) this._depthHoverImage.get(new Integer(depth));
			if (image != null) {
				image.setAlignment(this._imageAlignment);
				return image;
			}
		}
		if (this._iconHoverImage != null) {
			this._iconHoverImage.setAlignment(this._imageAlignment);
			return this._iconHoverImage;
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
				if (getCurrentPageId() == Integer.parseInt(page.getId())) {
					if (this._depthCurrentColor != null) {
						String color = (String) this._depthCurrentColor.get(new Integer(depth));
						if (color != null) {
							return color;
						}
						else {
							return (String) this._depthCurrentColor.get(new Integer(0));
						}
					}
				}
			}
			else {
				if (isCurrent(page)) {
					if (this._depthCurrentColor != null) {
						String color = (String) this._depthCurrentColor.get(new Integer(depth));
						if (color != null) {
							return color;
						}
						else {
							return (String) this._depthCurrentColor.get(new Integer(0));
						}
					}
				}
				if (isSelected(page)) {
					if (this._depthSelectedColor != null) {
						String color = (String) this._depthSelectedColor.get(new Integer(depth));
						if (color != null) {
							return color;
						}
						else {
							return (String) this._depthSelectedColor.get(new Integer(0));
						}
					}
				}
			}
		}

		if (this._depthColor != null) {
			String color = (String) this._depthColor.get(new Integer(depth));
			if (color != null) {
				return color;
			}
		}
		if (this._backgroundColor != null) {
			return this._backgroundColor;
		}

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
		if (this._depthHeight != null) {
			String height = (String) this._depthHeight.get(new Integer(depth));
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
		if (this._depthSpacingColor != null) {
			String color = (String) this._depthSpacingColor.get(new Integer(depth));
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
		if (this._depthSpacingImage != null) {
			Image image = (Image) this._depthSpacingImage.get(new Integer(depth));
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
		if (this._depthAlignment != null) {
			String alignment = (String) this._depthAlignment.get(new Integer(depth));
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
		if (this._depthPaddingTop != null) {
			Integer padding = (Integer) this._depthPaddingTop.get(new Integer(depth));
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
		if (this._depthImage != null) {
			Image image = (Image) this._depthImage.get(new Integer(depth));
			if (image != null) {
				image.setAlignment(this._imageAlignment);
				return image;
			}
		}
		if (this._iconImage != null) {
			this._iconImage.setAlignment(this._imageAlignment);
			return this._iconImage;
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
		if (this._depthSelectedImage != null) {
			Image image = (Image) this._depthSelectedImage.get(new Integer(depth));
			if (image != null) {
				image.setAlignment(this._imageAlignment);
				return image;
			}
		}
		if (this._iconSelectedImage != null) {
			this._iconSelectedImage.setAlignment(this._imageAlignment);
			return this._iconSelectedImage;
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
		if (this._depthCurrentImage != null) {
			Image image = (Image) this._depthCurrentImage.get(new Integer(depth));
			if (image != null) {
				image.setAlignment(this._imageAlignment);
				return image;
			}
		}
		if (this._iconCurrentImage != null) {
			this._iconCurrentImage.setAlignment(this._imageAlignment);
			return this._iconCurrentImage;
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
		if (this._depthColor == null) {
			this._depthColor = new HashMap();
		}
		this._depthColor.put(new Integer(depth - 1), color);
	}

	/**
	 * Sets the background hover color for a specific depth level.
	 * 
	 * @param depth
	 * @param color
	 */
	public void setDepthHoverColor(int depth, String color) {
		if (this._depthHoverColor == null) {
			this._depthHoverColor = new HashMap();
		}
		this._depthHoverColor.put(new Integer(depth - 1), color);
	}

	
	/**
	 * Sets the icon image to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthImage(int depth, Image image) {
		if (this._depthImage == null) {
			this._depthImage = new HashMap();
		}
		this._depthImage.put(new Integer(depth - 1), image);
	}

	/**
	 * Sets the current icon image to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthCurrentImage(int depth, Image image) {
		if (this._depthCurrentImage == null) {
			this._depthCurrentImage = new HashMap();
		}
		this._depthCurrentImage.put(new Integer(depth - 1), image);
	}

	/**
	 * Sets the selected icon image to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthSelectedImage(int depth, Image image) {
		if (this._depthSelectedImage == null) {
			this._depthSelectedImage = new HashMap();
		}
		this._depthSelectedImage.put(new Integer(depth - 1), image);
	}

	/**
	 * Sets the icon image to display for a specific depth level on hover.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthHoverImage(int depth, Image image) {
		if (this._depthHoverImage == null) {
			this._depthHoverImage = new HashMap();
		}
		this._depthHoverImage.put(new Integer(depth - 1), image);
	}

	/**
	 * Sets the icon image to display for a specific depth level on hover.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthHeight(int depth, String height) {
		if (this._depthHeight == null) {
			this._depthHeight = new HashMap();
		}
		this._depthHeight.put(new Integer(depth - 1), height);
	}

	/**
	 * Sets the spacing color to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthSpacingColor(int depth, String color) {
		if (this._depthSpacingColor == null) {
			this._depthSpacingColor = new HashMap();
		}
		this._depthSpacingColor.put(new Integer(depth - 1), color);
	}

	/**
	 * Sets the spacing image to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthSpacingImage(int depth, Image image) {
		if (this._depthSpacingImage == null) {
			this._depthSpacingImage = new HashMap();
		}
		this._depthSpacingImage.put(new Integer(depth - 1), image);
	}

	/**
	 * Sets the icon image to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthPaddingTop(int depth, int padding) {
		if (this._depthPaddingTop == null) {
			this._depthPaddingTop = new HashMap();
		}
		this._depthPaddingTop.put(new Integer(depth - 1), new Integer(padding));
	}


	/**
	 * Sets the icon image to display for all depth levels.
	 * 
	 * @param image
	 */
	public void setIconImage(Image image) {
		this._iconImage = image;
	}

	/**
	 * Sets the hover icon image to display for all depth levels.
	 * 
	 * @param image
	 */
	public void setIconHoverImage(Image image) {
		this._iconHoverImage = image;
	}

	/**
	 * Sets the current icon image to display for all depth levels.
	 * 
	 * @param image
	 */
	public void setIconCurrentImage(Image image) {
		this._iconCurrentImage = image;
	}

	/**
	 * Sets the selected icon image to display for all depth levels.
	 * 
	 * @param image
	 */
	public void setIconSelectedImage(Image image) {
		this._iconSelectedImage = image;
	}

	/**
	 * Sets the alignment of the text/link relative to the icon image specified.
	 * 
	 * @param imageAlignment
	 * @see com.idega.block.navigation.presentation.NavigationTree#setIconImage(com.idega.block.presentation.Image)
	 * @see com.idega.block.navigation.presentation.NavigationTree#setDepthImage(int,com.idega.block.presentation.Image)
	 */
	public void setImageAlignment(String imageAlignment) {
		this._imageAlignment = imageAlignment;
	}

	/**
	 * Sets the distance from the icon image to the link. Set to 0 by default.
	 * 
	 * @param imagePadding
	 * @see com.idega.block.navigation.presentation.NavigationTree#setIconImage(com.idega.block.presentation.Image)
	 * @see com.idega.block.navigation.presentation.NavigationTree#setDepthImage(int,com.idega.block.presentation.Image)
	 */
	public void setImagePadding(int imagePadding) {
		this._imagePadding = imagePadding;
	}
	/**
	 * Sets the closed <code>Image</code> to display in the tree.
	 * 
	 * @param closedImage
	 */
	public void setClosedImage(Image closedImage) {
		this._closedImage = closedImage;
	}

	/**
	 * Sets the closed image to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthClosedImage(int depth, Image image) {
		if (this._depthClosedImage == null) {
			this._depthClosedImage = new HashMap();
		}
		this._depthClosedImage.put(new Integer(depth - 1), image);
	}

	/**
	 * Sets the open <code>Image</code> to display in the tree.
	 * 
	 * @param openImage
	 */
	public void setOpenImage(Image openImage) {
		this._openImage = openImage;
	}

	/**
	 * Sets the open image to display for a specific depth level.
	 * 
	 * @param depth
	 * @param image
	 */
	public void setDepthOpenImage(int depth, Image image) {
		if (this._depthOpenImage == null) {
			this._depthOpenImage = new HashMap();
		}
		this._depthOpenImage.put(new Integer(depth - 1), image);
	}

	/**
	 * Sets the blank <code>Image</code> to display in the tree when page has no
	 * children.
	 * 
	 * @param blankImage
	 */
	public void setBlankImage(Image blankImage) {
		this._blankImage = blankImage;
	}

	/**
	 * Sets the background color for a specific depth level.
	 * 
	 * @param depth
	 * @param color
	 */
	public void setDepthCurrentColor(int depth, String color) {
		if (this._depthCurrentColor == null) {
			this._depthCurrentColor = new HashMap();
		}
		this._depthCurrentColor.put(new Integer(depth - 1), color);
	}
	/**
	 * Sets the background color for a specific depth level.
	 * 
	 * @param depth
	 * @param color
	 */
	public void setDepthSelectedColor(int depth, String color) {
		if (this._depthSelectedColor == null) {
			this._depthSelectedColor = new HashMap();
		}
		this._depthSelectedColor.put(new Integer(depth - 1), color);
	}
	
	/**
	 * Sets the alignment for a specific depth level.
	 * 
	 * @param depth
	 * @param alignment
	 */
	public void setDepthAlignment(int depth, String alignment) {
		if (this._depthAlignment == null) {
			this._depthAlignment = new HashMap();
		}
		this._depthAlignment.put(new Integer(depth - 1), alignment);
	}
	
	
	public void setPaddingImage(Image image) {
		this._paddingImage = image;
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
			if (this._depthHoverColor != null) {
				String color = (String) this._depthHoverColor.get(new Integer(depth));
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
		return (depth * this._indent) + this._initialIndent + this._padding;
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
		this._spaceBetween = spaceBetween;
	}
	
	/**
	 * Sets the indent for the first level in the tree. Set to 0 by default.
	 * 
	 * @param initialIndent
	 */
	public void setInitialIndent(int initialIndent) {
		this._initialIndent = initialIndent;
	}
	
	/**
	 * Sets the padding for individual cells in the <code>NavigationTree</code>.
	 * Set to 0 by default.
	 * 
	 * @param padding
	 */
	public void setPadding(int padding) {
		this._padding = padding;
	}
	
	/**
	 * Sets the width of the <code>NavigationTree</code>. Set to 150px by
	 * default.
	 * 
	 * @param width
	 */
	@Override
	public void setWidth(String width) {
		super.setWidth(width);
	}
	

	public void setAlignment(String alignment) {
		this._textAlignment = alignment;
	}

	/**
	 * Sets the background color for all depth levels.
	 * 
	 * @param color
	 */
	public void setBackgroundColor(String color) {
		this._backgroundColor = color;
	}
	

	/**
	 * Sets the indent from parent to child. Set to 12 by default.
	 * 
	 * @param indent
	 */
	public void setIndent(int indent) {
		this._indent = indent;
	}
	

	/**
	 * Sets the color for the border around the <code>NavigationTree</code>
	 * 
	 * @param borderColor
	 * @see com.idega.block.navigation.presentation.NavigationTree#setShowBorder(boolean)
	 */
	public void setBorderColor(String borderColor) {
		this._borderColor = borderColor;
	}

	/**
	 * Sets whether to set a border around the entire <code>NavigationTree</code>
	 * 
	 * @param showBorder
	 */
	public void setShowBorder(boolean showBorder) {
		this._showBorder = showBorder;
	}
	

	/**
	 * Returns the padding used for the tree.
	 * 
	 * @return
	 */
	protected int getPadding() {
		return this._padding;
	}

	/**
	 * Returns the background color used for the tree. Returns NULL if no color is
	 * set.
	 * 
	 * @return
	 */
	protected String getBackgroundColor() {
		return this._backgroundColor;
	}
	
	/**
	 * Gets if to use the style-class based layout (from the superclass) rather than the default table based one
	 * @return
	 */
	protected boolean isUseStyleBasedLayout() {
		return this.useStyleBasedLayout;
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