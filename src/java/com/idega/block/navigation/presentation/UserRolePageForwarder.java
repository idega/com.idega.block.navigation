/*
 * $Id$
 * Created on Jun 19, 2007
 *
 * Copyright (C) 2007 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.navigation.presentation;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.navigation.bean.UserHomePageBean;
import com.idega.block.navigation.business.UserHomePageResolver;
import com.idega.block.navigation.utils.NavigationConstants;
import com.idega.block.web2.business.JQuery;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.RadioButton;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

public class UserRolePageForwarder extends Block {

	@Autowired
	private JQuery jQuery;

	@Override
	public void main(IWContext iwc) throws RemoteException {
		ELUtil.getInstance().autowire(this);

		IWBundle navBundle = iwc.getIWMainApplication().getBundle(NavigationConstants.IW_BUNDLE_IDENTIFIER);
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, Arrays.asList(
				jQuery.getBundleURIToJQueryLib(),
				CoreConstants.DWR_ENGINE_SCRIPT,
				"/dwr/interface/WebUtil.js",
				navBundle.getVirtualPathWithFileNameString("javascript/NavigationHomePageHelper.js")
		));

		UserHomePageResolver homePageResolver = null;
		try {
			homePageResolver = ELUtil.getInstance().getBean(UserHomePageResolver.SPRING_BEAN_IDENTIFIER);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if (homePageResolver == null) {
			getLogger().warning(UserHomePageResolver.class.getName() + " is unknown");
			return;
		}

		List<UserHomePageBean> homePages = homePageResolver.getUserHomePages(iwc);
		if (ListUtil.isEmpty(homePages)) {
			getLogger().warning("There are no homepages");
			return;
		}

		String bundleIdentifier = iwc.getApplicationSettings()
				.getProperty(
						NavigationConstants.USER_ROLE_HOME_PAGE_RESOURCE_BUNDLE_PROPERTY,
						NavigationConstants.IW_BUNDLE_IDENTIFIER
		);
		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(bundleIdentifier).getResourceBundle(iwc);

		Layer layer = new Layer();
		layer.setStyleClass("userRolePageForwarder");
		add(layer);

		Lists list = new Lists();
		layer.add(list);

		for (UserHomePageBean page: homePages) {
			RadioButton button = new RadioButton("userRolePage", page.getUri());
			button.setStyleClass(page.getRole());

			ListItem item = new ListItem();
			item.setStyleClass(page.getId());
			item.add(button);
			item.add(new Text(page.getName()));
			list.add(item);
		}

		Link btn = new Link(new Span(new Text(iwrb.getLocalizedString("go", "Go!"))));
		btn.setURL("javascript:void(0);");
		btn.setOnClick("NavigationHomePageHelper.doNavigateToHomeFolder('" + iwc.getSessionId() + "');");
		layer.add(btn);
	}
}