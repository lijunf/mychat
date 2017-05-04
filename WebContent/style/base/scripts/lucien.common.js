/**
 * 以get方式发送ajax请求，
 * @param callback		回调函数
 * @param urladdress	请求url
 * @param isReturnData	是否有返回值
 */
function send_request(callback, urladdress, isReturnData) {  
    var xmlhttp = getXMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
        	if (xmlhttp.readyState == 4) {//readystate 为4即数据传输结束
			    try {
			    	if (xmlhttp.status == 200) {
						if (isReturnData && isReturnData == true) {
							callback(xmlhttp.responseText);
						}
					} else {
						callback("抱歉，没找到此页面:" + urladdress + "");
					}
		        } catch(e) {
		        	callback("抱歉，发送请求失败，请重试 " + e);
		        }
		   }
    };
    xmlhttp.open("get", urladdress, true);
    xmlhttp.send(null);
}

/**
 * 根据id获取dom对象
 * @param id
 * @returns
 */
function $ID(id) {
	return document.getElementById(id);
}

/**
 * 获取XMLHttpRequest对象
 * @returns
 */
function getXMLHttpRequest() {
    var xmlhttp = null;
	if (window.XMLHttpRequest) {
		try {
			xmlhttp = new XMLHttpRequest();
			xmlhttp.overrideMimeType("text/html;charset=UTF-8");//设定以UTF-8编码识别数据
		} catch (e) {}
	} else if (window.ActiveXObject) {
		try {
			xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
		} catch (e) {
			try {
				xmlhttp = new ActiveXObject("Msxml2.XMLHttp");
			} catch (e) {
				try {
					xmlhttp = new ActiveXObject("Msxml3.XMLHttp");
				} catch (e) {}
			}
		}
	}
    return xmlhttp;
}

Date.prototype.pattern = function(fmt) {        
    var o = {        
	    "M+" : this.getMonth()+1, //月份        
	    "d+" : this.getDate(), //日        
	    "h+" : this.getHours()%12 == 0 ? 12 : this.getHours()%12, //小时        
	    "H+" : this.getHours(), //小时        
	    "m+" : this.getMinutes(), //分        
	    "s+" : this.getSeconds(), //秒        
	    "q+" : Math.floor((this.getMonth()+3)/3), //季度        
	    "S" : this.getMilliseconds() //毫秒        
    };        
    var week = {        
	    "0" : "\u65e5",        
	    "1" : "\u4e00",        
	    "2" : "\u4e8c",        
	    "3" : "\u4e09",        
	    "4" : "\u56db",        
	    "5" : "\u4e94",        
	    "6" : "\u516d"       
    };        
    if (/(y+)/.test(fmt)) {        
        fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));        
    }        
    if (/(E+)/.test(fmt)) {        
        fmt=fmt.replace(RegExp.$1, ((RegExp.$1.length>1) ? (RegExp.$1.length>2 ? "\u661f\u671f" : "\u5468") : "")+week[this.getDay()+""]);        
    }        
    for (var k in o) {        
        if (new RegExp("("+ k +")").test(fmt)) {        
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));        
        }        
    }        
    return fmt;        
};

/**
 * ajax提交表单
 * @param form	表单
 * @param callback	回调函数
 */
function ajaxSubmitForm(form, callback) {
	var elements = form.elements;
	var element;
	var i;
	var postContent = "";
	for (i = 0; i < elements.length; ++i) {
		var element = elements[i];
		if (element.name) {
			if (element.type == "text" || element.type == "textarea" || element.type == "hidden") {
				postContent += encodeURIComponent(element.name) + "=" + encodeURIComponent(element.value) + "&";
			} else if (element.type == "select-one" || element.type == "select-multiple") {
				var options = element.options, j, item;
				for (j = 0; j < options.length; ++j) {
					item = options[j];
					if (item.selected) {
						postContent += encodeURIComponent(element.name) + "=" + encodeURIComponent(item.value) + "&";
						break;
					}
				}
			} else if (element.type == "checkbox" || element.type == "radio") {
				if (element.checked) {
					postContent += encodeURIComponent(element.name) + "=" + encodeURIComponent(element.value) + "&";
				}
			} else if (element.type == "file") {
				if (element.value != "") {
					postContent += encodeURIComponent(element.name) + "=" + encodeURIComponent(element.value) + "&";
				}
			} else {
				postContent += encodeURIComponent(element.name) + "=" + encodeURIComponent(element.value) + "&";
			}
		}
	}
	var url = form.action;
	if (form.method.toLowerCase() == "get") {
		$.get(url + '&' + postContent, function(data) {
			callback(data);
		}, "text");
	} else {
		$.post(url, postContent, function(data) {
			callback(data);
		}, "text");
	}
}

/**
 * 发送消息
 * @param url	消息接收地址
 * @param message	消息内容
 * @param getter	消息接收者
 * @param sender	消息发送者
 * @param type		消息类型，0普通消息、1加好友，2同意加为好友，3不同意加好友，4好友上线，5好友下线
 * @param callback	回调函数
 */
function sendMessage(url, message, getter, sender, type, callback) {
	if (typeof(WebSocket) !== "undefined") {
		var text = "{'getter':'" + getter + "','sender':'" + sender + "',content:'" + encode(message) + "','mtype':" + type + "}";
		webSocket.send(text);
	} else {
		var xmlhttp = getXMLHttpRequest();
		if (xmlhttp) {
			var data = "content=" + encodeURIComponent(encode(message)) + "&getter=" + getter + "&sender=" + sender + "&mtype=" + type;
			xmlhttp.open("post", url, true);
			xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
			xmlhttp.onreadystatechange = function() {
	        	if (xmlhttp.readyState == 4) {
			    	if (xmlhttp.status == 200) {
			    		if (callback) {
			    			callback(xmlhttp.responseText);
			    		}
			    	}
	        	}
			};
			xmlhttp.send(data);
		}
	}
}
