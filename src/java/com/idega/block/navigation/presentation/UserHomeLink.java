package com.idega.block.navigation.presentation;

import com.idega.user.data.User;
import com.idega.presentation.Table;
import com.idega.presentation.Image;
import com.idega.presentation.IWContext;
import com.idega.presentation.Block;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;


/**
 * Title: UserHomeLink
 * Description: A component that displays a user "homepage" if he has one assigned.
 * Copyright:    Copyright (c) 2000-2001 idega.is All Rights Reserved
 * Company:      idega
  *@author <a href="mailto:tryggvi@idega.is">Tryggvi Larusson</a>
 * @version 1.0
 */

public class UserHomeLink extends Link {

	protected static final String IW_BUNDLE_IDENTIFIER="com.idega.block.navigation";
	private static final String EMPTY_STRING = "";
	private static final String HOME_PAGE_KEY = "user_home_link.text";
	private static final String HOME_PAGE_KEY_VALUE = "My page";

	public UserHomeLink(){
		super(EMPTY_STRING);	
	}

	public void main(IWContext iwc){
		User newUser = iwc.getCurrentUser();
		if(newUser!=null){
			try{
				int homePageID = newUser.getHomePageID();
				if(homePageID!=-1){
					super.setPage(homePageID);
					super.setText(getResourceBundle(iwc).getLocalizedString(HOME_PAGE_KEY,HOME_PAGE_KEY_VALUE));
				}
			}
			catch(Exception e){
				e.printStackTrace();	
			}
		}
	}
	
	public String getBundleIdentifier(){
		return IW_BUNDLE_IDENTIFIER;
	}
}