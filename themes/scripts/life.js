var Tab=function(){

	this.corner={
		src:"/template/images/circle.png",
		width:"7px",
		height:"7px",
		background:"#fff",
		positions: ["topleft","topright"]
	};

	this.controller=document.createElement("a");
	this.controller.appendChild(document.createTextNode(arguments[0].caption));
	this.controller.style.cursor="pointer";
	
	this.id=arguments[0].id?arguments[0].id:0;
	this.attributes=arguments[0].attributes;
	this.cmd=function(){};
	this.selected=false;

	this.setId=function(id)
	{
		this.id=id;
		this.controller.setAttribute("id","tab_"+this.id);
		this.controller.setAttribute("href","#"+this.id);
		return this.controller;
	}
	this.getAttribute=function(attribute)
	{
		return this.attributes[attribute].nodeValue;
	}
	this.attachEvent=function(event,cmd)
	{
		var $this=this;
		$this.cmd=cmd;
		
		event=event || struct.Event.MOUSE.CLICK;
		struct.attachEvent(
			$this.controller,
			event,
			function()
			{
				$this.cmd($this.id);
			}
		);
	}
	this.click=function()
	{
		this.cmd(this.id);
	}
	this.select=function()
	{
		struct.save("dialog.status",this.id,24*3600*365);
		
		location.hash="#"+this.id;
		this.controller.className="current";
		this.controller.disabled="disabled";
		$(this.controller).addCorners("topleft,topright",this.corner);

		this.selected=true;
	}
	this.unselect=function()
	{
		this.controller.className="*";
		this.controller.disabled="";
		$(this.controller).removeCorners("topleft,topright");
		
		this.selected=false;
	}
}

var TabForm=function(){

		this.name=arguments[0];
		this.list=document.createElement("ul");
		this.list.setAttribute("class","nowrap");
		
		this.panel=document.createElement("div");
		this.panel.setAttribute("class","panel");
		
		this.index=0;
		this.current_index=0;		
		this.dialog=document.getElementById("tab");
		
		if(!this.dialog){
			this.dialog=document.createElement("div");
			this.dialog.setAttribute("id","tab");
			this.dialog.setAttribute("name","tab");
			this.dialog.setAttribute("style","clear:both;display:none");
		}
		
		if(typeof(location.hash)!='undefined' && location.hash.length>0)
		{
			var n=location.hash.substr(1);
			this.current_index=isNaN(n)?0:parseInt(n);
		}
		
		this.datalist=null;
		this.contents=new Array();
		this.resources=new Array();
		this.items=new Array();

	this.addTab=function(tab)
	{
		this.index=this.list.getElementsByTagName("li").length;
		
		var tabItem=document.createElement("li");
			tabItem.appendChild(tab.setId(this.index));
			
		this.list.appendChild(tabItem);
		this.dialog.appendChild(this.list);
		this.items.push(tab);
		
		return this;
	}
	
	this.selectTab=function()
	{
		this.current_index=(typeof(arguments[0])=='undefined'?this.current_index:arguments[0]);

		if(this.current_index>this.items.length)	this.current_index=0;
		for(var i=0;i<this.items.length;i++)
		{
			var tab=this.items[i];

			if(this.current_index==i)
			{
				tab.click();
			}
			else
			{
				tab.unselect();
			}
		}
	}
	this.unselectAll=function()
	{
		var i=0;
		while(i<this.items.length)
		{
			this.items[i++].unselect();
		}
	}
	this.appendElement=function(){
		if(arguments[0])	this.panel.appendChild(arguments[0]);
	}
	this.setText=function(text)
	{
		var form=document.createElement("form");
		form.setAttribute("method","post");
		form.setAttribute("onsubmit","return false");
		
		var textArea=document.createElement("textarea");
		textArea.setAttribute("rows",5);
		textArea.setAttribute("class","text");
		textArea.setAttribute("id","lifecontent");
		textArea.appendChild(document.createTextNode(text));
		
		form.appendChild(textArea);
		
		var $this=this;
		var buttons_panel=document.createElement("div");
		var save_button=document.createElement("a");
			save_button.setAttribute("href","javascript:void(0)");
			save_button.appendChild(document.createTextNode(" 保 存 "));
			struct.attachEvent(save_button,"click",function(){$this.save()});
			
		var cancel_button=document.createElement("a");
			cancel_button.setAttribute("href","javascript:void(0)");
			cancel_button.appendChild(document.createTextNode(" 取 消 "));
			struct.attachEvent(cancel_button,"click",function(){$this.close()});
			
		buttons_panel.appendChild(save_button);
		buttons_panel.appendChild(cancel_button);
		
		form.appendChild(buttons_panel);
		this.panel.innerHTML='';
		this.panel.appendChild(form);

		return this;
	}
	this.setPanel=function()
	{
		var text=arguments[0];
		var form=document.createElement("form");
			form.setAttribute("method","post");
			form.setAttribute("onsubmit","return false");
			
		var content=document.createElement("div");
			content.setAttribute("id","lifecontent");
			content.setAttribute("name","lifecontent");
			content.appendChild(text);
		
			form.appendChild(content);
			
		var clr=document.createElement("div");
			clr.setAttribute("style","clear:both;height:10px");
			form.appendChild(clr);
			
		this.panel.innerHTML='';
		this.panel.appendChild(form);
	}
	this.getPanel=function()
	{
		var form=document.createElement("form");
			form.setAttribute("method","post");
			form.setAttribute("onsubmit","return false");
			
		var content=document.createElement("div");
			content.setAttribute("id","lifecontent");
			content.setAttribute("class","lifecontent");
		
			form.appendChild(content);
			
		var clr=document.createElement("div");
			clr.setAttribute("style","clear:both;height:10px");
			form.appendChild(clr);
			
			this.panel.innerHTML='';
			this.panel.appendChild(form);
		
		return content;
	}
	this.setHTML=function(htmlText)
	{
		var form=document.createElement("form");
		form.setAttribute("method","post");
		form.setAttribute("onsubmit","return false");
		
		var html=document.createElement("div");
		html.innerHTML=(htmlText);
		
		form.appendChild(html);
		this.panel.innerHTML='';
		this.panel.appendChild(form);

		return this;
	}
	
	this.setForm=function(forms)
	{
		var form=document.createElement("form");
		form.setAttribute("method","post");
		form.setAttribute("onsubmit","return false");
		form.appendChild(forms);
		
		var $this=this;
		var buttons_panel=document.createElement("div");
		var save_button=document.createElement("a");
			save_button.setAttribute("href","javascript:void(0)");
			save_button.appendChild(document.createTextNode(" 保 存 "));
			struct.attachEvent(save_button,"click",function(){$this.save()});
			
		var cancel_button=document.createElement("a");
			cancel_button.setAttribute("href","javascript:void(0)");
			cancel_button.appendChild(document.createTextNode(" 取 消 "));
			struct.attachEvent(cancel_button,"click",function(){$this.close()});
			
		buttons_panel.appendChild(save_button);
		buttons_panel.appendChild(cancel_button);
		
		form.appendChild(buttons_panel);
		this.panel.innerHTML='';
		this.panel.appendChild(form);
		
		return this;
	}
	this.setList=function(list)
	{
		var form=document.createElement("form");
		form.setAttribute("method","post");
		form.setAttribute("onsubmit","return false");
		
		var $this=this;
		
		var list_panel=document.createElement("div");
			list_panel.setAttribute("id","list");
			list_panel.innerHTML=list;
			
		var buttons_panel=document.createElement("div");
			buttons_panel.setAttribute("style","text-align:right;width:100%");
		var save_button=document.createElement("a");
			save_button.setAttribute("href","javascript:void(0)");
			save_button.appendChild(document.createTextNode(" 保 存 "));
			struct.attachEvent(save_button,"click",function(){$this.save()});
			
		var cancel_button=document.createElement("a");
			cancel_button.setAttribute("href","javascript:void(0)");
			cancel_button.appendChild(document.createTextNode(" 取 消 "));
			struct.attachEvent(cancel_button,"click",function(){$this.close()});
			
		buttons_panel.appendChild(save_button);
		buttons_panel.appendChild(cancel_button);
		
		form.appendChild(list_panel);
		form.appendChild(buttons_panel);
		this.panel.innerHTML='';
		this.panel.appendChild(form);

		return this;
	}
	this.show=function()
	{
		this.dialog.style.display="block";	
		this.datalist.style.display="none";
	}
	this.hide=function()
	{
		this.dialog.style.display="none";
		this.datalist.style.display="block";
	}
	this.setData=function()
	{
		this.data=arguments[0].data;
		this.url=arguments[0].url;
	}
	this.save=function()
	{
		var $this=this;
		
	    if(document.getElementById("lifecontent").value==null || document.getElementById("lifecontent").value=="")
	    {
	    	new struct.message().showMessage("内容填写不完整，无法保存！");
	    	return;
	    }
		
		this.data.text=document.getElementById("lifecontent").value;
		
		if(confirm("你确定保存吗?"))
		{
			new Ajax.Request(
				$this.url,
				{
					parameters:$this.data,
					onSuccess:function(transport){
						if(transport.responseText.indexOf("true")==0)
						{
							new struct.message().showMessage("操作成功！");
							
							// 重新构造表单及其值
							$this.saveMemory();
						}
						else if(transport.responseText.indexOf("false")==0)
						{
							new struct.message().showMessage("内容填写不完整，无法保存！");
						}
						else if(confirm("你可能尚未登录或登录超时！点确定立即重新登录。"))
						{
							window.location.reload();
						}
					
					},
					onFailure:function(transport){
					
					}
				}
			);
		}
	}
	this.saveMemory=function()
	{
		document.location.reload();
	}
	this.close=function()
	{
		this.hide();
	}
	this.setup=function()
	{
		var parameters=arguments[0];
		this.datalist=document.getElementById(parameters.data);
		var itemlist=this.datalist.getElementsByTagName("li");
		var item=null,anchor=null;
		
		var $this=this;
		
		if(struct.get("dialog.status")!=null)
		{
			this.current_index=parseInt(struct.get("dialog.status"));
		}
		
		if(this.current_index >= itemlist.length/2) this.current_index=0;
		
		for(var i=0;i<itemlist.length;i++)
		{
			if(itemlist[i].parentNode==this.datalist)
			{
				item=itemlist[i].getElementsByTagName("a")[0];
				if(itemlist[i].getElementsByTagName("li").length>0)
				{
					this.resources[this.contents.length]=itemlist[i].getElementsByTagName("li")[0];
					this.contents[this.contents.length]=(this.resources[this.contents.length].childNodes.length==0)?'':this.resources[this.contents.length].childNodes[0].nodeValue;
				}
				
				var tab=new Tab({caption:item.childNodes[item.childNodes.length-1].nodeValue,attributes:item.attributes});
					tab.attachEvent("focus",function(){
						parameters.onrender(parseInt(this.id));
						$this.unselectAll();
						this.select();
					});
				this.addTab(tab);
				
				if(this.current_index==i)
				{
					tab.click();
				}
			}
		}
		
		this.dialog.appendChild(this.panel);
		document.getElementById(parameters.container).appendChild(this.dialog);
	}
}

