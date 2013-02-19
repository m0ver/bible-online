var viewer=function()
{
	this.title="";
	this.description="";
	this.lastBuildDate=new Date();

	this.items=new Array();
	this.index=0;
	
	arguments[0]=arguments[0]||{};
	
	this.start=typeof(arguments[0].start)=='function'?arguments[0].start:function(){};
	this.complete=typeof(arguments[0].complete)=='function'?arguments[0].complete:function(){};
	this.cancel=typeof(arguments[0].cancel)=='function'?arguments[0].cancel:function(){};
	
	this.load=function(){
		var $this=this;
		var url=arguments[0];
		
		var request=$.ajax({
			dataType:"xml",
			beforeSend:function(){$this.title="正在加载中...";$this.start();},
			url:url})
			.complete(function(){})
			.error(function(){$this.cancel();})
			.success(function(data){
				var XMLDocument=data;
				
				$this.title=XMLDocument.getElementsByTagName('title')[0].hasChildNodes()?XMLDocument.getElementsByTagName('title')[0].childNodes[0].nodeValue:"";
				$this.description=XMLDocument.getElementsByTagName('description')[0].hasChildNodes()?XMLDocument.getElementsByTagName('description')[0].childNodes[0].nodeValue:"";
				
				var articles=XMLDocument.getElementsByTagName('item');
				for(var i=articles.length-1;i>=0;i--)
				{
					var article=new Object();
					//alert(articles[i].getElementsByTagName('dc:creator'));
					try
					{
						article.author=articles[i].getElementsByTagName('dc:creator')[0].hasChildNodes()?articles[i].getElementsByTagName('dc:creator')[0].firstChild.nodeValue:"";
					}catch(e)
					{
						
					}
					article.title=articles[i].getElementsByTagName('title')[0].hasChildNodes()?articles[i].getElementsByTagName('title')[0].childNodes[0].nodeValue:"";
					article.link=articles[i].getElementsByTagName('guid')[0].hasChildNodes()?articles[i].getElementsByTagName('guid')[0].childNodes[0].nodeValue:"";
					article.pubDate=articles[i].getElementsByTagName('pubDate')[0].hasChildNodes()?articles[i].getElementsByTagName('pubDate')[0].childNodes[0].nodeValue:"";
					article.description=articles[i].getElementsByTagName('description')[0].hasChildNodes()?articles[i].getElementsByTagName('description')[0].childNodes[0].nodeValue:"";
					
					$this.items.push(article);
				}
				
				$this.show();
				$this.complete();
			});
	};
	
	this.list=function(){
		return this.index_list;
	};
	
	this.order=false;
	
	this.getList=function()
	{
		var $this=this;
		var list=document.createElement("ul");
		var i=0;
		
		if(this.order)
		{
			while(i<this.items.length)
			{
				var article=this.items[i];
				var li=document.createElement("li");
				var a=document.createElement("a");
				a.href="javascript:void(0)";
				a.setAttribute("index",i);
				a.appendChild(document.createTextNode(article.title));
				
				struct.attachEvent(a,"click",function($this){
					$this.index=this.getAttribute("index");
					$this.show();
				});
				li.appendChild(a);
				
				list.appendChild(li);
				
				i++;
			}
		}
		else
		{
			i=this.items.length-1;
			while(i>=0)
			{
				var article=this.items[i];
				var li=document.createElement("li");
				var a=document.createElement("a");
				a.href="javascript:void(0);";
				a.setAttribute("index",i);
				a.appendChild(document.createTextNode(article.title));

				var attributes={"index":a.getAttribute("index")};
				struct.attachEvent(a,"click",function(){
					$this.index=a.getAttribute("index");
					alert($this.index);
					$this.show();
				},attributes);
				
				li.appendChild(a);
				
				list.appendChild(li);
				
				i--;
			}
			
		}
		
		return list;
	};
	
	this.sort=function()
	{
		this.order=!this.order;
		
		return this.order;
	};
	
	this.dateFormat=function(date)
	{
		return struct.dateFormat(date,'yyyy年mm月dd日  HH点MM分ss秒');
	};
	
	this.show=function()
	{
		document.getElementById("title").innerHTML=this.title;
		document.getElementById("description").innerHTML=this.description;
		document.getElementById("lastBuildDate").innerHTML=this.dateFormat(this.lastBuildDate);
		
		if(arguments[0]) this.index=arguments[0];
		
		var article=this.items[this.index];
		
		var href=document.createElement("a");
		href.setAttribute("href",article.link);
		href.appendChild(document.createTextNode(article.title));
		
		document.getElementById("article-title").innerHTML='';
		document.getElementById("article-title").appendChild(href);
		document.getElementById("article-creator").innerHTML=article.author!=''?article.author:"未知";
		document.getElementById("article-description").innerHTML=article.description;
		document.getElementById("article-pubDate").innerHTML=this.dateFormat(article.pubDate);
	};
	
	this.next=function()
	{
		if(this.index<this.items.length-1)
		{
			this.index++;
		}
		else
		{
			this.index=0;
		}
		
		this.show();
	};
	
	this.prev=function()
	{
		if(this.index>0)
		{
			this.index--;
		}
		else
		{
			this.index=this.items.length-1;
		}
		
		this.show();
	}
	
}