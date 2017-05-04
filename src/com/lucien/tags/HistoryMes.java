package com.lucien.tags;


import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.lucien.entity.Message;
import com.lucien.model.ChatEnv;
import com.lucien.util.Base64;
import com.lucien.util.Configure;
import com.lucien.util.Constants;
import com.lucien.util.MyUtil;

/**
 * 显示历史聊天记录
 * @author Lucien	2013-08-02
 *
 */
public class HistoryMes extends TagSupport {

	private static final long serialVersionUID = 6079730698029299429L;

	public int doStartTag() throws JspException {
		try {
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			JspWriter out = pageContext.getOut();
			HttpSession session = request.getSession();
			String loginuser = (String) session.getAttribute(Constants.USERKEY);
			ChatEnv env = ChatEnv.getInstance();
			if (Configure.historySize > 0) {
				List<Message> mes = env.getHistoryMes(loginuser);
				if (mes != null && !mes.isEmpty()) {
					String str = null;
					for (Message m : mes) {
						String sender = m.getSender();
						String content = "";
						content = new String(Base64.decode(m.getContent()));
						if (sender.equals(loginuser)) {
							str = "<div><span class='ct_mes_title_i'>我 " + MyUtil.getDateStr(m.getSendtime(), null)+ "</span>"
				    				+ "<div class='ct_mes_content'>" + content + "</div></div>";
						} else {
							str = "<div><span class='ct_mes_title_y'>" + sender + " " + MyUtil.getDateStr(m.getSendtime(), null)+ "</span>"
				    				+ "<div class='ct_mes_content'>" + content + "</div></div>";
						}
			    		out.print(str);
					}
					str = "<div class='ct_mes_history'>—————以上是聊天室中的历史记录——————</div>";
					out.print(str);
				}
			}
		} catch (Exception e) {
		}
		return SKIP_BODY;
	}

}
