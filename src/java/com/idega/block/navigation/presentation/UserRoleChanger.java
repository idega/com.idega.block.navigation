package com.idega.block.navigation.presentation;

import java.rmi.RemoteException;
import java.util.Map;

import javax.ejb.FinderException;

import com.idega.block.navigation.business.UserHomePageResolver;
import com.idega.block.navigation.utils.NavigationConstants;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.data.ICPage;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.user.business.GroupBusiness;
import com.idega.user.data.Group;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

/**
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.6 $
 * 
 *          Changes preferred role for user AND navigates to user's home page
 * 
 *          Last modified: $Date: 2008/11/11 08:46:19 $ by $Author: valdas $
 */
public class UserRoleChanger extends Block {

	@Override
	public void main(IWContext iwc) {
		UserHomePageResolver homePageResolver = null;
		try {
			homePageResolver = ELUtil.getInstance().getBean(UserHomePageResolver.SPRING_BEAN_IDENTIFIER);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if (homePageResolver == null) {
			return;
		}
		
		String bundleIdentifier = iwc.getApplicationSettings().getProperty("user.role.forwarder.bundle", NavigationConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(bundleIdentifier).getResourceBundle(iwc);
		BuilderService builderService = null;
		try {
			builderService = getBuilderService(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (builderService == null) {
			return;
		}
		
		Layer container = new Layer();
		add(container);
		container.setStyleClass("userRoleSwitcherStyle");
		
		Lists list = new Lists();
		container.add(list);
		list.setStyleClass("userRoleSwitcherListStyle");
		
		Map<String, ICPage> homePages = homePageResolver.getUserHomePages(iwc);
		if (homePages != null) {
			ICPage page = null;
			for (String roleKey: homePages.keySet()) {
				page = homePages.get(roleKey);
				
				ListItem listItem = new ListItem();
				list.add(listItem);
				
				try {
					listItem.add(new Link(iwrb.getLocalizedString(new StringBuilder("role_name.").append(roleKey).toString(), roleKey),
																															builderService.getPageURI(page)));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		
		Map<String, ICPage> userCompanies = homePageResolver.getUserCompaniesPages(iwc);
		if (userCompanies == null) {
			return;
		}
		String attribute = homePageResolver.getCurrentCompanyAttributeId();
		if (StringUtil.isEmpty(attribute)) {
			return;
		}
		GroupBusiness groupBusiness = null;
		try {
			groupBusiness = (GroupBusiness) IBOLookup.getServiceInstance(iwc, GroupBusiness.class);
		} catch (IBOLookupException e) {
			e.printStackTrace();
		}
		if (groupBusiness == null) {
			return;
		}
		Group company = null;
		for (String groupId: userCompanies.keySet()) {
			company = null;
			try {
				company = groupBusiness.getGroupByGroupID(Integer.valueOf(groupId));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (FinderException e) {
				e.printStackTrace();
			}
			
			if (company != null) {
				Link link = null;
				try {
					link = new Link(company.getName(), builderService.getPageURI(userCompanies.get(groupId)));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				if (link != null) {
					ListItem listItem = new ListItem();
					list.add(listItem);
					
					link.addParameter(attribute, groupId);
					listItem.add(link);
				}
			}
		}
		
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
