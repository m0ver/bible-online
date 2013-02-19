var CSSController={};
CSSController.prototype.getStyle=function(selectorName) 
{
	var i=0;
	while (i<document.styleSheets.length) 
	{
		var rules;
		if (document.styleSheets[i].cssRules) {
			rules = document.styleSheets[i].cssRules;
		} else {
			rules = document.styleSheets[i].rules;
		}
		
		i++;
		
		var j=0;
		while (j<rules.length) {
			if (rules[j].selectorText == selectorName) {
				return rules[j].style;
			}
			
			j++;
		}
	}
	
	return false;
}