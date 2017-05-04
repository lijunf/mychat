package com.lucien.util;

import java.lang.reflect.Method;

/**
 * 启动新的线程来调用对象中的某个方法，
 * @author Lucien
 *
 */
public class MyThread extends Thread {
	private Object obj = null;				///< 容器对象引用
	private Method func = null;				///< 对象函数引用
	private Object[] args = null;
	private int state = 0;

	public MyThread(Object obj, String method, Object[] args) {
		set(obj, method, args);
	}
	
	public boolean finished() {
		return (state == 2);
	}
	
	public boolean started() {
		return (state > 0);
	}
	
	public String method() {
		return func.getName();
	}
	
	public boolean set(Object obj, String method, Object[] args) {
		boolean result = false;
		this.obj = obj;
		this.args = args;
		try {
			Class<?>[] classes = null;
			if (args != null) {
				classes = new Class[args.length];
				for (int index = 0; index < args.length; index++) {
					classes[index] = args[index].getClass();
				}
			}
			this.func = obj.getClass().getMethod(method, classes);
			result = (obj != null && func != null);
		} catch (Exception e) { }
		return result;
	}

	public void run() {
		try {
			if (func != null) {
				state = 1;
				func.invoke(obj, args);
				state = 2;
			}
		} catch (Exception e) { }
	}
	
	/**
	 * 启动线程去执行对象中的不带参数的方法
	 * @param obj		对象实例
	 * @param method	对象方法名
	 * @return
	 */
	public static MyThread start(Object obj, String method) {
		MyThread thread = new MyThread(obj, method, null);
		if (thread != null && thread.func != null) {
			thread.start();
		}
		return thread;
	}
	
	/**
	 * 
	 * 启动线程去执行对象中的带参数的方法
	 * @param obj		对象实例
	 * @param method	对象方法名
	 * @param args		方法中的参数
	 * @return
	 */
	public static MyThread start(Object obj, String method, Object[] args) {
		MyThread thread = new MyThread(obj, method, args);
		if (thread != null && thread.func != null) {
			thread.start();
		}
		return thread;
	}
}
