<%@ page import="com.lucien.model.*"%>
<%@ page import="com.lucien.entity.*"%>
<%@ page import="com.lucien.util.*"%>
<%@ page import="java.util.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="ChatTag" prefix="chat"%>

<%
	request.setCharacterEncoding("utf-8");
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>欢迎进入聊天室-我是${sessionScope.username}</title>
<link href="<%=basePath %>style/base/css/lucien.common.css" rel="stylesheet" type="text/css" /> 
<link href="<%=basePath %>style/base/css/lucien.room.css" rel="stylesheet" type="text/css" /> 
<link href="<%=basePath %>style/base/css/lucien.room2.css" rel="stylesheet" type="text/css" /> 
<script src="<%=basePath %>style/base/scripts/jquery-1.8.3.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/plugins/KE/kindeditor.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/lucien.common.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/lucien.room2.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/lucien.MesBox.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/lucien.webrtc.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/lucien.Encrypt.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/plugins/KF/kfbox.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/lucien.MesTip.js" type="text/javascript"></script>
</head>
<body>
<div id="ct_tip">
	<span id="ct_tip_mes"></span>
	<font>,</font>
	<span id="ct_tip_mes2"></span>
</div>
<div id="ct_dialog" class="ct_room">
	<div class="ct_left">
		<div class="ct_fieldset">
			<div class="ct_legend">在线用户--<a href="<%=basePath %>action-user-login.html">登录</a></div>
			<div class="ct_user_search hidden">
				<input type="text" name="keyword">
				<input type="submit" value="">
			</div>
			<div class="ct_users">
				<span class='ct_user_number'>在线人数<font>1</font>个</span>
				<span class='ct_myself'>${sessionScope.username}</span>
				<ul id="ct_friendlist">
				</ul>
			</div>
		</div>
	</div>
	<div class="ct_right">
		<div class="ct_dia_header">
			<img src="style/base/images/header/online/6.png"/>
			<span class="ct_dia_username"><font>${sessionScope.username}</font>与<font>所有人</font>聊天中</span>
		</div>
		<div class="ct_dia_content">
			<div id="ct_contents">
				<chat:historyMes/>
			</div>
		</div>
		<div class="ct_dia_bar"></div>
		<div class="ct_dia_message">
			<textarea id="ct_message"></textarea>
		</div>
		<div class="ct_dia_footer">
			<div class="ct_dia_btns">
				<div class="ct_dia_btns_left">Chrome浏览器中效果最佳</div>
				<span title="关闭" class="ct_dia_close_btn">关&nbsp;闭</span>
				<span title="发送" class="ct_dia_send_btn">发&nbsp;送</span>
				<span class='shortcut'>&nbsp;&darr;&nbsp;</span>
			</div>
		</div>
	</div>
</div>
<script type="text/javascript">
	function openChatItem(obj, username) {
		obj.innerHTML = username;
		if (timerArr && $('#ct_friendlist li:contains(未读消息)').length == 0) {
			$.blinkTitle.clear(timerArr);
		}
		window.open("action?method=launch&username=" + username, "_blank");
	}
	var timerArr;
	var getter = 'to_all';
	var mtype = 6;
	var loginuser = '${sessionScope.username}';
	var $contents = $("#ct_contents");
	var $userlist = $("#ct_friendlist");
	var $usernum = $('span.ct_user_number font');
	var webSocket = new WebSocketHelper("ws:<%=basePath.substring(5) %>websocket", function(m) {
		var content = m.content;
		content = decode(content);
		switch (m.mtype) {
			case 4:
				// 好友上线
				var html = "<li class='ct_friend' title='发送消息' onclick=\"openChatItem(this, '" + m.sender + "');\">" + m.sender + "</li>";
				$(html).appendTo('#ct_friendlist');
				if (audio_online) {
					audio_online.play();
				}
				break;
			case 5:
				// 好友下线
				$('#ct_friendlist li:contains(' + m.sender + ')').remove();
				if (audio_online) {
					audio_online.play();
				}
				break;
			case 6:
				// 普通群消息
				var html = "<div><span class='ct_mes_title_y'>" + m.sender + " " + m.sendTime + "</span>"
	    				+ "<div class='ct_mes_content'>" + content + "</div></div>";
	    		$contents.append(html);
	    		$contents.scrollTop($ID("ct_contents").scrollHeight);
	    		if (audio_msg) {
					audio_msg.play();
				}
				break;
			case 7:
				// 有未读消息
				$('#ct_friendlist li:contains(' + m.sender + ')').html(m.sender + " 有未读消息");
				timerArr = $.blinkTitle.show();
				if (audio_msg) {
					audio_msg.play();
				}
				break;
		}
	});
	getRoomMes();
</script>
</body>
</html>