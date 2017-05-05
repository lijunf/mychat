package com.lucien.servlet;

import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lucien.factory.UserFactory;
import com.lucien.model.ChatEnv;
import com.lucien.service.BaseService;
import com.lucien.util.Configure;
import com.lucien.util.Constants;
import com.lucien.util.MyThread;
import com.lucien.util.MyUtil;

@SuppressWarnings("serial")
public class WebAction extends HttpServlet {
	
	/**
	 * 以Get方式提交的表单处理函数
	 * @param req 查询对象
	 * @param resp 反馈对象
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handle(req, resp);
	}

	/**
	 * 以Post方式提交的表单处理函数
	 * @param req 查询对象
	 * @param resp 反馈对象
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handle(req, resp);
	}
	
	/**
	 * 操作请求处理主函数，解析并处理 WEB 操作请求
	 * @param request 操作请求对象
	 * @param response 操作响应对象
	 * @throws ServletException Servlet 异常对象
	 * @throws IOException IO 异常对象
	 * @throws ClassNotFoundException 
	 */
	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setHeader("Access-Control-Allow-Origin", "*");
		String method = request.getParameter(Constants.METHOD);		// 主方法，对应com.lucien.service.[method]Service类
		String act = request.getParameter(Constants.ACT);			// 对应service里面的方法，若为空service取Configure.defaultservice
		BaseService service = null;
		if (!MyUtil.isEmpty(method)) {
			String clazzstr = null;
			if (!MyUtil.isEmpty(act)) {
				clazzstr = Configure.servicepackage + "." + MyUtil.firstToUpperCase(method) + "Service";
			} else {
				clazzstr = Configure.servicepackage + "." + Configure.defaultservice;
				act = method;
			}
			try {
				Class<?> clazz = Class.forName(clazzstr);
				Constructor<?> structor = clazz.getConstructor(HttpServletRequest.class, HttpServletResponse.class);
				service = (BaseService) structor.newInstance(request, response);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (service != null) {
				MyUtil.invoke(service, act);
				String page = service.page;
				if (page != null) {
					getServletContext().getRequestDispatcher(page).forward(request, response);
				}
			}
		}
	}
	
	@Override
	public void init() throws ServletException {
		ChatEnv env = ChatEnv.getInstance();
		UserFactory userFactory = UserFactory.getInstance();
		userFactory.init(env);
		if (Configure.dbenabled) {
			// 如果未启用数据库，不用启动消息存储线程
			MyThread.start(env, "saveMessage");			//启动消息存储线程
		}
	}
	
	@Override
	public void destroy() {
		Configure.readMesBufferTimeout = 0;
		MyUtil.debug("服务器关闭了！！！！！！！！！！！");
	}
}
