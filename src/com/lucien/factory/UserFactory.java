package com.lucien.factory;


import java.util.List;

import com.lucien.cache.UserCache;
import com.lucien.database.MyDB;
import com.lucien.database.MyPage;
import com.lucien.database.MySql;
import com.lucien.entity.Friend;
import com.lucien.entity.Group;
import com.lucien.entity.User;
import com.lucien.model.ChatEnv;
import com.lucien.util.Configure;
import com.lucien.util.Constants;
import com.lucien.util.MyUtil;

/**
 * user工厂，专门负责管理用户user		2013-06-13
 * @author Lucien
 *
 */
public class UserFactory {
	
	protected int count;											///< 用户账号累加器
	
	private static final UserFactory userFactory =  new UserFactory();
	public UserCache users = null;	 											///< 用户缓存对象
	
	private UserFactory() {
		users = UserCache.getInstance();
	}
	
	 /**
     * 取得用户工厂      
     * @return
     */
    public static UserFactory getInstance() {      
       return userFactory;      
    } 
    
	/**
	 * 根据用户名或用户id,从数据库中获取全部用户信息形成user对象
	 * @param userid
	 * @return
	 */
	private User createUser(String username, String password) {
		User user = null;
		if (!MyUtil.isEmpty(username)) {
			/*
			select a.*,b.gid,b.gname,c.username friend,c.notename 
				from ct_user a 
					inner join ct_group b on a.username=b.creator 
					inner join ct_group_user c on b.gid=c.gid 
				where a.username=100000
			*/
			MyDB db = ChatEnv.getInstance().db;
			if (db != null) {
				String sql = "select * from " + Constants.dbprefix + "user where username='" + username + "' and enabled = 1";
				if (!MyUtil.isEmpty(password)) {
					sql += " and password = '" + MyUtil.md5(password) + "'";
				}
				user = (User) db.queryForObject(sql, User.class);
				/*if (user != null) {
					sql = "select * from " + Constants.dbprefix + "group where creator='" + username + "'";
					List<Group> groups = (List<Group>) db.queryForObjects(sql, Group.class);
					user.setGroups(groups);
					for (Group group : groups) {
						sql = "select * from " + Constants.dbprefix + "friend where gid=" + group.getGid();
						List<Friend> friends = (List<Friend>) db.queryForObjects(sql, Friend.class);
						group.setFriends(friends);
					}
				}*/
			}
			// MyUtil.debug("create：" + username);
		}
		return user;
	}
	
	/**
	 * 根据用户名获取用户，若缓存中不存在则重新创建
	 * @param username用户名
	 * @return
	 */
	public User getUser(String username) {
		return getUser(username, null, true);
	}
	
	/**
	 * 根据用户名获取用户
	 * @param username用户名
	 * @param create当没有用户的时候是否创建用户
	 * @return
	 */
	public User getUser(String username, boolean create) {
		return getUser(username, null, create);
	}
	
	/**
	 * 根据用户名获取用户对象，若缓存中不存在则重新创建
	 * @param username用户名
	 * @param password密码
	 * @return
	 */
	public User getUser(String username, String password) {
		return getUser(username, password, true);
	}
	
	/**
	 * 根据用户名获取用户
	 * @param username用户名
	 * @param password密码
	 * @param create当没有用户的时候是否创建用户
	 * @return
	 */
	public User getUser(String username, String password, boolean create) {
		User user = null;
		if (!MyUtil.isEmpty(username)) {
			user = users.getUser(username);
			if (user == null && create) {
				user = createUser(username, password);
				users.cacheUser(user);
			}
		}
		return user;
	}
	
	/**
	 * 保存用户
	 * @param user
	 * @return
	 */
	public boolean saveUser(User user) {
		boolean result = false;
		if (user != null) {
			user.setPassword(MyUtil.md5(user.getPassword()));	//密码md5加密
			MyDB db = ChatEnv.getInstance().db;
			if (db != null) {
				user.setUsername(String.valueOf(getAccount()));
				result = db.saveObject(user);
				if (result) {
					int gid = GroupFactory.getInstance().getGroupId();
					Group group = new Group(gid, "我的好友", user.getUsername());
					result = db.saveObject(group);
					if (result) {
						user.addGroup(group);
					}
					users.cacheUser(user);
				}
			}
		}
		return result;
	}
	
	/**
	 * 获取最新用户账号，生成规则取数据库user表username字段的最大值
	 * @return
	 */
	private synchronized int getAccount() {
		if (count == 0) {
			MyDB db = ChatEnv.getInstance().db;
			if (db != null) {
				count = db.queryForInt("select max(username) from " + Constants.dbprefix + "user where enabled = 1");
				count = count == 0 ? 99999 : count;
			}
		}
		count++;
		return count;
	}
	
	/**
	 * 根据字段名和字段值查询用户
	 * @param username	执行查询的用户
	 * @param field		user表中的字段名称 
	 * @param value		字段对应的值
	 * @param term		搜索条件配置规则，1表示like、2表示等于
	 * @param pagesize	查询结果每页大小
	 * @param current	当前是第几页
	 * @return
	 */
	public MyPage searchUser(String username, String field, String value, String term, int pagesize, int current) {
		if (!MyUtil.isEmpty(field) && !MyUtil.isEmpty(value)) {
			int _term = term != null ? Integer.parseInt(term) : -1;
			String sql = "select * from " + Constants.dbprefix + "user where " + field;
			switch (_term) {
				case 1:
					sql += " like '%" + value + "%'";
					break;
				case 2:
					sql += " = '" + value + "'";
					break;
			}
			sql = username != null ? sql + " and username != '" + username + "'" : sql;
			MyDB db = ChatEnv.getInstance().db;
			if (db != null) {
				MySql mySql = new MySql(sql, "username");
				MyPage myPage = db.queryForObjects(mySql, User.class, pagesize, current);
				return myPage;
			}
		}
		return null;
	}
	
	/**
     * 初始化默认客服用户
     */
    public void init(ChatEnv env) {
    	if (Configure.dbenabled) {
    		initDBKefu(env);
    	} else {
    		initDefaultKefu(env);
    	}
    }
    
    /**
     * 从数据库中提取客服
     * @param env
     */
    @SuppressWarnings("unchecked")
    public void initDBKefu(ChatEnv env) {
    	String sql = "select * from " + Constants.dbprefix + "user where kefuFlag = 1 or kefuFlag = 2";
    	List<User> kefus = (List<User>) env.db.queryForObjects(sql, User.class);
    	if (kefus != null) {
    		for (User user : kefus) {
        		users.cacheUser(user);
            	env.addLoginusr(user);
        	}
    	}
    }
    
    /**
     * 初始化默认的客服，仅当不支持数据库时使用
     * @param env
     */
    public void initDefaultKefu(ChatEnv env) {
    	User user1 = new User("kefu1", "为为", "111111", "为人们服务！！！", "1.png", 1, true);
    	users.cacheUser(user1);
    	env.addLoginusr(user1);
    	User user2 = new User("kefu2", "人人", "222222", "为人们服务！！！", "2.png", 1, true);
    	users.cacheUser(user2);
    	env.addLoginusr(user2);
    	User user3 = new User("kefu3", "民民", "333333", "为人们服务！！！", "4.png", 1, true);
    	users.cacheUser(user3);
    	env.addLoginusr(user3);
    	User user4 = new User("kefu4", "服服", "444444", "为人们服务！！！", "7.png", 1, true);
    	users.cacheUser(user4);
    	env.addLoginusr(user4);
    	User user5 = new User("kefu5", "务务", "555555", "为人们服务！！！", "8.png", 1, true);
    	users.cacheUser(user5);
    	env.addLoginusr(user5);
    	User user6 = new User("kefu6", "小乔", "666666", "战无不胜！！！", "3.png", 2, false);
    	users.cacheUser(user6);
    	env.addLoginusr(user6);
    	User user7 = new User("kefu7", "貂蝉", "777777", "战无不胜！！！", "0.png", 2, false);
    	users.cacheUser(user7);
    	env.addLoginusr(user7);
    	User user8 = new User("kefu8", "月英", "777777", "战无不胜！！！", "5.png", 2, false);
    	users.cacheUser(user8);
    	env.addLoginusr(user8);
    	User user9 = new User("kefu9", "祝融", "777777", "战无不胜！！！", "6.png", 2, false);
    	users.cacheUser(user9);
    	env.addLoginusr(user9);
    	User user10 = new User("kefu10", "云禄", "777777", "战无不胜！！！", "9.png", 2, false);
    	users.cacheUser(user10);
    	env.addLoginusr(user10);
    	user1.addGroup(new Group(1, "客户客服", user1.getUsername()));
    	user1.addFriend(new Friend(1, user2.getUsername(), user2.getNickname()));
    	user1.addFriend(new Friend(1, user3.getUsername(), user3.getNickname()));
    	user1.addFriend(new Friend(1, user4.getUsername(), user4.getNickname()));
    	user1.addFriend(new Friend(1, user5.getUsername(), user5.getNickname()));
    	user1.addGroup(new Group(6, "售前客服", user1.getUsername()));
    	user1.addFriend(new Friend(6, user6.getUsername(), user6.getNickname()));
    	user1.addFriend(new Friend(6, user7.getUsername(), user7.getNickname()));
    	user1.addFriend(new Friend(6, user8.getUsername(), user8.getNickname()));
    	user1.addFriend(new Friend(6, user9.getUsername(), user9.getNickname()));
    	user1.addFriend(new Friend(6, user10.getUsername(), user10.getNickname()));
    	
    	user2.addGroup(new Group(2, "客户客服", user2.getUsername()));
    	user2.addFriend(new Friend(2, user1.getUsername(), user1.getNickname()));
    	user2.addFriend(new Friend(2, user3.getUsername(), user3.getNickname()));
    	user2.addFriend(new Friend(2, user4.getUsername(), user4.getNickname()));
    	user2.addFriend(new Friend(2, user5.getUsername(), user5.getNickname()));
    	user2.addGroup(new Group(7, "售前客服", user2.getUsername()));
    	user2.addFriend(new Friend(7, user6.getUsername(), user6.getNickname()));
    	user2.addFriend(new Friend(7, user7.getUsername(), user7.getNickname()));
    	user2.addFriend(new Friend(7, user8.getUsername(), user8.getNickname()));
    	user2.addFriend(new Friend(7, user9.getUsername(), user9.getNickname()));
    	user2.addFriend(new Friend(7, user10.getUsername(), user10.getNickname()));
    	
    	user3.addGroup(new Group(3, "客户客服", user3.getUsername()));
    	user3.addFriend(new Friend(3, user2.getUsername(), user2.getNickname()));
    	user3.addFriend(new Friend(3, user1.getUsername(), user1.getNickname()));
    	user3.addFriend(new Friend(3, user4.getUsername(), user4.getNickname()));
    	user3.addFriend(new Friend(3, user5.getUsername(), user5.getNickname()));
    	user3.addGroup(new Group(8, "售前客服", user3.getUsername()));
    	user3.addFriend(new Friend(8, user6.getUsername(), user6.getNickname()));
    	user3.addFriend(new Friend(8, user7.getUsername(), user7.getNickname()));
    	user3.addFriend(new Friend(8, user8.getUsername(), user8.getNickname()));
    	user3.addFriend(new Friend(8, user9.getUsername(), user9.getNickname()));
    	user3.addFriend(new Friend(8, user10.getUsername(), user10.getNickname()));
    	
    	user4.addGroup(new Group(4, "客户客服", user4.getUsername()));
    	user4.addFriend(new Friend(4, user2.getUsername(), user2.getNickname()));
    	user4.addFriend(new Friend(4, user3.getUsername(), user3.getNickname()));
    	user4.addFriend(new Friend(4, user1.getUsername(), user1.getNickname()));
    	user4.addFriend(new Friend(4, user5.getUsername(), user5.getNickname()));
    	user4.addGroup(new Group(9, "售前客服", user4.getUsername()));
    	user4.addFriend(new Friend(9, user6.getUsername(), user6.getNickname()));
    	user4.addFriend(new Friend(9, user7.getUsername(), user7.getNickname()));
    	user4.addFriend(new Friend(9, user8.getUsername(), user8.getNickname()));
    	user4.addFriend(new Friend(9, user9.getUsername(), user9.getNickname()));
    	user4.addFriend(new Friend(9, user10.getUsername(), user10.getNickname()));
    	
    	user5.addGroup(new Group(5, "客户客服", user5.getUsername()));
    	user5.addFriend(new Friend(5, user2.getUsername(), user2.getNickname()));
    	user5.addFriend(new Friend(5, user3.getUsername(), user3.getNickname()));
    	user5.addFriend(new Friend(5, user4.getUsername(), user4.getNickname()));
    	user5.addFriend(new Friend(5, user1.getUsername(), user1.getNickname()));
    	user5.addGroup(new Group(10, "售前客服", user5.getUsername()));
    	user5.addFriend(new Friend(10, user6.getUsername(), user6.getNickname()));
    	user5.addFriend(new Friend(10, user7.getUsername(), user7.getNickname()));
    	user5.addFriend(new Friend(10, user8.getUsername(), user8.getNickname()));
    	user5.addFriend(new Friend(10, user9.getUsername(), user9.getNickname()));
    	user5.addFriend(new Friend(10, user10.getUsername(), user10.getNickname()));
    	
    	user6.addGroup(new Group(11, "客户客服", user6.getUsername()));
    	user6.addFriend(new Friend(11, user2.getUsername(), user2.getNickname()));
    	user6.addFriend(new Friend(11, user3.getUsername(), user3.getNickname()));
    	user6.addFriend(new Friend(11, user4.getUsername(), user4.getNickname()));
    	user6.addFriend(new Friend(11, user1.getUsername(), user1.getNickname()));
    	user6.addFriend(new Friend(11, user5.getUsername(), user5.getNickname()));
    	user6.addGroup(new Group(12, "售前客服", user6.getUsername()));
    	user6.addFriend(new Friend(12, user7.getUsername(), user7.getNickname()));
    	user6.addFriend(new Friend(12, user8.getUsername(), user8.getNickname()));
    	user6.addFriend(new Friend(12, user9.getUsername(), user9.getNickname()));
    	user6.addFriend(new Friend(12, user10.getUsername(), user10.getNickname()));
    	
    	user7.addGroup(new Group(13, "客户客服", user7.getUsername()));
    	user7.addFriend(new Friend(13, user2.getUsername(), user2.getNickname()));
    	user7.addFriend(new Friend(13, user3.getUsername(), user3.getNickname()));
    	user7.addFriend(new Friend(13, user4.getUsername(), user4.getNickname()));
    	user7.addFriend(new Friend(13, user1.getUsername(), user1.getNickname()));
    	user7.addFriend(new Friend(13, user5.getUsername(), user5.getNickname()));
    	user7.addGroup(new Group(14, "售前客服", user7.getUsername()));
    	user7.addFriend(new Friend(14, user6.getUsername(), user6.getNickname()));
    	user7.addFriend(new Friend(14, user8.getUsername(), user8.getNickname()));
    	user7.addFriend(new Friend(14, user9.getUsername(), user9.getNickname()));
    	user7.addFriend(new Friend(14, user10.getUsername(), user10.getNickname()));
    	
    	user8.addGroup(new Group(15, "客户客服", user8.getUsername()));
    	user8.addFriend(new Friend(15, user2.getUsername(), user2.getNickname()));
    	user8.addFriend(new Friend(15, user3.getUsername(), user3.getNickname()));
    	user8.addFriend(new Friend(15, user4.getUsername(), user4.getNickname()));
    	user8.addFriend(new Friend(15, user1.getUsername(), user1.getNickname()));
    	user8.addFriend(new Friend(15, user5.getUsername(), user5.getNickname()));
    	user8.addGroup(new Group(16, "售前客服", user8.getUsername()));
    	user8.addFriend(new Friend(16, user6.getUsername(), user6.getNickname()));
    	user8.addFriend(new Friend(16, user7.getUsername(), user7.getNickname()));
    	user8.addFriend(new Friend(16, user9.getUsername(), user9.getNickname()));
    	user8.addFriend(new Friend(16, user10.getUsername(), user10.getNickname()));
    	
    	user9.addGroup(new Group(17, "客户客服", user9.getUsername()));
    	user9.addFriend(new Friend(17, user2.getUsername(), user2.getNickname()));
    	user9.addFriend(new Friend(17, user3.getUsername(), user3.getNickname()));
    	user9.addFriend(new Friend(17, user4.getUsername(), user4.getNickname()));
    	user9.addFriend(new Friend(17, user1.getUsername(), user1.getNickname()));
    	user9.addFriend(new Friend(17, user5.getUsername(), user5.getNickname()));
    	user9.addGroup(new Group(18, "售前客服", user9.getUsername()));
    	user9.addFriend(new Friend(18, user6.getUsername(), user6.getNickname()));
    	user9.addFriend(new Friend(18, user7.getUsername(), user7.getNickname()));
    	user9.addFriend(new Friend(18, user8.getUsername(), user8.getNickname()));
    	user9.addFriend(new Friend(18, user10.getUsername(), user10.getNickname()));
    	
    	user10.addGroup(new Group(19, "客户客服", user10.getUsername()));
    	user10.addFriend(new Friend(19, user2.getUsername(), user2.getNickname()));
    	user10.addFriend(new Friend(19, user3.getUsername(), user3.getNickname()));
    	user10.addFriend(new Friend(19, user4.getUsername(), user4.getNickname()));
    	user10.addFriend(new Friend(19, user1.getUsername(), user1.getNickname()));
    	user10.addFriend(new Friend(19, user5.getUsername(), user5.getNickname()));
    	user10.addGroup(new Group(20, "售前客服", user10.getUsername()));
    	user10.addFriend(new Friend(20, user6.getUsername(), user6.getNickname()));
    	user10.addFriend(new Friend(20, user7.getUsername(), user7.getNickname()));
    	user10.addFriend(new Friend(20, user8.getUsername(), user8.getNickname()));
    	user10.addFriend(new Friend(20, user9.getUsername(), user9.getNickname()));
    }
}
