/**
 * 获取聊天室中的所有消息
 */
function getRoomMes() {
	send_request(function(data) {
		if (data) {
			try {
				data = eval(data);
				var users = data.users;
				var html = "";
				for (var i = 1; i < users.length; i++) {
					var username = users[i];
					var content = null;
					if (username.charAt(0) == '1') {
						username = username.substring(1);
						content = username + "&nbsp;<label>有未读信息</label>";
					} else {
						username = username.substring(1);
						content = username;
					}
					html += "<li class='ct_friend' title='发送消息' onclick=\"openChatItem(this, '" + username + "');\">" + content + "</li>";
				}
				$usernum.html(data.size);
				$userlist.html(html);
				
				var mes = data.meses;
				html = "";
				var count = 0;
    			for (var i = 0; i < mes.length; i++) {
    				var m = mes[i];
    				if (m.sender != loginuser) {
        				var content = m.content;
        				content = content.replace(/\+/g, "%20");
        				content = decodeURIComponent(content);
        				content = decode(content);
    					// 基本聊天消息
        				html += "<div><span class='ct_mes_title_y'>" + m.sender + " " + m.sendTime + "</span>"
    		    				+ "<div class='ct_mes_content'>" + content + "</div></div>";
        				count++;
    				}
    			}
    			if (count > 0 && audio_msg) {
					audio_msg.play();
				}
    			$contents.append(html);
				$contents.scrollTop($ID("ct_contents").scrollHeight);
			} catch (e) {
			}
		}
	}, "action?method=user&act=friendlist&mode=2", true);
}

var audio_online, audio_system; 
$(document).ready(function() {
	
	if (typeof(Audio) !== "undefined") {   
		audio_online = new Audio('style/base/audio/Global.ogg');
		audio_system = new Audio('style/base/audio/system.ogg');
	}
	
	if (webSocket) {
		webSocket.onmessage = function(event) {
			var m = eval("(" + event.data + ")");
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
		}
	} else {
		setInterval("getRoomMes()", 3000);
	}
})