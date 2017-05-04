var RtcConnect = function (_userId, _webSocketHelper) {
 
    var config = {"iceServers": [{"url": "stun:stun.l.google.com:19302"}]};
    var peerConnection = null;
    var remoteVideo = null;
    var userId = _userId;
    var webSocketHelper = _webSocketHelper;
 
    var createVideo = function (stream) {
    	window.URL = window.URL || window.webkitURL || window.mozURL || window.msURL;
        var src = window.URL.createObjectURL(stream);
        var video = $("<video />").attr("src", src).attr("controls", "controls");
        var close = $("<span />").addClass("close");
        var container = $("<div />").addClass("videoContainer videoRemote").append(video).append(close).appendTo($("body"));
        $("div.videoRemote").draggable();
        $("div.videoRemote .close").click(function() {
        	if (window.confirm('你确定要关闭视频通话吗？')) {
        		WebRTCHelper.closeRtcConnects();
            	WebRTCHelper.sendSignal(5);
    		}
        });
        video[0].play();
        remoteVideo = stream;
        return container;
    };
 
    var init = function() {
 
        window.RTCPeerConnection = window.RTCPeerConnection || window.webkitRTCPeerConnection || window.mozRTCPeerConnection || window.msRTCPeerConnection;
        peerConnection = new window.RTCPeerConnection(config);
 
        peerConnection.addEventListener('addstream', function(event) {
            createVideo(event.stream);
        });
        peerConnection.addEventListener('icecandidate', function(event) {
            var description = JSON.stringify(event.candidate);
            WebRTCHelper.sendSignal(4, description);
        });
        
        testEvent();
 
        peerConnection.addStream(WebRTCHelper.localStream);
    };
    
    var testEvent = function() {
    	peerConnection.addEventListener('removestream', function() {
            console.log('removestream');
        });
        peerConnection.addEventListener('datachannel', function() {
        	console.log('datachannel');
        });
        peerConnection.addEventListener('iceconnectionstatechange', function() {
        	console.log('iceconnectionstatechange');
        });
        peerConnection.addEventListener('negotiationneeded', function() {
        	console.log('negotiationneeded');
        });
        peerConnection.addEventListener('signalingstatechange', function() {
        	console.log('signalingstatechange');
        });
    };
 
    this.connect = function() {
        peerConnection.createOffer(function(offer) {
            peerConnection.setLocalDescription(offer);
 
            var description = JSON.stringify(offer);
            WebRTCHelper.sendSignal(2, description);
        });
    };
 
    this.acceptOffer = function(offer) {
        peerConnection.setRemoteDescription(new RTCSessionDescription(offer));
        peerConnection.createAnswer(function(answer) {
            peerConnection.setLocalDescription(answer);
            var description = JSON.stringify(answer);
 
            WebRTCHelper.sendSignal(3, description);
        });
    };
 
    this.acceptAnswer = function(answer) {
        peerConnection.setRemoteDescription(new RTCSessionDescription(answer));
    };
 
    this.addIceCandidate = function(candidate) {
    	if (candidate) {
    		peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
    	}
    };
    
    this.close = function() {
    	peerConnection && peerConnection.close();
    	remoteVideo && remoteVideo.stop();
    }
 
    init();
 
};
 
var WebSocketHelper = function(url, callback) {
    var ws = null;
    var url = url;
    var readyState = new Array("正在连接", "已建立连接", "正在关闭连接", "已关闭连接");
    var ct_tip = document.getElementById("ct_tip_mes");
 
    var init = function() {
        ws = new WebSocket(url);
        ws.onmessage = onmessage;
        ws.onerror = onerror;
        ws.onopen = onopen;
        ws.onclose = onclose;
    };
    
    var onopen = function() {
    	ct_tip && (ct_tip.innerHTML = readyState[ws.readyState]);
    };
 
    var onmessage = function(message) {
        callback(JSON.parse(message.data));
    };
    
    var onclose = function() {
    	ct_tip.innerHTML = readyState[webSocket.readyState];
		var isChrome = window.navigator.userAgent.indexOf("Chrome") !== -1;
		if (isChrome) {
			window.location.href = "action-user-login.html";
		}
    };
 
    this.send = function(data) {
        ws.send(data);
    };
 
    init();
};


var WebRTCHelper = {
	rtcConnects: {},
	localStream: null,
	getOrCreateRtcConnect: function (userId) {
        var rtcConnect = this.rtcConnects[userId];
        if (typeof (rtcConnect) == 'undefined') {
            rtcConnect = new RtcConnect(userId, webSocket);
            this.rtcConnects[userId] = rtcConnect;
        }
        return rtcConnect;
    },
	createLocalVideo: function(callback) {
		if (!this.localStream) {
    		navigator.getMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia; 
            navigator.getMedia({ 'video': true, 'audio': true }, function(stream) {
            	window.URL = window.URL || window.webkitURL || window.mozURL || window.msURL;
                var src = window.URL.createObjectURL(stream);
                var video = $("<video />").attr("src", src).attr("controls", "controls");
                var close = $("<span />").addClass("close");
                $("<div />").addClass("videoContainer videoLocal").append(video).append(close).appendTo($("body"));
                $("div.videoLocal").draggable();
                $("div.videoLocal .close").click(function() {
                	if (window.confirm('你确定要关闭视频通话吗？')) {
                		WebRTCHelper.closeRtcConnects();
                    	WebRTCHelper.sendSignal(5);	
                	}
                });
                video[0].play();
                WebRTCHelper.localStream = stream;
                callback && callback();
            }, function (error) { 
            	console.error(error); 
            });
    	} else {
    		callback && callback();
    	}
	},
	closeRtcConnects: function() {
		if (this.localStream) {
			$("div.videoContainer").remove();
			this.localStream.stop();
			this.localStream = null;
			for (var key in this.rtcConnects) {
				var rtcConnect = this.rtcConnects[key];
				rtcConnect.close();
				delete this.rtcConnects[key]
			}
		}
	},
	sendSignal: function(rtctype, description) {
		var message = JSON.stringify({ rtctype: rtctype, getter: getter, sender: loginuser, mtype: 10, description: description });
		webSocket.send(message);
	},
	dealMessage: function(m) {
		if (m.rtctype == 5) {
			m.content = '<p>已关闭视频连接</p>';
        	showMessage(m);
        	WebRTCHelper.closeRtcConnects();
		} else {
			this.createLocalVideo(function() {
				var rtcConnect = WebRTCHelper.getOrCreateRtcConnect(m.sender);
		        switch (m.rtctype) {
		            case 0: //Conect
		            	console.log('connect');
		                rtcConnect.connect();
		                break;
		            case 2: //Offer
		            	console.log('acceptOffer');
		                rtcConnect.acceptOffer(m.description);
		                break;
		            case 3: //Answer
		            	console.log('acceptAnswer');
		                rtcConnect.acceptAnswer(m.description);
		                break;
		            case 4: //IceCandidate
		            	console.log('addIceCandidate');
		                rtcConnect.addIceCandidate(m.description);
		                break;
		            default:
		                break;
		        }
			});
		}
	}
};