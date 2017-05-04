package com.lucien.database;

import java.util.List;

/**
 * 分页管理类		2013-06-23
 * @author Lucien
 *
 */
public class MyPage {

	private int pagesize;								///< 每页记录条数
	private int rows;									///< 总共记录数
	private int current;								///< 当前是第几页
	private int pages;									///< 一共有多少页
	private int begno;									///< 查询开始记录号
	private int endno;									///< 查询结尾记录号
	private List<?> rs;									///< 结果列表
	
	public MyPage(int pagesize, int current, int rows) {
		this.pagesize = pagesize;
		this.current = current;
		this.rows = rows;
		this.pages = rows % pagesize == 0 ? rows / pagesize : rows / pagesize + 1;
		this.begno = (current - 1) * pagesize + 1;
		this.endno = current * pagesize;
	}
	
	public void pages(int pages) {
		this.pages = pages;
	}
	
	public int pagesize() {
		return pagesize;
	}
	
	public int current() {
		return current;
	}
	
	public int rows() {
		return rows;
	}
	
	public int pages() {
		return pages;
	}
	
	public int begno() {
		return begno;
	}
	
	public int endno() {
		return endno;
	}
	
	public List<?> getRs() {
		return rs;
	}
	
	public void setRs(List<?> rs) {
		this.rs = rs;
	}
}
