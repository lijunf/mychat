package com.lucien.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lucien.util.Configure;


/**
 * 过滤器	2013-06-01
 * @author Lucien
 *
 */
public class SetCharacterEncoding implements Filter {
	
	protected ServletContext context;
    protected boolean ignore = true;

    /**
     * 初始化过滤器
     */
    public void init(FilterConfig filterConfig) throws ServletException {
		context = filterConfig.getServletContext();
		Configure.load("/configure.properties");			//加载配置文件
    }
    
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		String formMethod = request.getMethod();
		if ("post".equalsIgnoreCase(formMethod)) {
		    request.setCharacterEncoding(Configure.encoding);
		} else if ("get".equalsIgnoreCase(formMethod)) {
		    request = new RequestUrlWrapper(request, Configure.encoding);
		}
		response.setCharacterEncoding(Configure.encoding);
		filterChain.doFilter(request, response);
    }
   
    public void destroy() {
    }
}
