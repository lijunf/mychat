package com.lucien.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.lucien.database.MyDB;
import com.lucien.database.MyPage;
import com.lucien.entity.Friend;
import com.lucien.entity.Group;
import com.lucien.entity.User;
import com.lucien.factory.GroupFactory;
import com.lucien.factory.UserFactory;
import com.lucien.model.ChatEnv;
import com.lucien.util.Configure;
import com.lucien.util.Constants;
import com.lucien.util.MyUtil;

public class UserService extends BaseService {
	
	private ChatEnv env = null;

	public UserService(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
		env = ChatEnv.getInstance();
	}
	
	/**
	 * 后台用户登录
	 * access: action-user-login.html
	 * @return
	 * @throws IOException 
	 */
	public int login() throws IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		if (username != null && password != null) {
			HttpSession session = request.getSession();
			// 先退出已经登录的用户
			String loginuser = (String) session.getAttribute(Constants.USERKEY);
			if (loginuser != null) {
				session.removeAttribute(Constants.USERKEY);
				env.removeUser(loginuser);
			}
			User user = UserFactory.getInstance().getUser(username, password);
			if (user != null && (password.equals(user.getPassword()) || MyUtil.md5(password).equals(user.getPassword()))) {
				session.setAttribute(Constants.USERKEY, username);
				MyUtil.addCookie(response, "JSESSIONID", session.getId(), session.getMaxInactiveInterval());
				HttpSession _session = user.getSession();
				if (_session != null && _session != session) {
					user.setSession(null);
					try {
						_session.invalidate();
					} catch (Exception e) {}
				}
				user.setSession(session);
				env.addLoginusr(user);
				response.sendRedirect("action-room.html");
				/*boolean result = env.addLoginusr(user);
				if (result) {
					response.sendRedirect("action-room.html");
				} else {
					request.setAttribute("message", "您的账号已经登录！");
					page = Configure.basepath + "login.jsp";
				}*/
			} else {
				request.setAttribute("message", "用户名或密码不正确！");
				page = Configure.basepath + "login.jsp";
			}
		} else {
			// page = Configure.basepath + "login.jsp";
			response.sendRedirect("action-home.html");
		}
		return -1;
	}
	
	/**
	 * 用户注册
	 * access: action-user-reg.html
	 * @return
	 * @throws IOException 
	 */
	public int reg() throws IOException {
		HttpSession session = request.getSession();
		User user = (User) MyUtil.requestForObject(User.class, request);
		boolean result = UserFactory.getInstance().saveUser(user);
		if (result) {
			session.setAttribute(Constants.USERKEY, user.getUsername());
			env.addLoginusr(user);
			//request.setAttribute("sysmes", "你的登录账号<font color='red'>" + user.getUsername() + "</font>，请牢记该账号.<br>下次登录系统只能用该账号，昵称无效。");
			response.sendRedirect("action-room.html?first=1");
		} else {
			request.setAttribute("message", "注册失败");
			page = Configure.basepath + "reg.jsp";
		}
		return -1;
	}
	
	/**
	 * 进入用户信息页面
	 * access: action-user-account.html
	 * @return
	 * @throws IOException 
	 */
	public int account() throws IOException {
		if (!MyUtil.isEmpty(loginuser())) {
			page = Configure.basepath + "account.jsp";
		} else {
			response.sendRedirect("action-regInput.html");
		}
		return -1;
	}
	
	/**
	 * 根据登陆用户获取聊天室用户列表
	 * 根据不同的配置模式，返回不同的jso数据格式
	 * 参数中必须有mode
	 * mode==1 : {senders:[...],friends:[...]}
	 * mode==2 : {size:10,users:['loginuser','1100000','0100001'.....]}
	 * mode==3 : {size:10,users:['loginuser','1100000','0100001'.....]}
	 * access: action-user-friendlist.html
	 * @return
	 * @throws IOException 
	 */
	public int friendlist() throws IOException {
		PrintWriter out = response.getWriter();
		String mode = request.getParameter("mode");
		if (mode != null) {
			out.print(env.friendsToJson(loginuser(), Integer.parseInt(mode)));
			MyUtil.disableCache(response);
		}
		return -1;
	}
	
	/**
	 * 退出聊天室
	 * access: action-user-exit.html
	 * @return
	 * @throws IOException 
	 */
	public int exit() throws IOException {
		HttpSession session = request.getSession();
		/*String loginuser = (String) session.getAttribute(Constants.USERKEY);
		if (loginuser != null) {
			session.removeAttribute(Constants.USERKEY);
			env.removeUser(loginuser);
		}*/
		User user = getUser();
		if (user != null) {
			user.setSession(null);
		}
		session.invalidate();
		response.sendRedirect("action-home.html");
		return -1;
	}
	
	/**
	 * 根据查找条件查找用户
	 * 参数中必须有type(查询类型)、pagesize(每页大小)、current(当前第几页)
	 * type==1:表示根据用户名(username)查询
	 * type==2:表示根据用户昵称(nickname)查询
	 * type==3:表示根据用户真实姓名(realname)查询
	 * type==4:表示根据用户地址(address)查询
	 * type==5:表示根据用户性别(sex)查询
	 * type==6:表示根据用户年龄(age)查询
	 * access: action-user-userlist.html
	 * @return 返回json数据，格式：{current:当然第几页,pages:一共多少页,users:[{username:,nickname:...},{}]}
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public int userlist() throws IOException {
		String loginuser = loginuser();
		if (loginuser != null) {
			int type = Integer.parseInt(request.getParameter("type"));
			int pagesize = Integer.parseInt(request.getParameter("pagesize"));
			int current = Integer.parseInt(request.getParameter("current"));
			UserFactory userFactory = UserFactory.getInstance();
			MyPage myPage = null;
			String term = request.getParameter("term");
			switch (type) {
				case 1:
					String username = request.getParameter("username");
					myPage = userFactory.searchUser(loginuser, "username", username, term, pagesize, current);
					break;
				case 2:
					String nickname = request.getParameter("nickname");
					myPage = userFactory.searchUser(loginuser, "nickname", nickname, term, pagesize, current);
					break;
				case 3:
					String realname = request.getParameter("realname");
					myPage = userFactory.searchUser(loginuser, "realname", realname, term, pagesize, current);
					break;
				case 4:
					String address = request.getParameter("address");
					myPage = userFactory.searchUser(loginuser, "address", address, term, pagesize, current);
					break;
				case 5:
					String sex = request.getParameter("sex");
					myPage = userFactory.searchUser(loginuser, "sex", sex, term, pagesize, current);
					break;
				case 6:
					String age = request.getParameter("age");
					myPage = userFactory.searchUser(loginuser, "age", age, term, pagesize, current);
					break;
			}
			if (myPage != null) {
				PrintWriter out = response.getWriter();
				StringBuilder json = new StringBuilder();
				json.append("({");
				json.append("current:").append(myPage.current()).append(",");
				json.append("pages:").append(myPage.pages()).append(",");
				List<User> users = (List<User>) myPage.getRs();
				String mark = "";
				json.append("users:[");
				if (users != null) {
					for (User user : users) {
						json.append(mark).append(user.toJson());
						mark = ",";
					}
				}
				json.append("]");
				json.append("})");
				out.print(json);
			}
		}
		return -1;
	}
	
	/**
	 * 获取登录用户的所有组
	 * 返回json数据，格式：[{gid:,gname:},{...},...]
	 * access: action-user-groups.html
	 * @return
	 * @throws IOException 
	 */
	public int groups() throws IOException {
		User user = getUser();
		if (user != null) {
			PrintWriter out = response.getWriter();
			StringBuilder json = new StringBuilder();
			json.append("[");
			String mark = "";
			for (Group group : user.getGroups().values()) {
				json.append(mark).append(group.toJson());
				mark = ",";
			}
			json.append("]");
			out.print(json);
		}
		return -1;
	}
	
	/**
	 * 判断是否已经是好友，参数中必须有username(要判断的用户名)
	 * access: action-user-isfriend.html
	 * @return
	 * @throws IOException 
	 */
	public int isfriend() throws IOException {
		User user = getUser();
		if (user != null) {
			String username = request.getParameter("username");
			PrintWriter out = response.getWriter();
			if (user.hasFriend(username)) {
				out.print("1");
			} else {
				out.print("0");
			}
		}
		return -1;
	}
	
	/**
	 * 添加好友，参数中必须有gid(所属组的id)、username(好友用户名)，notename(备注名称)可选
	 * access: action-user-addfriend.html
	 * @return
	 */
	public int addfriend() {
		User user = getUser();
		if (user != null) {
			Friend friend = (Friend) MyUtil.requestForObject(Friend.class, request);
			if (!user.hasFriend(friend.getUsername())) {
				user.addFriend(friend);
				MyDB db = ChatEnv.getInstance().db;
				if (db != null) {
					db.saveObject(friend);
				}
			}
		}
		return -1;
	}
	
	/**
	 * 添加分组，参数中必须有gname(组名)
	 * access: action-user-addgroup.html
	 * @return
	 * @throws IOException 
	 */
	public int addgroup() throws IOException {
		User user = getUser();
		if (user != null) {
			String gname = request.getParameter("gname");
			if (!MyUtil.isEmpty(gname)) {
				Group group = new Group(gname, user.getUsername());
				GroupFactory groupFactory = GroupFactory.getInstance();
				groupFactory.saveGroup(group);
				user.addGroup(group);
			}
		}
		return groups();
	}
	
	/**
	 * 改变好友所属的分组，参数中必须有gid(原组id)、cgid(改变后的id)、username(好友用户名)
	 * access: action-user-transgroup.html
	 * @return
	 */
	public int transgroup() {
		User user = getUser();
		if (user != null) {
			String gid = request.getParameter("gid");
			String cgid = request.getParameter("cgid");
			String username = request.getParameter("username");
			boolean result = user.transgroup(Integer.parseInt(gid), username, Integer.parseInt(cgid));
			if (result) {
				String sql = "update " + Constants.dbprefix + "friend set gid=" + cgid 
						+ " where gid=" + gid + " and username='" + username + "'";
				MyDB db = ChatEnv.getInstance().db;
				if (db != null) {
					db.executeStatement(sql);
				}
			}
		}
		return -1;
	}
	
	/**
	 * 删除分组，参数中必须有gid(组id)
	 * access: action-user-removegroup.html
	 * @return
	 */
	public int removegroup() {
		User user = getUser();
		if (user != null) {
			String gid = request.getParameter("gid");
			boolean result = user.removegroup(Integer.parseInt(gid));
			if (result) {
				String sql = "delete from " + Constants.dbprefix + "group where gid=" + gid;
				MyDB db = ChatEnv.getInstance().db;
				if (db != null) {
					db.executeStatement(sql);
				}
			}
		}
		return -1;
	}
	
	/**
	 * 重命名组，参数中必须有gid(组id)、gname(新的组名)
	 * access: action-user-renamegroup.html
	 * @return
	 */
	public int renamegroup() {
		User user = getUser();
		if (user != null) {
			String gid = request.getParameter("gid");
			String gname = request.getParameter("gname");
			if (user.renamegroup(Integer.parseInt(gid), gname)) {
				String sql = "update " + Constants.dbprefix + "group set gname='" + gname + "' where gid=" + gid;
				MyDB db = ChatEnv.getInstance().db;
				if (db != null) {
					db.executeStatement(sql);
				}
			}
		}
		return -1;
	}
	
	/**
	 * 修改好友备注名称，参数中必须有gid(组id)、username(好友用户名 )、notename(新的好友备注名称)
	 * access: action-user-renotename.html
	 * @return
	 */
	public int renotename() {
		User user = getUser();
		if (user != null) {
			String gid = request.getParameter("gid");
			String username = request.getParameter("username");
			String notename = request.getParameter("notename");
			if (user.renotename(Integer.parseInt(gid), username, notename)) {
				String sql = "update " + Constants.dbprefix + "friend set notename='" + notename 
						+ "' where gid=" + gid + " and username='" + username + "'";
				MyDB db = ChatEnv.getInstance().db;
				if (db != null) {
					db.executeStatement(sql);
				}
			}
		}
		return -1;
	}
	
	/**
	 * 删除好友，参数中必须有gid(组id)、username(好友用户名 )
	 * access: action-user-removefriend.html
	 * @return
	 */
	public int removefriend() {
		User user = getUser();
		if (user != null) {
			String gid = request.getParameter("gid");
			String username = request.getParameter("username");
			if (user.removefriend(Integer.parseInt(gid), username)) {
				String sql = "delete from " + Constants.dbprefix + "friend "
						+ "where gid=" + gid + " and username='" + username + "'";
				MyDB db = ChatEnv.getInstance().db;
				if (db != null) {
					db.executeStatement(sql);
				}
			}
		}
		return -1;
	}
	
	/**
	 * 获取登录用户名
	 * @return
	 */
	private String loginuser() {
		HttpSession session = request.getSession();
		String loginuser = (String) session.getAttribute(Constants.USERKEY);
		return loginuser;
	}
	
	/**
	 * 获取登录用户实体对象
	 * @return
	 */
	private User getUser() {
		UserFactory userFactory = UserFactory.getInstance();
		User user = userFactory.getUser(loginuser());
		return user;
	}
}
