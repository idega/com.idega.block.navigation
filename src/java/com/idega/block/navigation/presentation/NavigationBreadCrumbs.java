/*
 * Created on 14.5.2004
 */
package com.idega.block.navigation.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.idega.builder.business.PageTreeNode;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.data.ICPage;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;


/**
 * @author laddi
 */
public class NavigationBreadCrumbs extends Block {

	private ICPage iRoot;
	private ICPage iCurrentPage;

	private int iLevels = -1;
	private int iPadding = 0;
	
	private String iSeperatorText;
	private Image iSeperatorImage;
	
	private String iLinkStyleName;
	private String iTextStyleName;
	private String iSeperatorStyleName;
	
	private BuilderService iBuilderService;
	private PageTreeNode iCurrentNode;
	
	boolean iIgnoreCategoryPages = false;
	
	/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObject#main(com.idega.presentation.IWContext)
	 */
	public void main(IWContext iwc) throws Exception {
		iBuilderService = getBuilderService(iwc);

		if (iRoot == null) {
			iRoot = iBuilderService.getRootPage();
		}
		iCurrentPage = iBuilderService.getCurrentPage(iwc);
		
		List pages = new ArrayList();
		PageTreeNode page = new PageTreeNode(((Integer) iCurrentPage.getPrimaryKey()).intValue(), iwc);
		boolean showPage = true;
		boolean isCategoryPage = false;
		int level = 1;
		while (showPage) {
			if (page.getNodeID() == ((Integer) iRoot.getPrimaryKey()).intValue()) {
				showPage = false;
			}
			
			if (iIgnoreCategoryPages && page.isCategory()) {
				isCategoryPage = true;
			}
			else {
				isCategoryPage = false;
			}
			
			if (!isCategoryPage) {
				if (page.getNodeID() == ((Integer) iCurrentPage.getPrimaryKey()).intValue()) {
					Text pageText = new Text(page.getLocalizedNodeName(iwc));
					if (iTextStyleName != null) {
						pageText.setStyleClass(iTextStyleName);
					}
					pages.add(pageText);
				}
				else {
					Link pageLink = new Link(page.getLocalizedNodeName(iwc));
					pageLink.setPage(page.getNodeID());
					if (iLinkStyleName != null) {
						pageLink.setStyleClass(iLinkStyleName);
					}
					pages.add(pageLink);
				}
			}
			level++;
			
			if (iLevels != -1 && level > iLevels) {
				showPage = false;
			}
			
			page = (PageTreeNode) page.getParentNode();
			if (page == null) {
				showPage = false;
			}
		}
		
		Collections.reverse(pages);

		Table table = new Table();
		table.setCellpadding(0);
		table.setCellspacing(0);
		int column = 1;

		Iterator iter = pages.iterator();
		while (iter.hasNext()) {
			PresentationObject object = (PresentationObject) iter.next();
			table.add(object, column, 1);

			if (column == 1) {
				table.setCellpaddingRight(column++, 1, iPadding);
			}
			else {
				table.setCellpaddingLeft(column, 1, iPadding);
				table.setCellpaddingRight(column++, 1, iPadding);
			}
			
			if (iter.hasNext()) {
				if (iSeperatorText != null) {
					Text seperatorText = new Text(iSeperatorText);
					if (iSeperatorStyleName != null) {
						seperatorText.setStyleClass(iSeperatorStyleName);
					}
					table.add(seperatorText, column++, 1);
				}
				else if (iSeperatorImage != null) {
					table.add(iSeperatorImage, column++, 1);
				}
			}
		}
		
		add(table);
	}
	
	/**
	 * @param levels The levels to set.
	 */
	public void setLevels(int levels) {
		this.iLevels = levels;
	}
	
	/**
	 * @param linkStyleName The linkStyleName to set.
	 */
	public void setLinkStyleName(String linkStyleName) {
		this.iLinkStyleName = linkStyleName;
	}
	
	/**
	 * @param padding The padding to set.
	 */
	public void setPadding(int padding) {
		this.iPadding = padding;
	}
	
	/**
	 * @param root The root to set.
	 */
	public void setRoot(ICPage root) {
		this.iRoot = root;
	}
	
	/**
	 * @param seperatorImage The seperatorImage to set.
	 */
	public void setSeperatorImage(Image seperatorImage) {
		this.iSeperatorImage = seperatorImage;
	}
	
	/**
	 * @param seperatorStyleName The seperatorStyleName to set.
	 */
	public void setSeperatorStyleName(String seperatorStyleName) {
		this.iSeperatorStyleName = seperatorStyleName;
	}
	
	/**
	 * @param seperatorText The seperatorText to set.
	 */
	public void setSeperatorText(String seperatorText) {
		this.iSeperatorText = seperatorText;
	}
	
	/**
	 * @param textStyleName The textStyleName to set.
	 */
	public void setTextStyleName(String textStyleName) {
		this.iTextStyleName = textStyleName;
	}
	
	/**
	 * @param ignoreCategoryPages The ignoreCategoryPages to set.
	 */
	public void setIgnoreCategoryPages(boolean ignoreCategoryPages) {
		this.iIgnoreCategoryPages = ignoreCategoryPages;
	}
}