package com.idega.block.navigation.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.idega.block.navigation.utils.NavigationConstants;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.accesscontrol.data.ICRole;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.SelectOption;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;

/**
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.2 $
 *
 * Changes preferred role for user AND navigates to user's home page
 *
 * Last modified: $Date: 2008/07/29 11:28:54 $ by $Author: valdas $
 */
public class UserRoleChanger extends Block {
	
	private static final Logger logger = Logger.getLogger(UserRoleChanger.class.getName());
	
	private IWResourceBundle iwrb = null;

	@Override
	public void main(IWContext iwc) {
		if (!iwc.isLoggedOn()) {
			logger.log(Level.WARNING, "User must be logged to be able change preferred role");
			return;
		}
		
		List<String> files = new ArrayList<String>();
		files.add(CoreConstants.DWR_ENGINE_SCRIPT);
		files.add("/dwr/interface/UserBusiness.js");
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, files);
		
		iwrb = getResourceBundle(iwc);
		
		Layer container = new Layer();
		add(container);
		
		Layer headingContainer = new Layer();
		container.add(headingContainer);
		headingContainer.add(new Heading1(iwrb.getLocalizedString("change_user_role", "Change role")));
		
		Layer roleChooserContainer = new Layer();
		container.add(roleChooserContainer);
		roleChooserContainer.add(getRolesChooser(iwc));
	}
	
	private DropdownMenu getRolesChooser(IWContext iwc) {	
		DropdownMenu rolesChooser = new DropdownMenu();
		rolesChooser.setOnChange(new StringBuilder("changePreferredRoleForUser('").append(rolesChooser.getId()).append("');").toString());
		rolesChooser.addFirstOption(new SelectOption(iwrb.getLocalizedString("select_user_role", "Select role"), -1));
	
		StringBuilder functionScript = new StringBuilder("function changePreferredRoleForUser(id) {var roleChooser = document.getElementById(id); ");
		functionScript.append("if (roleChooser == null) {return false;} if (roleChooser.selectedIndex == -1) {return false;} ");
		functionScript.append("var selectedRoleValue = roleChooser.options[roleChooser.selectedIndex].value; if (selectedRoleValue == -1) {return false;} ");
		functionScript.append("showLoadingMessage('").append(iwrb.getLocalizedString("saving", "Saving...")).append("'); ");
		functionScript.append("UserBusiness.setPreferredRoleAndGetHomePageUri(selectedRoleValue, {callback: function(uri) { ");
		functionScript.append("closeAllLoadingMessages(); if (uri != null) { window.location.href = uri; }}});}");
		PresentationUtil.addJavaScriptActionToBody(iwc, functionScript.toString());
		
		User currentUser = iwc.getCurrentUser();
		
		UserBusiness userBusiness = null;
		try {
			userBusiness = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
		} catch (IBOLookupException e) {
			logger.log(Level.SEVERE, "Error getting UserBusiness!", e);
		}
		
		IWResourceBundle coreIWRB = iwc.getIWMainApplication().getBundle(CoreConstants.CORE_IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		List<ICRole> rolesForUser = userBusiness.getAvailableRolesForUserAsPreferredRoles(currentUser);
		if (ListUtil.isEmpty(rolesForUser)) {
			rolesChooser.addFirstOption(new SelectOption(iwrb.getLocalizedString("no_role_to_choose", "There are no roles available"), -1));
			rolesChooser.setDisabled(true);
		}
		else {
			for (ICRole role: rolesForUser) {
				rolesChooser.addOption(new SelectOption(coreIWRB.getLocalizedString(role.getRoleNameLocalizableKey(), role.getRoleKey()), role.getRoleKey()));
			}
	
			ICRole preferredRole = currentUser.getPreferredRole();
			if (preferredRole != null) {
				rolesChooser.setSelectedElement(preferredRole.getRoleKey());
			}
			
			rolesChooser.setToolTip(iwrb.getLocalizedString("select_user_preferred_role", "Select preferred role"));
		}
		return rolesChooser;
	}
	
	@Override
	public String getBundleIdentifier() {
		return NavigationConstants.IW_BUNDLE_IDENTIFIER;
	}
	
	@Override
	public String getBuilderName(IWUserContext iwuc) {
		return this.getClass().getSimpleName();
	}
	
}
