<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.lucien.util.*"%>
<%@ page import="com.lucien.factory.*"%>
<%@ page import="com.lucien.entity.*"%>
<%
	request.setCharacterEncoding("utf-8");
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";

	String username = request.getParameter("username");
	String loginuser = (String) session.getAttribute(Constants.USERKEY);
	UserFactory userFactory = UserFactory.getInstance();
	User user = userFactory.getUser(loginuser);
	User friend = userFactory.getUser(username);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>我是<%=user != null ? user.getNickname() : "" %>(<%=loginuser %>)</title>
<link href="<%=basePath %>style/base/css/lucien.common.css" rel="stylesheet" type="text/css" /> 
<link href="<%=basePath %>style/base/css/lucien.dialog.css" rel="stylesheet" type="text/css" /> 
<link href="<%=basePath %>style/base/css/jquery.hiAlerts.css" rel="stylesheet" type="text/css" /> 
<script src="<%=basePath %>style/base/plugins/KE/kindeditor.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/jquery-1.8.3.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/jquery.ui.draggable.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/jquery.hiAlerts-min.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/lucien.common.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/lucien.dialog.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/lucien.MesBox.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/lucien.Encrypt.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/lucien.webrtc.js" type="text/javascript"></script>
</head>
<body>
<div id="ct_tip">
	<span id="ct_tip_mes"></span>
	<font>,</font>
	<span id="ct_tip_mes2"></span>
</div>
<div class="ct_dialogue">
	<div class="ct_dia_header">
		<img src="<%=user != null ? user.getHeader() : "style/base/images/header/online/6.png" %>"/>
		<span class="ct_dia_mesinput"></span>
		<span class="ct_dia_username">与<font><%=friend != null ? friend.getNickname() : "" %></font>(<%=username %>)聊天中</span>
	</div>
	<div class="ct_dia_content">
		<div id="ct_contents"></div>
	</div>
	<div class="ct_dia_bar"></div>
	<div class="ct_dia_message">
		<textarea id="ct_message"></textarea>
	</div>
	<div class="ct_dia_footer">
		<div class="ct_dia_btns">
			<div class="ct_dia_btns_left">Chrome浏览器中效果最佳</div>
			<span class='shortcut'>&nbsp;&darr;&nbsp;</span>
			<span title="发送" class="ct_dia_send_btn">发&nbsp;送</span>
			<span title="关闭" class="ct_dia_close_btn">关&nbsp;闭</span>
			<span title="视频" class="ct_dia_video_btn">视&nbsp;频</span>
		</div>
	</div>
</div>
<script type="text/javascript">
var getter = '<%=username%>';
var loginuser = '<%=loginuser%>';
var mtype = 0;
var $contents = $("#ct_contents");
var webSocket = null;
var mesInterval = null;
localDB.init(getter, loginuser, function() {
	getMes('<%=basePath %>action?method=getMes', '<%=loginuser%>', '<%=username%>');
	if (typeof(WebSocket) !== "undefined") {
		webSocket = new WebSocketHelper('ws:<%=basePath.substring(5) %>websocket?username=<%=username%>', function(m) {
			switch (m.mtype) {
				case 0:
					// 普通消息
					m.content = decode(m.content);
					showMessage(m);
					localDB.addData(m.sender, loginuser, m.content, m.sendTime);
					break;
				case 8:
					// 正在输入
					$('span.ct_dia_mesinput').html('正在输入...');
					break;
				case 9:
					// 取消输入
					$('span.ct_dia_mesinput').html('');
					break;
				case 10:
					// WebRTC
					WebRTCHelper.dealMessage(m);
					break;
			}
		});
	} else {
		mesInterval = setInterval("getMes('<%=basePath %>action?method=getMes', '<%=loginuser%>', '<%=username%>')", 3000);
	}
});
</script>
</body>
</html>