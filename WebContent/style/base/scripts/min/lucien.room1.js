var MoveTest = {
	beforeDrag: function(treeId, treeNodes) {
		if (!treeNodes[0].isParent) {
			treeNodes[0].LpId = treeNodes[0].pId;
			return true;
		} 
		return false;
	},
	prevTree: function(treeId, treeNodes, targetNode) {
		return !targetNode.isParent && targetNode.parentTId != treeNodes[0].parentTId;
	},
	nextTree: function(treeId, treeNodes, targetNode) {
		return !targetNode.isParent && targetNode.parentTId != treeNodes[0].parentTId;
	},
	innerTree: function(treeId, treeNodes, targetNode) {
		return targetNode!=null && targetNode.isParent && targetNode.tId != treeNodes[0].parentTId;
	},
	onDrop: function(e, treeId, treeNodes, targetNode, moveType) {
		if (treeNodes[0].LpId != treeNodes[0].pId) {
			send_request(null, "action?method=user&act=transgroup&gid=" + treeNodes[0].LpId 
					+ "&cgid=" + treeNodes[0].pId + "&username=" + treeNodes[0].id, false);
			// alert(treeNodes[0].id + " beloe from " + treeNodes[0].LpId + " move to " + treeNodes[0].pId);
		}
	},
	showIconForTree :function(treeId, treeNode) {
		return !treeNode.isParent;
	},
	beforeClick: function(treeId, treeNode) {
		if (!treeNode.isParent) {
			openChatItem(null, treeNode.id);
		}
	},
	beforeRemove: function(treeId, treeNode) {
		if (!treeNode.isParent) {
			if (confirm("确定要删除" + treeNode.name + "吗？\r\n操作不可恢复！")) {
				$.post("action?method=user&act=removefriend", "gid=" + treeNode.pId + "&username=" + treeNode.id);
				return true;
			} else {
				return false;
			}
		} else {
			if (confirm("确定要删除" + treeNode.name + "吗？\r\n会把组里的好友一起删掉，操作不可恢复！")) {
				$.post("action?method=user&act=removegroup", "gid=" + treeNode.id);
				return true;
			} else {
				return false;
			}
		}
	},
	onRename: function(event, treeId, treeNode) {
		if (treeNode.isParent) {
			$.post("action?method=user&act=renamegroup", "gid=" + treeNode.id + "&gname=" + treeNode.name)
		} else {
			$.post("action?method=user&act=renotename", "gid=" + treeNode.pId + "&username=" + treeNode.id + "&notename=" + treeNode.name)
		}
	},
	getFont: function(treeId, node) {
		return node.font ? node.font : {};
	},
	getFontCss: function(treeId, treeNode) {
		return (!!treeNode.highlight) ? {color:"red", "font-weight":"bold"} : {color:"#333", "font-weight":"normal"};
	}
};

var setting = {
	edit: {
		enable: true,
		showRemoveBtn: false,
		showRenameBtn: false,
		drag: {
			prev: MoveTest.prevTree,
			next: MoveTest.nextTree,
			inner: MoveTest.innerTree
		}
	},
	data: {
		keep: {
			parent: true,
			leaf: true
		},
		simpleData: {
			enable: true
		}
	},
	callback: {
		beforeDrag: MoveTest.beforeDrag,
		onDrop: MoveTest.onDrop,
		beforeClick : MoveTest.beforeClick,
		beforeRemove : MoveTest.beforeRemove,
		onRename : MoveTest.onRename
	},
	view: {
		selectedMulti: false,
		showLine: false,
		showIcon: MoveTest.showIconForTree,
		fontCss: MoveTest.getFontCss,
		nameIsHTML: true
	}
};

/* var zNodes = [
	{ id:1, pId:0, name:"我的好友", isParent: true, open:true},
	{ id:"王五", pId:1, name:"王五", neckname:'王麻子'},
	{ id:"赵六", pId:1, name:"赵六"},
	{ id:2, pId:0, name:"我的同学", isParent: true, open:true},
	{ id:"张三", pId:2, name:"张三"},
	{ id:"李四", pId:2, name:"李四"}
];

$(document).ready(function(){
	$.fn.zTree.init($("#ct_friendlist"), setting, zNodes);
}); 
*/
		
function setEdit() {
	zTree = $.fn.zTree.getZTreeObj("ct_friendlist");
	zTree.setting.edit.showRemoveBtn = true;
	zTree.setting.edit.showRenameBtn = true;
	zTree.setting.edit.removeTitle = "remove";
	zTree.setting.edit.renameTitle = "rename";
}

var audio_online, audio_system, audio_msg; 
$(document).ready(function() {
	
	if (typeof(Audio) !== "undefined") {   
		audio_online = new Audio('style/base/audio/Global.ogg');
		audio_system = new Audio('style/base/audio/system.ogg');
		audio_msg = new Audio('style/base/audio/msg.ogg');
	}
	
	// 点击搜索按钮时，将返回的搜索结果放入相应的地方
	$("input.ct_search_btn").click(function() {
		ajaxSubmitForm(this.form, function(data) {
			if (data) {
				data = eval(data);
				var html = "";
				for (var i = 0; i < data.users.length; i++) {
					var _data = data.users[i];
					html += "<div class='ct_userinfo'>"
							+ "<img src='" + _data.header + "'/>"
							+ "<ul>"
							+ "<li><font color='red'>" + _data.nickname + "</font>（" + _data.username + "）</li>"
							+ "<li>"
							+ "<font>" + _data.sex + "</font>"
							+ "<font class='ct_width5'></font>"
							+ "<font>" + _data.age + "岁</font>"
							+ "<font class='ct_width5'></font>"
							+ "<font>" + _data.address + "</font>"
							+ "</li>"
							+ "<li>"
							+ "<font class='ct_width5'></font>"
							+ "<span onclick=\"openChatItem(null, '" + _data.username + "');\">发起会话</span>"
							+ "<font class='ct_width5'></font>"
							+ "<span onclick=\"sendAddFriend('" + _data.username + "');\">加为好友</span>"
							+ "</li>"
							+ "</ul>"
							+"</div>";
				}
				$("div.ct_rcontainer").html(html);
				// 分页处理
				var current = data.current;
				var pages = data.pages;
				$("font.ct_current").html(current);
				if (current > 1) {
					$("span.ct_last").css("color","blue");
					$("span.ct_last").click(function() {
						$("input.ct_current").val(current - 1);
						$("input.ct_search_btn").trigger("click");
					});
				} else {
					$("span.ct_last").css("color","black");
					$('span#new_group').unbind("click");
				}
				if (current < pages) {
					$("span.ct_next").css("color","blue");
					$("span.ct_next").click(function() {
						$("input.ct_current").val(current + 1);
						$("input.ct_search_btn").trigger("click");
					});
				} else {
					$("span.ct_next").css("color","black");
					$('span#new_group').unbind("click");
				}
			} else {
				$("div.ct_rcontainer").html("");
			}
		});
	});
	
	updateUserList();
	
	if (typeof(webSocket) != "undefined" && webSocket) {
		setInterval(animation, 500);
		webSocket.onmessage = function(event) {
			var m = eval("(" + event.data + ")");
			handleWSMes(m);
		}
	} else {
		timer = 0;
		$.ajaxSetup({cache:false})
		mesInterval = setInterval(animation2, 500);
	}
});

/**
 * 消息提示动画，websocket中使用
 */
function animation() {
	$("div.ct_rfooter span").toggleClass("hidden");
}

/**
 * 消息提示动画与定时到服务器获取数据，在不支持websocket饿浏览器中使用
 */
function animation2() {
	$("div.ct_rfooter span").toggleClass("hidden");
	if (timer == 0 || timer == 5000) {
		timer = 0;
		getRoomMes();
	}
	timer += 500;
}

/**
 * 更新好友列表
 */
function updateUserList() {
	send_request(function(data) {
		if (data) {
			try {
				data = eval(data);
				$.fn.zTree.init($("#ct_friendlist"), setting, data);
				setEdit();
			} catch (e) {}
		}
	}, "action?method=user&act=friendlist&mode=1", true);
}

/**
 * 获取聊天室中的所有消息
 */
function getRoomMes() {
	send_request(function(data) {
		if (data) {
			try {
				if ('invalidate' == data) {
	    			//clearInterval(mesInterval);
					window.location.href = "action-user-login.html";
	    		}
				data = eval(data);
				// 处理普通信息的发送者
				var senders = data.unreads;
				var html = "";
				for (var i = 0; i < senders.length; i++) {
					var user = senders[i];
					html +=  "<span id='tip_" + user.username + "' onclick=\"_remove(this);openChatItem(this, '" + user.username + "');\" title='" + user.nickname + "'>"
							+ "<img src='" + user.header + "'>"
							+"</span>";
				}
				if (senders.length > 0 && audio_msg) {
					audio_msg.play();
				}
				
				// 处理系统信的发送者
				var systems = data.systems;
				for (var i = 0; i < systems.length; i++) {
					var user = systems[i];
					html +=  "<span id='tip_" + user.username + "' onclick=\"_remove(this);getMes('action?method=getMes', null, '" + user.username + "', 1);\" title='" + user.nickname + "'>"
							+ "<img src='" + user.header + "'>"
							+"</span>";
				}
				if (systems.length > 0 && audio_system) {
					audio_system.play();
				}
				$("div.ct_rfooter").html(html);
				
				if (senders.length + systems.length > 0) {
					timerArr = $.blinkTitle.show();
				}
				
				// 处理用户状态改变信息
				var status = data.status;
				for (var i = 0; i < status.length; i++) {
					handleMes(status[i]);
				}
			} catch (e) {}
		}
	}, "action?method=getRoomMes", true);
}

/**
 * 删除元素
 * @param tar
 */
function _remove(tar) {
	$(tar).remove();
	if ($("div.ct_rfooter span").length == 0) {
		$.blinkTitle.clear(timerArr);
	}
}

/**
 * 从服务器获取消息
 * @param url		服务器地址
 * @param getter	消息接收者
 * @param sender	消息发送者
 */
function getMes(url, getter, sender, mtype) {
	var xmlhttp = getXMLHttpRequest();
	if (xmlhttp) {
		var data = "sender=" + sender;
		if (getter) {
			data += "&getter=" + getter;
		}
		if (mtype) {
			data += "&mtype=" + mtype
		}
		xmlhttp.open("post", url, true);
		xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xmlhttp.onreadystatechange = function() {
        	if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
	    		var mes = xmlhttp.responseText;
	    		if ('invalidate' == mes) {
	    			//clearInterval(mesInterval);
	    			window.location.href = "action-user-login.html";
	    		}
	    		// 消息格式:[{mesid: ,content: ,sendTime: },{mesid: ,content: ,sendTime: }]
	    		if (mes) {
	    			mes = eval(mes);
	    			for (var i = 0; i < mes.length; i++) {
	    				handleMes(mes[i]);
	    			}
	    		}
        	} else {
	    		//clearInterval(mesInterval);
	    		//window.location.href = "action-user-login.html";
	    	}
		};
		xmlhttp.send(data);
	}
}

/**
 * 处理websocket的消息
 * @param m
 */
function handleWSMes(m) {
	if (m) {
		var content = m.content;
		content = decode(content);
		switch (m.mtype) {
			case 1:
			case 2:
			case 3:
				var tip = $("#tip_" + m.sender);
				if (tip.length == 0) {
					var html =  "<span id='tip_" + m.sender + "' onclick=\"_remove(this);getMes('action?method=getMes', null, '" + m.sender + "', 1);\">"
							+ "<img src='style/base/images/header/01.png'>"
							+"</span>";
					$("div.ct_rfooter").append(html);
					if (!timerArr) {
						timerArr = $.blinkTitle.show();
					}
				}
				if (audio_system) {
					audio_system.play();
				}
				break;
			case 4:
				// 好友上线
				friendOnline(m.sender);
				break;
			case 5:
				// 好友下线
				friendOffline(m.sender);
				break;
			case 7:
				// 有未读消息
				var node = zTree.getNodeByParam('id', m.sender);
				var icon = node ? node.icon.replace("/16/", "/") : 'style/base/images/header/00.png';
				var name = node ? node.name : '游客';
				var tip = $("#tip_" + m.sender);
				if (tip.length == 0) {
					var html =  "<span id='tip_" + m.sender + "' onclick=\"_remove(this);openChatItem(this, '" + m.sender + "');\" title='" + name + "'>"
							+ "<img src='" + icon + "'>"
							+"</span>";
					$("div.ct_rfooter").append(html);
					if (!timerArr) {
						timerArr = $.blinkTitle.show();
					}
				}
				if (audio_msg) {
					audio_msg.play();
				}
				break;
		}
	}
}

/**
 * 根据消息类型对消息进行处理
 * @param message
 */
function handleMes(m) {
	if (m) {
		var content = m.content;
		content = content.replace(/\+/g, "%20");
		content = decodeURIComponent(content);
		content = decode(content);
		switch (m.mtype) {
			// case 0:
				// 基本聊天消息(如果把dialog做到room里面而不是作为一个单独页面需要修改此处)
				// var html = "<div><span class='ct_mes_title_y'>" + sender + " " + m.sendTime + "</span>"
	    		//		+ "<div class='ct_mes_content'>" + content + "</div></div>";
				// $contents.append(html);
				// $contents.scrollTop($ID("ct_contents").scrollHeight);
				// break;
			case 1:
				// 发送加好友请求
				hiConfirm("你同意" + m.sender + "加您为好友吗?<br>请求信息：" + content, "好友请求", function(r) {
					if (r) {
						sendMessage("action?method=sendMes", "同意加好友", m.sender, m.getter, 2);
						$.post("action?method=user&act=isfriend", "username=" + m.sender, function(data) {
							if (data != '1') {
								addFriend(m.getter, m.sender, "选择分组");
							}
						}, "text");
					} else {
						sendMessage("action?method=sendMes", "拒绝加好友", m.sender, m.getter, 3);
					}
				});
				break;
			case 2:
				// 同意加为好友
				addFriend(m.getter, m.sender, m.sender + "同意添加好友");
				hiAlert(m.sender + content, '消息');
				break;
			case 3:
				// 被拒绝加为好友
				hiAlert(m.sender + content, '消息');
				break;
			case 4:
				// 好友上线
				friendOnline(m.sender);
				break;
			case 5:
				// 好友下线
				friendOffline(m.sender);
				break;
		}
	}
}

/**
 * 好友上线
 * @param name
 */
function friendOnline(name) {
	//var zTree = $.fn.zTree.getZTreeObj("ct_friendlist");
	var node = zTree.getNodeByParam('id', name);
	node.icon = node.icon.replace('/offline/', '/online/');
	node.highlight = true;
	zTree.updateNode(node);
	if (audio_online) {
		audio_online.play();
	}
}

/**
 * 好友下线
 * @param name
 */
function friendOffline(name) {
	//var zTree = $.fn.zTree.getZTreeObj("ct_friendlist");
	var node = zTree.getNodeByParam('id', name);
	node.icon = node.icon.replace('/online/', '/offline/');
	node.highlight = false;
	zTree.updateNode(node);
	if (audio_online) {
		audio_online.play();
	}
}

/**
 * 添加好友整个流程,getter添加sender为好友
 * @param getter消息接收者
 * @param sender消息发送者
 */
function addFriend(getter, sender, title) {
	send_request(function(data) {
		data = eval(data);
		var html = "<div>" +
				"<form action='action?method=user&act=addfriend' method='post'>" +
				"<input type='hidden' name='username' value='" + sender + "'/>" +
				"<ul class='ct_addfri_info'>" +
				"<li>我的组：<div id='ct_group'><select name='gid'>";
		for (var i = 0; i < data.length; i++) {
			html += "<option value='" + data[i].gid + "'>" + data[i].gname + "</option>";
		}
		html += "</select></div><font class='ct_width20'></font><span id='new_group'>新建组</span></li>" +
				"<li>备注名：<input type='text' name='notename'/></li>" +
				"</ul>" +
				"<div id='popup_panel'>" +
				"<input type='button' id='popup_ok' value='&nbsp;确定&nbsp;'>" +
				"</div>" +
				"</form></div>";
		hiBox(html, title,'','','','.a_close');
		$('input#popup_ok').click(function() {
			if ($('select[name=gid]')) {
				ajaxSubmitForm(this.form, function(data) {
					$("span#popup_close").trigger("click");
					hiAlert("添加好友成功", '消息');
					updateUserList();
				});
			}
		});
		$('span#new_group').click(function() {
			addGroup();
		});
	}, "action?method=user&act=groups", true);
}

/**
 * 添加分组
 */
function addGroup() {
	$('div#ct_group').html("<input type='text' name='gname'>");
	var $newgroup = $('span#new_group');
	$newgroup.text("添加");
	$newgroup.unbind("click");
	$newgroup.click(function() {
		var gname = $('input[name=gname]').val();
		if (gname) {
			$.post("action?method=user&act=addgroup", "gname=" + gname, function(data) {
				data = eval(data);
				var html = "<select name='gid'>";
				for (var i = 0; i < data.length; i++) {
					html += "<option value='" + data[i].gid + "'>" + data[i].gname + "</option>";
				}
				html  +	"</select>";
				$('div#ct_group').html(html);
				var $newgroup = $('span#new_group');
				$newgroup.text("新建组");
				$newgroup.unbind("click");
				$newgroup.click(function() {
					addGroup();
				});
			}, "text");
		}
	});
}

/**
 * 根据选择的搜索条件，设置搜索自段
 * @param select
 */
function resetInput(select) {
	if (select) {
		switch (parseInt(select.value)) {
			case 1:
				$("div#filterInput").show();
				$("div#filterRadio").hide();
				$("div#filterInput Input[type='text']").attr('name', 'username');
				break;
			case 2:
				$("div#filterInput").show();
				$("div#filterRadio").hide();
				$("div#filterInput Input[type='text']").attr('name', 'nickname');
				break;
			case 3:
				$("div#filterInput").show();
				$("div#filterRadio").hide();
				$("div#filterInput Input[type='text']").attr('name', 'realname');
				break;
			case 4:
				$("div#filterInput").show();
				$("div#filterRadio").hide();
				$("div#filterInput Input[type='text']").attr('name', 'address');
				break;
			case 5:
				$("div#filterInput").hide();
				$("div#filterRadio").show();
				$('div#filterInput').attr('name', 'sex');
				break;
			case 6:
				$("div#filterInput").show();
				$("div#filterRadio").hide();
				$("div#filterInput Input[type='text']").attr('name', 'age');
				break;
		}
	}
}