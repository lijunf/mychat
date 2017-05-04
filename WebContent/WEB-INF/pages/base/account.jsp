<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
	String username = (String) session.getAttribute("username");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>用户注册信息</title>
<link href="<%=basePath %>style/base/css/lucien.common.css" rel="stylesheet" type="text/css" /> 
<link href="<%=basePath %>style/base/css/lucien.account.css" rel="stylesheet" type="text/css" /> 
</head>
<body>
<div class="ct_account_area">
	<form action="<%=basePath %>action?method=updateUser" method="post">
		<fieldset>
			<legend>注册信息</legend>
			<div class="ct_message">
				您好
				<font class="ct_font16 ct_red"></font>
				，您已注册成功。你的账号
				<font class="ct_font16 ct_red"><%=username %></font>
				，请牢记该账号.<br>下次登录系统只能用该账号，昵称无效。
			</div>
			<ul>
				<li>
					<label>昵称：</label>
					<input type="text" name="nickname"/>
					<span class="ct_mes_cname"></span>
				</li>
				<li>
					<label>真实姓名：</label>
					<input type="text" name="realname"/>
					<span class="ct_mes_rname"></span>
				</li>
				<li>
					<label>头像：</label>
					<input type="text" name="realname"/>
					<span class="ct_mes_header"></span>
				</li>
				<li>
					<label>邮箱：</label>
					<input type="text" name="email"/>
					<span class="ct_mes_email">例如：lijunf@163.com</span>
				</li>
				<li>
					<label>性别：</label>
					<input type="radio" name="sex" checked="checked" value="1"/>
					<font class="ct_font13">男</font>
					<font class="ct_width20"></font>
					<input type="radio" name="sex" value="0"/>
					<font class="ct_font13">女</font>
					<span class="ct_mes_sex"></span>
				</li>
				<li class="ct_account_btns">
					<label>&nbsp;</label>
					<input class="ct_button" type="submit" value="修改个人资料">
					<font class="ct_width20"></font>
					<font class="ct_font12">直接<a href="<%=basePath %>action-room.html">进入</a>聊天室</font>
				</li>
			</ul> 
		</fieldset>
	</form>
</div>
</body>
</html>