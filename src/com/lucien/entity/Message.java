package com.lucien.entity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lucien.util.Base64;
import com.lucien.util.Configure;
import com.lucien.util.Constants;
import com.lucien.util.MyUtil;

/**
 * 信息封装类，对应数据库ct_Message表	2013-06-01
 * @author Lucien
 *
 */
public class Message {

	private int mesid;
	private String sender;
	private String getter;
	private String content;
	private Date sendtime = new Date();
	private int mtype;								///< 消息类型：0普通消息、1加好友，2同意加为好友，3不同意加好友，4好友上线，5好友下线，6群消息，7有未读消息，8正在输入，10和WebRTC相关
	private int rtctype;							///< WebRTC消息类型：0 Conect，2 Offer，3 Answer， 4 IceCandidate
	private String description;						///< WebRTC description
	private boolean isread = false;
	
	public boolean websocket = false;				///< 是否通过websocket发送的消息
	public Map<String, String> params = null;
	
	public Message() {
	}
	
	public Message(String sender, String getter, String content, int mtype, boolean websocket) {
		this.sender = sender;
		this.getter = getter;
		this.content = content;
		this.mtype = mtype;
		this.websocket = websocket;
	}
	
	public Message(String sender, String getter, String content, int mtype, int rtctype, boolean websocket) {
		this.sender = sender;
		this.getter = getter;
		this.content = content;
		this.mtype = mtype;
		this.rtctype = rtctype;
		this.websocket = websocket;
	}
	
	public Message(Message message) {
		super();
		this.getter = message.getter;
		this.sender = message.sender;
		this.content = message.content;
		this.sendtime = message.sendtime;
		this.mtype = message.mtype;
		this.websocket = message.websocket;
	}
	
	/**
	 * 判断消息是否是与添加好友相关的
	 * @return
	 */
	public boolean is123() {
		return mtype == Constants.MESSAGE_TYPE_ADDFRIEND
				|| mtype == Constants.MESSAGE_TYPE_AGREEADDF
				|| mtype == Constants.MESSAGE_TYPE_REFUSEADD;
	}

	/**
	 * 是否状态信息
	 * @return
	 */
	public boolean isStatusMes() {
		return mtype == Constants.MESSAGE_TYPE_ONLINE
				|| mtype == Constants.MESSAGE_TYPE_OFFLINE;
	}
	
	/**
	 * 将消息转换为json格式：{mesid: ,content: ,sendTime: ,type: },{mesid: ,content: ,sendTime: type: }
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public String toJson() {
		StringBuilder buf = new StringBuilder();
		String content = "";
		try {
			if (this.content != null && !websocket) {
				content = URLEncoder.encode(this.content, Configure.encoding);
			} else {
				content = this.content;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		buf.append("{");
		buf.append("\"mesid\":\"").append(mesid).append("\",");
		buf.append("\"getter\":\"").append(getter).append("\",");
		buf.append("\"sender\":\"").append(sender).append("\",");
		buf.append("\"content\":\"").append(content).append("\",");
		buf.append("\"sendTime\":\"").append(MyUtil.getDateStr(sendtime, null)).append("\",");
		buf.append("\"mtype\":").append(mtype).append(",");
		buf.append("\"rtctype\":").append(rtctype).append(",");
		buf.append("\"description\":").append(description).append("");
		buf.append("}");
		return buf.toString();
	}
	
	/**
	 * 将消息转换为字符串格式：date xxx对yyyy说：content
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public String toStr() {
		StringBuilder buf = new StringBuilder();
		String content = this.content != null ? new String(Base64.decode(this.content)) : "";
		buf.append(MyUtil.getDateStr(sendtime, null)).append(" ")
			.append(sender).append("对").append(getter).append("说：")
			.append(content).append(description);
		return buf.toString();
	}
	
	/**
	 * 讲属性全部做成字符串加到List集合中
	 * @return
	 */
	public List<String> toList() {
		List<String> list = new ArrayList<String>();
		list.add(sender);
		list.add(getter);
		list.add(content);
		list.add(MyUtil.getDateStr(sendtime, null));
		list.add(String.valueOf(mtype));
		list.add(isread ? "1" : "0");
		return list;
	}
	
	/**
	 * 过滤消息中的非法内容
	 */
	public void filterIllegalContent() {
		if (content != null) {
			content = content.replaceAll("(?i)<script\\p{ASCII}*script>", "");
			content = content.replaceAll("(?i)<iframe\\p{ASCII}*iframe>", "");
			// 还需考虑过滤a,img等标签
		}
	}
	
	public int getMesid() {
		return mesid;
	}

	public void setMesid(int mesid) {
		this.mesid = mesid;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getGetter() {
		return getter;
	}

	public void setGetter(String getter) {
		this.getter = getter;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getSendtime() {
		return sendtime;
	}

	public void setSendtime(Date sendtime) {
		this.sendtime = sendtime;
	}

	public int getMtype() {
		return mtype;
	}

	public void setMtype(int mtype) {
		this.mtype = mtype;
	}
	
	public int getRtctype() {
		return rtctype;
	}

	public void setRtctype(int rtctype) {
		this.rtctype = rtctype;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isIsread() {
		return isread;
	}

	public void setIsread(boolean isread) {
		this.isread = isread;
	}
}
