package com.idega.block.navigation.business;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.navigation.bean.UserHomePageBean;
import com.idega.block.navigation.utils.NavigationConstants;
import com.idega.core.accesscontrol.business.NotLoggedOnException;
import com.idega.core.accesscontrol.business.StandardRoleHomePageResolver;
import com.idega.core.accesscontrol.business.StandardRoles;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.builder.data.ICPage;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.user.business.UserCompanyBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

@Scope("singleton")
@Service(UserHomePageResolver.SPRING_BEAN_IDENTIFIER)
public class UserPageHomeResolverImpl implements UserHomePageResolver {

	private static final Logger LOGGER = Logger.getLogger(UserPageHomeResolverImpl.class.getName());
	
	@Autowired(required = false) private UserCompanyBusiness userCompanyBusiness;
	
	@SuppressWarnings("unchecked")
	public List<UserHomePageBean> getUserHomePages(IWContext iwc) {
		User user = getCurrentUser(iwc);
		if (user == null) {
			return null;
		}
		
		Set<String> userRoles = iwc.getAccessController().getAllRolesForCurrentUser(iwc);
		if (ListUtil.isEmpty(userRoles)) {
			return null;
		}
		BuilderService builderService = getBuilderService();
		if (builderService == null) {
			return null;
		}
		
		List<UserHomePageBean> homePages = new ArrayList<UserHomePageBean>();
		
		String uri = null;
		Collection<Group> roleGroups = null;
		int currentPageId = getCurrentPageId(iwc);
		List<String> addedHomePages = new ArrayList<String>();
		IWResourceBundle coreResourceBundle = CoreUtil.getCoreBundle().getResourceBundle(iwc);
		
		String bundleIdentifier = iwc.getApplicationSettings().getProperty(NavigationConstants.USER_ROLE_HOME_PAGE_RESOURCE_BUNDLE_PROPERTY,
																														NavigationConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(bundleIdentifier).getResourceBundle(iwc);
		
		for (String roleKey : userRoles) {
			if (StandardRoles.ALL_STANDARD_ROLES.contains(roleKey)) {
				StandardRoleHomePageResolver enumerator = StandardRoles.getRoleEnumerator(roleKey);
				
				if (enumerator != null) {
					homePages.add(new UserHomePageBean(roleKey, enumerator.getLocalizedName(coreResourceBundle), enumerator.getUri()));
				}
			}
			else {
				roleGroups = iwc.getAccessController().getAllGroupsForRoleKey(roleKey, iwc);
				if (!ListUtil.isEmpty(roleGroups)) {
					String localizedRoleName = null;
					for (Group group: roleGroups) {
						if (canAddPageForGroup(group, currentPageId)) {
							uri = null;
							ICPage page = group.getHomePage();
							
							try {
								uri = builderService.getPageURI(page);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
							localizedRoleName = iwrb.getLocalizedString(new StringBuilder("role_name.").append(roleKey).toString(), roleKey);			
							
							if (!StringUtil.isEmpty(uri) && !addedHomePages.contains(uri)) {
								homePages.add(new UserHomePageBean(roleKey, localizedRoleName, uri));
								addedHomePages.add(uri);
							}
						}
					}
				}
			}
		}
		
		if (ListUtil.isEmpty(homePages)) {
			return null;
		}
		
		return homePages;
	}
	
	public Map<String, ICPage> getUserCompaniesPages(IWContext iwc) {
		User user = getCurrentUser(iwc);
		if (user == null) {
			return null;
		}
		if (getUserCompanyBusiness() == null) {
			return null;
		}
		
		Collection<Group> companies = null;
		try {
			companies = getUserCompanyBusiness().getUsersCompanies(iwc, user);
		} catch (RemoteException e) {
			LOGGER.log(Level.SEVERE, "Error getting companies for user: " + user);
		}
		if (ListUtil.isEmpty(companies) || companies.size() == 1) {
			return null;
		}
		
		Map<String, ICPage> homePages = new HashMap<String, ICPage>();
		
		String currentCompanyId = null;
		Object o = iwc.getSessionAttribute(getUserCompanyBusiness().getCurrentCompanyAttributeId());
		if (o instanceof String) {
			currentCompanyId = o.toString();
		}
		
		ICPage page = null;
		for (Group company: companies) {
			page = getUserCompanyBusiness().getHomePageForCompany(company);
			
			if (canAddPageForCompany(company, page, currentCompanyId)) {
				homePages.put(company.getId(), page);
			}
		}
		
		if (ListUtil.isEmpty(homePages.values())) {
			return null;
		}
		
		return homePages;
	}
	
	public String getCurrentCompanyAttributeId() {
		return getUserCompanyBusiness() == null ? null : getUserCompanyBusiness().getCurrentCompanyAttributeId();
	}
	
	private boolean canAddPageForCompany(Group company, ICPage page, String currentCompanyId) {
		if (company == null || page == null) {
			return false;
		}
		
		return page != null && (StringUtil.isEmpty(currentCompanyId) ? true : !currentCompanyId.equals(company.getId()));
	}
	
	private boolean canAddPageForGroup(Group group, int currentPageId) {
		if (group == null) {
			return false;
		}
		
		int groupHomePageId = group.getHomePageID();
		return groupHomePageId > 0 && currentPageId != groupHomePageId;
	}
	
	private User getCurrentUser(IWContext iwc) {
		try {
			return iwc.getCurrentUser();
		} catch(NotLoggedOnException e) {
			LOGGER.log(Level.INFO, "User is not logged!");
		}
		return null;
	}
	
	private int getCurrentPageId(IWContext iwc) {
		BuilderService builderService = getBuilderService();
		if (builderService == null) {
			return -1;
		}
		
		ICPage currentPage = null;
		try {
			currentPage = builderService.getICPage(builderService.getPageKeyByURI(iwc.getRequestURI()));
		} catch(Exception e) {
			LOGGER.log(Level.SEVERE, "Error getting current page by URI: " + iwc.getRequestURI(), e);
		}
		if (currentPage == null) {
			try {
				currentPage = builderService.getCurrentPage(iwc);
			} catch (RemoteException e) {
				LOGGER.log(Level.SEVERE, "Error getting current page", e);
			}
		}
		if (currentPage == null) {
			return -1;
		}
		
		try {
			return Integer.valueOf(builderService.getPageKeyByURI(iwc.getRequestURI()));
		} catch(Exception e) {
			LOGGER.log(Level.SEVERE, "Error getting ID of current page", e);
		}
		
		return -1;
	}
	
	private BuilderService getBuilderService() {
		try {
			return BuilderServiceFactory.getBuilderService(IWMainApplication.getDefaultIWApplicationContext());
		} catch (RemoteException e) {
			LOGGER.log(Level.SEVERE, "Error getting: " + BuilderService.class.getName(), e);
		}
		return null;
	}
	

	public UserCompanyBusiness getUserCompanyBusiness() {
		return userCompanyBusiness;
	}

	public void setUserCompanyBusiness(UserCompanyBusiness userCompanyBusiness) {
		this.userCompanyBusiness = userCompanyBusiness;
	}

}
