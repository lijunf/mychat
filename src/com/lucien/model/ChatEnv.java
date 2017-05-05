package com.lucien.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.catalina.websocket.WsOutbound;

import com.lucien.database.MyDB;
import com.lucien.entity.Message;
import com.lucien.entity.User;
import com.lucien.factory.UserFactory;
import com.lucien.util.Configure;
import com.lucien.util.Constants;
import com.lucien.util.MyUtil;

/**
 * 全局环境类	2013-06-01
 * @author Lucien
 *
 */
public class ChatEnv {
	
	public final Map<String, Map<String, WsOutbound>> outbounds = new ConcurrentHashMap<String, Map<String, WsOutbound>>();
	
	private Map<String, User> loginusr = null; 									///< 登录的后台用户列表
	private Vector<User> kefu = null;											///< 登录的客服
	private Map<String, Object> visitors = null; 								///< 游客列表
	private int vsize = 1;														///< 访问网站的游客计数器
	
	private Map<String, Vector<Message>> unreads;								///< 未被读取的消息容器，以消息接收者作为key，存放message.mtype为0的未读消息
	private Map<String, Vector<Message>> systems;								///< 系统消息，存放message.mtype为1,2,3的未读消息
	private Map<String, Vector<Message>> status;								///< 用户改变状态消息，存放message.mtype为4,5的未读消息
	private Map<String, Vector<Message>> crowds;								///< mode == 2时，聊天室里面的群消息
	private Queue<Message> history;												///< mode == 2时，聊天历史记录，保存数量可以在configure.properties中配置	
	private Queue<Message> readMes = null;										///< 已经读过的消息
	
	public MyDB db = null;														///< 数据库操作类
	
	private ChatEnv() {
		loginusr = new ConcurrentHashMap<String, User>();
		kefu = new Vector<User>();
		visitors = new ConcurrentHashMap<String, Object>();
		unreads = new ConcurrentHashMap<String, Vector<Message>>();
		readMes = new LinkedBlockingDeque<Message>();
		systems = new ConcurrentHashMap<String, Vector<Message>>();
		status = new ConcurrentHashMap<String, Vector<Message>>();
		crowds = new ConcurrentHashMap<String, Vector<Message>>();
		history = new LinkedBlockingDeque<Message>(Configure.historySize);
		if (Configure.dbenabled) {
			db = new MyDB();
			db.setSystem(Configure.system);
			db.setDBType(Configure.dbtype);
			db.setDBHost(Configure.dbhost);
			db.setDBName(Configure.dbname);
			db.setLogUser(Configure.dbuser);
			db.setLogPwd(Configure.dbpwd);
			db.setDbLinkSize(Configure.dbLinkSize);
		}
		vsize = Configure.visitorSize;
	}

	private static final ChatEnv env = new ChatEnv();
	
	public static ChatEnv getInstance() {
		return env;
	}
	
	/**
	 * 自动产生游客用户名，并将游客添加的到游客列表中
	 * @return 返回游客姓名
	 */
	public String addVisitor() {
		String username = Configure.visitorprefix + vsize;
		username = addVisitor(username);
		return username;
	}
	
	/**
	 * 将游客添加的到游客列表中
	 * @param username
	 */
	public String addVisitor(String username) {
		synchronized (visitors) {
			while (visitors.containsKey(username)) {
				username = Configure.visitorprefix + ++vsize;
			}
			vsize++;
		}
		visitorOnline(username);
		MyUtil.debug(username + "登录聊天室");
		return username;
	}
	
	/**
	 * 游客上线
	 * @param username
	 */
	private void visitorOnline(String username) {
		visitorStatusUpdate(username, Constants.MESSAGE_TYPE_ONLINE);
		visitors.put(username, Constants.PRESENT);
	}
	
	/**
	 * 更新游客状态
	 * @param username
	 * @param type
	 */
	private void visitorStatusUpdate(String username, int type) {
		Set<String> names = visitors.keySet();
		for (String name : names) {
			Message message = new Message();
			message.setSender(username);
			message.setGetter(name);
			message.setMtype(type);
			toCrowd(message, null);
		}
	}
	
	/**
	 * 向message缓冲池中添加未读消息
	 * @param message
	 */
	public void addMessage(Message message) {
		if (message != null) {
			if (Constants.MESSAGE_TYPE_ISINPUT == message.getMtype() 
					|| Constants.MESSAGE_TYPE_NOINPUT == message.getMtype()) {
				addStatusMes(message);
				return;
			}
			MyUtil.info(message.toStr());
			// 过滤消息内容
			message.filterIllegalContent();
			WsOutbound outbound = getOutbound(message);
			if (outbound != null) {
				message.websocket = true;
				MyUtil.sendMessage(outbound, message.toJson());
				return;
			} else {
				Map<String, WsOutbound> bounds = outbounds.get(message.getGetter());
				if (bounds != null) {
					WsOutbound bound = bounds.get(Constants.TO_ALL);
					if (message.getMtype() == Constants.MESSAGE_TYPE_NORMALMES ||
							message.getMtype() == Constants.MESSAGE_TYPE_WEBRTC) {
						Message m = new Message(message.getSender(), message.getGetter(), "", Constants.MESSAGE_TYPE_HASMES, true);
						MyUtil.sendMessage(bound, m.toJson());
					} else {
						MyUtil.sendMessage(bound, message.toJson());
						if (!message.is123()) {
							return;
						}
					}
				}
			}
			if (message.getMtype() > Constants.MESSAGE_TYPE_NORMALMES && message.getMtype() < Constants.MESSAGE_TYPE_ONLINE) {
				Vector<Message> mes = systems.get(message.getGetter());
				if (mes == null) {
					mes = new Vector<Message>();
					systems.put(message.getGetter(), mes);
				}
				mes.add(message);
			} else {
				if (message.getMtype() == Constants.MESSAGE_TYPE_CROWDS) {
					for (String visitor : visitors.keySet()) {
						toCrowd(message, visitor);
					}
					for (String visitor : loginusr.keySet()) {
						toCrowd(message, visitor);
					}
					addHistory(message);
				} else {
					Vector<Message> mes = unreads.get(message.getGetter());
					if (mes == null) {
						mes = new Vector<Message>();
						unreads.put(message.getGetter(), mes);
					}
					message.websocket = false;
					mes.add(message);
				}
			}
		}
	}
	
	/**
	 * 获取WsOutbound对象
	 * @param message
	 * @return
	 */
	private WsOutbound getOutbound(Message message) {
		WsOutbound outbound = null;
		if (message != null) {
			Map<String, WsOutbound> bounds = outbounds.get(message.getGetter());
			if (bounds != null) {
				outbound = bounds.get(message.getSender());
			}
		}
		return outbound;
	}

	/**
	 * 处理群消息，将消息发给指定接收着
	 * @param message	群消息
	 * @param getter	接收者
	 */
	private void toCrowd(Message message, String getter) {
		if (getter == null || !getter.equals(message.getSender())) {
			Message m = new Message(message);
			if (getter != null) m.setGetter(getter);
			m.setSender(Constants.TO_ALL);
			WsOutbound outbound = getOutbound(m);
			m.setSender(message.getSender());
			if (outbound != null) {
				m.websocket = true;
				MyUtil.sendMessage(outbound, m.toJson());
			} else {
				m.websocket = false;
				if (m.getMtype() == Constants.MESSAGE_TYPE_CROWDS) {
					addCrowdsMes(m);
				}
			}
		}
	}
	
	/**
	 * 向历史记录中添加消息
	 * 此处考虑是否需要同步
	 * @param message
	 */
	private void addHistory(Message message) {
		if (Configure.historySize > 0) {
			if (history.size() >= Configure.historySize) {
				history.poll();
			}
			history.add(message);
		}
	}
	
	/**
	 * 获取未读聊天记录,mtype==0时从unreads里面获取，其它值时从sysmes里获取
	 * @param getter	收信人
	 * @param sender	发送人
	 * @param mtype		消息类型
	 * @return			返回信息列表
	 */
	public List<Message> getMessage(String getter, String sender, int mtype) {
		List<Message> result = null;
		if (getter != null && sender != null) {
			result = new ArrayList<Message>();
			Vector<Message> meses = mtype == 0 ? unreads.get(getter) : systems.get(getter);
			if (meses != null) {
				for (Message m : meses) {
					if (sender.equals(m.getSender())) {
						m.setIsread(true);
						result.add(m);
						if (Configure.dbenabled) {
							readMes.add(m);
						}
					}
				}
				meses.removeAll(result);
			}
		}
		return result;
	}
	

	/**
	 * 添加用户改变状态的消息,考虑是否需要线程同步
	 * @param message
	 */
	public void addStatusMes(Message message) {
		if (message != null) {
			WsOutbound outbound = getOutbound(message);
			if (outbound != null) {
				message.websocket = true;
				MyUtil.sendMessage(outbound, message.toJson());
				return;
			} else {
				if (message.isStatusMes()) {
					Map<String, WsOutbound> bounds = outbounds.get(message.getGetter());
					if (bounds != null) {
						outbound = bounds.get(Constants.TO_ALL);
						message.websocket = true;
						MyUtil.sendMessage(outbound, message.toJson());
						return;
					}
				} else {
					return;
				}
			}
			Vector<Message> mes = status.get(message.getGetter());
			if (mes == null) {
				mes = new Vector<Message>();
				status.put(message.getGetter(), mes);
			}
			int count = 0;
			Message _m = null;
			for (Message m : mes) {
				if (message.getSender().equals(m.getSender())) {
					count++;
					_m = m;
				}
			}
			if (count == 0) {
				mes.add(message);
			}
			if (message.getMtype() == Constants.MESSAGE_TYPE_OFFLINE && count == 1) {
				mes.remove(_m);
			}
		}
	}
	
	/**
	 * 获取好友改变用户的信息
	 * @param getter
	 * @return
	 */
	public List<Message> getStatusMes(String getter) {
		List<Message> result = null;
		if (getter != null) {
			result = new ArrayList<Message>();
			Vector<Message> meses = status.get(getter);
			if (meses != null) {
				result.addAll(meses);
				meses.removeAll(result);
			}
		}
		return result;
	}
	
	/**
	 * 获取用户的群信息
	 * @param getter
	 * @return
	 */
	public List<Message> getCrowdsMes(String getter) {
		List<Message> result = null;
		if (getter != null) {
			result = new ArrayList<Message>();
			Vector<Message> meses = crowds.get(getter);
			if (meses != null) {
				result.addAll(meses);
				meses.removeAll(result);
			}
		}
		return result;
	}
	
	/**
	 * 向crowds中添加消息
	 * @param message
	 * @return
	 */
	public void addCrowdsMes(Message message) {
		Vector<Message> mes = crowds.get(message.getGetter());
		if (mes == null) {
			mes = new Vector<Message>();
			crowds.put(message.getGetter(), mes);
		}
		mes.add(message);
	}
	
	/**
	 * 获取历史聊天记录
	 * @param getter
	 * @return
	 */
	public List<Message> getHistoryMes(String getter) {
		List<Message> result = null;
		if(getter != null && Configure.historySize > 0) {
			result = new ArrayList<Message>();
			result.addAll(history);
		}
		return result;
	}
	
	/**
	 * sender是否有给getter发送过消息
	 * @param getter
	 * @param sender
	 * @return
	 */
	public boolean hasMessage(String getter, String sender) {
		boolean result = false;
		if (getter != null && sender != null) {
			Vector<Message> mes = unreads.get(getter);
			if (mes != null) {
				for (Message m : mes) {
					if (sender.equals(m.getSender())) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * 添加后台登录用户
	 * @param username
	 */
	public boolean addLoginusr(User user) {
		boolean result = false;
		if (user != null) {
			// user.online();
			if (user.getKefuFlag() > 0) {
				if (!kefu.contains(user)) {
					kefu.add(user);
				}
				result = true;
			} else {
				if (!loginusr.containsKey(user.getUsername())) {
					loginusr.put(user.getUsername(), user);
					result = true;
				}
			}
		}
		return result;
	}
	
	/**
	 * 获取登录用户
	 * @return
	 */
	public Set<String> getLoginusr() {
		return loginusr.keySet();
	}
	
	/**
	 * 判断是否是注册用户登陆
	 * @param username
	 * @return
	 */
	public boolean isLoginusr(String username) {
		return loginusr.containsKey(username);
	}
	
	/**
	 * 删除聊天室中的指定用户
	 * @param username
	 */
	public void removeUser(String username) {
		if (username != null) {
			User user = UserFactory.getInstance().getUser(username);
			if (user != null) {
				user.offline();
			} else {
				clearMessage(username);
				visitorStatusUpdate(username, Constants.MESSAGE_TYPE_OFFLINE);
			}
			loginusr.remove(username);
			visitors.remove(username);
		}
	}
	
	/**
	 * 获取登录聊天室的所有用户
	 * @param type	取值1,2,3		1代表去后台登录用户列表，2代表取游客列表，3代表所有登录用户
	 * @return
	 */
	public Set<String> getUsers(int type) {
		Set<String> users = new HashSet<String>();
		if ((type & 0x1) == 1) {
			users.addAll(loginusr.keySet());
		}
		if ((type & 0x2) == 2) {
			users.addAll(visitors.keySet());
		}
		return users;
	}
	
	/**
	 * 获取登录游客
	 * @return
	 */
	public Set<String> getVisitors() {
		return visitors.keySet();
	}
	
	/**
	 * 获取当前聊天室中在线人数
	 * @return
	 */
	public int usernumbers() {
		return visitors.size() + loginusr.size();
	}
	
	/**
	 * 接收者获取当前有发送信息给接收者的人员集合
	 * @param getter信息接收者
	 * @param mtype 消息类型
	 * @return
	 */
	public Set<String> getSenders(String getter, int mtype) {
		Set<String> senders = null;
		if (getter != null) {
			Vector<Message> mes = mtype == 0 ? unreads.get(getter) : systems.get(getter);
			if (mes != null && mes.size() > 0) {
				senders = new HashSet<String>();
				for (Message m : mes) {
					senders.add(m.getSender());
				}
			}
		}
		return senders;
	}
	
	/**
	 * 根据登录用户名，将用户的好友转换为json字串
	 * @param loginuser	登录用户名
	 * @param mode	模式
	 * @return
	 */
	public String friendsToJson(String loginuser, int mode) {
		StringBuilder json = new StringBuilder();
		if (!MyUtil.isEmpty(loginuser)) {
			switch (mode) {
				case Constants.MODE_EMP2EMP:
					// 返回json数据格式：[{id:1, pId:0, name:"我的好友", isParent: true},{id:"100000", pId:1, name:"王五"}。。。]
					UserFactory userFactory = UserFactory.getInstance();
					json.append("[");
					User user = userFactory.getUser(loginuser);
					if (user != null) {
						json.append(user.toTreeJson());
					}
					json.append("]");
					break;
				case Constants.MODE_VTR2ALL:
					// 返回json格式数据：{size:10,users:['loginuser','1100000','0100001'.....]}
					Set<String> users = getUsers(3);
					users.remove(loginuser);
					json.append("({")
						.append("size:").append(usernumbers()).append(",")
						.append("users:[")
						.append("'").append(loginuser).append("'");
					for (String username : users) {
						if (hasMessage(loginuser, username)) {
							username = "1" + username;
						} else {
							username = "0" + username;
						}
						json.append(",'").append(username).append("'");
					}
					json.append("],");
					json.append("meses:");
					List<Message> mes = env.getCrowdsMes(loginuser);
					String mark = "";
					json.append("[");
					if (mes != null) {
						for (Message m : mes) {
							json.append(mark).append(m.toJson());
							mark = ",";
						}
					}
					json.append("]");
					json.append("})");
					break;
			}
		}
		return json.toString();
	}
	
	/**
	 * 获取客服列表
	 * @return 返回json数据，格式[{user.toJson()}...]
	 */
	public String getKefu() {
		StringBuilder strs = new StringBuilder();
		char mark = ' ';
		strs.append("[");
		if (kefu.size() != 0) {
			for (User user : kefu) {
				strs.append(mark);
				strs.append(user.toSimpleJson());
				mark = ',';
			}
		}
		strs.append("]");
		return strs.toString();
	}
	
	/**
	 * 清除用户消息
	 * @param username
	 */
	private void clearMessage(String username) {
		if (!MyUtil.isEmpty(username)) {
			crowds.remove(username);
		}
	}
	
	/**
	 * 将消息保存
	 */
	public void saveMessage() {
		List<Message> meses = new ArrayList<Message>();
		String sql = "insert into ct_message (sender,getter,content,sendtime,mtype,isread) values (?,?,?,?,?,?)";
		int count = 0;
		while(true) {
			try {
				while (!readMes.isEmpty()) {
					meses.add(readMes.poll());
				}
				int size = meses.size();
				if (size > 0) {
					if (size > Configure.readMesBufferSize || count >= Configure.readMesBufferTimeout) {
						List<List<String>> params = new ArrayList<List<String>>();
						for (Message mes : meses) {
							if (mes.getContent() != null) {
								params.add(mes.toList());
							}
						}
						db.executeBatchStatement(sql, params);
						meses.clear();
						count = 0;
					} else {
						count++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
