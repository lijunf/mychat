function websocket_connect(host) {
	try {
		var readyState = new Array("正在连接", "已建立连接", "正在关闭连接", "已关闭连接");
		var ct_tip = document.getElementById("ct_tip_mes");
		if (typeof(WebSocket) !== "undefined") {
			var webSocket = new WebSocket(host);
			webSocket.onopen = function() {
				ct_tip.innerHTML = readyState[webSocket.readyState];
			}
			webSocket.onclose = function() {
				//alert(readyState[webSocket.readyState]);
				ct_tip.innerHTML = readyState[webSocket.readyState];
				var isChrome = window.navigator.userAgent.indexOf("Chrome") !== -1;
				if (isChrome) {
					window.location.href = "action-user-login.html";
				}
			}
			if (webSocket.readyState != 3) {
				return webSocket;
			}
			return null;
		}
	} catch (e) {
		return null;
	}
}

function websocket_send(text) {
	if (text != "") {
		try {
			if (webSocket) {
				webSocket.send(text);
			}
		} catch (e) {
		}
	}
}

function websocket_disconnect() {
	if (webSocket) {
		webSocket.close();
	}
}