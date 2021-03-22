
/* *
 * @Copyright Mover.
 * @author Mover Zhou.
 * @site: http://www.ingod.today
 * usage: 
 * 		
 * */

(function($){
	
	$.fn.corner = {
			background:"#cecece",
			radius:3,
			border: {
				color:"#cecece",
			}
	};
	
	$.data("topleft",false);
	$.data("topright",false);
	$.data("bottomleft",false);
	$.data("bottomright",false);
	
	$top=null;
	$bottom=null;
	
	$.fn.addCorner=function(){
		
		if((typeof arguments[0] == undefined) || (typeof arguments[0]!= "string"))
		{
			alert("Invalid arguments. available arguments:'topleft | topright | bottomleft | bottomright'");
			return;
		}
		
		if(typeof(arguments[1]) != "undefined")
		{
			$.fn.corner = arguments[1];
		}
		
		switch(arguments[0]){
			case "topleft":
				this.data("topleft",true);

				break;
			case "topright":
				this.data("topright",true);
				
				break;
			case "bottomleft":
				this.data("bottomleft",true);
				
				break;
			case "bottomright":
				this.data("bottomright",true);
				
				break;
			default:break;
		}
		
		$top_corner=this.data("topleft")||this.data("topright");
		$bottom_corner=this.data("bottomleft")||this.data("bottomright");
		
		/*if($bottom_corner)
		{
			for(i=1;i<this.corner.radius;i++){
				
				$corner=$("<div></div>").css({"background-color":$(this).corner.background,"margin":0,"padding-top":"1px"});
	
				if(this.data("topleft")||this.data("bottomleft"))
				{
					$corner.css("margin-left",i+"px");
				}
				
				if(this.data("topright")||this.data("bottomright")){
					$corner.css("margin-right",i+"px");
				}
				
				$(this).prepend($corner);
			}
		}
		else 
		{*/
			for(i=this.corner.radius;i>0 && $top_corner;i--){
//				$bottom=$("<div id=\"__bottom_tmp__\" />");

				if($top==null || $top.find(".top_corner"+i).length==0)
				{
					$top=$top || $("<div id=\"__top_tmp__\" />");
					$corner=$("<div class=\"top_corner"+i+"\"></div>").css({"background-color":$(this).corner.background,"margin":0,"padding-top":"1px"});

					if(this.data("topleft")||this.data("bottomleft"))
					{
						$corner.css("margin-left",i+"px");
					}
					
					if(this.data("topright")||this.data("bottomright")){
						$corner.css("margin-right",i+"px");
					}

					$top.append($corner);
				}
				else
				{
					$corner=$(".top_corner"+i+"");
					
					if(this.data("topleft")||this.data("bottomleft"))
					{
						$corner.css("margin-left",i+"px");
					}
					
					if(this.data("topright")||this.data("bottomright")){
						$corner.css("margin-right",i+"px");
					}
				}
			}
			
			if($top_corner) $(this).prepend($top.html())
	//	}

	}
	
	$.fn.addCorners=function(){
		var corner_list=$(this).corner.positions;
		if(arguments.length>1)
		{
			if((typeof arguments[0] == undefined) || (typeof arguments[0]!= "string"))
			{
				alert("Invalid arguments. available arguments:'topleft,topright, bottomleft,bottomright'");
				return;
			}
			else
				corner_list=arguments[0].split(",");
			
			if(typeof(arguments[1]) != "undefined")
			{
				$(this).data("corner",arguments[1]);
			}
		}
		else {
			if(typeof(arguments[0]) != "undefined")
			{
				$(this).data("corner",arguments[0]);
			}
		}
		
		for(var i=0;i<corner_list.length;i++)
		{
			$(this).addCorner(corner_list[i],$(this).data("corner"));
		}
	}
	
	$.fn.removeCorners=function()
	{
		if((typeof arguments[0] == undefined) || (typeof arguments[0]!= "string"))
		{
			alert("Invalid arguments. available arguments:'topleft,topright, bottomleft,bottomright'");
			return;
		}
		else
			corner_list=arguments[0].split(",");
		
		for(var i=0;i<corner_list.length;i++)
		{
			$(this).removeCorner(corner_list[i]);
		}

	}
	
	$.fn.removeCorner=function()
	{
		if( typeof arguments[0] == undefined){
			alert("error for build corners to remove");return;
		}
		
		switch(arguments[0]){
			case "topleft":
				if(this.data("topleft"))
					this.data("topleft").remove();
				break;
			case "topright":
				if(this.data("topright"))
					this.data("topright").remove();
				break;
			case "bottomleft":
				if(this.data("bottomleft"))
					this.data("bottomleft").remove();
				break;
			case "bottomright":
				if(this.data("bottomright"))
					this.data("bottomright").remove();
				break;
			default:break;
		}
	}
	
})(jQuery);
