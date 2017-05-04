package com.lucien.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.lucien.database.MyDB;
import com.lucien.model.ChatEnv;
import com.lucien.util.Constants;
import com.lucien.util.MyUtil;

/**
 * 注册用户类，对应数据库ct_User表	2013-06-01
 * 对象建模规则：与数据库字段对应的属性采用private，其它属性采用public或protected，并且属性类型与数据库字段类型必须一一对应
 * @author Lucien
 *
 */
public class User {

	private String username;								///< 用户名系统自动生成，主键
	private String nickname;								///< 用户昵称
	private String password;								///< 密码
	private String email;									///< 注册邮箱
	private boolean sex = true;								///< 性别,true男、false女
	private String header;									///< 用户头像路径
	private String realname;								///< 真实姓名
	private String engname;									///< 英文名
	private int birthday;									///< 生日
	private String address;									///< 地址
	private String phone;									///< 电话
	private String profession;								///< 职业
	private String education;								///< 学历
	private String school;									///< 学校
	private String homepage;								///< 个人主页
	private String autograph;								///< 个性签名
	private String elucidate;								///< 个人说明
	
	private String ipaddr;									///< ip地址
	private Date firsttime = new Date();					///< 注册时间
	private Date lasttime = new Date();						///< 最后一次登录时间
	private int loginnum = 1;								///< 登录次数
	private boolean enabled = true;							///< 用户是否可用
	private int kefuFlag = 0;								///< 客服标志,0不是客服、1咨询客服、2售前客服
	
	protected int status = Constants.USER_STATUS_OFFLINE;	///< 登陆状态，1在线、2离线、离开、忙碌、请勿打扰、隐身
	protected Map<Integer, Group> groups;					///< 用户拥有的所有分组
	protected HttpSession session;								///< 对应的session对象
	
	public User() {
	}
	
	public User(String username, String nickname, String password, String autograph, String header, int kefuFlag, boolean sex) {
		this.username = username;
		this.nickname = nickname;
		this.password = password;
		this.autograph = autograph;
		this.header = header;
		this.kefuFlag = kefuFlag;
		this.sex = sex;
	}
	
	/**
	 * 用户上线，向好友发送在线消息
	 */
	public void online() {
		status = Constants.USER_STATUS_ONLINE;
		sendMesToFriend(Constants.MESSAGE_TYPE_ONLINE);
	}
	
	/**
	 * 用户下线，向好友发送离线消息
	 */
	public void offline() {
		status = Constants.USER_STATUS_OFFLINE;
		sendMesToFriend(Constants.MESSAGE_TYPE_OFFLINE);
	}
	
	/**
	 * 向所有好友发送消息
	 * @param mtype	消息类型
	 */
	private void sendMesToFriend(int mtype) {
		if (getGroups() != null) {
			ChatEnv env = ChatEnv.getInstance();
			for (Group group : getGroups().values()) {
				if (group.getFriends() != null) {
					for (Friend friend : group.getFriends().values()) {
						Message message = new Message();
						message.setSender(username);
						message.setGetter(friend.getUsername());
						message.setMtype(mtype);
						env.addStatusMes(message);
					}
				}
			}
		}
	}

	/**
	 * 添加好友
	 * @param friend 好友对象
	 */
	public void addFriend(Friend friend) {
		if (getGroups() != null && friend != null) {
			Group group = getGroups().get(friend.getGid());
			if (group != null) {
				group.getFriends().put(friend.getUsername(), friend);
			}
		}
	}
	
	/**
	 * 用户是否存在指定好友
	 * @param username 好友用户名
	 * @return
	 */
	public boolean hasFriend(String username) {
		boolean result = false;
		if (getGroups() != null && username != null) {
			for (Group group : getGroups().values()) {
				if (group.getFriends().containsKey(username)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * 添加组
	 * @param group	组对象
	 */
	public void addGroup(Group group) {
		if (group != null) {
			getGroups().put(group.getGid(), group);
		}
	}
	
	/**
	 * 改变好友所属的分组
	 * @param gid	好友原始组id
	 * @param username	好友用户名
	 * @param cgid	好友需要改变到的组id
	 */
	public boolean transgroup(int gid, String username, int cgid) {
		boolean result = false;
		if (getGroups() != null) {
			Group group = getGroups().get(gid);
			if (group != null) {
				Friend friend = group.getFriends().remove(username);
				if (friend != null) {
					friend.setGid(cgid);
					group = getGroups().get(cgid);
					if (group != null) {
						group.getFriends().put(username, friend);
						result = true;
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * 删除好友分组
	 * @param gid 要删除的组id
	 * @return
	 */
	public boolean removegroup(int gid) {
		return getGroups().remove(gid) != null;
	}
	
	/**
	 * 修改组名
	 * @param gid	要修改的组id
	 * @param gname	修改后的组名
	 * @return
	 */
	public boolean renamegroup(int gid, String gname) {
		boolean result = false;
		Group group = getGroups().get(gid);
		if (group != null) {
			group.setGname(gname);
			result = true;
		}
		return result;
	}
	
	/**
	 * 修改好友备注名称
	 * @param gid	好友所属组id
	 * @param username	好友用户名
	 * @param notename	修改后的好友备注名
	 * @return
	 */
	public boolean renotename(int gid, String username, String notename) {
		boolean result = false;
		Group group = getGroups().get(gid);
		if (group != null) {
			Friend friend = group.getFriends().get(username);
			if (friend != null) {
				friend.setNotename(notename);
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * 删除好友
	 * @param gid	好友所属组id
	 * @param username	好友用户名
	 * @return
	 */
	public boolean removefriend(int gid, String username) {
		boolean result = false;
		Group group = getGroups().get(gid);
		if (group != null) {
			result = group.getFriends().remove(username) != null;
		}
		return result;
	}
	
	/**
	 * 将用户所有组及组中好友转换为json:{id:1, pId:0, name:"我的好友", isParent: true},{id:"100000", pId:1, name:"王五"}。。。
	 * @return
	 */
	public String toTreeJson() {
		StringBuilder json = new StringBuilder();
		String mark = "";
		if (getGroups() != null) {
			for (Group group : getGroups().values()) {
				Map<String, Friend> friends = group.getFriends();
				int friendsize = friends != null ? friends.size() : 0;
				String gname = group.getGname();
				gname = friendsize != 0 ? gname + " [" + friendsize + "]": gname;
				json.append(mark).append("{");
				json.append("id:").append(group.getGid()).append(",pId:0,")
					.append("name:'").append(gname).append("',isParent:true,open:true");
				json.append("}");
				mark = ",";
				if (friendsize != 0) {
					for (Friend friend : friends.values()) {
						json.append(mark).append("{");
						User user = friend.getUser();
						json.append("id:'").append(friend.getUsername()).append("',")
							.append("pId:").append(friend.getGid()).append(",");
						if (user != null) {
							json.append("icon:'").append(user.getMinHeader()).append("',");
							// json.append("iconSkin:'red',");
							if (user.status == Constants.USER_STATUS_ONLINE) {
								json.append("highlight:true,");
							}
						}
						// json.append("status:").append(user != null ? user.status : Constants.USER_STATUS_OFFLINE).append(",")
						json.append("name:'").append(friend.getNotename()).append("'");
						json.append("}");
					}
				}
			}
		}
		return json.toString();
	}
	
	/**
	 * 将用户信息转换为json
	 * @return
	 */
	public String toJson() {
		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("username:'").append(username).append("',")
			.append("nickname:'").append(nickname == null ? "" : nickname).append("',")
			.append("realname:'").append(realname == null ? "" : realname).append("',")
			.append("engname:'").append(engname == null ? "" : engname).append("',")
			.append("header:'").append(getHeader()).append("',")
			.append("sex:'").append(getSex()).append("',")
			.append("email:'").append(email == null ? "" : email).append("',")
			.append("age:").append(getAge()).append(",")
			.append("address:'").append(address == null ? "" : address).append("',")
			.append("phone:'").append(phone == null ? "" : phone).append("',")
			.append("profession:'").append(profession == null ? "" : profession).append("',")
			.append("education:'").append(education == null ? "" : education).append("',")
			.append("school:'").append(school == null ? "" : school).append("',")
			.append("homepage:'").append(homepage == null ? "" : homepage).append("',")
			.append("autograph:'").append(autograph == null ? "" : autograph).append("',")
			.append("elucidate:'").append(elucidate == null ? "" : elucidate).append("',")
			.append("status:").append(status).append("");
		json.append("}");
		return json.toString();
	}
	
	/**
	 * 将用户信息转换为json，指定username、nickname、header3个属性
	 * @return
	 */
	public String toSimpleJson() {
		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("username:'").append(username).append("',")
			.append("nickname:'").append(nickname).append("',")
			.append("header:'").append(getHeader()).append("',")
			.append("minheader:'").append(getMinHeader()).append("',")
			.append("kefuFlag:").append(kefuFlag).append(",")
			.append("status:").append(status).append("");
		json.append("}");
		return json.toString();
	}

	/**
	 * 获得用户的年龄
	 * @return
	 */
	public int getAge() {
		int age = -1;
		if (birthday != 0) {
			Calendar cal = Calendar.getInstance();
			int _year = cal.get(Calendar.YEAR);
			int year = birthday / 10000;
			age = _year - year;
			age = age < 0 ? 0 : age;
		}
		return age;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getEngname() {
		return engname;
	}

	public void setEngname(String engname) {
		this.engname = engname;
	}
	
	public String getMinHeader() {
		if (status == Constants.USER_STATUS_ONLINE) {
			return "style/base/images/header/16/online/" + (header == null ? (sex ? "1.png" : "0.png") : header);
		}
		return "style/base/images/header/16/offline/" + (header == null ? (sex ? "1.png" : "0.png") : header);
	}

	public String getHeader() {
		if (status == Constants.USER_STATUS_ONLINE) {
			return "style/base/images/header/online/" + (header == null ? (sex ? "1.png" : "0.png") : header);
		}
		return "style/base/images/header/offline/" + (header == null ? (sex ? "1.png" : "0.png") : header);
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getSex() {
		return sex ? "男" : "女";
	}

	public void setSex(boolean sex) {
		this.sex = sex;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getBirthday() {
		return birthday;
	}

	public void setBirthday(int birthday) {
		this.birthday = birthday;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getProfession() {
		return profession;
	}

	public void setProfession(String profession) {
		this.profession = profession;
	}

	public String getEducation() {
		return education;
	}

	public void setEducation(String education) {
		this.education = education;
	}

	public String getSchool() {
		return school;
	}

	public void setSchool(String school) {
		this.school = school;
	}

	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public String getAutograph() {
		return autograph;
	}

	public void setAutograph(String autograph) {
		this.autograph = autograph;
	}

	public String getElucidate() {
		return elucidate;
	}

	public void setElucidate(String elucidate) {
		this.elucidate = elucidate;
	}

	public String getIpaddr() {
		return ipaddr;
	}

	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}

	public Date getFirsttime() {
		return firsttime;
	}

	public void setFirsttime(Date firsttime) {
		this.firsttime = firsttime;
	}

	public Date getLasttime() {
		return lasttime;
	}

	public void setLasttime(Date lasttime) {
		this.lasttime = lasttime;
	}

	public int getLoginnum() {
		return loginnum;
	}

	public void setLoginnum(int loginnum) {
		this.loginnum = loginnum;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getKefuFlag() {
		return kefuFlag;
	}

	public void setKefuFlag(int kefuFlag) {
		this.kefuFlag = kefuFlag;
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, Group> getGroups() {
		if (groups == null) {
			groups = new HashMap<Integer, Group>();
			MyDB db = ChatEnv.getInstance() != null ? ChatEnv.getInstance().db : null;
			if (db != null) {
				String sql = "select * from " + Constants.dbprefix + "group where creator='" + username + "'";
				List<Group> groups = (List<Group>) db.queryForObjects(sql, Group.class);
				setGroups(groups);
			}
		}
		return groups;
	}
	
	public void setGroups(List<Group> groups) {
		if (groups != null) {
			Map<Integer, Group> map = getGroups();
			for (Group group : groups) {
				map.put(group.getGid(), group);
			}
		}
	}
	
	public HttpSession getSession() {
		return session;
	}

	public void setSession(HttpSession session) {
		this.session = session;
	}

	@Override
	protected void finalize() throws Throwable {
		MyUtil.debug(username + " 被gc销毁");
	}
}
