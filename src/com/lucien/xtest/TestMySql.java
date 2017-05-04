package com.lucien.xtest;

import com.lucien.database.MySql;

public class TestMySql {
	public static void main(String[] args) {
		MySql mysql = new MySql("select sss,* from ct_user where 1 = 1 group by username order by userid,ee", null);
		System.out.println(mysql.fullSql());
	}
}
