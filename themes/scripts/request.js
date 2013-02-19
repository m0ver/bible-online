var HttpRequest=function()
{
	this.settings: {
		onRequest: {},
		onComplete: {},
		onCancel: {},
		onSuccess: {},
		onFailure: {},
		onException: {},
		headers: {
			'X-Requested-With': 'XMLHttpRequest',
			'Accept': 'text/javascript, text/html, application/xml, text/xml, */*'
		},
		method: 'post',
		urlEncoded: true,
		encoding: 'utf-8',
	},

	this.request=false;
	if(window.XMLHttpRequest){
		this.request=new XMLHttpRequest();
		if(this.request.overrideMimeType){
			this.request.overrideMimeType("text/xml");
		}
	}
	else if(window.ActiveXObject){
		try{
			this.request=new ActiveXObject("Msxml2.XMLHTTP") || new ActiveXObject("Microsoft.XMLHTTP");
		}catch(e){
			window.alert("Can't creat XMLHttpRequest Object.");
			return false;
		}
	}
	
	this.send(){
		//url,callback,data
		if(typeof(arguments[0])=='undefined')
		{
			alert("Invalid Parameters");
			return false;
		}
		
		if(typeof(arguments[0].url)!='undefined')
		{
			this.url=arguments[0].url;
		}
		else
		{
			return false;
		}
		
		if(typeof(arguments[0].data)!='undefined'){
			this.data=arguments[0].data;
		}
		
		this.time = new Date().getTime();
		this.url += (this.url.indexOf("?") >= 0) ? "&time=" + this.time : "?time=" + this.time;
		
		if(typeof(this.data) =='undefined'){
			this.request.open("GET",this.url,true);
			this.request.send(null);
		}else{
			this.request.open('POST' , this.url, true);
			this.request.setRequestHeader("Content-Length",this.data.length);
			this.request.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
			this.request.send(data);
		}
		
		if(typeof(arguments[0].callback) == "function" ){
			this.request.onreadystatechange = function (){
				if (this.request.readyState == 1){
					
				}else if(this.request.readyState == 2){
					
				}else if(this.request.readyState == 3){
					
				}else if(this.request.readyState == 4){
					if(this.request.status == 200 || this.request.status == 304){
						callback(this.request);
					}else{
						alert("Error loading page\n" + this.request.status + ":" + this.request.statusText);
					}
				}
			}
		}
	}
};

function convert(str){
	f = new Array(/\r?\n/g, /\+/g, /\&/g);
	r = new Array('%0A', '%2B', '%26');
	for (var i = 0; i < f.length; i++){
		str = str.replace(f[i], r[i]);
	}
	return str;
}