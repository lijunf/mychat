package com.lucien.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Hashtable;

import com.lucien.entity.User;

/**
 * 用户信息缓存容器对象，专门负责缓存用户信息		2013-06-18
 * @author Lucien
 *
 */
public class UserCache {
	
	private static final UserCache cache = new UserCache();				///< 一个Cache实例      
    private Hashtable<String, UserRef> userRefs;						///< 用于Cache内容的存储      
    private ReferenceQueue<User> queue;									///< 垃圾Reference的队列      
       
    /**
     * 继承SoftReference，使得每一个实例都具有可识别的标识。      
     * 并且该标识与其在HashMap内的key相同。
     * @author Lucien
     *
     */
    private class UserRef extends SoftReference<User> {      
    	private String _key = "";      
       
    	public UserRef(User user, ReferenceQueue<User> queue) {      
    		super(user, queue);      
    		_key = user.getUsername();      
    	}      
    }      
       
    /**
     * 构建一个缓存器实例      
     */
    private UserCache() {      
    	userRefs = new Hashtable<String, UserRef>();      
    	queue = new ReferenceQueue<User>();      
    }      
       
    /**
     * 取得缓存器实例      
     * @return
     */
    public static UserCache getInstance() {      
    	return cache;      
    }      
       
    /**
     * 以软引用的方式对一个User对象的实例进行引用并保存该引用      
     * @param user
     */
    public void cacheUser(User user) {
    	if (user != null) {
    		cleanCache();	// 清除垃圾引用      
    		UserRef ref = new UserRef(user, queue);      
    		userRefs.put(user.getUsername(), ref);      
    	}
    }      
       
    /**
     * 依据所指定的ID号，重新获取相应User对象的实例      
     * @param username
     * @return
     */
    public User getUser(String username) {      
    	User user = null;      
    	// 缓存中是否有该User实例的软引用，如果有，从软引用中取得。      
		if (userRefs.containsKey(username)) {      
			UserRef ref = (UserRef) userRefs.get(username);      
			user = (User) ref.get();      
		}      
		return user;      
    }      
       
    /**
     * 清除那些所软引用的User对象已经被回收的UserRef对象      
     */
    private void cleanCache() {      
    	UserRef ref = null;      
    	while ((ref = (UserRef) queue.poll()) != null) {      
    		userRefs.remove(ref._key);      
    	}      
    }      
       
    /**
     * 清除Cache内的全部内容      
     */
    public void clearCache() {      
    	cleanCache();      
    	userRefs.clear();      
    	System.gc();      
    	System.runFinalization();      
    }
}