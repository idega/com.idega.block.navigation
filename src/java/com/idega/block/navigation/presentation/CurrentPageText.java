/*
 * Created on 25.5.2004
 */
package com.idega.block.navigation.presentation;

import com.idega.builder.business.PageTreeNode;
import com.idega.core.builder.data.ICPage;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.Text;


/**
 * @author laddi
 */
public class CurrentPageText extends Text {

	public void main(IWContext iwc) throws Exception {
		ICPage currentPage = getBuilderService(iwc).getCurrentPage(iwc);
		PageTreeNode page = new PageTreeNode(((Integer) currentPage.getPrimaryKey()).intValue(), iwc);
		setText(page.getLocalizedNodeName(iwc));
	}
}