package com.idega.block.navigation.presentation;

import com.idega.idegaweb.*;
import com.idega.presentation.*;
import com.idega.presentation.ui.*;
import com.idega.presentation.text.*;
import com.idega.builder.business.*;

import java.util.*;

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
 	 
 	public void main(IWContext iwc){
    	//setStyles();
		iwrb = getResourceBundle(iwc);
	    if ( rootNode == -1 ) {
	      rootNode = BuilderLogic.getStartPageId(iwc);
	    }

	    String sCurrentPageId = iwc.getParameter(com.idega.builder.business.BuilderLogic.IB_PAGE_PARAMETER);
	    currentPageId = sCurrentPageId !=null ? Integer.parseInt(sCurrentPageId):rootNode;
	    String name = getDropdownParameter();
	    DropdownMenu dropDown = new DropdownMenu(name);
	    
	    PageTreeNode node = new PageTreeNode(rootNode, iwc);
	    Iterator iter = node.getChildren();
	    while (iter.hasNext()){
	    	PageTreeNode n = (PageTreeNode) iter.next();
	    	int id = n.getNodeID();
	    	String url = BuilderLogic.getInstance().getIBPageURL(iwc,id);
	    	dropDown.addMenuElement(url,n.getNodeName());
      		
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
 		s.append("\n\t var URL = document.").append(formName).append(".").append(dropDownName);
 		s.append( ".options[document.").append(formName).append(".").append(dropDownName);
 		s.append( ".selectedIndex].value;");
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
