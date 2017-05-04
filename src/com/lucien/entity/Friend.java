package com.lucien.entity;

import com.lucien.factory.UserFactory;

/**
 * 好友信息类,对应数据库ct_friend表		2013-06-16
 * @author Lucien
 *
 */
public class Friend {
	
	private int gid;									///< 所属组id
	private String username;							///< 好友用户名
	private String notename;							///< 好友备注名称
	
	protected User user;								///< 好友对象引用
	
	public Friend() {
		
	}
	
	public Friend(int gid, String username, String notename) {
		this.gid = gid;
		this.username = username;
		this.notename = notename;
	}
	
	public int getGid() {
		return gid;
	}
	
	public void setGid(int gid) {
		this.gid = gid;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getNotename() {
		return notename == null ? (user != null ? user.getNickname() : username) : notename;
	}

	public void setNotename(String notename) {
		this.notename = notename;
	}

	public User getUser() {
		if (user == null) {
			UserFactory userFactory = UserFactory.getInstance();
			user = userFactory.getUser(username);
		}
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
