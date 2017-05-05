if (typeof kfPath == 'undefined') {
	kfPath = "http://127.0.0.1:8088/mychat/";
}

/**
 * 加载css文件
 * @param path
 */
function loadStyle(path) {
	var link = document.createElement('link');
	link.setAttribute('type', 'text/css');
	link.setAttribute('rel', 'stylesheet');
	link.setAttribute('href', path);
	document.getElementsByTagName("head")[0].appendChild(link);
}

loadStyle(kfPath + 'style/base/plugins/KF/kfmenu.css');
var html = "<div style='top: 186px; width: 25px;' class='ct_kfbox' id='ct_kfbox' onmouseover='javascript:show()' onmouseout='javascript:dopar=setTimeout(function(){closeed()},800)'>"
	+ "<div class='ct_kfhead'>"
	+ "	<img style='display: none;' src='"+kfPath+"style/base/plugins/KF/images/qq_11.gif' class='ct_log' id='ct_qqtitlelog' onclick=\"javascript:dolookmenu('xiao')\"/>" 
	+ "	<img style='display: none;' src='"+kfPath+"style/base/plugins/KF/images/qq_06.gif' class='ct_xiao' id='ct_qqxiao' onclick=\"javascript:dolookmenu('xiao')\"/> "
	+ "	<img src='"+kfPath+"style/base/plugins/KF/images/qq_08.gif' class='ct_close' id='qqclose' onclick=\"javascript:dolookmenu('close')\"/>"
	+ "</div>"
	+ "<div style='height: 200px;' class='ct_kfmain' id='ct_kfmain'>"
	+ "	<div style='display: none;' class='ct_kfcontent' id='ct_kfcontent'>"
	+ "		<div class='ct_kfitemtitle' onclick='showandhide(0)'>"
	+ "			<img src='"+kfPath+"style/base/plugins/KF/images/boot.jpg' style='margin: 5px auto'/>&nbsp;&nbsp;&nbsp;&nbsp;售前咨询"
	+ "		</div>"
	+ "		<div class='ct_kfitem'>"
	+ "			<ul id='ct_k0'>"
	+ "			</ul>"
	+ "		</div>"
	+ "		<div class='ct_kfitemtitle' onclick='showandhide(1)'>"
	+ "			<img src='"+kfPath+"style/base/plugins/KF/images/boot.jpg' style='margin: 5px auto'/>&nbsp;&nbsp;&nbsp;&nbsp;客户服务"
	+ "		</div>"
	+ "		<div class='ct_kfitem'>"
	+ "			<ul id='ct_k1'>"
	+ "			</ul>"
	+ "		</div>"
	+ "	</div>"
	+ "	<div style='height: 310px;' id='ct_kfmenu' class='ct_kfmenu' onclick='javascript:show()'>"
	+ "		<img src='"+kfPath+"style/base/plugins/KF/images/menu_on.gif' id='ct_kfmenuimg' onclick=\"javascript:dolookmenu('xiao')\" border='0'/>"
	+ "	</div>"
	+ "	<div class='ct_kf53button'>"
	+ "		<a href='' target='_blank'><img src='"+kfPath+"style/base/plugins/KF/images/qq2.gif' border='0'/></a>"
	+ "	</div>"
	+ "</div>"
	+ "<div class='ct_kffoot'>"
	+ "	<img src='"+kfPath+"style/base/plugins/KF/images/index_bot2_04.gif' border='0'/>"
	+ "</div>"
	+ "</div>";

var iscolse = false;
var thisSeeHeiht = ((document.documentElement.clientHeight == 0) ? document.body.clientHeight
		: document.documentElement.clientHeight);
var isotherhide = ((thisSeeHeiht > 400) ? false : true);
String.prototype.Trim = function() {
	return this.replace(/(^\s*)|(\s*$)/g, "");
}
function dolookmenu(v) {
	if (v == "close") {
		document.getElementById("ct_kfbox").style.display = "none";
	} else if (v == "xiao") {
		iscolse = true;
		closeed();
	}
}


function showandhide(ii) {
	var alli = 1;//总项数,以0开始
	for ( var c = 0; c <= alli; c++) {
		var doobj = document.getElementById("k" + c);
		doobj.style.display = "none";
	}
	if (isotherhide) {
		document.getElementById("k" + ii).style.display = "";
	} else {
		document.getElementById("k" + ii).style.display = "";
	}
}

window.onload = function() {
	var objE = document.createElement("div"); 
	objE.innerHTML = html; 
	document.getElementsByTagName("body")[0].appendChild(objE);
	objbox = document.getElementById("ct_kfbox");
	objmenuimg = document.getElementById("ct_kfmenuimg");
	objcontent = document.getElementById("ct_kfcontent");
	objboxmain = document.getElementById("ct_kfmain");
	objkfmenu = document.getElementById("ct_kfmenu");
	objkfmenu.style.height = "310px";
	objboxmain.style.height = "210px";
	kfboxh1 = kfboxh;
	initFloatTips();
	getKefu();
	setInterval(getKefu, 60000);
}

/**
 * 获取客服列表
 */
function getKefu() {
	send_request(function(data) {
		var users = eval(data);
		var html1 = "";
		var html0 = "";
		for (var i = 0; i < users.length; i++) {
			var user = users[i];
			if (user.kefuFlag == 1) {
				html1 += "<li title='发起聊天' class='QQonline'>&nbsp;&nbsp;"
					+ "	<a target='_blank' href=''>"
					+ "		<img src='" + kfPath + user.minheader + "'/>"
					+ "	</a>&nbsp;&nbsp;"
					+ "	<a target='_blank'" 
					+ (user.status == 1 ? " class='ct_red'" : "")
					+ " href='"+kfPath+"action?method=launch&username=" + user.username + "'>" + user.nickname + "</a>"
					+ "</li>";
			} else if (user.kefuFlag == 2) {
				html0 += "<li title='发起聊天' class='QQonline'>&nbsp;&nbsp;"
					+ "	<a target='_blank' href=''>"
					+ "		<img src='" + kfPath + user.minheader + "'/>"
					+ "	</a>&nbsp;&nbsp;"
					+ "	<a target='_blank'" 
					+ (user.status == 1 ? " class='ct_red'" : "")
					+ " href='"+kfPath+"action?method=launch&username=" + user.username + "'>" + user.nickname + "</a>"
					+ "</li>";
			}
		}
		document.getElementById("ct_k1").innerHTML = html1;
		document.getElementById("ct_k0").innerHTML = html0;
	}, kfPath + "action?method=kefu", true);
}

var tips;
var theTop = 186;
var old = theTop;
var prox;
var proy;
var proxc;
var proyc;
var dopar;
var dropmsg;
var movetop = 80;//设定上移多少px '180
var ismovetop = true;
var objbox;
var objmenuimg;
var objcontent;
var objboxmain;
var objkfmenu;
var kfboxh = 380;
var kfboxh1;

function initFloatTips() {
	tips = document.getElementById('ct_kfbox');
	moveTips();
}

function moveTips() {
	clearTimeout(dropmsg);
	var tt = 50;
	if (window.innerHeight) {
		pos = window.pageYOffset
	} else if (document.documentElement
			&& document.documentElement.scrollTop) {
		pos = document.documentElement.scrollTop
	} else if (document.body) {
		pos = document.body.scrollTop;
	}

	pos = pos - tips.offsetTop + theTop;
	pos = tips.offsetTop + pos / 5;
	if (pos < theTop) {
		pos = theTop;
	}
	if (pos != old) {
		tips.style.top = pos + "px";
		tt = 10;
	}
	old = pos;
	dropmsg = setTimeout(moveTips, tt);
}


function show() {
	if (iscolse)
		return;
	clearInterval(prox);
	clearInterval(proy);
	clearInterval(proxc);
	clearInterval(proyc);
	clearTimeout(dopar);

	objmenuimg.src = kfPath + "style/base/plugins/KF/images/menu_off.gif";
	prox = setInterval(function() {
		openx(objbox, 150)
	}, 10);
}
function openx(o, x) {//横起打开
	var cx = parseInt(o.offsetWidth);
	if (cx < x) {
		o.style.width = (cx + Math.ceil((x - cx) / 2)) + "px";
	} else {
		if (ismovetop) {
			var thistop = objbox.offsetTop;
			//objbox.style.top=(thistop - movetop)+"px";
			theTop = theTop - movetop;
			ismovetop = false;
			objcontent.style.display = "block";
			document.getElementById("ct_qqtitlelog").style.display = "";
			document.getElementById("ct_qqxiao").style.display = "";
		}
		clearInterval(prox);

		proy = setInterval(function() {
			openy(objboxmain, kfboxh)
		}, 10);
	}

}
function openy(o, y) {
	var cy = parseInt(o.offsetHeight);
	if (cy < y) {
		o.style.height = (cy + Math.ceil((y - cy) / 1)) + "px";
	} else {

		clearInterval(proy);

		return;
	}
}
function closeed() {
	clearInterval(prox);
	clearInterval(proy);
	clearInterval(proxc);
	clearInterval(proyc);
	clearTimeout(dopar);
	proyc = setInterval(function() {
		closey(objbox)
	}, 10);
}
function closey(o) {
	if (kfboxh1 > 210) {
		kfboxh1 = kfboxh1 - Math.ceil(kfboxh1 / 10);

		objboxmain.style.height = kfboxh1 + "px";
	} else {
		clearInterval(proyc);
		kfboxh1 = kfboxh;
		//objcontent.style.width=0;
		objcontent.style.display = "none";
		document.getElementById("ct_qqtitlelog").style.display = "none";
		document.getElementById("ct_qqxiao").style.display = "none";
		proxc = setInterval(function() {
			closex(o)
		}, 10);

	}
}
function closex(o) {
	var cx = parseInt(o.offsetWidth);
	if (cx > 25) {
		o.style.width = (cx - Math.ceil(cx / 10)) + "px";
	} else {
		if (!ismovetop) {
			var thistop = objbox.offsetTop;
			theTop = theTop + movetop;
			ismovetop = true;
		}
		clearInterval(proxc);
		objmenuimg.src = kfPath + "style/base/plugins/KF/images/menu_on.gif";
		iscolse = false;
		return;
	}
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