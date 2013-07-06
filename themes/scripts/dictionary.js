/**
 * @author Dongxu Huang
 * @date   2010-2-21
 */

var Options =
{
    "dict_disable": ["checked", false],
    "ctrl_only": ["checked", false],
    "english_only": ["checked", false]
};
function close()
{
	window.self.close();
}
var retphrase = "";
var basetrans = "";
var webtrans = "";
var noBaseTrans = false;
var noWebTrans = false;
function isChinese(temp) 
{ 
	var re = /[^\u4e00-\u9fa5]/; 
	if(re.test(temp)) return false; 
	return true; 
}
function isJapanese(temp) 
{ 
	var re = /[^\u0800-\u4e00]/; 
	if(re.test(temp)) return false; 
	return true; 
}
function isKoera(str) {
	for(i=0; i<str.length; i++) {
	if(((str.charCodeAt(i) > 0x3130 && str.charCodeAt(i) < 0x318F) || (str.charCodeAt(i) >= 0xAC00 && str.charCodeAt(i) <= 0xD7A3))) {
		return true;
		}
	}
	return false;
}
function isContainKoera(temp)
{
	var cnt = 0;
	for(var i=0;i < temp.length ; i++)
	{
		if(isKoera(temp.charAt(i)))
			cnt++;
	}
	if (cnt > 0) return true;
	return false;
}

function isContainChinese(temp)
{
	var cnt = 0;
	for(var i=0;i < temp.length ; i++)
	{
		if(isChinese(temp.charAt(i)))
			cnt++;
	}
	if (cnt > 5) return true;
	return false;
}
function isContainJapanese(temp)
{
	var cnt = 0;
	for(var i=0;i < temp.length ; i++)
	{
		if(isJapanese(temp.charAt(i)))
			cnt++;
	}
	if (cnt > 2) return true;
	return false;
}

function wordsXML(xmlnode){

	var items = xmlnode.getElementsByTagName("item");
	var html="<ul>";
	for(i=0;i<items.length;i++) {
		html+="<li><a href=\"javascript:void(0)\">"+items[i].getAttribute("word")+"</a></span>";
		if(items[i].getAttribute("phoneticSymbol")!=null && items[i].getAttribute("phoneticSymbol")!='')
		html+="  <span>["+items[i].getAttribute("phoneticSymbol")+"]</span> ";
		html+="<span class=\"intepretation\">"+items[i].childNodes[0].nodeValue.replace(/[\n]/g,'<br />')+"</span></li>";
	}
	html+="</ul>";
	return html;
}

var langType = '';
function translateXML(xmlnode){
	var translate = "<strong>查询:</strong><br/>";
	var root = xmlnode.getElementsByTagName("yodaodict")[0];
	
	if ("" + root.getElementsByTagName("return-phrase")[0].childNodes[0] != "undefined") 
		retphrase = root.getElementsByTagName("return-phrase")[0].childNodes[0].nodeValue;
	
	
	
	if ("" + root.getElementsByTagName("lang")[0]  != "undefined") {
		langType = root.getElementsByTagName("lang")[0].childNodes[0].nodeValue;
	}
	var strpho = "";
 
	if (""+ root.getElementsByTagName("phonetic-symbol")[0] != "undefined" ) {
		if(""+ root.getElementsByTagName("phonetic-symbol")[0].childNodes[0] != "undefined")
			var pho = root.getElementsByTagName("phonetic-symbol")[0].childNodes[0].nodeValue;
		
		if (pho != null) {
			strpho = "&nbsp;[" + pho + "]";
		}
	}
	
	if (""+ root.getElementsByTagName("translation")[0] == "undefined")
	{
		 noBaseTrans = true;
	}
	if (""+ root.getElementsByTagName("web-translation")[0] == "undefined")
	{
		 noWebTrans = true;
	}
	
	
	if (noBaseTrans == false) {
		translate += retphrase + "<br/><br/><strong>基本释义:</strong><br/>";
		
		if ("" + root.getElementsByTagName("translation")[0].childNodes[0] != "undefined") 
			var translations = root.getElementsByTagName("translation");
		else {
			basetrans += '未找到基本释义';
		}
		
		for (var i = 0; i < translations.length; i++) {
			var line = translations[i].getElementsByTagName("content")[0].childNodes[0].nodeValue + "<br/>";
			if (line.length > 50) {
				var reg = /[;；]/;
				var childs = line.split(reg);
				line = '';
				for (var i = 0; i < childs.length; i++) 
					line += childs[i] + "<br/>";
			}
			basetrans += line;
			
		}
	}
	if (noWebTrans == false) {
		if ("" + root.getElementsByTagName("web-translation")[0].childNodes[0] != "undefined") 
			var webtranslations = root.getElementsByTagName("web-translation");
		else {
			webtrans += '未找到网络释义';
		}
		
		for (var i = 0; i < webtranslations.length; i++) {
			webtrans += webtranslations[i].getElementsByTagName("key")[0].childNodes[0].nodeValue + ":  ";
			webtrans += webtranslations[i].getElementsByTagName("trans")[0].getElementsByTagName("value")[0].childNodes[0].nodeValue + "<br/>";
		}
	}
	mainFrameQuery();
	return ;
}
var _word;

function mainQuery(word,callback) {
		var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function(data) {
          if (xhr.readyState == 4) {
            if (xhr.status == 200) {
              var dataText = translateXML(xhr.responseXML);
			  if(dataText != null)
              	callback(dataText);
            }
          }
        }
		_word = word;
        var url = 'http://dict.youdao.com/fsearch?client=deskdict&keyfrom=chrome.extension&q='+encodeURIComponent(word)+'&pos=-1&doctype=xml&xmlVersion=3.2&dogVersion=1.0&vendor=unknown&appVer=3.1.17.4208&le=eng'
		xhr.open('GET', url, true);
        xhr.send();
}
function removeDiv(divname)
{
	var div=document.getElementById(divname);
	if(div == null) return;
	div.parentNode.removeChild(div);
}
function mainFrameQuery(){
	removeDiv('opt_text');
	removeDiv('opt_text');
	removeDiv('opt_text');
	removeDiv('opt_text');
	var lan = '';
	if(isContainKoera(_word))
	{
		lan = "&le=ko";
	}
	if(isContainJapanese(_word))
	{
		lan = "&le=jap";
	}
	if(langType == 'fr')
	{
		lan = "&le=fr";
	}
	var res = document.getElementById('result');
	res.innerHTML = '';
	if (noBaseTrans == false) {
		if(langType=='ko')
			basetrans = "<strong>韩汉翻译:</strong><br/>" + basetrans;
		else if (langType == 'jap')
			basetrans = "<strong>日汉翻译:</strong><br/>" + basetrans;
		else if (langType == 'fr')
			basetrans = "<strong>法汉翻译:</strong><br/>" + basetrans;
		else basetrans = "<strong>英汉翻译:</strong><br/>" + basetrans;
    	res.innerHTML = basetrans;
	}
	if (noWebTrans == false) {
		webtrans = "<strong>网络释义:</strong><br/>" + webtrans;
		res.innerHTML += webtrans;
	}
	if(noBaseTrans == false || noWebTrans == false)
	{
		res.innerHTML +="<a href ='http://dict.youdao.com/search?q="+encodeURIComponent(_word)+"&ue=utf8&keyfrom=chrome.extension"+lan+"' target=_blank>点击 查看详细释义</a>";
	}
	if(noBaseTrans && noWebTrans)
	{
		res.innerHTML = "未找到英汉翻译!";
		res.innerHTML +="<br><a href ='http://www.youdao.com/search?q="+encodeURIComponent(_word)+"&ue=utf8&keyfrom=chrome.extension' target=_blank>尝试用有道搜索</a>";
	}
	retphrase='';
	webtrans = '';
	basetrans = '';
	_word ='';
	langType='';
	noBaseTrans = false;
	noWebTrans = false;
	document.getElementsByName('word').focus();
}
function save_options()
{
	changeIcon();
	for (key in Options)
    {
        if (Options[key][0] == "checked")
        {
            Options[key][1] = document.getElementById(key).checked;
        }
    }
	localStorage["ColorOptions"] = JSON.stringify(Options);
}
function goFeedback()
{
	window.open("http://feedback.youdao.com/deskapp_report.jsp?prodtype=deskdict&ver=chrome.extension");
}
function goAbout()
{
	window.open("http://cidian.youdao.com/chromeplus");
}
function initIcon()
{

}
function changeIcon()
{
	
	if (document.getElementById('dict_disable').checked) {
		
		var a = document.getElementById('ctrl_only');
		a.disabled = true;
		
		a = document.getElementById('english_only');
		a.disabled = true;
		
		chrome.browserAction.setIcon({
			path: "icon_nodict.gif"
		})
	}
	else {
		var a = document.getElementById('ctrl_only');
		a.disabled = false;
		
		a = document.getElementById('english_only');
		a.disabled = false;
		
		chrome.browserAction.setIcon({
			path: "icon_dict.gif"
		})
	}
}

function check()
{
   var word = document.getElementsByName("word")[0].value;
   window.open("http://dict.youdao.com/search?q="+encodeURI(word)+"&ue=utf8&keyfrom=chrome.index");
}
function restore_options()
{
    var localOptions = JSON.parse(localStorage["ColorOptions"]);
    
    for (key in localOptions)
    {
        optionValue = localOptions[key];
        if (!optionValue) return;
        var element = document.getElementById(key);
        if (element)
        {
            element.value = localOptions[key][1];
            switch (localOptions[key][0])
            {
            case "checked":
                if (localOptions[key][1]) element.checked = true;
                else element.checked = false;
                break;
            }
        }
    }
    
}

/*
 * Yodao Dict for Chrome Backgroud Page
 * @Author : Dongxu
 * @Date   : 2010-03-02
 * 
 * */

var DefaultOptions =
{
    "dict_disable": ["checked", false],
    "ctrl_only": ["checked", false],
    "english_only": ["checked", true]
};

var DictTranslate = {
	"return-phrase": "",
	"lang":"",
	"translation": []
}

initIcon();
  
sprintfWrapper = {

  init : function () {

    if (typeof arguments == "undefined") { return null; }
    if (arguments.length < 1) { return null; }
    if (typeof arguments[0] != "string") { return null; }
    if (typeof RegExp == "undefined") { return null; }

    var string = arguments[0];
    var exp = new RegExp(/(%([%]|(\-)?(\+|\x20)?(0)?(\d+)?(\.(\d)?)?([bcdfosxX])))/g);
    var matches = new Array();
    var strings = new Array();
    var convCount = 0;
    var stringPosStart = 0;
    var stringPosEnd = 0;
    var matchPosEnd = 0;
    var newString = '';
    var match = null;

    while (match = exp.exec(string)) {
      if (match[9]) { convCount += 1; }

      stringPosStart = matchPosEnd;
      stringPosEnd = exp.lastIndex - match[0].length;
      strings[strings.length] = string.substring(stringPosStart, stringPosEnd);

      matchPosEnd = exp.lastIndex;
      matches[matches.length] = {
        match: match[0],
        left: match[3] ? true : false,
        sign: match[4] || '',
        pad: match[5] || ' ',
        min: match[6] || 0,
        precision: match[8],
        code: match[9] || '%',
        negative: parseInt(arguments[convCount]) < 0 ? true : false,
        argument: String(arguments[convCount])
      };
    }
    strings[strings.length] = string.substring(matchPosEnd);

    if (matches.length == 0) { return string; }
    if ((arguments.length - 1) < convCount) { return null; }

    var code = null;
    var match = null;
    var i = null;

    for (i=0; i<matches.length; i++) {

      if (matches[i].code == '%') { substitution = '%' }
      else if (matches[i].code == 'b') {
        matches[i].argument = String(Math.abs(parseInt(matches[i].argument)).toString(2));
        substitution = sprintfWrapper.convert(matches[i], true);
      }
      else if (matches[i].code == 'c') {
        matches[i].argument = String(String.fromCharCode(parseInt(Math.abs(parseInt(matches[i].argument)))));
        substitution = sprintfWrapper.convert(matches[i], true);
      }
      else if (matches[i].code == 'd') {
        matches[i].argument = String(Math.abs(parseInt(matches[i].argument)));
        substitution = sprintfWrapper.convert(matches[i]);
      }
      else if (matches[i].code == 'f') {
        matches[i].argument = String(Math.abs(parseFloat(matches[i].argument)).toFixed(matches[i].precision ? matches[i].precision : 6));
        substitution = sprintfWrapper.convert(matches[i]);
      }
      else if (matches[i].code == 'o') {
        matches[i].argument = String(Math.abs(parseInt(matches[i].argument)).toString(8));
        substitution = sprintfWrapper.convert(matches[i]);
      }
      else if (matches[i].code == 's') {
        matches[i].argument = matches[i].argument.substring(0, matches[i].precision ? matches[i].precision : matches[i].argument.length)
        substitution = sprintfWrapper.convert(matches[i], true);
      }
      else if (matches[i].code == 'x') {
        matches[i].argument = String(Math.abs(parseInt(matches[i].argument)).toString(16));
        substitution = sprintfWrapper.convert(matches[i]);
      }
      else if (matches[i].code == 'X') {
        matches[i].argument = String(Math.abs(parseInt(matches[i].argument)).toString(16));
        substitution = sprintfWrapper.convert(matches[i]).toUpperCase();
      }
      else {
        substitution = matches[i].match;
      }

      newString += strings[i];
      newString += substitution;

    }
    newString += strings[i];

    return newString;

  },

  convert : function(match, nosign){
    if (nosign) {
      match.sign = '';
    } else {
      match.sign = match.negative ? '-' : match.sign;
    }
    var l = match.min - match.argument.length + 1 - match.sign.length;
    var pad = new Array(l < 0 ? 0 : l).join(match.pad);
    if (!match.left) {
      if (match.pad == "0" || nosign) {
        return match.sign + pad + match.argument;
      } else {
        return pad + match.sign + match.argument;
      }
    } else {
      if (match.pad == "0" || nosign) {
        return match.sign + match.argument + pad.replace(/0/g, ' ');
      } else {
        return match.sign + match.argument + pad;
      }
    }
  }
}

sprintf = sprintfWrapper.init;
 

 
function html_encode(str)   
{   
  var s = "";   
  if (str.length == 0) return "";   
  s = str.replace(/&/g, "&gt;");   
  s = s.replace(/</g, "&lt;");   
  s = s.replace(/>/g, "&gt;");   
  s = s.replace(/ /g, "&nbsp;");   
  s = s.replace(/\'/g, "&#39;");   
  s = s.replace(/\"/g, "&quot;");   
  s = s.replace(/\n/g, "<br>");   
  return s;   
}   

function genTable(word,strpho, baseTrans, webTrans)
{
	var lan='';
	if(isContainKoera(word))
	{
		lan = "&le=ko";
	}
	if(isContainJapanese(word))
	{
		lan = "&le=jap";
	} 
	var title = word;
	if((isContainChinese(title) || isContainJapanese(title) || isContainKoera(title)) && title.length > 15)
	{
		title = title.substring(0,10) + '...';
	}
	if(title.length > 25)
	{
		title = title.substring(0,15) + ' ...';
	}
	var fmt='';
	if (noBaseTrans && noWebTrans) {
		
		fmt = 
		'    <div class="top"><div id="topborder"><a href="http://dict.youdao.com/search?q=' +
		encodeURIComponent(word) +
		'&keyfrom=chrome.extension' +
		lan +
		'" title="查看完整释义" class="icon" target=_blank></a> <a href="http://dict.youdao.com/search?q=' +
		encodeURIComponent(word) +
		'&keyfrom=chrome.extension' +
		lan +
		'" target=_blank title="查看完整释义" class="title">' +
		html_encode(title) +
		'</a>&nbsp;<span>' +
		strpho +
		'</span><span style="float:right;"><a href="http://www.youdao.com/search?q=' +
		encodeURIComponent(word) +
		'&ue=utf8&keyfrom=chrome.extension" target=_blank>详细</a></span></div></div>' +
		'    <div id="yddMiddle">';
	}
	else {
		fmt = 
		'    <div class="top"><div class="topborder"><a href="http://dict.youdao.com/search?q=' +
		encodeURIComponent(word) +
		'&keyfrom=chrome.extension' +
		lan +
		'" title="查看完整释义" class="icon" style="padding:0px 0px 0px 0px;padding-top:17px;" target=_blank></a> <a href="http://dict.youdao.com/search?q=' +
		encodeURIComponent(word) +
		'&keyfrom=chrome.extension' +
		lan +
		'" target=_blank title="查看完整释义" class="title">' +
		title +
		'</a>&nbsp;<span>' +
		strpho +
		'&nbsp;&nbsp;</span><span id="voice" style="padding:2px;height:15px;width:15px">' +
		speach +
		'</span></div></div>' +
		'    <div class="middle">';
	}
	if (noBaseTrans == false) {
		var base=
			 '  <div class="trans-wrapper" style="display:block;padding:0px 0px 0px 0px" class="simpleTrans">' +
			 '        <div class="tabs"><span class="tab">基本翻译</span></div>' +
			 '        %s'+
			 '	</div>' ;
	    base = sprintf(base,baseTrans);
		fmt+=base;
	}
	if (noWebTrans == false) {
		var web=
			'       <div class="trans-wrapper" style="display:block;padding:0px 0px 0px 0px">' +
			'        <div class="tabs"><span class="tab">网络释义</span></div>' +
			'        %s' +
			'      </div>';
		web = sprintf(web, webTrans);
		fmt+=web;
	}
	if (noBaseTrans && noWebTrans) {
		fmt += '&nbsp;&nbsp;没有英汉互译结果<br/>&nbsp;&nbsp;<a href="http://www.youdao.com/search?q=' + encodeURIComponent(word) + '&ue=utf8&keyfrom=chrome.extension" target=_blank>请尝试网页搜索</a>';
	}
	fmt +=  '  </div>';
	 
	res = fmt;
	noBaseTrans = false;
 	noWebTrans = false;
	speach='';	
	//alert(res);
	return res;
}
var noBaseTrans = false;
var noWebTrans = false;
var speach='';	
function translateXML(xmlnode)
{
	var translate = "<strong>查询:</strong><br/>";
	var root = xmlnode.getElementsByTagName("yodaodict")[0];
	
	if(""+ root.getElementsByTagName("return-phrase")[0].childNodes[0] != "undefined" )
		var retphrase = root.getElementsByTagName("return-phrase")[0].childNodes[0].nodeValue;
	
	if(""+ root.getElementsByTagName("dictcn-speach")[0] != "undefined" )
		speach = root.getElementsByTagName("dictcn-speach")[0].childNodes[0].nodeValue;
		
	var lang = "&le=";	
	
	if(root.getElementsByTagName("lang")!=null && typeof(root.getElementsByTagName("lang")[0]) != "undefined" )
	{
		if(root.getElementsByTagName("lang")[0]!=null){
			$nodes = root.getElementsByTagName("lang")[0].childNodes;
			for(var i=0;i<$nodes.length;i++){  
		        if($nodes[i].nodeType == 1){  
		        	lang += $nodes[i].nodeValue; break;
		        }
		    }  
		}
	}
	
	var strpho = "";
	if (""+ root.getElementsByTagName("phonetic-symbol")[0] != "undefined" ) {
		
		if(""+ root.getElementsByTagName("phonetic-symbol")[0].childNodes[0] != "undefined"){
			if(root.getElementsByTagName("phonetic-symbol")[0].childNodes[0]!=null){
				pho = root.getElementsByTagName("phonetic-symbol")[0].childNodes[0].nodeValue;
			
				if (pho != null) {
					strpho = "&nbsp;[" + pho + "]";
				}
			}
		}

	}
	
	if (""+ root.getElementsByTagName("translation")[0] == "undefined")
	{
		 noBaseTrans = true;
	}
	if (""+ root.getElementsByTagName("web-translation")[0] == "undefined")
	{
		 noWebTrans = true;
	}
	
	var basetrans = "";
	var webtrans = "";
 	var translations;
	var webtranslations;
	if (noBaseTrans == false) {
		if ("" + root.getElementsByTagName("translation")[0].childNodes[0] != "undefined") {
			translations = root.getElementsByTagName("translation");
		}
		else {
			noBaseTrans = true;
		}
		var i;
		basetrans = '<ul>';
		for ( i = 0; i < translations.length - 1; i++) {
			basetrans += '<li class="trans-container">' + translations[i].getElementsByTagName("content")[0].childNodes[0].nodeValue + "</li>";
		}
		basetrans += '<li class="trans-container">' + translations[i].getElementsByTagName("content")[0].childNodes[0].nodeValue + "</li>";
		basetrans += '</ul>';
	}
	
	if (noWebTrans == false) {
		if ("" + root.getElementsByTagName("web-translation")[0].childNodes[0] != "undefined") {
			webtranslations = root.getElementsByTagName("web-translation");
		}
		else {
			noWebTrans = true;
		}
		var i;
		webtrans = '<ul>';
		for ( i = 0; i < webtranslations.length -1 ; i++) {
			webtrans += '<li class="trans-container"><a href="http://dict.youdao.com/search?q=' + encodeURIComponent(webtranslations[i].getElementsByTagName("key")[0].childNodes[0].nodeValue) + '&keyfrom=chrome.extension'+lang+'" target=_blank>' + webtranslations[i].getElementsByTagName("key")[0].childNodes[0].nodeValue + ":</a> ";
			webtrans += webtranslations[i].getElementsByTagName("trans")[0].getElementsByTagName("value")[0].childNodes[0].nodeValue + "</li>";
		}
		webtrans += '<li class="trans-container"><a href="http://dict.youdao.com/search?q=' + encodeURIComponent(webtranslations[i].getElementsByTagName("key")[0].childNodes[0].nodeValue) + '&keyfrom=chrome.extension'+lang+'" target=_blank>' + webtranslations[i].getElementsByTagName("key")[0].childNodes[0].nodeValue + ":</a> ";
		webtrans += webtranslations[i].getElementsByTagName("trans")[0].getElementsByTagName("value")[0].childNodes[0].nodeValue + "</li>";
		webtrans += '</ul>';
	}
	return genTable(retphrase,strpho,basetrans,webtrans);
	//return translate; 
}
function translateTransXML(xmlnode)
{
	var s = xmlnode.indexOf("CDATA[");
	var e = xmlnode.indexOf("]]");
	var input_str = xmlnode.substring(s+6,e);
	
	var remain = xmlnode.substring(e+2,xmlnode.length-1);
	s = remain.indexOf("CDATA[");
	e = remain.indexOf("]]");
	trans_str = remain.substring(s+6,e);
	
	trans_str_tmp = trans_str.replace(/^\s*/, "").replace(/\s*$/, "");
	input_str_tmp = input_str.replace(/^\s*/, "").replace(/\s*$/, "");
	
	if((isContainChinese(input_str_tmp) || isContainJapanese(input_str_tmp) || isContainKoera(input_str_tmp)) && input_str_tmp.length > 15)
	{
		input_str_tmp = input_str_tmp.substring(0,8) + ' ...';
	}
	else if(input_str_tmp.length > 25)
	{
		input_str_tmp = input_str_tmp.substring(0,15) + ' ...';
	}
	 
	
	if(trans_str_tmp == input_str_tmp) return null;
	
	var res = '    <div class="top"><div id="topborder"><a href="http://fanyi.youdao.com/translate?i='+encodeURIComponent(input_str)+'&keyfrom=chrome" class="icon" target=_blank"></a><span>'+input_str_tmp+'</span><span style="float:right;font-weight:normal;font-size:10px"><a href="http://fanyi.youdao.com/translate?i='+encodeURIComponent(input_str)+'&smartresult=dict&keyfrom=chrome.extension" target=_blank>详细</a></span></div></div>'+
			'    <div class="middle">'+
			'      <div class="trans-wrapper">'+
			'        <div class="trans-container">'+trans_str+'</div>'+
			'      '+
			'	</div>'+
			'  </div>';

 	 
	return res;
}

var loading=false;
function fetchWordWithoutDeskDict(e, word,callback) {
	  e = e || event;   
	  var target = e.target || e.srcElement;
	  if (target.nodeName != 'LI') return;

	  var verse_id = target.getElementsByTagName('a')[0].innerHTML;
	  var url=document.location.href;
	  var referer = url.substring(url.indexOf('/?'),url.indexOf('q=bible'));
	  
	  var params = url.substring(url.indexOf('q=bible')).split('/');
	  if(params.length==1) referer += params[0]+"/1/1/"+verse_id;
	  if(params.length==2) referer += params[0]+"/"+params[1]+"/1/"+verse_id;
	  if(params.length==3) referer += params[0]+"/"+params[1]+"/"+params[2]+"/"+verse_id;
	  if(params.length==4) referer += params[0]+"/"+params[1]+"/"+params[2]+"/"+verse_id;
	  
        var lang='';
        if(!word && !loading) return;
		if (isContainKoera(word)) {
			 lang = '&le=ko';
		}

		new struct.message().showMessage("正在加载……");

		$.ajax({url:'/?q=services/getword/'+encodeURIComponent(word),
			type:'POST',dataType: 'xml',data:{'referer': referer}
			})
			.success(function(data){
	              var dataText = translateXML(data);
	              if(dataText!=null)
				  	callback(dataText);
	              
	              loading=!loading;
			});
};
var _word;
var _callback;
var _timer;
function handleTimeout()
{
	fetchWordWithoutDeskDict(_word,_callback);
}
function isKoera(str) {
	for(i=0; i<str.length; i++) {
	if(((str.charCodeAt(i) > 0x3130 && str.charCodeAt(i) < 0x318F) || (str.charCodeAt(i) >= 0xAC00 && str.charCodeAt(i) <= 0xD7A3))) {
		return true;
		}
	}
	return false;
}
function isContainKoera(temp)
{
	var cnt = 0;
	for(var i=0;i < temp.length ; i++)
	{
		if(isKoera(temp.charAt(i)))
			cnt++;
	}
	if (cnt > 0) return true;
	return false;
}
function fetchWord(word,callback) {
		if (isContainKoera(word)) {
			fetchWordWithoutDeskDict(word,callback);
			return;
		}
        var xhr = new XMLHttpRequest();
		_word = word;
		_callback = callback;
        xhr.onreadystatechange = function(data){
			clearTimeout(_timer);
		}
		var url = 'http://127.0.0.1:8999/word='+word+'&';
		xhr.open('GET', url, true);
        xhr.send();
		_timer = setTimeout(handleTimeout,600);
};

function fetchTranslate(words,callback)
{
	new struct.message().showMessage("正在加载……");
		$.ajax({url:'/?q=services/translate/'+encodeURIComponent(words),
			type:'GET',dataType: 'xml'
			})
			.success(function(data){
	              var dataText = translateTransXML(data);
	              if(dataText!=null)
				  	callback(dataText);
			});
		
}

function _dictGetSel()
{
	if (window.getSelection) return window.getSelection();
	else if (document.getSelection) return document.getSelection();
	else if (document.selection) return document.selection.createRange().text;
	else return false;
}