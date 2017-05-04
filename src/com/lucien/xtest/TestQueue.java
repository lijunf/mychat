package com.lucien.xtest;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class TestQueue {

	public static void main(String[] args) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("a", "a");
		map.remove("b");
		System.out.println(map);
		
		Queue<String> q = new LinkedBlockingDeque<String>(15);
		for (int i = 0; i < 10; i++) {
			q.add("A" + i);
		}
		System.out.println(q);
	}
}
