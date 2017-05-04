package com.lucien.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.websocket.WsOutbound;
import org.apache.log4j.Logger;

/**
 * 我的工具类
 * @author Lucien
 *
 */
public class MyUtil {
	
	public static final Logger logger = Logger.getLogger(MyUtil.class.getName());
	
	/**
	 * 输出控制台日志
	 * @param log
	 */
	public static void debug(String... logs) {
		if (Configure.debug) {
			System.out.print(MyUtil.getDateStr(new Date(), null) + " ");
			for (String log : logs) {
				System.out.print(log);
			}
			System.out.println();
		}
	}
	
	/**
	 * log4j日志
	 * @param log
	 */
	public static void info(String log) {
		logger.info(log);
	}
	
	/**
	 * 判断字串是否GBK编码
	 * @param string
	 * @return
	 * @throws java.io.UnsupportedEncodingException
	 */
	public static boolean isGBK(String string) throws java.io.UnsupportedEncodingException {
		byte[] bytes = string.replace('?', 'a').getBytes("ISO-8859-1");
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == 63) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 转换字符串编码
	 * @param target	需要转换的字串
	 * @param charSet	需要转换成的编码
	 * @return
	 */
	public static String convertCharSet(String target, String charSet) {
		try {
			if ((!isGBK(target)) || " ".equals(target)) {
				return new String(target.trim().getBytes("ISO-8859-1"),	charSet);
			} else {
				return target;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return target;
		}
	}
	
	/**
	 * 发送消息
	 * @param outbound
	 * @param content
	 */
	public static void sendMessage(WsOutbound outbound, String content) {
		if (outbound != null && content != null) {
			CharBuffer buffer = CharBuffer.wrap(content);
			try {
				outbound.writeTextMessage(buffer);
				outbound.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 将一个字串拆分成相应的值对并保存在一个Map中， 如"a=1,b= 2, c=3"
	 * @param source，待拆分字串
	 * @param delim1，第一分隔符
	 * @param delim2，第二分隔符
	 * @return，返回一个Map对象，它不为null;其健值为小写、首尾没有多余空格的Strig对象
	 */
	public static Map<String, String> map(String source, String delim1, String delim2) {
		if (source != null && delim1 != null && delim2 != null) {
			String[] sv = source.split(delim1);
			return map(sv, delim2);
		}
		return null;
	}
	
	/**
	 * 将一个字串数组拆分成相应的值对并保存在一个Map中， 如 { "a=1", "b= 2", "c=3" }
	 * @param source 待拆分字串
	 * @param delim 分隔符
	 * @return 返回一个 Map 对象，它不为 null, 其健值为小写、首尾没有多余空格的 String 对象
	 */
	public static Map<String, String> map(String[] source, String delim) {
		if (source != null && delim != null) {
			Map<String, String> map = new LinkedHashMap<String, String>();	
			for (int index = 0; index < source.length; index++) {
				if (!isEmpty(source[index].trim())) {
					int pos = source[index].indexOf(delim);
					String key, value;
					if (pos != -1) {
						key = source[index].substring(0, pos);
						value = source[index].substring(pos + 1).trim();
					}
					else {
						key = source[index];
						value = source[index];
					}
					map.put(key.trim().toLowerCase(), value);
				}
			}
			return map;
		}
		return null;
	}

	/**
	 * md5加密字串
	 * @param source
	 * @return
	 */
	public static String md5(String source) {  
	    StringBuilder sb = new StringBuilder(32);  
	    try {  
	        MessageDigest md = MessageDigest.getInstance("MD5");  
	        byte[] array = md.digest(source.getBytes("utf-8"));  
	        for (int i = 0; i < array.length; i++) {  
	            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).toUpperCase().substring(1, 3));  
	        }
	    } catch (Exception e) {  
	        e.printStackTrace();
	        return null;  
	    }  
	    return sb.toString();  
	}
	
	/**
	 * 将字串首字母变大写
	 * @param str
	 * @return
	 */
	public static String firstToUpperCase(String str) {
		if (!isEmpty(str)) {
			str = str.substring(0, 1).toUpperCase() + str.substring(1);
		}
		return str;
	}
	
	/**
	 * 将java.util.Date转换为java.sql.Timestamp
	 * @param date
	 * @return
	 */
	public static Timestamp parseSqlDate(Date date) {  
		if (date != null)
			return new Timestamp(date.getTime());
		else
			return null;
	}  
	
	/**
	 * 根据Class创建新的对象，对象的属性值从request参数里面获取
	 * @param clazz			 需要构建对象的Class
	 * @param request	HttpServletRequest
	 * @return			对象实例
	 */
	public static Object requestForObject(Class<?> clazz, HttpServletRequest request) {
		Object obj = null;
		if (clazz != null && request != null) {
			try {
				obj = Class.forName(clazz.getName()).newInstance();
				Field[] fields = clazz.getDeclaredFields();
				for (Field field : fields) {
					String fieldname = field.getName();
					String fieldvalue = request.getParameter(fieldname);
					if (!MyUtil.isEmpty(fieldvalue)) {
						field.setAccessible(true);
						if (field.getType() == String.class) {
							field.set(obj, fieldvalue);
						} else if (field.getType() == boolean.class) {
							boolean value = Boolean.parseBoolean(fieldvalue);
							field.set(obj, value);
						} else if (field.getType() == int.class) {
							int value = Integer.parseInt(fieldvalue);
							field.set(obj, value);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return obj;
	}
	
	/**
	 * 判断字符串是否为空
	 * @param value
	 * @return
	 */
	public static boolean isEmpty(String value) {
		return (value == null || value.equals("")) ? true : false;
	}
	
	/**
	 * 禁用页面缓存
	 * @param response
	 */
	public static void disableCache(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
	}
	
	/**
	 * 通过方法名称执行对象的方法
	 * @param obj		对象实例
	 * @param method	方法名称
	 * @return			返回方法执行后的结果
	 */
	public static Object invoke(Object obj, String method) {
		if (obj != null && !isEmpty(method)) {
			Method m;
			try {
				m = obj.getClass().getMethod(method);
				return m.invoke(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 根据格式，获得日期字串
	 * @param date		指定日期,为空默认取当前日期
	 * @param format	日期格式，为空默认yyyy/MM/dd HH:mm:ss
	 * @return			返回日期转换后的结果
	 */
	public static String getDateStr(Date date, String format) {
		date = date == null ? new Date() : date;
		format = format == null ? "yyyy/MM/dd HH:mm:ss" : format;
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}
	
	/**
	 * 获取客服端IP地址
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) { 
	    String ip = request.getHeader("x-forwarded-for"); 
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	        ip = request.getHeader("Proxy-Client-IP"); 
	    } 
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	        ip = request.getHeader("WL-Proxy-Client-IP"); 
	    } 
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	        ip = request.getRemoteAddr(); 
	    } 
	    return ip; 
	} 
	
	/**
	 * 获取客服端Mac地址
	 * @param ip
	 * @return
	 */
	public static String getMACAddress(String ip){ 
        String str = ""; 
        String macAddress = ""; 
        try { 
            Process p = Runtime.getRuntime().exec("nbtstat -A " + ip); 
            InputStreamReader ir = new InputStreamReader(p.getInputStream()); 
            LineNumberReader input = new LineNumberReader(ir); 
            for (int i = 1; i < 100; i++) { 
                str = input.readLine(); 
                if (str != null) { 
                    if (str.indexOf("MAC Address") > 1) { 
                        macAddress = str.substring(str.indexOf("MAC Address") + 14, str.length()); 
                        break; 
                    } 
                } 
            } 
        } catch (IOException e) { 
            e.printStackTrace(System.out); 
        } 
        return macAddress; 
    } 

	/** 
	 * 设置cookie 
	 * @param response 
	 * @param name  cookie名字 
	 * @param value cookie值 
	 * @param maxAge cookie生命周期  以秒为单位 
	 */
	public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) { 
		try {
			Cookie cookie = new Cookie(name, URLEncoder.encode(value, Configure.encoding)); 
		    cookie.setPath(Configure.path); 
		    if(maxAge > 0)  cookie.setMaxAge(maxAge); 
		    response.addCookie(cookie); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	/** 
	 * 根据名字获取cookie 
	 * @param request 
	 * @param name cookie名字 
	 * @return 
	 * @throws  
	 */
	public static String getCookieByName(HttpServletRequest request, String name) {
		String result = null;
	    Map<String,Cookie> cookieMap = ReadCookieMap(request); 
	    if (cookieMap.containsKey(name)) { 
	        Cookie cookie = (Cookie) cookieMap.get(name); 
	        try {
				result = URLDecoder.decode(cookie.getValue(), Configure.encoding);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} 
	    }
	    return result;
	} 

	/** 
	 * 将cookie封装到Map里面 
	 * @param request 
	 * @return 
	 */
	private static Map<String,Cookie> ReadCookieMap(HttpServletRequest request) {   
	    Map<String, Cookie> cookieMap = new HashMap<String, Cookie>(); 
	    Cookie[] cookies = request.getCookies(); 
	    if (null != cookies) { 
	        for (Cookie cookie : cookies) { 
	            cookieMap.put(cookie.getName(), cookie); 
	        } 
	    } 
	    return cookieMap; 
	} 

}
