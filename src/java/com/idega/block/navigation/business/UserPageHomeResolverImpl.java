package com.idega.block.navigation.business;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.navigation.bean.UserHomePageBean;
import com.idega.block.navigation.utils.NavigationConstants;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.accesscontrol.business.StandardRoleHomePageResolver;
import com.idega.core.accesscontrol.business.StandardRoles;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.builder.data.ICPage;
import com.idega.core.builder.data.ICPageHome;
import com.idega.core.business.DefaultSpringBean;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.user.business.UserBusiness;
import com.idega.user.business.UserCompanyBusiness;
import com.idega.user.data.Group;
import com.idega.user.data.GroupHome;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.DBUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service(UserHomePageResolver.SPRING_BEAN_IDENTIFIER)
public class UserPageHomeResolverImpl extends DefaultSpringBean implements UserHomePageResolver {

	@Autowired(required = false)
	private UserCompanyBusiness userCompanyBusiness;

	@Override
	public List<UserHomePageBean> getUserHomePages(IWContext iwc) {
		com.idega.user.data.bean.User currentUser = getCurrentUser();
		User user = getLegacyUser(currentUser);
		if (user == null) {
			getLogger().warning("User is unknown");
			return null;
		}

		Set<String> userRoles = iwc.getAccessController().getAllRolesForCurrentUser(iwc);
		if (ListUtil.isEmpty(userRoles)) {
			getLogger().warning(user + " has no roles");
			return null;
		}

		BuilderService builderService = getBuilderService();
		if (builderService == null) {
			getLogger().warning(BuilderService.class.getName() + " is not available");
			return null;
		}

		List<UserHomePageBean> homePages = new ArrayList<UserHomePageBean>();

		String uri = null;
		Collection<com.idega.user.data.bean.Group> roleGroups = null;
		int currentPageId = getCurrentPageId(iwc);
		Map<String, Boolean> addedHomePages = new HashMap<>();
		IWResourceBundle coreResourceBundle = CoreUtil.getCoreBundle().getResourceBundle(iwc);

		String bundleIdentifier = iwc.getApplicationSettings().getProperty(NavigationConstants.USER_ROLE_HOME_PAGE_RESOURCE_BUNDLE_PROPERTY,
																														NavigationConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(bundleIdentifier).getResourceBundle(iwc);

		Locale locale = getCurrentLocale();

		AccessController accessController = iwc.getAccessController();

		for (String roleKey : userRoles) {
			if (StandardRoles.ALL_STANDARD_ROLES.contains(roleKey)) {
				StandardRoleHomePageResolver enumerator = StandardRoles.getRoleEnumerator(roleKey);

				if (enumerator != null) {
					uri = enumerator.getUri();
					if (!StringUtil.isEmpty(uri) && !addedHomePages.containsKey(uri)) {
						homePages.add(new UserHomePageBean(roleKey, enumerator.getLocalizedName(coreResourceBundle), uri, roleKey));
						addedHomePages.put(uri, Boolean.TRUE);
					}
				}
			}
			else {
				roleGroups = accessController.getAllUserGroupsForRoleKey(roleKey, iwc, currentUser);
				if (!ListUtil.isEmpty(roleGroups)) {
					for (com.idega.user.data.bean.Group group: roleGroups) {
						if (canAddPageForGroup(group, currentPageId)) {
							uri = null;
							com.idega.core.builder.data.bean.ICPage page = group.getHomePage();

							addHomePage(page == null ? null : page.getID(), builderService, iwrb, addedHomePages, roleKey, locale, homePages);
						}
					}
				}
			}
		}

		if (ListUtil.isEmpty(homePages)) {
			UserBusiness userBusiness = getServiceInstance(UserBusiness.class);
			List<List<Integer>> allHomePages = userBusiness.getHomePageIds(user);
			if (!ListUtil.isEmpty(allHomePages)) {
				for (List<Integer> ids: allHomePages) {
					if (ListUtil.isEmpty(ids)) {
						continue;
					}

					for (Integer id: ids) {
						addHomePage(id, builderService, iwrb, addedHomePages, getRoleForHomePage(accessController, userRoles, id), locale, homePages);
					}
				}
			}

			int homePageId = user.getHomePageID();
			if (homePageId > 0) {
				addHomePage(homePageId, builderService, iwrb, addedHomePages, getRoleForHomePage(accessController, userRoles, homePageId), locale, homePages);
			}
		}

		if (ListUtil.isEmpty(homePages)) {
			return null;
		}

		return homePages;
	}

	private String getRoleForHomePage(AccessController accessController, Set<String> userRoles, Integer id) {
		if (id == null || ListUtil.isEmpty(userRoles)) {
			return null;
		}

		try {
			GroupHome groupHome = (GroupHome) IDOLookup.getHome(Group.class);
			Group group = groupHome.findByHomePageID(id);
			if (group == null) {
				return null;
			}

			Collection<String> groupRoles = accessController.getAllRolesKeysForGroup(group);
			if (ListUtil.isEmpty(groupRoles)) {
				return null;
			}

			for (String groupRole: groupRoles) {
				if (userRoles.contains(groupRole)) {
					return groupRole;
				}
			}
		} catch (Exception e) {
			getLogger().warning("Error getting role by page ID: " + id);
		}

		return null;
	}

	private void addHomePage(
			Integer id,
			BuilderService builderService,
			IWResourceBundle iwrb,
			Map<String, Boolean> addedHomePages,
			String roleKey,
			Locale locale,
			List<UserHomePageBean> homePages
	) {
		if (id == null) {
			return;
		}

		ICPage page = null;
		try {
			ICPageHome icPageHome = (ICPageHome) IDOLookup.getHome(ICPage.class);
			page = icPageHome.findByPrimaryKey(id);
		} catch (Exception e) {
			getLogger().warning("Error getting page by ID: " + id);
		}
		addHomePage(page, builderService, iwrb, addedHomePages, roleKey, locale, homePages);
	}

	private void addHomePage(
			ICPage page,
			BuilderService builderService,
			IWResourceBundle iwrb,
			Map<String, Boolean> addedHomePages,
			String roleKey,
			Locale locale,
			List<UserHomePageBean> homePages
	) {
		if (page == null) {
			return;
		}

		String uri = null;
		try {
			uri = builderService.getPageURI(page.getId());
		} catch (RemoteException e) {
			getLogger().log(Level.WARNING, "Error getting uri for page: " + page.getId(), e);
		}
		String localizedRoleName = StringUtil.isEmpty(roleKey) ? page.getName(locale) : iwrb.getLocalizedString(new StringBuilder("role_name.").append(roleKey).toString(), roleKey);
		roleKey = StringUtil.isEmpty(roleKey) ? CoreConstants.EMPTY : roleKey;

		if (!StringUtil.isEmpty(uri) && !addedHomePages.containsKey(uri)) {
			homePages.add(new UserHomePageBean(roleKey, localizedRoleName, uri, roleKey));
			addedHomePages.put(uri, Boolean.TRUE);
		}
	}

	@Override
	public Map<String, ICPage> getUserCompaniesPages(IWContext iwc) {
		User user = getLegacyUser(getCurrentUser());
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
			getLogger().log(Level.SEVERE, "Error getting companies for user: " + user);
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

	@Override
	public String getCurrentCompanyAttributeId() {
		return getUserCompanyBusiness() == null ? null : getUserCompanyBusiness().getCurrentCompanyAttributeId();
	}

	private boolean canAddPageForCompany(Group company, ICPage page, String currentCompanyId) {
		if (company == null || page == null) {
			return false;
		}

		return page != null && (StringUtil.isEmpty(currentCompanyId) ? true : !currentCompanyId.equals(company.getId()));
	}

	@Transactional(readOnly = true)
	private boolean canAddPageForGroup(com.idega.user.data.bean.Group group, int currentPageId) {
		if (group == null) {
			return false;
		}
		if (!DBUtil.getInstance().isInitialized(group)) {
			group = DBUtil.getInstance().lazyLoad(group);
		}

		Integer homePageId = null;
		try {
			com.idega.core.builder.data.bean.ICPage homePage = group.getHomePage();
			if (homePage != null) {
				if (!DBUtil.getInstance().isInitialized(homePage)) {
					homePage = DBUtil.getInstance().lazyLoad(homePage);
				}
				homePageId = homePage.getID();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (homePageId == null) {
			try {
				GroupHome groupHome = (GroupHome) IDOLookup.getHome(Group.class);
				Group tmpGroup = groupHome.findByPrimaryKey(group.getID());
				ICPage homePage = tmpGroup.getHomePage();
				if (homePage != null) {
					homePageId = Integer.valueOf(homePage.getId());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return homePageId != null && currentPageId != Integer.valueOf(homePageId).intValue();
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
			getLogger().log(Level.SEVERE, "Error getting current page by URI: " + iwc.getRequestURI(), e);
		}
		if (currentPage == null) {
			try {
				currentPage = builderService.getCurrentPage(iwc);
			} catch (RemoteException e) {
				getLogger().log(Level.SEVERE, "Error getting current page", e);
			}
		}
		if (currentPage == null) {
			return -1;
		}

		try {
			return Integer.valueOf(builderService.getPageKeyByURI(iwc.getRequestURI()));
		} catch(Exception e) {
			getLogger().log(Level.SEVERE, "Error getting ID of current page", e);
		}

		return -1;
	}

	private BuilderService getBuilderService() {
		try {
			return BuilderServiceFactory.getBuilderService(IWMainApplication.getDefaultIWApplicationContext());
		} catch (RemoteException e) {
			getLogger().log(Level.SEVERE, "Error getting: " + BuilderService.class.getName(), e);
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
