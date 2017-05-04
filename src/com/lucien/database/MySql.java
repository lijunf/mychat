package com.lucien.database;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lucien.util.MyUtil;

/**
 * sql查询语句封装类		2013-06-24
 * 暂时只支持简单sql查询，不支持包含子查询的sql语句 ，如果sql中没有where条件，可以加上 where 1=1
 * @author Lucien
 *
 */
public class MySql {

	private String select;
	private String from;
	private String where;
	private String group;
	private String order;
	
	private String key;
	
	private MyDB db;
	
	public MySql(String sql) {
		this(sql, null);
	}
	
	public MySql(String sql, String key) {
		analyse(sql);
    	this.key = key;
	}
	
	/**
	 * 拆分sql语句
	 * @param sql
	 */
	private void analyse(String sql) {
		if (!MyUtil.isEmpty(sql)) {
			Pattern pattern = Pattern.compile("select (.+) from (.+) where (.+)");
			Matcher m = pattern.matcher(sql);
    		if (m.find()) {
    			select = m.group(1);
    			from = m.group(2);
    			where = m.group(3);
    			pattern = Pattern.compile("(.+) group by (.+)");
    			m = pattern.matcher(where);
    			if (m.find()) {
    				where = m.group(1);
    				group = m.group(2);
    				pattern = Pattern.compile("(.+) order by (.+)");
    				m = pattern.matcher(group);
    				if (m.find()) {
    					group = m.group(1);
    					order = m.group(2);
    				}
    			} else {
    				pattern = Pattern.compile("(.+) order by (.+)");
    				m = pattern.matcher(where);
    				if (m.find()) {
    					where = m.group(1);
    					order = m.group(2);
    				}
    			}
    		}
		}
	}
	
	/**
	 * 返回完整sql语句
	 * @return
	 */
	public String fullSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("select ").append(select).append(" from ").append(from).append(" where ").append(where);
		if (group != null) {
			sql.append(" group by ").append(group);
		}
		if (order != null) {
			sql.append(" order by ").append(order);
		}
		return sql.toString();
	}
	
	/**
	 * 返回查询记录数的sql语句
	 * @return
	 */
	public String countSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("select count(");
		if (key != null) {
			sql.append(key);
		} else {
			sql.append("*");
		}
		sql.append(") from ").append(from).append(" where ").append(where);
		if (group != null) {
			sql.append(" group by ").append(group);
		}
		if (order != null) {
			sql.append(" order by ").append(order);
		}
		return sql.toString();
	}
	
	/**
	 * 根据分页对象生成分页查询语句
	 * @param dbtype
	 * @return
	 */
	public String pageSql(MyPage myPage) {
		StringBuilder sql = new StringBuilder();
		if (myPage != null) {
			if (MyDB.DB_MYSQL.equals(db.dbType)) {
				sql.append(fullSql()).append(" limit ")
					.append((myPage.begno() - 1)).append(",").append(myPage.pagesize());
			} else if (MyDB.DB_MSSQL.equals(db.dbType)) {
				if (key != null) {
					sql.append("select top ").append(myPage.pagesize())
						.append(" ").append(select).append(" from ").append(from)
						.append(" where ").append(where).append(" and ");
						
					sql.append(key).append(" not in (")
						.append(" select top ").append(myPage.begno() - 1).append(" ") .append(key)
						.append(" from ").append(from).append(" where ").append(where);
					if (group != null) {
						sql.append(" group by ").append(group);
					}
					if (order != null) {
						sql.append(" order by ").append(order);
					}
					sql.append(")");
				}
			} else if (MyDB.DB_ORACLE.equals(db.dbType)) {
				// 没有测试
				sql.append("select ").append(select).append(" from (")
					.append("select ").append(select).append(",rownum rn from (")
					.append(fullSql())
					.append(") where rownum <= ").append(myPage.endno())
					.append(") where rn >= ").append(myPage.begno());
			}
		}
		return sql.toString();
	}
	
	public void MyDB(MyDB db) {
		this.db = db;
	}
}
