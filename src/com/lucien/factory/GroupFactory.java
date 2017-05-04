package com.lucien.factory;

import com.lucien.database.MyDB;
import com.lucien.entity.Group;
import com.lucien.model.ChatEnv;
import com.lucien.util.Constants;

/**
 * group类的工厂，专门负责管理group		2013-06-16
 * @author Lucien
 *
 */
public class GroupFactory {
	
	protected int count;											///< id计数器
	
	private static final GroupFactory groupFactory = new GroupFactory();
	
	private GroupFactory() {
		super();
	}
	
	/**
     * 取得用户组工厂      
     * @return
     */
	public static GroupFactory getInstance() {
		return groupFactory;
	}
	
	/**
	 * 获取组id，生成规则取数据库group表gid字段的最大值
	 * @return
	 */
	public synchronized int getGroupId() {
		if (count == 0) {
			MyDB db = ChatEnv.getInstance().db;
			if (db != null) {
				count = db.queryForInt("SELECT max(gid) FROM " + Constants.dbprefix + "group");
			}
		}
		count++;
		return count;
	}
	
	/**
	 * 保存组
	 * @param group
	 */
	public void saveGroup(Group group) {
		if (group != null) {
			group.setGid(getGroupId());
			MyDB db = ChatEnv.getInstance().db;
			if (db != null) {
				db.saveObject(group);
			}
		}
	}
}
