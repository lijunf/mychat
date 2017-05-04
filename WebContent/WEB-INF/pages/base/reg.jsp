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
<title>用户注册</title>
<link href="<%=basePath %>style/base/css/lucien.common.css" rel="stylesheet" type="text/css" /> 
<link href="<%=basePath %>style/base/css/lucien.reg.css" rel="stylesheet" type="text/css" />
<link href="<%=basePath %>style/base/css/jquery.passwordStrength.css" rel="stylesheet" type="text/css" />
<link href="<%=basePath %>style/base/plugins/My97DatePicker/skin/WdatePicker.css" rel="stylesheet" type="text/css" />
<script src="<%=basePath %>style/base/scripts/jquery-1.8.3.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/jquery.passwordStrength.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/scripts/lucien.reg.js" type="text/javascript"></script>
<script src="<%=basePath %>style/base/plugins/My97DatePicker/WdatePicker.js" type="text/javascript"></script>
<script type="text/javascript">
	$(function(){
		$('#pass').passwordStrength();
	});
</script>
</head>
<body>
<div id="ct_dialog" class="ct_reg_area">
	<form action="<%=basePath %>action?method=user&act=reg" method="post" onsubmit="return check();">
		<fieldset class="draggable">
			<legend>快速注册</legend>
			<div class="ct_message"><%=message %></div>
			<ul>
				<li>
					<label>账号：</label>
					<span class="ct_mes_user">注册成功后会自动生成账号</span>
				</li>
				<li>
					<label><font color="red">*</font>昵称：</label>
					<input type="text" autocomplete="off" name="nickname"/>
					<span class="ct_mes_name"></span>
				</li>
				<li style="margin-bottom:10px;">
					<label><font color="red">*</font>密码：</label>
					<input type="password" id="pass" autocomplete="off" name="password"/>
					<div id="passwordStrengthDiv" class="is0"></div>
					<span class="ct_mes_pwd">至少6位</span>
				</li>
				<li>
					<label><font color="red">*</font>确认密码：</label>
					<input type="password" autocomplete="off" name="password2"/>
					<span class="ct_mes_pwd2"></span>
				</li>
				<li>
					<label><font color="red">*</font>邮箱：</label>
					<input type="email" name="email"/>
					<span class="ct_mes_email">格式：xxxxxx@yyy.zzz</span>
				</li>
				<li>
					<label><font color="red">*</font>性别：</label>
					<div class="ct_radio_list">
						<input type="radio" name="sex" checked="checked" value="true"/>
						<font class="ct_font14 ct_v_top">男</font>
						<font class="ct_width20"></font>
						<input type="radio" name="sex" value="false"/>
						<font class="ct_font14 ct_v_top">女</font>
						<span class="ct_mes_sex"></span>
					</div>
				</li>
				<li>
					<label><font color="red">*</font>头像：</label>
					<input id="ct_header" type="hidden" name="header" value="1.png"/>
					<div id="ct_headers" class="ct_headers">
						<img class="ct_header" onclick="choiceHeader(this)" title="0.png" src="style/base/images/header/online/0.png">
						<img class="ct_header" style="border-color: red;" onclick="choiceHeader(this)" title="1.png" src="style/base/images/header/online/1.png">
						<img class="ct_header" onclick="choiceHeader(this)" title="2.png" src="style/base/images/header/online/2.png">
						<img class="ct_header" onclick="choiceHeader(this)" title="3.png" src="style/base/images/header/online/3.png">
						<img class="ct_header" onclick="choiceHeader(this)" title="4.png" src="style/base/images/header/online/4.png">
						<img class="ct_header" onclick="choiceHeader(this)" title="5.png" src="style/base/images/header/online/5.png">
						<img class="ct_header" onclick="choiceHeader(this)" title="6.png" src="style/base/images/header/online/6.png">
						<img class="ct_header" onclick="choiceHeader(this)" title="7.png" src="style/base/images/header/online/7.png">
						<img class="ct_header" onclick="choiceHeader(this)" title="8.png" src="style/base/images/header/online/8.png">
						<img class="ct_header" onclick="choiceHeader(this)" title="9.png" src="style/base/images/header/online/9.png">
					</div>
					<span id="ct_header_btn" class="ct_header_btn1"></span>
				</li>
			</ul>
			<div class="ct_hide_bottom" title="点击填写详细信息"></div>
			<ul id="ct_sub_content" class="ct_sub_content">
				<li>
					<label>真实姓名：</label>
					<input type="text" name="realname"/>
					<span class="ct_mes_realname"></span>
				</li>
				<li>
					<label>英文名：</label>
					<input type="text" name="engname"/>
					<span class="ct_mes_engname"></span>
				</li>
				<li>
					<label>出生日期：</label>
					<input type="text" name="birthday" onclick="WdatePicker()"/>
					<span class="ct_mes_birthday"></span>
				</li>
				<li>
					<label>地址：</label>
					<input type="text" name="address"/>
					<span class="ct_mes_address"></span>
				</li>
				<li>
					<label>电话：</label>
					<input type="text" name="phone"/>
					<span class="ct_mes_phone"></span>
				</li>
				<li>
					<label>职业：</label>
					<input type="text" name="profession"/>
					<span class="ct_mes_profession"></span>
				</li>
				<li>
					<label>学历：</label>
					<input type="text" name="education"/>
					<span class="ct_mes_education"></span>
				</li>
				<li>
					<label>学校：</label>
					<input type="text" name="school"/>
					<span class="ct_mes_school"></span>
				</li>
				<li>
					<label>个人主页：</label>
					<input type="text" name="homepage"/>
					<span class="ct_mes_homepage"></span>
				</li>
				<li>
					<label>个性签名：</label>
					<textarea name="autograph" rows="2" cols="40"></textarea>
				</li>
				<li>
					<label>个人说明：</label>
					<textarea name="elucidate" rows="3" cols="50"></textarea>
				</li>
			</ul>
			<ul>
				<li>
					<label></label>
					<div class="ct_checkbox_list">
						<input type="checkbox" checked="checked" name="ct_agree"/>
						<span>
					 		同意<a href="">服务条款</a>和<a href="">隐私权相关政策</a>
					 	</span>
					</div>
				 </li> 
				<li class="ct_reg_btns">
					<label>&nbsp;</label>
					<input type="submit" class="ct_button" value="注册">
					<font class="ct_width30"></font>
					<input type="reset" class="ct_button" value="重置">
					<font class="ct_width30"></font>
					<font class="ct_font12">已有账号请<a href="<%=basePath %>action-home.html">登录</a></font>
				</li>
			</ul> 
		</fieldset>
	</form>
</div>
</body>
</html>