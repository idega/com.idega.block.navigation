package com.idega.block.navigation.presentation;

import java.util.Iterator;

import com.idega.builder.business.PageTreeNode;
import com.idega.core.builder.business.BuilderService;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.Link;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000-2001 idega.is All Rights Reserved
 * Company:      idega
  *@author <a href="mailto:aron@idega.is">Aron Birkir</a>
 * @version 1.0
 */

public class NavigationDropdownMenu extends Block{
	
	private IWBundle iwb;
 	private IWResourceBundle iwrb;
 	private final static String IW_BUNDLE_IDENTIFIER="com.idega.block.navigation";
 	private String prmDropdown = "nav__drp__mnu_";
 	
 	private int rootNode = -1;
 	private int currentPageId = -1;
 	private int parentPageId = -1;
 	private boolean useSubmitButton;
 	
 	public String getBundleIdentifier(){
    	return IW_BUNDLE_IDENTIFIER;
 	 }
 	 
 	public String getDropdownParameter(){
 		return prmDropdown+ getICObjectInstanceID();
 	}
 	 
 	public void main(IWContext iwc)throws Exception{
    	//setStyles();
    	BuilderService bs = getBuilderService(iwc);
		iwrb = getResourceBundle(iwc);
	    if ( rootNode == -1 ) {
	      rootNode = bs.getRootPageId();
	    }

	    //String sCurrentPageId = iwc.getParameter(com.idega.builder.business.BuilderLogic.IB_PAGE_PARAMETER);
	    //currentPageId = sCurrentPageId !=null ? Integer.parseInt(sCurrentPageId):rootNode;
	    String name = getDropdownParameter();
	    DropdownMenu dropDown = new DropdownMenu(name);
	    
	    PageTreeNode node = new PageTreeNode(rootNode, iwc);
	    Iterator iter = node.getChildrenIterator();
	    while (iter.hasNext()){
	    	PageTreeNode n = (PageTreeNode) iter.next();
	    	int id = n.getNodeID();
	    	String url = bs.getPageURI(id);
	    	dropDown.addMenuElement(url,n.getLocalizedNodeName(iwc));
      		
	    }
	    Form f = new Form();
	    f.setAction("");
	    String formName = name+"form";
	    f.setName(formName);
	    add(f);
	    f.getParentPage().getAssociatedScript().addFunction("navHandler",getScriptSource(name,formName));
	    f.add(dropDown);
	    if(useSubmitButton){
	    	Link btn= new Link(iwrb.getLocalizedImageButton("go","Go!"));
	    	btn.setURL("javascript:"+getScriptCaller(formName));
	    	btn.setOnClick("javascript:"+getScriptCaller(formName));
	    	f.add(btn);
	    }
	    else{
	    	dropDown.setOnChange(getScriptCaller(formName));
	    }
	    
 	}
 	
 	public String getScriptCaller(String formName){
 		return "navHandler(this)";
 	}
 	
 	public String getScriptSource(String dropDownName,String formName){
 		StringBuffer s = new StringBuffer();
 		s.append("\n function navHandler(myForm){");
 		s.append("\n\t var URL = findObj('").append(dropDownName);
 		s.append( "').options[findObj('").append(dropDownName);
 		s.append( "').selectedIndex].value;");
 		s.append("\n\t window.location.href = URL;");
 		s.append("\n }");
 		return s.toString();
 	}
 	
 	public void setUseSubmitButton(boolean use){
 		this.useSubmitButton = use;
 	}
 	
 	public void setRootNode(int rootId){
   		 rootNode  = rootId;
 	}
 	
 	
}
