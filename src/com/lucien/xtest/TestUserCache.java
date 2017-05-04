package com.lucien.xtest;

import com.lucien.cache.UserCache;
import com.lucien.entity.User;

public class TestUserCache {
	 public static void main(String[] args) throws InterruptedException {
			UserCache users = UserCache.getInstance();
			for (int i = 0; i < 10000000; i++) {
				users.cacheUser(new User());
				//Thread.sleep(1);
			}
			//System.out.println(users.getUser("用户1").username());
			Thread.sleep(1000);
		}
}
