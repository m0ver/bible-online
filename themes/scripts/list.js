var ListBar = function() {
		this.items = document.createElement("div");
		this.items.setAttribute("style","width:100%;");
		this.appendItem = function() {
			var a = document.createElement("a");
			a.setAttribute("href", arguments[0].href);
			a.setAttribute("style","float:left;display:block;font-size:14px;font-weight:normal;line-height:30px;padding:0 10px;");
			a.innerHTML=(arguments[0].text);

			this.items.appendChild(a);
		};
		this.show = function() {
			return this.items;
		}
	};
var List = function() {
	var $this = this;
	this.items = null;
	this.item_list = new Array();
	if (typeof (arguments[0]) != 'undefined') {
		this.parentNode = typeof (arguments[0].container) != 'undefined' ? arguments[0].container
				: document.createElement("div");
	}

	this.container = document.createElement("div");
	this.header = null;

	this.navigator = document.createElement("div");

	this.page_next_button = document.createElement("a");
	this.page_next_button.innerHTML=("下一页");
	this.page_next_button.setAttribute("href", "javascript:void(0)");
	this.page_next_button.setAttribute("style","font-size:12px;padding:3px");

	this.page_back_button = document.createElement("a");
	this.page_back_button.innerHTML=("上一页");
	this.page_back_button.setAttribute("href", "javascript:void(0)");
	this.page_back_button.setAttribute("style","font-size:12px;padding:3px");

	this.page_number_list = document.createElement("ul");
	this.page_number_list.setAttribute("class", "page-navigator");

	struct.attachEvent(this.page_next_button,"click", function(event) {
		$this.controller.next();
	});

	struct.attachEvent(this.page_back_button,"click", function(event) {
		$this.controller.previous();
	});

	this.navigator.appendChild(this.page_back_button);
	this.navigator.appendChild(this.page_next_button);

	this.controller = {
		settings : {
			page_size : 10,
			page_amount : 0,
			start : 0,
			current_index : 1,
			end : 0,
			pointer : $this
		},
		setSize : function() {
			this.settings.page_size = arguments[0];
		},
		next : function() {
			if (this.settings.start + this.settings.page_size < this.settings.end) {
				this.settings.start = this.settings.start + this.settings.page_size;
				this.select();
			}
		},
		previous : function() {
			if (this.settings.start - this.settings.page_size >= 0) {
				this.settings.start = this.settings.start
						- this.settings.page_size;
				this.select();
			}
		},
		select : function() {

			var $this = this.settings.pointer;

			if (typeof (arguments[0]) != 'undefined' && !isNaN(arguments[0])) {
				this.settings.current_index = arguments[0];
				this.settings.start = (this.settings.current_index - 1)
						* this.settings.page_size;
			} else {
				this.settings.current_index = this.settings.start
						/ this.settings.page_size + 1;
			}

			if (this.settings.page_amount == 0) {
				if (this.settings.end % this.settings.page_size > 0 ) {
					this.settings.page_amount = (this.settings.end - this.settings.end
							% this.settings.page_size)
							/ this.settings.page_size + 1;

					var page_number_selector_item = document.createElement("li");
					var page_number_button = document.createElement("a");
					page_number_button.innerHTML=(this.settings.page_amount);
					page_number_button.setAttribute("href",
							"javascript:void(0)");
					page_number_button.setAttribute("style","font-size:12px;");

					struct.attachEvent(page_number_button,"click", function() {
						$this.controller.select($this.controller.settings.page_amount);
					});

					page_number_selector_item.appendChild(page_number_button);
					$this.page_number_list.appendChild(page_number_selector_item);
				} else {
					this.settings.page_amount = this.settings.end % this.settings.page_size / this.settings.page_size;
				}
			}

			for ( var i = 0; i < $this.page_number_list.childNodes.length; i++) {
				if (i + 1 == this.settings.current_index) {
					$this.page_number_list.childNodes[i].setAttribute("class",
							"selected");
				} else {
					$this.page_number_list.childNodes[i].setAttribute("class",
							"");
				}
			}

			var size = this.settings.start + this.settings.page_size;
			if (size > this.settings.end)
				size = this.settings.end;

			$this.items = document.createElement("ul");
			$this.items.setAttribute("id", "listBox");
			$this.items.setAttribute("style", "width:100%;");

			for ( var i = this.settings.start; i < size; i++) {
				$this.items.appendChild($this.item_list[i]);
			}

			$this.update();
		}
	};
	this.setHeader = function() {
		this.header = arguments[0];
	};
	this.appendItem = function() {
		var item = arguments[0].item;
		var controlbar = arguments[0].controlbar;
		controlbar.setAttribute("item-id","item-"+this.item_list.length+"-"+item.getAttribute("id"));
		controlbar.style.display="none";

		var info = document.createElement("a");
		info.setAttribute("href", "javascript:void(0)");
		info.setAttribute("style", "font-size:12px");
		info.innerHTML='';
		info.appendChild(item);
		info.appendChild(controlbar);
		struct.attachEvent(info,"focus", function() {
			this.style.background = "#E8F2FE";
			controlbar.style.display="block";
		});

		struct.attachEvent(info,"blur", function() {
			this.style.background = "";
			controlbar.style.display="none";
		});

		var _item = document.createElement("li");
		_item.setAttribute("id", "item-"+this.item_list.length+"-"+item.getAttribute("id"));
		_item.setAttribute("style", "clear:both;width:100%;text-align:left;");
		_item.appendChild(info);
		
		this.item_list.push(_item);

		this.controller.settings.end = this.item_list.length;

		if (this.item_list.length % this.controller.settings.page_size == 0 ) {
			var page_number_selector_item = document.createElement("li");
			var page_number = this.item_list.length
					/ this.controller.settings.page_size;

			var page_number_button = document.createElement("a");
			page_number_button.innerHTML=(page_number);
			page_number_button.setAttribute("href", "javascript:void(0)");
			page_number_button.setAttribute("style","font-size:12px;");

			struct.attachEvent(page_number_button,"click", function() {
				$this.controller.select(page_number);
			});

			page_number_selector_item.appendChild(page_number_button);
			this.page_number_list.appendChild(page_number_selector_item);
		}
	};
	this.removeItem = function() {
		var item = arguments[0];
		
		for(var i=0;i<this.item_list.length;i++)
		{
			if(this.item_list[i].getAttribute("id")==item.getAttribute("id"))
			{
				//alert(this.item_list[i].getAttribute("id"));
				//this.item_list.remove(this.item_list[i]);
			}
		}
		//alert(this.item_list.length);
		this.items.removeChild(item);
	};
	this.show = function() {
		if (this.header)
			this.container.appendChild(this.header);

		this.list = document.createElement("div");
		this.list.setAttribute("id", "list");

		this.list.appendChild(this.items);
		this.container.appendChild(this.list);

		return this.container;
	};
	this.update = function() {
		this.container = document.createElement("div");
		if (this.header)
			this.container.appendChild(this.header);

		this.list = document.createElement("div");
		this.list.setAttribute("id", "list");
		this.list.appendChild(this.items);

		this.container.appendChild(this.list);
		this.container.appendChild(this.navigator);
		this.container.appendChild(this.page_number_list);

		this.parentNode.innerHTML='';
		this.parentNode.appendChild(this.container);
	}
};