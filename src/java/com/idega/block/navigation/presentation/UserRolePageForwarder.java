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
import java.util.Map;

import com.idega.block.navigation.business.UserHomePageResolver;
import com.idega.block.navigation.utils.NavigationConstants;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.data.ICPage;
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
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;

public class UserRolePageForwarder extends Block {

	@Override
	public void main(IWContext iwc) throws RemoteException {
		UserHomePageResolver homePageResolver = null;
		try {
			homePageResolver = ELUtil.getInstance().getBean(UserHomePageResolver.SPRING_BEAN_IDENTIFIER);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if (homePageResolver == null) {
			return;
		}
		
		Map<String, ICPage> rolePageMap = homePageResolver.getUserHomePages(iwc);
		if (rolePageMap == null || ListUtil.isEmpty(rolePageMap.values())) {
			return;
		}
		
		String bundleIdentifier = iwc.getApplicationSettings().getProperty("user.role.forwarder.bundle", NavigationConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(bundleIdentifier).getResourceBundle(iwc);
			
		BuilderService bs = getBuilderService(iwc);
		getParentPage().getAssociatedScript().addFunction("navHandler", getScriptSource());

		Layer layer = new Layer();
		layer.setStyleClass("userRolePageForwarder");
		add(layer);

		Lists list = new Lists();
		layer.add(list);

		for (String role: rolePageMap.keySet()) {
			ICPage page = rolePageMap.get(role);

			RadioButton button = new RadioButton("userRolePage", bs.getPageURI(page));

			ListItem item = new ListItem();
			item.setStyleClass(role);
			item.add(button);
			item.add(new Text(iwrb.getLocalizedString("role_name." + role, role)));
			list.add(item);
		}

		Link btn = new Link(new Span(new Text(iwrb.getLocalizedString("go", "Go!"))));
		btn.setURL("javascript:" + getScriptCaller("userRolePage"));
		layer.add(btn);
	}

	private String getScriptCaller(String dropDownName) {
		return "navHandler(document.getElementsByName('" + dropDownName + "'))";
	}

	private String getScriptSource() {
		StringBuffer s = new StringBuffer();
		s.append("function navHandler(inputs) {");
		s.append("\n\t").append("for (var a = 0; a < inputs.length; a++) {");
		s.append("\n\t\t").append("var	input = inputs[a];");
		s.append("\n\t\t").append("if (input.checked) {");
		s.append("\n\t\t\t").append("var URL = input.value;	");
		s.append("\n\t\t\t").append("window.location.href = URL;");
		s.append("\n\t\t").append("}");
		s.append("\n\t").append("}");
		s.append("\n").append("}");
		return s.toString();
	}
}