var NavigationHomePageHelper = {};

NavigationHomePageHelper.doNavigateToHomeFolder = function(sessionId) {
	jQuery('input[name=\'userRolePage\'][type=\'radio\']').each(function() {
		var input = jQuery(this);
		var checked = input.attr('checked');
		if (checked != null && checked == 'checked') {
			WebUtil.setActiveRole(sessionId, input.attr('class'), {
				callback: function(result) {
					window.location.href = input.val();
				}
			});
		}
	});
}