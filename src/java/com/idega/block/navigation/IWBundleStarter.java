package com.idega.block.navigation;

import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.servlet.filter.IWBundleResourceFilter;

public class IWBundleStarter implements IWBundleStartable {

	public void start(IWBundle starterBundle) {
		IWBundleResourceFilter.copyAllFilesFromJarDirectory(starterBundle.getApplication(), starterBundle, "/facelets/");
		IWBundleResourceFilter.copyAllFilesFromJarDirectory(starterBundle.getApplication(), starterBundle, "/resources/");
	}

	public void stop(IWBundle starterBundle) {
	}
}