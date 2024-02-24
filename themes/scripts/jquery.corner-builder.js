
/* *
 * @Copyright Mover.
 * @author Mover Zhou.
 * @site: https://www.ingod.today
 * usage: 
 * 		$(function(){new builder("panel_id",{src:"",height:"",width:"",background:""})});
 * */

(function($){
	
	$.fn.corner = {
			src:"../images/circle.png",
			width:"10px",
			height:"10px",
			background:"#cecece",
			positions: ["topleft","topright","bottomleft","bottomright"]
	};
	
	$.data("topleft",null);
	$.data("topright",null);
	$.data("bottomleft",null);
	$.data("bottomright",null);
	
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
		
		height=parseInt($(this).corner.height)/2;
		width=parseInt($(this).corner.width)/2;

		switch(arguments[0]){
			case "topleft":
				$corner=$("<div class=\"topleft\"></div>").css({"position":"absolute","left":0,"top":0,"background":"url(\""+$(this).corner.src+"\") no-repeat scroll 0 0 "+$(this).corner.background+"","height":height+"px","width":width+"px"});
				this.data("topleft",$corner);

				break;
			case "topright":
				$corner=$("<div class=\"topright\"></div>").css({"position":"absolute","right":0,"top":0,"background":"url(\""+$(this).corner.src+"\") no-repeat scroll -"+width+"px 0 "+$(this).corner.background+"","height":height+"px","width":width+"px"});
				this.data("topright",$corner);
				
				break;
			case "bottomleft":
				$corner=$("<div class=\"bottomleft\"></div>").css({"position":"absolute","left":0,"bottom":0,"background":"url(\""+$(this).corner.src+"\") no-repeat scroll 0 -"+height+"px "+$(this).corner.background+"","height":height+"px","width":width+"px"});
				this.data("bottomleft",$corner);
				
				break;
			case "bottomright":
				$corner=$("<div class=\"bottomright\"></div>").css({"position":"absolute","right":0,"bottom":0,"background":"url(\""+$(this).corner.src+"\") no-repeat scroll -"+width+"px -"+height+"px "+$(this).corner.background+"","height":height+"px","width":width+"px"});
				this.data("bottomright",$corner);
				
				break;
			default:break;
		}

		if(typeof($corner) != "undefined" && $(this).find("."+arguments[0]).length==0)
		$(this).prepend($corner).css({"position":"relative"});
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
	
	$.fn.restoreCorner=function()
	{
		if( typeof arguments[0] == undefined){
			alert("error for build corners to restore");return;
		}
		
		$(this).addCorner(arguments[0]);				
	}
	
	$.fn.replaceCorner=function()
	{
		if( typeof arguments[0] == undefined){
			alert("error for build corners to replace");return;
		}
		
		if( typeof arguments[1] == undefined){
			alert("error for build corners to replace");return;
		}
		
		type=arguments[0];
		
		if(this.data(type))
		{
			var _corner=arguments[1];
			var height=parseInt(_corner.height)/2;
			var width=parseInt(_corner.width)/2;
			
			switch(type){
				case "topleft":
					if(this.data("topleft"))
					{
						$corner=$("<div class=\"topleft\"></div>").css({"position":"absolute","left":0,"top":0,"background":"url(\""+_corner.src+"\") no-repeat scroll 0 0 "+_corner.background+"","height":height+"px","width":width+"px"});
					}
					
					break;
				case "topright":
					if(this.data("topright"))
					{
						$corner=$("<div class=\"topright\"></div>").css({"position":"absolute","right":0,"top":0,"background":"url(\""+_corner.src+"\") no-repeat scroll -"+width+"px 0 "+_corner.background+"","height":height+"px","width":width+"px"});
					}
					
					break;
				case "bottomleft":
					if(this.data("bottomleft"))
					{
						$corner=$("<div class=\"bottomleft\"></div>").css({"position":"absolute","left":0,"bottom":0,"background":"url(\""+_corner.src+"\") no-repeat scroll 0 -"+height+"px "+_corner.background+"","height":height+"px","width":width+"px"});
					}
					
					break;
				case "bottomright":
					if(this.data("bottomright"))
					{
						$corner=$("<div class=\"bottomright\"></div>").css({"position":"absolute","right":0,"bottom":0,"background":"url(\""+_corner.src+"\") no-repeat scroll -"+width+"px -"+height+"px "+_corner.background+"","height":height+"px","width":width+"px"});
					}
					
					break;
				default:break;
			}
			
			this.data(type).replaceWith($corner);
		}

	}
})(jQuery);
