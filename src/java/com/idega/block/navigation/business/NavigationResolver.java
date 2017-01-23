package com.idega.block.navigation.business;

import com.idega.presentation.IWContext;

public interface NavigationResolver {

	public Boolean isPageEnabled(IWContext iwc, String pageKey, String disabledPages);

}