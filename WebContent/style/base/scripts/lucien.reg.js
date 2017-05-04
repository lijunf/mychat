/**
 * 选择头像
 * @param img点击的图片对象
 */
function choiceHeader(img) {
	$("img.ct_header").css("border-color", "#cccccc");
	$(img).css("border-color", "red");
	$("#ct_header").val(img.title);
}

$(document).ready(function() {
	$('span#ct_header_btn').click(function() {
		$("div#ct_headers").toggleClass("ct_headers");
		$('span#ct_header_btn').toggleClass("ct_header_btn");
	});
	$('div.ct_hide_bottom').click(function() {
		$('ul#ct_sub_content').toggleClass("ct_sub_content");
	});
});
	
/**
 * 验证表单信息
 * @returns {Boolean}
 */
function check() {
	var $nickname = $('input[name=nickname]');
	if ($nickname.val() == '') {
		$nickname.css('border-color', 'red');
		$nickname.focus();
		$('span.ct_mes_name').html('昵称不能为空！');
		$nickname.keyup(function() {
			if ($nickname.val() != '') {
				$nickname.css('border-color', '#666666');
				$('span.ct_mes_name').html('&radic;');
				$nickname = null;
			}
		});
		return false;
	}
	
	var $password = $('input[name=password]');
	if ($password.val().length < 6) {
		$password.css('border-color', 'red');
		$password.focus();
		$password.keyup(function() {
			if ($password.val().length > 5) {
				$password.css('border-color', '#666666');
				$('span.ct_mes_pwd').html('&radic;');
				$nickname = null;
			}
		});
		return false;
	}
	
	var $password2 = $('input[name=password2]');
	if ($password2.val() != $password.val()) {
		$password2.css('border-color', 'red');
		$password2.focus();
		$('span.ct_mes_pwd2').html('两次密码不一致！');
		$password2.keyup(function() {
			if ($password2.val() == $password.val()) {
				$password2.css('border-color', '#666666');
				$('span.ct_mes_pwd2').html('&radic;');
				$nickname = null;
			}
		});
		return false;
	}
	
	var $email = $('input[name=email]');
	var myreg = /^([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/;
	if(!myreg.test($email.val())) {
		$email.css('border-color', 'red');
		$email.focus();
		$('span.ct_mes_email').html('邮箱格式不正确！例如：lijunf@163.com');
		$email.keyup(function() {
			if (myreg.test($email.val())) {
				$email.css('border-color', '#666666');
				$('span.ct_mes_email').html('&radic;');
				$email = null;
			}
		});
		return false;
	}
}