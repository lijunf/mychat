package com.lucien.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.lucien.entity.Message;
import com.lucien.entity.User;
import com.lucien.factory.UserFactory;
import com.lucien.model.ChatEnv;
import com.lucien.util.Configure;
import com.lucien.util.Constants;
import com.lucien.util.MyUtil;

public class ChatService extends BaseService {

	private ChatEnv env = null;
	
	public ChatService(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
		env = ChatEnv.getInstance();
	}

	/**
	 * 访问主界面
	 * access: action-home.html
	 * @return
	 * @throws IOException 
	 */
	public int home() throws IOException {
		HttpSession session = request.getSession();
		String username = (String) session.getAttribute(Constants.USERKEY);
		if (username != null && !username.startsWith(Configure.visitorprefix)) {
			response.sendRedirect("action-room.html");
		} else {
			page = Configure.basepath + "login.jsp";
		}
		return -1;
	}
	
	/**
	 * 跳到注册页面
	 * access: action-regInput.html
	 * @return
	 */
	public int regInput() {
		page = Configure.basepath + "reg.jsp";
		return -1;
	}
	
	/**
	 * 进入聊天室
	 * 根据模式选择用户聊天室
	 * Configure.mode==1：只支持注册用户聊天
	 * Configure.mode==2：只支持游客（未注册）与注册用户和注册用户与注册用户之间的聊天
	 * Configure.mode==3：只支持游客（未注册）与游客聊天
	 * access: action-room.html
	 * @return
	 * @throws IOException 
	 */
	public int room() throws IOException {
		HttpSession session = request.getSession();
		String username = (String) session.getAttribute(Constants.USERKEY);
		if (username == null) {
			if (Configure.mode == Constants.MODE_VTR2ALL) {
				String referer = request.getHeader("referer");
				if (referer != null && referer.endsWith("/action-home.html")) {
					setVisitorInfo();
					page = Configure.basepath + "room2.jsp";
				} else {
					response.sendRedirect("action-home.html");
					//page = Configure.basepath + "login.jsp";
				}
			} else {
				// 不支持匿名聊天
				// page = Configure.basepath + "login.jsp";
				response.sendRedirect("action-home.html");
			}
		} else {
			User user = UserFactory.getInstance().getUser(username, false);
			if (user != null) {
				page = Configure.basepath + "room1.jsp";
			} else {
				page = Configure.basepath + "room2.jsp";
			}
		}
		return -1;
	}
	
	/**
	 * 获取客服列表
	 * @return 返回json数据，格式[{user.toJson()}...]
	 * @throws IOException 
	 */
	public int kefu() throws IOException {
		HttpSession session = request.getSession();
		String username = (String) session.getAttribute(Constants.USERKEY);
		if (username == null) {
			setVisitorInfo();
		}
		PrintWriter out = response.getWriter();
		out.print(env.getKefu());
		return -1;
	}
	
	/**
	 * 设置游客信息
	 */
	private void setVisitorInfo() {
		String username = MyUtil.getCookieByName(request, Constants.USERKEY);
		if (username == null) {
			username = env.addVisitor();
			MyUtil.addCookie(response, Constants.USERKEY, username, 3600 * 12 * 365);
		} else {
			String name = env.addVisitor(username);
			if (!username.equals(name)) {
				username = name;
				MyUtil.addCookie(response, Constants.USERKEY, username, 3600 * 12 * 365);
			}
		}
		HttpSession session = request.getSession();
		session.setAttribute(Constants.USERKEY, username);
		MyUtil.addCookie(response, "JSESSIONID", session.getId(), session.getMaxInactiveInterval());
	}
	
	/**
	 * 发送消息
	 * 参数中必须带有getter(接收者用户名)、sender(发送者用户名即登陆用户)
	 * access: action-sendMes.html
	 * @return
	 */
	public int sendMes() {
		HttpSession session = request.getSession();
		String username = (String) session.getAttribute(Constants.USERKEY);
		if (username != null) {
			Message message = (Message) MyUtil.requestForObject(Message.class, request);
			if (username.equals(message.getSender())) {
				env.addMessage(message);
			}
		}
		return -1;
	}
	
	/**
	 * 根据发送者和接收者来获取消息
	 * 参数中必须带有getter(接收者用户名即登陆用户)、sender(发送者用户名)
	 * access: action-getMes.html
	 * @return 返回json数据，格式：[{mesid: ,content: ,sendTime: ,type: },{...}...]
	 * @throws IOException 
	 */
	public int getMes() throws IOException {
		HttpSession session = request.getSession();
		String loginusr = (String) session.getAttribute(Constants.USERKEY);
		PrintWriter out = response.getWriter();
		if (loginusr != null) {
			String getter = request.getParameter("getter");
			String sender = request.getParameter("sender");
			String mtype = request.getParameter("mtype");
			int type = mtype == null ? 0 : Integer.parseInt(mtype);
			StringBuilder buf = new StringBuilder();
			getter = getter == null ? loginusr : getter;
			if (getter.equals(loginusr)) {
				List<Message> mes = env.getMessage(getter, sender, type);
				String mark = "";
				buf.append("[");
				if (mes != null) {
					for (Message m : mes) {
						buf.append(mark).append(m.toJson());
						mark = ",";
					}
				}
				buf.append("]");
			}
			out.print(buf.toString());
			MyUtil.disableCache(response);
		} else {
			out.print("invalidate");
		}
		return -1;
	}
	
	/**
	 * 启动聊天窗口
	 * access: action-launch.html
	 * @return
	 * @throws IOException 
	 */
	public int launch() throws IOException {
		HttpSession session = request.getSession();
		String loginuser = (String) session.getAttribute(Constants.USERKEY);
		if (loginuser == null) {
			response.sendRedirect("action-room.html");
		} else {
			page = Configure.basepath + "dialog.jsp";
		}
		return -1;
	}
	
	/**
	 * 获取房间中的所有信息，json格式：{mes:[{系统消息（好友验证、同意加好友、拒绝加好友、好友上线...）}],senders:[{用户json}]}
	 * access: action-getRoomMes.html
	 * @return
	 * @throws IOException 
	 */
	public int getRoomMes() throws IOException {
		HttpSession session = request.getSession();
		String loginuser = (String) session.getAttribute(Constants.USERKEY);
		PrintWriter out = response.getWriter();
		if (!MyUtil.isEmpty(loginuser)) {
			StringBuilder json = new StringBuilder();
			UserFactory userFactory = UserFactory.getInstance();
			
			json.append("({");
			json.append("systems:[");
			Set<String> systems = env.getSenders(loginuser, 1);
			String mark = "";
			if (systems != null) {
				for (String username : systems) {
					User user = userFactory.getUser(username);
					if (user != null) {
						json.append(mark).append(user.toSimpleJson());
						mark = ",";
					}
				}
			}
			json.append("],");
			Set<String> senders = env.getSenders(loginuser, 0);
			json.append("unreads:[");
			if (senders != null) {
				mark = "";
				for (String sender : senders) {
					User user = userFactory.getUser(sender);
					if (user != null) {
						json.append(mark).append(user.toSimpleJson());
					} else {
						json.append(mark).append("{")
							.append("username:'").append(sender).append("',")
							.append("nickname:'游客',")
							.append("header:'style/base/images/header/00.png'")
							.append("}");
					}
					mark = ",";
				}
			}
			json.append("],");
			List<Message> mes = env.getStatusMes(loginuser);
			json.append("status:[");
			mark = "";
			for (Message m : mes) {
				json.append(mark).append(m.toJson());
				mark = ",";
			}
			json.append("]})");
			out.print(json);
			MyUtil.disableCache(response);
		} else {
			out.print("invalidate");
		}
		return -1;
	}
	
}
