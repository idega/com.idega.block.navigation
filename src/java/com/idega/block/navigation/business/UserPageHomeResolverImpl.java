package com.idega.block.navigation.business;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.core.accesscontrol.business.NotLoggedOnException;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.builder.data.ICPage;
import com.idega.presentation.IWContext;
import com.idega.user.business.UserCompanyBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.User;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

@Scope("singleton")
@Service(UserHomePageResolver.SPRING_BEAN_IDENTIFIER)
public class UserPageHomeResolverImpl implements UserHomePageResolver {

	private static final Logger LOGGER = Logger.getLogger(UserPageHomeResolverImpl.class.getName());
	
	@Autowired(required = false) private UserCompanyBusiness userCompanyBusiness;
	
	@SuppressWarnings("unchecked")
	public Map<String, ICPage> getUserHomePages(IWContext iwc) {
		User user = getCurrentUser(iwc);
		if (user == null) {
			return null;
		}
		
		Set<String> userRoles = iwc.getAccessController().getAllRolesForCurrentUser(iwc);
		if (ListUtil.isEmpty(userRoles)) {
			return null;
		}
		
//		Collection<Group> groups = user.getParentGroups();
//		if (ListUtil.isEmpty(groups)) {
//			return null;
//		}
//		
		Map<String, ICPage> homePages = new HashMap<String, ICPage>();
//		
//		int currentPageId = getCurrentPageId(iwc);
//		for (Group group: groups) {
//			if (canAddPageForGroup(group, currentPageId)) {
//				ICPage page = group.getHomePage();
//				
//				Collection<ICPermission> permissions = iwc.getAccessController().getAllRolesForGroup(group);
//				if (!ListUtil.isEmpty(permissions)) {
//					for (ICPermission permission: permissions) {
//						LOGGER.log(Level.INFO, "Adding home page '"+page.getName()+"' for role: '" + permission.getPermissionString() + "', group: " + group.getName());
//						homePages.put(permission.getPermissionString(), page);
//					}
//				}
//			}
//		}
		
		Collection<Group> roleGroups = null;
		int currentPageId = getCurrentPageId(iwc);
		for (String roleKey : userRoles) {
			roleGroups = iwc.getAccessController().getAllGroupsForRoleKey(roleKey, iwc);
			if (ListUtil.isEmpty(roleGroups)) {
				LOGGER.log(Level.INFO, "Role: '" + roleKey + "' doesn't have any groups!");
			}
			else {
				for (Group group: roleGroups) {
					if (canAddPageForGroup(group, currentPageId)) {
						ICPage page = group.getHomePage();
						
						if (homePages.values().contains(page)) {
							LOGGER.log(Level.INFO, "Home page '"+page.getName()+"' for role: '" + roleKey + "', group: '" + group.getName() + "' already added!");
						}
						else {
							LOGGER.log(Level.INFO, "Adding home page '"+page.getName()+"' for role: '" + roleKey + "', group: " + group.getName());
							homePages.put(roleKey, page);
						}
					}
				}
			}
		}
		
		if (ListUtil.isEmpty(homePages.values())) {
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
		BuilderService builderService = null;
		try {
			builderService = BuilderServiceFactory.getBuilderService(iwc);
		} catch (RemoteException e) {
			LOGGER.log(Level.SEVERE, "Error getting: " + BuilderService.class.getName(), e);
		}
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

	public UserCompanyBusiness getUserCompanyBusiness() {
		return userCompanyBusiness;
	}

	public void setUserCompanyBusiness(UserCompanyBusiness userCompanyBusiness) {
		this.userCompanyBusiness = userCompanyBusiness;
	}

}
