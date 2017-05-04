<%@ page import="com.lucien.model.*"%>
<%@ page import="com.lucien.util.*"%>
<%@ page import="com.lucien.factory.*"%>
<%@ page import="com.lucien.entity.*"%>
<%@ page import="java.util.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	request.setCharacterEncoding("utf-8");
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
	
	String loginuser = (String) session.getAttribute(Constants.USERKEY);
	UserFactory userFactory = UserFactory.getInstance();
	User user = userFactory.getUser(loginuser);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>欢迎<%=user.getNickname() %>(<%=loginuser %>)登陆聊天室</title>
	<link href="<%=basePath %>style/base/css/lucien.common.css" rel="stylesheet" type="text/css" /> 
	<link href="<%=basePath %>style/base/css/lucien.room.css" rel="stylesheet" type="text/css" /> 
	<link href="<%=basePath %>style/base/css/lucien.room1.css" rel="stylesheet" type="text/css" /> 
	<link href="<%=basePath %>style/base/css/jquery.hiAlerts.css" rel="stylesheet" type="text/css" /> 
	<link href="<%=basePath %>style/base/css/zTreeStyle/zTreeStyle.css" rel="stylesheet" type="text/css">
	<script src="<%=basePath %>style/base/scripts/jquery-1.8.3.js" type="text/javascript"></script>
	<script src="<%=basePath %>style/base/scripts/jquery.ztree.core-3.5.js" type="text/javascript"></script>
	<script src="<%=basePath %>style/base/scripts/jquery.ztree.exedit-3.5.js" type="text/javascript"></script>
	<script src="<%=basePath %>style/base/scripts/jquery.ui.draggable.js" type="text/javascript"></script>
	<script src="<%=basePath %>style/base/scripts/jquery.hiAlerts-min.js" type="text/javascript"></script>
	<script src="<%=basePath %>style/base/scripts/lucien.webrtc.js" type="text/javascript"></script>
	<script src="<%=basePath %>style/base/scripts/lucien.Encrypt.js" type="text/javascript"></script>
	<script src="<%=basePath %>style/base/scripts/lucien.common.js" type="text/javascript"></script>
	<script src="<%=basePath %>style/base/scripts/lucien.room1.js" type="text/javascript"></script>
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
			<div class="ct_legend">
				<img class="ct_user_header" src="<%=user.getHeader()%>">
				<div class="ct_user_info">
					<span class="ct_username">
						<strong><%=user.getNickname() %></strong>
						<font><%=user.getUsername() %></font>
					</span>
					<span title="<%=user.getAutograph() != null ? user.getAutograph() : "" %>"><%=user.getAutograph() != null ? user.getAutograph() : "" %></span>
				</div>
				<a class="ct_user_exit" href="action-user-exit.html">退出</a>
			</div>
			<div class="ct_user_search hidden">
				<input type="text" name="keyword">
				<input type="submit" value="">
			</div>
			<ul id="ct_friendlist" class="ztree">
			</ul>
		</div>
	</div>
	<div class="ct_right">
		<div class="ct_content">
			<form action="<%=basePath %>action?method=user&act=userlist" method="post">
				<ul class="ct_rnav">
					<li class="ct_user_search">
						<font>查找好友：</font>
						<select onchange="resetInput(this);" name="type">
							<option value="1" selected="selected">用户名</option>
							<option value="2">用户昵称</option>
							<option value="3">真实姓名</option>
							<option value="4">地区</option>
							<option value="5">性别</option>
							<option value="6">年龄</option>
						</select>
						<select name="term">
							<option value="2">等于</option>
							<option value="1">包含</option>
						</select>
						<input type="hidden" name="pagesize" value="18">
						<input type="hidden" class="ct_current" name="current" value="1">
						<div id="filterInput">
							<input type="text" name="username">
							<input type="button" class="ct_search_btn">
						</div>
						<div id="filterRadio" style="display:none;">
							<label class="ct_width15"></label>
							<input type="radio" name="sex" checked="checked" value="1">男&nbsp;
							<input type="radio" name="sex" value="0">女&nbsp;
							<label class="ct_width20"></label>
							<input type="button" class="ct_search_btn">
						</div>
					</li>
				</ul>
				<div class="ct_rpaging">
					<font>当前第</font>
					<font class="ct_current">1</font>
					<font>页</font>
					<span class="ct_last">上一页</span>
					<span class="ct_next">下一页</span>
				</div>
			</form>
			<div class="ct_rcontainer">
				
			</div>
			<div class="ct_rfooter">
				
			</div>
		</div>
	</div>
</div>
<script type="text/javascript">
	<%
		String first = request.getParameter("first");
		if (first != null && "1".equals(first)) {
			String sysmes = "你的登录账号<font color='red'>" + user.getUsername() + "</font>，请牢记该账号.<br>下次登录系统只能用该账号，昵称无效。";
			out.print("hiAlert(\"" + sysmes + "\", '消息');");
		}
	%>
	var timerArr;
	var mesInterval = null;
	function openChatItem(obj, username) {
		window.open("action?method=launch&username=" + username, "_blank");
	}
	function sendAddFriend(name) {
		$.post("action?method=user&act=isfriend", "username=" + name, function(data) {
			if (data != '1') {
				hiPrompt("验证信息:", "<%=user.getRealname() %>", "请输入", function(data) {
					if(data) {
						sendMessage("action?method=sendMes", data, name, "<%=loginuser %>", 1, function(data) {
							hiAlert('等待好友验证', '消息');
						});
					}
				}); 
			} else {
				hiAlert(name + "已是好友", '消息');
			}
		}, "text");
	}
	var webSocket = new WebSocketHelper("ws:<%=basePath.substring(5) %>websocket", function(m) {
		handleWSMes(m);
	});
	getRoomMes();
</script>
</body>
</html>