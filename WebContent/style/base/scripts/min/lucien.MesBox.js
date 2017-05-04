KE.show({
	id : 'ct_message',
	height : '143px',
	useContextmenu : false,
	cssPath : 'style/base/plugins/KE/index.css',
	items : [
	 'emoticons', 'fontname', 'fontsize', 'textcolor', 'bgcolor', 'bold', 'italic', 'underline',
	'removeformat', 'justifyleft', 'justifycenter', 'justifyright', 'insertorderedlist',
	'insertunorderedlist']
});

//采用html5的本来存储保存聊天记录
var localDB = {
	db : null,
	supportDB : false,
	init : function(getter, loginuser, callback) {
		if (typeof(openDatabase) !== 'undefined') {
			// alert("恭喜您支持本地数据库");
			this.supportDB = true;
			this.db = openDatabase('MychatData', '', 'Mychat Database', 102400);
			this.showAllData(getter, loginuser, callback);
			$("#ct_tip_mes2").html("支持SQLLite");
		} else {
			$("#ct_tip_mes2").html("不支持SQLLite");
			if (callback) {
				callback();
			}
		}
	},
	removeAllData : function() {
		$contents.html('');
	},
	showData : function(m) {
		var html = "";
		if (m.sender == loginuser) {
			html = "<div><span class='ct_mes_title_i'>我 " +  m.sendtime + "</span>"
				+ "<div class='ct_mes_content'>" + m.content + "</div></div>";
		} else {
			html = "<div><span class='ct_mes_title_y'>" + m.sender + " " + m.sendtime + "</span>"
				+ "<div class='ct_mes_content'>" + m.content + "</div></div>";
		}
		$contents.append(html);
	},
	// 获取本地数据库存储的聊天记录，并展示
	showAllData : function(getter, loginuser, callback) {
		if (this.supportDB) {
			this.db.transaction(function(tx) {
				tx.executeSql('CREATE TABLE IF NOT EXISTS ct_message(sender TEXT, getter TEXT, content TEXT, sendtime TEXT)', [], function(tx, rs) {
					//alert("保存数据成功");
					//保存数据成功
				}, function(tx, error) {
					alert("保存数据失败");
					//保存数据失败
				});
				tx.executeSql('SELECT * FROM ct_message WHERE (getter = ? AND sender = ? ) OR (sender = ? AND getter = ?)', [getter, loginuser, getter, loginuser], function(tx, rs) {
					localDB.removeAllData();
					for (var i = 0; i < rs.rows.length; i++) {
						localDB.showData(rs.rows.item(i));
					}
					if (rs.rows.length > 0) {
						$contents.append("<div class='ct_mes_history'>—————以上是您的历史聊天记录——————</div>");
						$contents.scrollTop($ID("ct_contents").scrollHeight);
					}
					if (callback) {
						callback();
					}
				});
			});
		}
	},
	// 向本地数据库中插入记录
	addData : function(sender, getter, content, sendtime) {
		if (this.supportDB) {
			this.db.transaction(function(tx) {
				tx.executeSql('INSERT INTO ct_message VALUES(?, ?, ?, ?)', [sender, getter, content, sendtime], function(tx, rs) {
					//alert("保存数据成功");
					//保存数据成功
				}, function(tx, error) {
					alert("保存数据失败");
					//保存数据失败
				})
			});
		}
	}
};

/**
 * 发送消息
 * @param url
 * @param getter
 * @param sender
 * @param mtype
 * @param content
 */
function sendMessage(url, getter, sender, mtype, message) {
	mtype = !mtype ? 0 : mtype;
	if (typeof(webSocket) != "undefined" && webSocket) {
		var text = "getter:" + getter + ",sender:" + sender + ",content:" + encode(message) + ",mtype:" + mtype;
		websocket_send(text);
		if (mtype == 0 || mtype == 6) {
			localDB.addData(sender, getter, message, new Date().pattern('yyyy/MM/dd HH:mm:ss'));
			mesSended(message);
		}
	} else {
		var xmlhttp = getXMLHttpRequest();
		if (xmlhttp) {
			var data = "content=" + encodeURIComponent(encode(message)) + "&getter=" + getter + "&sender=" + sender + "&mtype=" + mtype;
			xmlhttp.open("post", url, true);
			xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
			xmlhttp.onreadystatechange = function() {
	        	if (xmlhttp.readyState == 4) {
			    	if (xmlhttp.status == 200) {
			    		if (mtype == 0 || mtype == 6) {
			    			localDB.addData(sender, getter, message, new Date().pattern('yyyy/MM/dd HH:mm:ss'));
			    			mesSended(message);
			    		}
			    	}
	        	}
			};
			xmlhttp.send(data);
		}
	}
}

/**
 * 从消息输入框中取得消息，并发送
 * @param url		地址
 * @param getter	消息接收者
 * @param sender	消息发送者
 */
function sendMes(url, getter, sender, mtype) {
	var message = KE.util.getData("ct_message");
	var noMes = false;
	if (message) {
		var pos = message.lastIndexOf('\n<p>&nbsp;</p>');
		if (pos != -1 && message.length - pos == 14) {
			message = message.substr(0, pos);
		}
		pos = message.indexOf('<br />\n');
		if (pos == 0) {
			message = message.substr(7);
		}
		pos = message.indexOf('<p>&nbsp;</p>\n');
		if (pos == 0) {
			message = message.substr(14);
		}
		if (message != '<p>&nbsp;</p>') {
			sendMessage(url, getter, sender, mtype, message);
		} else {
			noMes = true;
		}
	} else {
		noMes = true;
	}
	if (noMes) {
		KE.util.setFullHtml("ct_message","");
		var $defmes = $('ul#ct_defaultmes');
		if ($defmes.length != 0) {
			$defmes.show();
		} else {
			var html = "<ul id='ct_defaultmes' class='ct_mesbox'>" +
				"<li onclick='chooseMes(this)' class='current'>您好！</li>" +
				"<li onclick='chooseMes(this)'>哦</li>" +
				"<li onclick='chooseMes(this)'>好了，好了，我知道了</li>" +
				"<li onclick='chooseMes(this)'>是吗？</li>" +
				"<li onclick='chooseMes(this)'>不会是真的吧？</li>" 
				"</ul>";
			$('div.ct_dia_btns').append(html);
		}
	}
}

/**
 * 发送信息完成后的处理
 * @param message
 */
function mesSended(message) {
	var date = new Date();
	var html = "<div><span class='ct_mes_title_i'>我 " + date.pattern('yyyy/MM/dd HH:mm:ss') + "</span>"
			+ "<div class='ct_mes_content'>" + message + "</div></div>";
	$contents.append(html);
	$contents.scrollTop($ID("ct_contents").scrollHeight);
	KE.util.setFullHtml("ct_message","");
}

/**
 * 选择系统默认消息
 * @param tar
 */
function chooseMes(tar) {
	$('ul#ct_defaultmes li').removeClass('current');
	tar.className = 'current';
	KE.util.setFullHtml("ct_message", tar.innerHTML);
}

/**
 * 显示设置enter快捷键的div
 */
function openshortcut(tar) {
	if (tar) {
		var $shortcut = $('ul#ct_shortcut');
		if ($shortcut.length != 0) {
			$shortcut.show();
		} else {
			var html = "<ul id='ct_shortcut' class='ct_mesbox'>" +
				"<li onclick='setshortcut(this, false)' class='current'>按Enter键发送消息</li>" +
				"<li onclick='setshortcut(this, true)'>按Ctrl + Enter键发送消息</li>" +
				"</ul>";
			$('div.ct_dia_btns').append(html);
		}
	}
}

/**
 * 按Enter键发送消息
 */
function setshortcut(tar, ctrl) {
	$('ul#ct_shortcut li').removeClass('current');
	tar.className = 'current';
	isctrl = ctrl;
}

document.onclick = function(e) {
	var src = e ? e.target : event.srcElement;
	if (src.className == 'shortcut') return;
	$('ul#ct_shortcut').hide();
	if (src.className != 'ct_dia_send_btn') {
		$('ul#ct_defaultmes').hide();
	}
}

var ctrl = 0;
var isctrl = false;
var audio_msg;
$(document).ready(function() {
	
	if (typeof(Audio) !== "undefined") {   
		audio_msg = new Audio('style/base/audio/msg.ogg');
	}
	
	$('span.ct_dia_close_btn').click(function() {
		window.close();
	})
	
	$('span.ct_dia_send_btn').click(function() {
		sendMes('action?method=sendMes', getter, loginuser, mtype);
	})
	
	$('span.shortcut').click(function() {
		openshortcut(this);
	})
	
	$(document).keyup(function(e){
		if (e.keyCode == 13) {
			var $mesbox = $('ul#ct_defaultmes:visible');
			if ($mesbox.length > 0) {
				KE.util.setFullHtml("ct_message", $mesbox.find('li.current').text());
				$mesbox.hide();
			} else {
				if (isctrl) {
					if (ctrl == 1) {
						$('span.ct_dia_send_btn').trigger("click");
						ctrl = 0;
					}
				} else {
					$('span.ct_dia_send_btn').trigger("click");
				}
			}
		} else if (e.keyCode == 40) {
			var $mesbox = $('ul#ct_defaultmes:visible');
			if ($mesbox.length > 0) {
				var $list = $mesbox.find('li.current');
				if ($list.next().length > 0) {
					$list.removeClass('current');
					$list.next().addClass('current');
				} else {
					$list.removeClass('current');
					$mesbox.find('li:first').addClass('current');
				}
			}
		} else if (e.keyCode == 38) {
			var $mesbox = $('ul#ct_defaultmes:visible');
			if ($mesbox.length > 0) {
				var $list = $mesbox.find('li.current');
				if ($list.prev().length > 0) {
					$list.removeClass('current');
					$list.prev().addClass('current');
				} else {
					$list.removeClass('current');
					$mesbox.find('li:last').addClass('current');
				}
			}
		}
		if (e.ctrlKey) {
			ctrl = 0;
		}
	});
	
	$(document).keydown(function(e){
		if (e.ctrlKey) {
			ctrl = 1;
		}
	});
	
	KE.event.ready(function() {
		$("iframe.ke-iframe").contents().focus(function() {
			sendMessage('action?method=sendMes', getter, loginuser, 8, '');
		})
		$("iframe.ke-iframe").contents().blur(function() {
			sendMessage('action?method=sendMes', getter, loginuser, 9, '');
		})
		$("iframe").contents().keyup(function(e){
			if (e.keyCode == 13) {
				var $mesbox = $('ul#ct_defaultmes:visible');
				if ($mesbox.length > 0) {
					KE.util.setFullHtml("ct_message", $mesbox.find('li.current').text());
					$mesbox.hide();
				} else {
					if (isctrl) {
						if (ctrl == 1) {
							$('span.ct_dia_send_btn').trigger("click");
							ctrl = 0;
						}
					} else {
						$('span.ct_dia_send_btn').trigger("click");
					}
				}
			} else if (e.keyCode == 40) {
				var $mesbox = $('ul#ct_defaultmes:visible');
				if ($mesbox.length > 0) {
					var $list = $mesbox.find('li.current');
					if ($list.next().length > 0) {
						$list.removeClass('current');
						$list.next().addClass('current');
					} else {
						$list.removeClass('current');
						$mesbox.find('li:first').addClass('current');
					}
				}
			} else if (e.keyCode == 38) {
				var $mesbox = $('ul#ct_defaultmes:visible');
				if ($mesbox.length > 0) {
					var $list = $mesbox.find('li.current');
					if ($list.prev().length > 0) {
						$list.removeClass('current');
						$list.prev().addClass('current');
					} else {
						$list.removeClass('current');
						$mesbox.find('li:last').addClass('current');
					}
				}
			}
			if (e.ctrlKey) {
				ctrl = 0;
			}
		});
		
		$("iframe").contents().keydown(function(e){
			if (e.ctrlKey) {
				ctrl = 1;
			}
		});
	});

});

