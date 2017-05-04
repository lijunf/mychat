/**
 * 展示消息
 * @param m
 */
function showMessage(m) {
	var html = "<div><span class='ct_mes_title_y'>" + m.sender + " " + m.sendTime + "</span>"
			+ "<div class='ct_mes_content'>" + m.content + "</div></div>";
	$contents.append(html);
	$contents.scrollTop($ID("ct_contents").scrollHeight);
	if (audio_msg) {
		audio_msg.play();
	}
}

/**
 * 从服务器获取消息
 * @param url		服务器地址
 * @param getter	消息接收者
 * @param sender	消息发送者
 */
function getMes(url, getter, sender) {
	var xmlhttp = getXMLHttpRequest();
	if (xmlhttp) {
		var data = "getter=" + getter + "&sender=" + sender + "&mtype=0";
		xmlhttp.open("post", url, true);
		xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xmlhttp.onreadystatechange = function() {
        	if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
	    		var mes = xmlhttp.responseText;
	    		if ('invalidate' == mes) {
	    			//clearInterval(mesInterval);
	        		window.close();
	    		}
	    		// 消息格式:[{mesid: ,content: ,sendTime: },{mesid: ,content: ,sendTime: }]
	    		if (mes) {
	    			mes = eval(mes);
	    			var html = "";
	    			for (var i = 0; i < mes.length; i++) {
	    				var m = mes[i];
	    				switch (m.mtype) {
		    				case 0:
		    					// 普通消息
		    					if (m.sender != loginuser) {
			    					var content = m.content;
				    				content = content.replace(/\+/g, "%20");
				    				content = decodeURIComponent(content);
				    				content = decode(content);
				    				m.content = content
									/*// 基本聊天消息
				    				html += "<div><span class='ct_mes_title_y'>" + sender + " " + m.sendTime + "</span>"
						    				+ "<div class='ct_mes_content'>" + content + "</div></div>";
				    				if (audio_msg) {
				    					audio_msg.play();
				    				}*/
				    				showMessage(m);
				    				localDB.addData(sender, loginuser, content, m.sendTime);
			    				}
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
	    			}
	    			//$contents.append(html);
    				//$contents.scrollTop($ID("ct_contents").scrollHeight);
	    		}
        	}
		};
		xmlhttp.send(data);
	}
}