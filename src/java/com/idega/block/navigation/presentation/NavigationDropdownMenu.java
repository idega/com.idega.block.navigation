package com.idega.block.navigation.presentation;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.navigation.utils.NavigationConstants;
import com.idega.block.web2.business.JQuery;
import com.idega.builder.business.PageTreeNode;
import com.idega.core.builder.business.BuilderService;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

/**
 * Title: Description: Copyright: Copyright (c) 2000-2001 idega.is All Rights
 * Reserved Company: idega
 * 
 * @author <a href="mailto:aron@idega.is">Aron Birkir </a>
 * @version 1.0
 */

public class NavigationDropdownMenu extends Block {

	@Autowired
	private JQuery jQuery;

	private IWResourceBundle iwrb;

	private String prmDropdown = "nav__drp__mnu_";

	private int rootNode = -1;

	@SuppressWarnings("unused")
	@Deprecated
	private int spaceBetween = 0;

	private boolean useSubmitButton = false;

	@SuppressWarnings("unused")
	@Deprecated
	private boolean setButtonAsLink = false;
	@SuppressWarnings("unused")
	@Deprecated
	private boolean useGeneratedButton = false;
	@SuppressWarnings("unused")
	@Deprecated
	private boolean useImageLink = false;
	@SuppressWarnings("unused")
	@Deprecated
	private Image buttonImage;

	@SuppressWarnings("unused")
	@Deprecated
	private String iLinkStyleClass;
	@SuppressWarnings("unused")
	@Deprecated
	private String iInputStyleClass;
	@SuppressWarnings("unused")
	@Deprecated
	private String iButtonStyleClass;
	@SuppressWarnings("unused")
	@Deprecated
	private String iDropDownMenuWidth;

	private String iFirstMenuElementText;
	
	public String getBundleIdentifier() {
		return NavigationConstants.IW_BUNDLE_IDENTIFIER;
	}

	public String getDropdownParameter() {
		return this.prmDropdown + getICObjectInstanceID();
	}

	public void main(IWContext iwc) throws Exception {
		IWBundle iwb = getBundle(iwc);
		
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getJQuery().getBundleURIToJQueryLib());
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, iwb.getVirtualPathWithFileNameString("javascript/navigationDropdown.js"));

		BuilderService bs = getBuilderService(iwc);
		this.iwrb = getResourceBundle(iwc);
		if (this.rootNode == -1) {
			this.rootNode = bs.getRootPageId();
		}

		String name = getDropdownParameter();
		DropdownMenu dropDown = new DropdownMenu(name);

		PageTreeNode node = new PageTreeNode(this.rootNode, iwc);
		Iterator iter = node.getChildrenIterator();
		while (iter.hasNext()) {
			PageTreeNode n = (PageTreeNode) iter.next();
			int id = n.getNodeID();
			String url = bs.getPageURI(id);
			dropDown.addMenuElement(url, n.getLocalizedNodeName(iwc));
		}

		Layer layer = new Layer();
		layer.setStyleClass("navigationDropdownMenu");
		add(layer);
		
		layer.add(dropDown);

		if (this.useSubmitButton) {
			Link link = new Link(new Span(new Text(this.iwrb.getLocalizedString("go", "Go!"))));
			layer.add(link);
		}
		else {
			if (this.iFirstMenuElementText != null) {
				dropDown.addMenuElementFirst("", this.iFirstMenuElementText);
			}
			else {
				dropDown.addMenuElementFirst("", "");
			}

			dropDown.setStyleClass("standalone");
		}
	}

	@Deprecated
	public String getScriptCaller(String dropDownName) {
		return "navHandler(findObj('" + dropDownName + "'))";
	}

	@Deprecated
	public String getScriptSource() {
		StringBuffer s = new StringBuffer();
		s.append("\n function navHandler(input){");
		s.append("\n\t var URL = input.options[input.selectedIndex].value;");
		s.append("\n\t window.location.href = URL;");
		s.append("\n }");
		return s.toString();
	}

	public void setUseSubmitButton(boolean use) {
		this.useSubmitButton = use;
	}

	public void setRootNode(int rootId) {
		this.rootNode = rootId;
	}

	@Deprecated
	public void setButtonStyleClass(String buttonStyleClass) {
		this.iButtonStyleClass = buttonStyleClass;
	}

	@Deprecated
	public void setInputStyleClass(String inputStyleClass) {
		this.iInputStyleClass = inputStyleClass;
	}

	@Deprecated
	public void setLinkStyleClass(String linkStyleClass) {
		this.iLinkStyleClass = linkStyleClass;
	}

	@Deprecated
	public void setDropDownMenuWidth(String dropDownMenuWidth) {
		this.iDropDownMenuWidth = dropDownMenuWidth;
	}

	/**
	 * @param firstMenuElementText The firstMenuElementText to set.
	 */
	public void setFirstMenuElementText(String firstMenuElementText) {
		this.iFirstMenuElementText = firstMenuElementText;
	}

	@Deprecated
	public void setSpaceBetween(int spaceBetween) {
		this.spaceBetween = spaceBetween;
	}

	@Deprecated
	public void setSetButtonAsLink(boolean setButtonAsLink) {
		this.setButtonAsLink = setButtonAsLink;
	}

	@Deprecated
	public void setUseGeneratedButton(boolean useGeneratedButton) {
		this.useGeneratedButton = useGeneratedButton;
	}
	
	@Deprecated
	public void setButtonImage(Image buttonImage) {
		this.buttonImage = buttonImage;
		this.useImageLink = true;
	}
	
	private JQuery getJQuery() {
		if (this.jQuery == null) {
			ELUtil.getInstance().autowire(this);	
		}
		return jQuery;
	}
}