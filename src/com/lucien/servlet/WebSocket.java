package com.lucien.servlet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

import com.lucien.entity.Message;
import com.lucien.model.ChatEnv;
import com.lucien.util.Configure;
import com.lucien.util.Constants;
import com.lucien.util.MyUtil;

@WebServlet("/websocket")
public class WebSocket extends WebSocketServlet {
	
	private ChatEnv env = ChatEnv.getInstance();

	private static final long serialVersionUID = -1058445282919079067L;

	@Override
	protected StreamInbound createWebSocketInbound(String arg0,	HttpServletRequest request) {
		HttpSession session = request.getSession();
		String loginuser = (String) session.getAttribute(Constants.USERKEY);
		String username = (String) request.getParameter(Constants.USERKEY);
		username = username == null ? Constants.TO_ALL : MyUtil.convertCharSet(username, Configure.encoding);
		return new ChatMessageInbound(loginuser, username);
	}

	class ChatMessageInbound extends MessageInbound {
		
		private String loginuser;
		private String username;
		
		public ChatMessageInbound(String loginuser, String username) {
			this.loginuser = loginuser;
			this.username = username;
		}
		
		@Override
		protected void onOpen(WsOutbound outbound) {
			if (loginuser != null && username != null) {
				Map<String, WsOutbound> outbounds = env.outbounds.get(loginuser);
				if (outbounds == null) {
					outbounds = new ConcurrentHashMap<String, WsOutbound>();
					env.outbounds.put(loginuser, outbounds);
				}
				outbounds.put(username, outbound);
			}
			super.onOpen(outbound);
		}

		@Override
		protected void onClose(int status) {
			if (loginuser != null) {
				Map<String, WsOutbound> outbounds = env.outbounds.get(loginuser);
				if (outbounds != null) {
					outbounds.remove(username);
				}
				// 告诉对方（username）我的连接已经断掉，做视频中断处理
				// Message message = new Message(loginuser, username, "", 10, 5, true);
				// env.addMessage(message);
			}
			super.onClose(status);
		}

		@Override
		protected void onBinaryMessage(ByteBuffer buffer) throws IOException {
			// TODO Auto-generated method stub
		}

		/**
		 * 接受消息，消息格式为：getter:xxxx,content:yyyy,mtype:1
		 */
		@Override
		protected void onTextMessage(CharBuffer buffer) throws IOException {
			String msg = buffer.toString();
			JSONObject json = JSONObject.fromObject(msg);
			// Map<String, String> msgs = MyUtil.map(msg, ",", ":");
			// Message message = new Message(loginuser, json.getString("getter"), json.getString("content"), Integer.parseInt(json.getString("mtype")), true);
			Message message = (Message) JSONObject.toBean(json, Message.class);
			env.addMessage(message);
		}
	}
}
