var $j = jQuery.noConflict();

$j(document).ready(function() {
	$j('div.navigationDropdownMenu select.standalone').change(function() {
		navigateToURL(this);
	})
	
	$j('div.navigationDropdownMenu a').click(function() {
		navigateToURL($j(this).siblings('select:first'));
	})
});

function navigateToURL(input) {
	var URL = input.options[input.selectedIndex].value;
	window.location.href = URL;
}