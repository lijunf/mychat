<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
	String message = (String) request.getAttribute("message");
	message = message == null ? "" : message;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>登陆聊天室</title>
<link href="<%=basePath %>style/base/css/lucien.common.css" rel="stylesheet" type="text/css" /> 
<link href="<%=basePath %>style/base/css/lucien.login.css" rel="stylesheet" type="text/css" /> 
<script src="<%=basePath %>style/base/scripts/jquery-1.8.3.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/lucien.common.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/plugins/KF/kfbox.js" type="text/javascript"></script>
</head>
<body>
<div id="ct_dialog" class="ct_login_area">
	<form action="<%=basePath %>action-user-login.html" method="post">
		<fieldset>
			<legend>登陆聊天室</legend>
			<div class="ct_message"><%=message %></div>
			<ul>
				<li><label>用户名：</label><input type="text" name="username" autocomplete="off" placeholder="6位账号 " value=""/></li>
				<li><label>密码：</label><input type="password" name="password" autocomplete="off" value=""/></li>
				<li class="ct_login_btns">
					<label>&nbsp;</label>
					<input type="submit" class="ct_button" value="登陆">
					<font class="ct_width15"></font>
					<a>忘记密码?</a>
				</li>
				<li class="ct_login_tips">
					<label></label>
					<font class="ct_font12">没有账号？<font class="ct_width5"></font><a href="<%=basePath %>action-regInput.html">注册</a></font>
					<font class="ct_width5"></font><font class="ct_font12"><a href="<%=basePath %>action-room.html">快速体验</a></font>
				</li>
			</ul> 
		</fieldset>
	</form>
</div>
</body>
</html>