package com.lucien.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lucien.database.MyDB;
import com.lucien.model.ChatEnv;
import com.lucien.util.Constants;

/**
 * 好友分组类，对应数据库ct_group表		2013-06-13
 * @author Lucien
 *
 */
public class Group {

	private int gid;																	///< 组id,唯一标识
	private String gname;																///< 组名
	private String creator;																///< 创建者
	protected Map<String, Friend> friends;												///< 该组所有好友
	
	public Group() {
	}
	
	public Group(String gname, String creator) {
		this.gname = gname;
		this.creator = creator;
	}
	
	public Group(int gid, String gname, String creator) {
		this(gname, creator);
		this.gid = gid;
	}

	public int getGid() {
		return gid;
	}
	
	public void setGid(int gid) {
		this.gid = gid;
	}
	
	public String getGname() {
		return gname;
	}
	
	public void setGname(String gname) {
		this.gname = gname;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Friend> getFriends() {
		if (friends == null) {
			friends = new HashMap<String, Friend>();
			MyDB db = ChatEnv.getInstance() != null ? ChatEnv.getInstance().db : null;
			if (db != null) {
				String sql = "select * from " + Constants.dbprefix + "friend where gid=" + getGid();
				List<Friend> friends = (List<Friend>) db.queryForObjects(sql, Friend.class);
				setFriends(friends);
			}
		}
		return friends;
	}
	
	public void setFriends(List<Friend> friends) {
		if (friends != null) {
			Map<String, Friend> map = getFriends();
			for (Friend friend : friends) {
				friend.getUser();
				map.put(friend.getUsername(), friend);
			}
		}
	}
	
	public String getCreator() {
		return creator;
	}
	
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	/**
	 * 向组中添加好友
	 * @param friend
	 */
	public void addFriend(Friend friend) {
		if (friend != null) {
			getFriends().put(friend.getUsername(), friend);
		}
	}
	
	/**
	 * 将组信息转换json数据，格式{gid:,gname:}
	 * @return
	 */
	public String toJson() {
		StringBuilder json = new StringBuilder();
		json.append("{")
			.append("gid:").append(gid).append(",")
			.append("gname:'").append(gname).append("'")
			.append("}");
		return json.toString();
	}
}
