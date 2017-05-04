package com.lucien.listener;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.catalina.websocket.WsOutbound;

import com.lucien.entity.User;
import com.lucien.factory.UserFactory;
import com.lucien.model.ChatEnv;
import com.lucien.util.Constants;
import com.lucien.util.MyUtil;

/**
 * session 侦听器 
 * 考虑是否用HttpSessionBindingListener替换
 * @author Lucien 2013-06-06
 *
 */
public class SessionListener implements HttpSessionListener {
	
	public void sessionCreated(HttpSessionEvent event) {
		MyUtil.debug("new session:" + event.getSession().getId()); 
	}

	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session = event.getSession(); 
		String username = (String) session.getAttribute(Constants.USERKEY);
		User user = UserFactory.getInstance().getUser(username);
		if (user != null) {
			user.setSession(null);
		}
		if (username != null) {
			ChatEnv env = ChatEnv.getInstance();
			env.removeUser(username);
			Map<String, WsOutbound> outbounds = env.outbounds.remove(username);
			if (outbounds != null) {
				Set<String> keys = outbounds.keySet();
				if (keys != null) {
					for (String key : keys) {
						WsOutbound outbound = outbounds.get(key);
						try {
							outbound.close(0, null);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			MyUtil.debug(username + "	退出聊天室");
		}
	}
}
