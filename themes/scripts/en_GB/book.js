var Option=function()
{
	var o=document.createElement('OPTION');
	o.text=arguments[0].text;
	o.value=arguments[0].value;
	o.number=arguments[0].number;
	
	return o;
}
var Book=function()
{
	this.old_books_node=$(arguments[0].old_books_id);
	this.old_chapters_node=$(arguments[0].old_chapters_id);
	
	this.new_books_node=$(arguments[0].new_books_id);
	this.new_chapters_node=$(arguments[0].new_chapters_id);
	
	this.book_id=0;
	this.chapter_id=0;
	this.article_id=-1;
	
	this.url="/operator?action=feed&chapterid="+this.chapter_id+"&bookid="+this.book_id+"&clsid=3";
	
	this.load=function(book_id,chapter_id,article_id)
	{	
		var $this=this;
		this.book_id=book_id;
		this.chapter_id=chapter_id;
		
		if(typeof(article_id)!='undefined')this.article_id=article_id;
		
		this.url="/operator?action=feed&chapterid="+this.chapter_id+"&bookid="+this.book_id+"&clsid=3";

		for(var i=1;i<books.length;i++)
		{
			if(i<40)
			{
				this.old_books_node.options.add(new Option({"text":books[i].book_name,"value":i,"number":books[i].chapter_number}));
			}
			else
			{
				this.new_books_node.options.add(new Option({"text":books[i].book_name,"value":i,"number":books[i].chapter_number}));
			}
		}
		
		struct.attachEvent(this.old_books_node,"change",function(){
			$this.refresh_chapter($this.old_books_node);
		});
		struct.attachEvent(this.new_books_node,"change",function(){
			$this.refresh_chapter($this.new_books_node);
		});
				
		this.load_chapter();
	};
	
	this.refresh_chapter=function()
	{
		var $this=arguments[0];
		var chapters_node;
		this.book_id=$this.options[$this.selectedIndex].value;
		
		var chapters_number=books[this.book_id].chapter_number+1;
		
		if(this.book_id<40)
		{
			if($this==this.old_books_node)
			chapters_node=this.old_chapters_node;
			else
			chapters_node=this.new_chapters_node;
		}
		else
		{
			chapters_node=this.new_chapters_node;
		}
		
		chapters_node.options.length=1;

		for(var i=1;i<chapters_number;i++)
		{
			chapters_node.options.add(new Option({"text":"Chapter "+i,"value":i}));
		}
	};
	
	this.load_chapter=function()
	{
		var $this=this;
		var chapters_node;
		var chapters_number=books[this.book_id].chapter_number+1;
		
		if(this.book_id<40)
		{
			this.old_books_node.selectedIndex=this.book_id;
			chapters_node=this.old_chapters_node;
		}
		else
		{
			this.new_books_node.selectedIndex=this.book_id-39;
			chapters_node=this.new_chapters_node;
		}
		
		chapters_node.options.length=1;
		for(var i=1;i<chapters_number;i++)
		{
			chapters_node.options.add(new Option({"text":"Chapter "+i,"value":i}));
		}
		
		chapters_node.selectedIndex=this.chapter_id;
		
		struct.attachEvent(this.old_chapters_node,"change",function(){
			$this.go($this.old_chapters_node);
		});
		
		struct.attachEvent(this.new_chapters_node,"change",function(){
			$this.go($this.new_chapters_node);
		});
	};
	
	this.go=function ()
	{
		var $this=arguments[0];
		this.chapter_id=$this.options[$this.selectedIndex].value;
		location.href="?action=list&chapterid="+this.chapter_id+"&bookid="+this.book_id+"&clsid=3";
	};
	
	this.show=function()
	{
		var $this=this;

		new Ajax.Request(
			this.url,
			{
				onSuccess:function(transport){
					$this.resources=transport.responseXML;
					var list = $this.resources.getElementsByTagName("item");
					$this.write();
				},
				onFailure:function(transport)
				{
					alert("There are some problems with the remote server when loading the Examination!");
				}
			});
	};
	
	this.write=function()
	{
		var doc = this.resources;
		
		var list = doc.getElementsByTagName("item");
		var count = list.length;
		var left_column = "",right_column="";
		
		if((list!=null)&&(count>0))
		{
			left_column+="<ol>";
			right_column+="<ol>";
			
			var n=parseInt(count/2);
			for(var i=0;i< n;i++)
			{
				var item = list[i],item1=list[i+n];
                   
				line = item.childNodes[0].nodeValue;
				if(i==0)line="<span class='firstletter'>"+line.substring(0,1)+"</span>"+line.substring(1,line.length);
				left_column+="<li"+(this.article_id==item.getAttribute('id')?" class=\"selected\"":"")+"><span class=\"sup\" onmousedown=\"rightMenu.show(event,"+item.getAttribute('uid')+")\">"+item.getAttribute('id')+"</span>"+line+"</li>";
				
				line1 = item1.childNodes[0].nodeValue;
				right_column+="<li"+(this.article_id==item1.getAttribute('id')?" class=\"selected\"":"")+"><span class=\"sup\" onmousedown=\"rightMenu.show(event,"+item1.getAttribute('uid')+")\">"+item1.getAttribute('id')+"</span>"+line1+"</li>";
			}
			
			if((i+n)<count)right_column+="<li"+(this.article_id==list[i+n].getAttribute('id')?" class=\"selected\"":"")+"><span class=\"sup\" onmousedown=\"rightMenu.show(event,"+list[i+n].getAttribute('uid')+")\">"+list[i+n].getAttribute('id')+"</span>"+list[i+n].childNodes[0].nodeValue+"</li>";
			left_column+="</ol>";
			right_column+="</ol>";
		}
		else
		{
		    left_column+="None";
		}
		
		$("column1").update(left_column);
		$("column2").update(right_column);
	};

}
