package com.idega.block.navigation.presentation;

import com.idega.business.IBOLookup;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.Text;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;

/**
 * @author Laddi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WelcomeMessage extends Text {

	protected static final String IW_BUNDLE_IDENTIFIER="com.idega.block.navigation";
	private static final String WELCOME_KEY = "welcome_message.text";
	private static final String WELCOME_KEY_VALUE = "My page";

	public WelcomeMessage() {
		super("");
	}
	
	public void main(IWContext iwc) {
		User newUser = iwc.getCurrentUser();
		
		if(newUser!=null){
			try {
				super.setText(getResourceBundle(iwc).getLocalizedString(WELCOME_KEY,WELCOME_KEY_VALUE)+" "+newUser.getName());	
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected UserBusiness getUserBusiness(IWContext iwc)throws java.rmi.RemoteException{
		return (UserBusiness)IBOLookup.getServiceInstance(iwc,UserBusiness.class);
	}	
	
	public String getBundleIdentifier(){
		return IW_BUNDLE_IDENTIFIER;
	}
}
