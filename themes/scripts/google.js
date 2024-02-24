window.listener = {};
( function() {
	var $window = window, $document = document, r = $window.listener, currentLanguage, $registered, x, t, input, fa, ha = "", I = null, A = null, v = null, E = -1, $form, textbox, p, w, C = null, q = null, tempField, J, arr = {}, l = null, $url, K, ItemSize = 0, L = 0, Q = 0, Timer = null, lang, G = false, S = false, X = navigator.userAgent
			.toLowerCase(), ja = X.indexOf("opera") != -1, Y = X
			.indexOf("msie") != -1
			&& !ja, ka = false;
	{
		var Z = / applewebkit\/(\d+)/.exec(X);
		if (Z)
			ka = Z.length > 1 && Z[1] < 500
	}

	var T = null;
	r.InstallAC = function(a, b, c, e, d, g, i, j, D) {
		currentLanguage = d || "en";
		r.install(a, b, e, D, null)
	};

	r.install = function(a, b, $search, e, d) {
		$form = a;
		textbox = b;
		$search = $search || "search";
		$registered = e;
		T = d;
		x = request() == null && !$registered;
		currentLanguage = currentLanguage || "zh-CN";
		lang = /^(zh-(CN|TW)|ja|ko)$/.test(currentLanguage);

		K = $search;
		$url = K + "?hl=" + ua(currentLanguage) + "&client=suggest";

		if (x) {
			setCookie("qu", "", 0, K, null, null)
		}

		$form.onsubmit = ta;
		textbox.autocomplete = "off";
		struct.attachEvent(textbox, "blur", ya);
		struct.attachEvent(textbox, "beforedeactivate", za);
		struct.attachEvent(textbox, Y ? "keydown" : "keypress", ma);
		struct.attachEvent(textbox, "keyup", na);

		input = (fa = (t = textbox.value));
		p = document.createElement("table");
		p.setAttribute("id", "completeTable");
		p.id = "completeTable";
		p.cellSpacing = (p.cellPadding = "0");
		w = p.style;
		setClassName(p, "mAutoComplete");
		H();
		$setAttribute($document.body, p);

		if (Y) {
			C = document.createElement("iframe");
			q = C.style;
			C.id = "completeIFrame";
			q.zIndex = "1";
			q.position = "absolute";
			q.display = "block";
			q.borderWidth = 0;
			$setAttribute($document.body, C);
		}
		U();
		Add("", [], []);
		xa();
		if (x) {
			var g = document.createElement("div"), i = g.style;
			i.visibility = "hidden";
			i.position = "absolute";
			i.left = "0";
			i.top = "-10000";
			i.width = (i.height = "0");
			var j = document.createElement("iframe");
			j.name = "completionFrame";
			j.id = "completionFrame";
			j.src = "http://www.google.com" + $url;
			$setAttribute(g, j);
			$setAttribute($document.body, g)
		}
		//processEvent($window,"resize",function(){U()});
		struct.attachEvent($window, "resize", function() {
			U()
		});
		if (lang) {
			$window.setInterval(Fa, 10)
		}
		tempField = $form_appendChild("aq");
		J = $form_appendChild("oq");
		ca()
	};

	function xa() {
		var a = $document.body.dir == "rtl", b = a ? "right" : "left", c = a ? "left"
				: "right", e = $document.getElementsByTagName("head")[0], d = function(
				g, i) {
			if (!$document.styleSheets || ka) {
				var j = document.createElement("style"), D = $document.createTextNode(g
						+ " { " + i + " }");
				$setAttribute(j, D);
				$setAttribute(e, j)
			} else {
				if ($document.styleSheets.length == 0) {
					var j = document.createElement("style");
					$setAttribute(e, j)
				}

				var z = $document.styleSheets[0];
				if (z.insertRule) {
					z.insertRule(g + " { " + i + " }", z.cssRules.length)
				} else if (z.addRule) {
					z.addRule(g, i)
				}
			}
		};

		d(
				".mAutoComplete",
				"font-size:12px;font-family:arial,sans-serif;cursor:default;line-height:17px;border:1px solid #bdc7d8;z-index:99;position:absolute;background-color:white;margin:0;");
		d(".aAutoComplete", "background-color:white;");
		d(".bAutoComplete", "background-color:#3366cc;color:white;");
		d(".cAutoComplete", "white-space:nowrap;overflow:hidden;text-align:"
				+ b + ";padding-" + b + ":3px;"
				+ (ja ? "padding-bottom:1px;" : ""));
		d(".dAutoComplete",
				"white-space:nowrap;overflow:hidden;font-size:12px;text-align:"
						+ c + ";color:green;padding-" + b + ":3px;padding-" + c
						+ ":3px;");
		d(".bAutoComplete td", "border:0px solid #000000;color:white;");
		d(".eAutoComplete td",
				"border:0px solid #000000;padding:0 3px 2px;text-decoration:none;text-align:"
						+ c + ";font-size:12px;line-height:15px;");
		d(".eAutoComplete span",
				"color:blue;text-decoration:none;cursor:pointer;")
	}

	function U() {
		if (p) {
			w.left = getPosition(textbox)[0] + "px";
			w.top = getPosition(textbox)[1] + textbox.offsetHeight - 1 + "px";
			w.width = textbox.offsetWidth + "px";
			if (C) {
				q.left = w.left;
				q.top = w.top;
				q.width = w.width;
				q.height = p.offsetHeight + "px"
			}

		}
	}

	//var appendTimer=null;
	function $setAttribute(a, b) {
		a.appendChild(b);
	}

	function $form_appendChild(a) {
		var b = document.createElement("input");
		b.type = "hidden";
		b.name = a;
		b.value = null;
		b.disabled = true;
		$setAttribute($form, b);
		return b
	}

	function ya(a) {
		if (!G) {
			H()
		}
		G = false
	}

	function za(a) {
		if (G) {
			$window.event.cancelBubble = true;
			$window.event.returnValue = false
		}

		G = false
	}

	function ma(a) {
		var b = a.keyCode;

		if (!key(b)) {
			return true
		}

		Q++;
		if (Q % 3 == 1)
			$(b);

		return false
	}

	function na(a) {
		var b = a.keyCode;
		if (!(lang && key(b)) && Q == 0) {
			$(b)
		}
		Q = 0;
		return false
	}

	function $(a) {
		if (lang && key(a))
			Ga();

		if (textbox.value != t || a == 39) {
			input = textbox.value;
			if (a != 39)
				J.value = input
		}

		if (down_key(a)) {
			ra(E + 1)
		} else if (up_key(a)) {
			ra(E - 1)
		}

		U();
		if (ha != input && !Timer) {
			Timer = $window.setTimeout(H, 500)
		}

		t = textbox.value;
		if (t == "" && !I)
			ca()
	}

	function up_key(a) {
		return a == 38 || a == 63232
	}

	function down_key(a) {
		return a == 40 || a == 63233
	}

	function key(a) {
		return up_key(a) || down_key(a)
	}

	function Aa() {
		textbox.blur();
		tempField.value = "t";
		W(this.completeString);
		if (ta()){
			$form.submit()
		}
			
	}

	function qa() {
		if (S)
			return;
		if (v)
			setClassName(v, "aAutoComplete");
		setClassName(this, "bAutoComplete");
		v = this;
		for ( var a = 0; a < A.length; a++) {
			if (A[a] == v) {
				E = a;
				break
			}
		}
	}

	function Ba() {
		if (S) {
			S = false;
			qa.call(this)
		}
	}

	function ra(a) {
		if (input != ha || !I)
			return;
		tempField.value = "t";
		if (!A || A.length <= 0)
			return;

		var b = A.length;
		if (T)
			b -= 1;
		ba();
		if (v)
			setClassName(v, "aAutoComplete");
		if (a == b || a == -1) {
			E = -1;
			W(input);
			focus();
			return

		} else if (a > b) {
			a = 0
		} else if (a < -1) {
			a = b - 1
		}

		E = a;
		v = A.item(a);
		setClassName(v, "bAutoComplete");
		W(v.completeString)
	}

	function H() {
		if (Timer) {
			$window.clearTimeout(Timer);
			Timer = null
		}

		w.visibility = "hidden";
		if (C)
			q.visibility = "hidden";
	}

	function ba() {
		w.visibility = "visible";
		if (C)
			q.visibility = "visible";
		U();
		S = true
	}

	r.Suggest_apply = function(a, b, c) {
		if (c.length == 0 || c[0] < 2)
			return;
		var e = [], d = [], g = c[0], i = Math.floor((c.length - 1) / g);
		for ( var j = 0; j < i; j++) {
			e.push(c[j * g + 1]);
			d.push(c[j * g + 2])
		}

		V(a, b, e, d)
	};

	function V(a, b, c, e) {
		if (ItemSize > 0)
			ItemSize--;
		Add(b, c, e);
		if (b != input)
			return;

		if (Timer) {
			$window.clearTimeout(Timer);
			Timer = null
		}

		ha = b;
		Da(p, c, e);
		E = -1;
		A = p.rows;
		if (A.length > 0) {
			ba()
		} else {
			H()
		}
	}

	r.sendRPCDone = V;
	r.jsonRPCDone = function(a) {
		var b;
		a.unshift(b);
		if (a.length >= 3) {
			if (a.length < 4)
				a.push( [])
		}

		V.apply(null, a)
	};

	function Add(a, b, c) {
		arr[a] = [ b, c ]
	}

	function ta() {
		if (x) {
			setCookie("qu", "", 0, K, null, null)
		}

		H();
		tempField.disabled = (J.disabled = true);
		if (J.value != textbox.value) {
			tempField.value = "t";
			tempField.disabled = false;
			J.disabled = false
		} else if (tempField.value) {
			tempField.disabled = false
		} else if (L >= 3 || ItemSize >= 10) {
			tempField.value = "input";
			tempField.disabled = false
		}
		
		$form.action=$form.action+"/"+textbox.value;

		return true
	}

	function ca() {
		if (L >= 3)
			return false;
		if (fa != input) {
			var a = input, b = arr[input];
			if (b) {
				V(null, input, b[0], b[1])
			} else {
				ItemSize++;
				if ($registered) {
					var c = document.createElement("script");
					c.setAttribute("type", "text/javascript");
					c.setAttribute("charset", "utf-8");
					c.setAttribute("id", "jsonpACScriptTag");
					c.setAttribute("src", "http://suggestqueries.google.com"
							+ $url
							+ "&json=t&jsonp=window.google.ac.jsonRPCDone&hl="
							+ currentLanguage + "&q=" + a);
					alert("http://suggestqueries.google.com" + $url
							+ "&json=t&jsonp=window.google.ac.jsonRPCDone&hl="
							+ currentLanguage + "&q=" + a);
					var e = $document.getElementById("jsonpACScriptTag"), d = $document
							.getElementsByTagName("head")[0];
					if (e)
						d.removeChild(e);
					$setAttribute(d, c)
				} else if (x) {
					setCookie("qu", a, null, K, null, null);
					$window.frames.completionFrame.document.location
							.reload(true)
				} else {
					process(a)
				}
			}
			focus()
		}

		fa = input;
		var g = 100;
		for ( var i = 1; i <= (ItemSize - 2) / 2; ++i) {
			g *= 2
		}
		g += 50;
		I = $window.setTimeout(ca, g);
		return true
	}

	function ua(a) {
		if (encodeURIComponent)
			return encodeURIComponent(a);
		if (escape)
			return escape(a)
	}

	function W(a) {
		textbox.value = a;
		t = a
	}

	r.setFieldValue = W;
	function focus() {
		textbox.focus()
	}

	function getPosition($element) {
		var position = new Array();
		position[0] = 0;
		position[1] = 0;
		while ($element) {
			position[0] += $element["offsetLeft"];
			position[1] += $element["offsetTop"];

			$element = $element.offsetParent;
		}

		return position;
	}

	function setCookie(a, b, c, e, d, g) {
		$document.cookie = a + "=" + b
				+ (c ? "; expires=" + c.toGMTString() : "")
				+ (e ? "; path=" + e : "") + (d ? "; domain=" + d : "")
				+ (g ? "; secure" : "")
	}

	function setClassName(a, b) {
		a.className = b
	}

	function Ca(a) {
		var b = new RegExp(
				"^[\\s\\u1100-\\u11FF\\u3040-\\u30FF\\u3130-\\u318F\\u31F0-\\u31FF\\u3400-\\u4DBF\\u4E00-\\u9FFF\\uAC00-\\uD7A3\\uF900-\\uFAFF\\uFF65-\\uFFDC]+$");
		return b.test(a)
	}

	function Da(a, b, c) {
		while (a.rows.length > 0)
			a.deleteRow(-1);
		for ( var e = 0; e < b.length; ++e) {
			var d = a.insertRow(-1);
			d.onmousedown = Aa;
			d.onmouseover = qa;
			d.onmousemove = Ba;
			d.completeString = b[e];
			setClassName(d, "aAutoComplete");
			var g = document.createElement("td");
			g.innerHTML = b[e];
			setClassName(g, "cAutoComplete");
			if (Y && Ca(b[e])) {
				g.style.paddingTop = "2px"
			}
			$setAttribute(d, g);
			var i = document.createElement("td");
			i.innerHTML = c[e];
			setClassName(i, "dAutoComplete");
			$setAttribute(d, i)
		}

		if (T && b.length > 0) {
			var j = a.insertRow(-1);
			j.onmousedown = function(ga) {
				if (ga && ga.stopPropagation) {
					ga.stopPropagation();
					ba();
					textbox.focus()
				} else {
					G = true
				}
				return false
			};
			var D = document.createElement("td");
			D.colSpan = 2;
			setClassName(j, "eAutoComplete");
			var z = document.createElement("span");
			$setAttribute(j, D);
			$setAttribute(D, z);
			z.innerHTML = T;
			z.onclick = function() {
				H();
				$window.clearTimeout(I);
				I = null;
				tempField.value = "x"
			}
		}
	}

	function request() {
		try {
			if (window.XMLHttpRequest) {
				return new XMLHttpRequest();
			} else if (window.ActiveXObject) {
				return new ActiveXObject('MSXML2.XMLHTTP')
						|| new ActiveXObject('Microsoft.XMLHTTP');
			} else
				return false;
		} catch (e) {
			return false;
		}
	}
	;

	function process(keyword) {
		if (l && l.readyState != 0 && l.readyState != 4) {
			l.abort()
		}

		l = request();

		if (l) {
			//$document.location.href="keyword.jsp?keyword="+(keyword);
			var location = 'https:' == document.location.protocol ? 'https://www.ingod.today' : '');
			l.open("POST", location + "/?q=bible/keywords", true);
			l.setRequestHeader("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");

			l.onreadystatechange = function() {
				if (l.readyState == 4 && l.responseText) {
					switch (l.status) {
					case 403:
						L = 1000;
						break;
					case 302:
					case 500:
					case 502:
					case 503:
						L++;
						break;
					case 200:
						var b = l.responseText;

						if (b.charAt(0) != "<"
								&& (b.indexOf("sendRPCDone") != -1 || b
										.indexOf("Suggest_apply") != -1)) {
							if (b.match(/^\s*(sendRPCDone|Suggest_apply)/)) {
								b = "window.listener." + b.replace(/^\s+/, "")
							}

							eval(b);
						} else {
							ItemSize--
						}

					default:
						L = 0;
						break
					}
				}
			};

			l.send("keyword=" + keyword)
		}
	}

	function Fa() {
		var a = textbox.value;
		if (a != t) {
			$(0)
		}

		t = a
	}

	function Ga() {
		G = true;
		textbox.blur();
		$window.setTimeout(focus, 10)
	}
})();