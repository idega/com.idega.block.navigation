package com.idega.block.navigation.business;

import java.util.Map;

import com.idega.core.builder.data.ICPage;
import com.idega.presentation.IWContext;

public interface UserHomePageResolver {
	
	public static final String SPRING_BEAN_IDENTIFIER = "userHomePageResolver";

	public Map<String, ICPage> getUserHomePages(IWContext iwc);
	
	public Map<String, ICPage> getUserCompaniesPages(IWContext iwc);
	
	public String getCurrentCompanyAttributeId();
}
