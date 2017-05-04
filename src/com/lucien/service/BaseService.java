package com.lucien.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * service父类，所有service全部继承该类		2013-06-29
 * @author Lucien
 *
 */
public class BaseService {

	protected HttpServletRequest request = null;
	protected HttpServletResponse response = null;
	public String page;
	
	protected BaseService(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}
}
