package com.lucien.xtest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;

import com.lucien.database.MyDB;

public class TestDB {
	public static void main(String[] args) throws Exception {
    	MyDB db = new MyDB();
		db.setDBType("mssql");
		db.setDBHost("localhost");
		db.setDBName("mychat");
		db.setLogUser("sa");
		db.setLogPwd("chat@2013");
		db.setSystem("linux");
		
		String sql = "insert into ct_message (sender,getter,content,sendtime,mtype,isread) values (?,?,?,?,?,?)";
		Connection conn = db.connect();
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, "zhangsan");
		stmt.setString(2, "lisi");
		stmt.setString(3, "hello");
		//stmt.setDate(4, new Date(System.currentTimeMillis()));
		stmt.setObject(4, (Object) new Date());
		stmt.setInt(5, 1);
		stmt.setBoolean(6, true);
		stmt.execute();
		/*String sql = "select * from dbo.ct_user where username = '100001'";
		User user = (User) db.queryForObject(sql, User.class);
		System.out.println(user.getNickname());*/
		/*String sql = "select * from dbo.ct_user";
		List<Object> users = db.queryForObjects(sql, User.class);
		for (Object obj : users) {
			User user = (User) obj;
			System.out.println(user.sex());
		}*/
		
    	/*MyDB db = new MyDB();
		db.setDBType("mysql");
		db.setDBHost("localhost");
		db.setDBName("mychat");
		db.setLogUser("root");
		db.setLogPwd("");
		db.setSystem("linux");*/
		
		//String sql = "insert into message (sender,getter,content,sendtime,isread) values ('张三', '李四','您好！','2013/06/03 19:28:46','1')";
		//System.out.println(db.executeStatement(sql));
		
		/*String sql = "SELECT max(gid) FROM ct_group";
		System.out.println(db.queryForInt(sql));*/
		
    }
}
