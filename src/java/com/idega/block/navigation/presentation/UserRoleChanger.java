package com.idega.block.navigation.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;

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
import com.idega.user.business.UserCompanyBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

/**
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.4 $
 * 
 *          Changes preferred role for user AND navigates to user's home page
 * 
 *          Last modified: $Date: 2008/11/04 13:12:14 $ by $Author: valdas $
 */
public class UserRoleChanger extends Block {

	private static final Logger logger = Logger.getLogger(UserRoleChanger.class.getName());

	private IWResourceBundle iwrb = null;

	@Autowired(required = false) private UserCompanyBusiness userCompanyBusiness;

	public UserRoleChanger() {
		try {
			ELUtil.getInstance().autowire(this);
		} catch (BeanCreationException e) {
			logger.log(Level.WARNING, "Failed to autowire propertie userCompanyBusiness");
		}
	}

	@Override
	public void main(IWContext iwc) {
		if (!iwc.isLoggedOn()) {
			logger.log(Level.WARNING, "User must be logged to be able change preferred role");
			return;
		}

		List<String> files = new ArrayList<String>();
		files.add(CoreConstants.DWR_ENGINE_SCRIPT);
		files.add("/dwr/interface/UserBusiness.js");
		if (userCompanyBusiness != null) {
			files.add("/dwr/interface/UserCompanyBusiness.js");
		}
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

		if (userCompanyBusiness != null) {
			Layer companyChooserContainer = new Layer();
			container.add(companyChooserContainer);
			companyChooserContainer.add(getCompanyChooser(iwc));
		}
	}

	private DropdownMenu getRolesChooser(IWContext iwc) {
		DropdownMenu rolesChooser = new DropdownMenu();
		rolesChooser.setOnChange(new StringBuilder("changePreferredRoleForUser('").append(rolesChooser.getId()).append("');").toString());
		rolesChooser.addFirstOption(new SelectOption(iwrb.getLocalizedString("select_user_role", "Select role"), -1));

		StringBuilder functionScript = new StringBuilder("function changePreferredRoleForUser(id) {var roleChooser = document.getElementById(id); ");
		functionScript.append("if (roleChooser == null) {return false;} if (roleChooser.selectedIndex == -1) {return false;} ");
		functionScript.append("var selectedRoleValue = roleChooser.options[roleChooser.selectedIndex].value; if (selectedRoleValue == -1) {return false;} ");
		functionScript.append("showLoadingMessage('").append(iwrb.getLocalizedString("saving", "Saving...")).append("'); ");
		functionScript.append("UserBusiness.setPreferedCompanyForCurrentUser(selectedRoleValue, {callback: function(uri) { ");
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
		} else {
			for (ICRole role : rolesForUser) {
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

	protected DropdownMenu getCompanyChooser(IWContext iwc) {
		DropdownMenu companyChooser = new DropdownMenu();
		companyChooser.setOnChange(new StringBuilder("changePreferredCompanyForUser('").append(companyChooser.getId()).append("');").toString());
		companyChooser.addFirstOption(new SelectOption(iwrb.getLocalizedString("select_user_company", "Select company"), -1));

		StringBuilder functionScript = new StringBuilder("function changePreferredCompanyForUser(id) {var companyChooser = document.getElementById(id); ");
		functionScript.append("if (companyChooser == null) {return false;} if (companyChooser.selectedIndex == -1) {return false;} ");
		functionScript.append("var selectedCompanyValue = companyChooser.options[companyChooser.selectedIndex].value; if (selectedCompanyValue == -1) {return false;} ");
		functionScript.append("showLoadingMessage('").append(iwrb.getLocalizedString("saving", "Saving...")).append("'); ");
		functionScript.append("UserCompanyBusiness.setPreferedCompanyForCurrentUser(selectedCompanyValue, {callback: function() { ");
		functionScript.append("closeAllLoadingMessages();}});}");
		PresentationUtil.addJavaScriptActionToBody(iwc, functionScript.toString());

		User currentUser = iwc.getCurrentUser();

		Collection<Group> userCompanies = null;
		if (userCompanyBusiness != null) {
			try {
				userCompanies = userCompanyBusiness.getUsersCompanies(iwc, currentUser);
			} catch (RemoteException e1) {
				logger.log(Level.SEVERE, "Error getting user companies!", e1);
			}
		}
		
		if (ListUtil.isEmpty(userCompanies)) {
			// if user has no company nothing to show
			return null;
		} else if (userCompanies.size() == 1) {
			// don't show if user has only one company
			return null;
		} else {
			for (Group group : userCompanies) {
				companyChooser.addOption(new SelectOption(group.getName(), group.getId()));
			}
		}

		if (userCompanyBusiness != null) {
			try {
				Group preferedGroup = userCompanyBusiness.getPreferedCompanyForUser(iwc, currentUser);
				if (preferedGroup != null) {
					companyChooser.setSelectedElement(preferedGroup.getId());
				}
			} catch (RemoteException e) {
				logger.log(Level.SEVERE, "Error getting user prefered companies!", e);
			}
		}
		
		return companyChooser;
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
