package com.idega.block.navigation.presentation;

import java.util.Iterator;

import com.idega.builder.business.PageTreeNode;
import com.idega.core.builder.business.BuilderService;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;

/**
 * Title: Description: Copyright: Copyright (c) 2000-2001 idega.is All Rights
 * Reserved Company: idega
 * 
 * @author <a href="mailto:aron@idega.is">Aron Birkir </a>
 * @version 1.0
 */

public class NavigationDropdownMenu extends Block {

	private final static String IW_BUNDLE_IDENTIFIER = "com.idega.block.navigation";

	private IWBundle iwb;
	private IWResourceBundle iwrb;

	private String prmDropdown = "nav__drp__mnu_";

	private int rootNode = -1;
	private int currentPageId = -1;
	private int parentPageId = -1;
	private int spaceBetween = 0;

	private boolean useSubmitButton = false;
	private boolean setButtonAsLink = false;
	private boolean useGeneratedButton = false;

	private String iLinkStyleClass;
	private String iInputStyleClass;
	private String iButtonStyleClass;
	
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	public String getDropdownParameter() {
		return prmDropdown + getICObjectInstanceID();
	}

	public void main(IWContext iwc) throws Exception {
		BuilderService bs = getBuilderService(iwc);
		iwrb = getResourceBundle(iwc);
		if (rootNode == -1) {
			rootNode = bs.getRootPageId();
		}

		String name = getDropdownParameter();
		DropdownMenu dropDown = new DropdownMenu(name);
		if (iInputStyleClass != null) {
			dropDown.setStyleClass(iInputStyleClass);
		}

		PageTreeNode node = new PageTreeNode(rootNode, iwc);
		Iterator iter = node.getChildrenIterator();
		while (iter.hasNext()) {
			PageTreeNode n = (PageTreeNode) iter.next();
			int id = n.getNodeID();
			String url = bs.getPageURI(id);
			dropDown.addMenuElement(url, n.getLocalizedNodeName(iwc));
		}

		Form f = new Form();
		f.setAction("");
		String formName = name + "form";
		f.setName(formName);
		add(f);
		
		Table table = new Table();
		table.setCellpadding(0);
		table.setCellspacing(0);
		int column = 1;
		f.add(table);
		
		f.getParentPage().getAssociatedScript().addFunction("navHandler", getScriptSource(name, formName));
		table.add(dropDown, column, 1);
		
		if (spaceBetween > 0) {
			table.setWidth(column++, spaceBetween);
		}
		
		if (useSubmitButton) {
			if (useGeneratedButton) {
				Link btn = new Link(iwrb.getLocalizedImageButton("go", "Go!"));
				btn.setURL("javascript:" + getScriptCaller(formName));
				btn.setOnClick("javascript:" + getScriptCaller(formName));
				table.add(btn, column, 1);
			}
			else if (setButtonAsLink) {
				Link btn = new Link(iwrb.getLocalizedString("go", "Go!"));
				btn.setURL("javascript:" + getScriptCaller(formName));
				btn.setOnClick("javascript:" + getScriptCaller(formName));
				if (iLinkStyleClass != null) {
					btn.setStyleClass(iLinkStyleClass);
				}
				table.add(btn, column, 1);
			}
			else {
				GenericButton btn = new GenericButton("go", iwrb.getLocalizedString("go", "Go!"));
				btn.setOnClick("javascript:" + getScriptCaller(formName));
				if (iButtonStyleClass != null) {
					btn.setStyleClass(iButtonStyleClass);
				}
				table.add(btn, column, 1);
			}
		}
		else {
			dropDown.addMenuElementFirst("", "");
			dropDown.setOnChange(getScriptCaller(formName));
		}

	}

	public String getScriptCaller(String formName) {
		return "navHandler(this)";
	}

	public String getScriptSource(String dropDownName, String formName) {
		StringBuffer s = new StringBuffer();
		s.append("\n function navHandler(myForm){");
		s.append("\n\t var URL = findObj('").append(dropDownName);
		s.append("').options[findObj('").append(dropDownName);
		s.append("').selectedIndex].value;");
		s.append("\n\t if (URL.length > 0) window.location.href = URL;");
		s.append("\n }");
		return s.toString();
	}

	public void setUseSubmitButton(boolean use) {
		this.useSubmitButton = use;
	}

	public void setRootNode(int rootId) {
		rootNode = rootId;
	}
	/**
	 * @param buttonStyleClass The buttonStyleClass to set.
	 */
	public void setButtonStyleClass(String buttonStyleClass) {
		iButtonStyleClass = buttonStyleClass;
	}
	/**
	 * @param inputStyleClass The inputStyleClass to set.
	 */
	public void setInputStyleClass(String inputStyleClass) {
		iInputStyleClass = inputStyleClass;
	}
	/**
	 * @param linkStyleClass The linkStyleClass to set.
	 */
	public void setLinkStyleClass(String linkStyleClass) {
		iLinkStyleClass = linkStyleClass;
	}
	/**
	 * @param spaceBetween The spaceBetween to set.
	 */
	public void setSpaceBetween(int spaceBetween) {
		this.spaceBetween = spaceBetween;
	}
	/**
	 * @param setButtonAsLink The setButtonAsLink to set.
	 */
	public void setSetButtonAsLink(boolean setButtonAsLink) {
		this.setButtonAsLink = setButtonAsLink;
	}
	/**
	 * @param useGeneratedButton The useGeneratedButton to set.
	 */
	public void setUseGeneratedButton(boolean useGeneratedButton) {
		this.useGeneratedButton = useGeneratedButton;
	}
}