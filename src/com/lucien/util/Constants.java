package com.lucien.util;

/**
 * 静态常量存放类
 * @author Lucien
 *
 */
public class Constants {

	public static final String METHOD = "method";
	public static final String ACT = "act";
	public static final String USERKEY = "username";
	public static final String TO_ALL = "to_all";								///< 代表向聊天室中所有人发送消息		
	
	public static final int MODE_EMP2EMP = 1;
	public static final int MODE_VTR2ALL = 2;
	
	public static final int MESSAGE_TYPE_NORMALMES = 0;				///< 消息类型-聊天消息
	public static final int MESSAGE_TYPE_ADDFRIEND = 1;				///< 消息类型-添加好友
	public static final int MESSAGE_TYPE_AGREEADDF = 2;				///< 消息类型-同意添加好友
	public static final int MESSAGE_TYPE_REFUSEADD = 3;				///< 消息类型-拒绝添加好友
	public static final int MESSAGE_TYPE_ONLINE = 4;				///< 消息类型-上线
	public static final int MESSAGE_TYPE_OFFLINE = 5;				///< 消息类型-下线
	public static final int MESSAGE_TYPE_CROWDS = 6;				///< 消息类型-群消息
	public static final int MESSAGE_TYPE_HASMES = 7;				///< 消息类型-有未读消息
	public static final int MESSAGE_TYPE_ISINPUT = 8;				///< 消息类型-正在输入
	public static final int MESSAGE_TYPE_NOINPUT = 9;				///< 消息类型-取消输入
	public static final int MESSAGE_TYPE_WEBRTC = 10;				///< 消息类型-WebRTC相关的消息
	
	public static final int USER_STATUS_ONLINE = 1;					///< 用户状态-在线
	public static final int USER_STATUS_OFFLINE = 2;				///< 用户状态-离线
	
	public static String dbprefix = "ct_";							///< 数据库表前缀
	
	public static final Object PRESENT = new Object();				///< Dummy value to associate with an Object in the backing Map
	
}
