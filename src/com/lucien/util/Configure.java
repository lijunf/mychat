package com.lucien.util;

import java.util.Properties;

/**
 * web聊天室环境配置类
 * @author Lucien
 *
 */
public class Configure {
	
	public static boolean debug = false;									///< debug模式

	public static String skin = "base";										///< 皮肤文件夹
	
	public static String encoding = "UTF-8";								///< 网站编码
	
	public static String basepath = "/WEB-INF/pages/" + skin + "/";			///< 页面路径
	
	public static String path = "/mychat/";									///< 网站路径
	
	public static String visitorprefix = "youke";								///< 游客自动命名的前缀
	
	public static int mode = Constants.MODE_VTR2ALL;						///< 聊天模式。1只支持后台用户，2只支持匿名用户对后台用户，3支持匿名用户对匿名用户或后台用户

	public static boolean dbenabled = false;								///< 是否启用数据库，即是否将信息保存到数据库中
	public static String system = "linux";
	public static String dbtype = "mysql";
	public static String dbhost = "localhost";
	public static String dbname = "mychat";
	public static String dbuser = "root";
	public static String dbpwd = "";
	public static int dbLinkSize = 25;
	
	public static int readMesBufferSize = 50;								///< 已读信息缓冲区大小，数值越小，将信息保存入数据库的实时性越高，但性能相对较弱。相反数值越大性能越好
	public static int readMesBufferTimeout = 60;							///< 已读信息缓冲区超时时间，如果缓冲区中信息数量为达到缓冲区大小，并且达到该时间，会将缓冲区信息存储到数据库
	public static int historySize = 15;										///< 历史聊天消息条数
	public static int visitorSize = 1;										///< 聊天室游客数量
	
	public static String servicepackage = "com.lucien.service";				///< service类所在包
	public static String defaultservice = "ChatService";					///< 默认service类
	/**
	 * 加载配置文件
	 * @param filename
	 */
	public static void load(String filename) {
		Properties prop = new Properties();
		try {
			prop.load(Configure.class.getResourceAsStream(filename));
			Configure.debug = Boolean.valueOf(prop.getProperty("debug"));
			Configure.skin = prop.getProperty("skin");
			Configure.encoding = prop.getProperty("encoding");
			Configure.path = prop.getProperty("path");
			Configure.visitorprefix = prop.getProperty("visitorprefix");
			Configure.mode = Integer.parseInt(prop.getProperty("mode"));
			Configure.dbenabled = Boolean.parseBoolean(prop.getProperty("dbenabled"));
			Configure.system = prop.getProperty("system");
			Configure.dbtype = prop.getProperty("dbtype");
			Configure.dbhost = prop.getProperty("dbhost");
			Configure.dbname = prop.getProperty("dbname");
			Configure.dbuser = prop.getProperty("dbuser");
			Configure.dbpwd = prop.getProperty("dbpwd");
			Configure.dbLinkSize = Integer.parseInt(prop.getProperty("dbLinkSize"));
			Configure.readMesBufferSize = Integer.parseInt(prop.getProperty("readMesBufferSize"));
			Configure.readMesBufferTimeout = Integer.parseInt(prop.getProperty("readMesBufferTimeout"));
			Configure.historySize = Integer.parseInt(prop.getProperty("historySize"));
			Configure.visitorSize = Integer.parseInt(prop.getProperty("visitorSize"));
			MyUtil.debug("加载配置文件成功！");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 保持配置文件
	 * @param filename
	 */
	public static void save(String filename) {
		Properties prop = new Properties();
		try {
			prop.load(Configure.class.getResourceAsStream(filename));
			prop.setProperty("visitorSize", "" + Configure.visitorSize);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public static void main(String[] args) {
		Configure.load("/configure.properties");
		System.out.println(visitorSize);
		Configure.visitorSize = 20;
		Configure.save("/configure.properties");
	}
}
