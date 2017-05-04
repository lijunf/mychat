package com.lucien.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 独立版的filter，拦截以.html结尾的uri。转发到相应的Action进行处理
 * 转发规则:
 * 		/action/room.html				--			/action?method=room
 * 		/action/user-login.html			--			/action?method=user&act=login
 * @author Lucien
 * @version 1.0
 * @created 2013-08-03
 *
 */
public class DispatcherFilter implements Filter {
	
	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String uri = httpRequest.getServletPath();
		if (uri.startsWith("/action-")) {
			uri = uri.substring(8);
			uri = uri.replace(".html", "");
			String[] strs = uri.split("-");
			String url = null;
			switch (strs.length) {
				case 1:
					url = "/action?method=" + strs[0];
					break;
				case 2:
					url = "/action?method=" + strs[0] + "&act=" + strs[1];
					break;
			}
			request.getServletContext().getRequestDispatcher(url).forward(request, response);
			return;
		}
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		
	}

}
