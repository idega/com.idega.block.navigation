package com.idega.block.navigation.business;

import java.util.List;
import java.util.Map;

import com.idega.block.navigation.bean.UserHomePageBean;
import com.idega.core.builder.data.ICPage;
import com.idega.presentation.IWContext;

public interface UserHomePageResolver {
	
	public static final String SPRING_BEAN_IDENTIFIER = "userHomePageResolver";

	public List<UserHomePageBean> getUserHomePages(IWContext iwc);
	
	public Map<String, ICPage> getUserCompaniesPages(IWContext iwc);
	
	public String getCurrentCompanyAttributeId();
}
